package com.dismoi.scout.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings.canDrawOverlays
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import com.dismoi.scout.accessibility.BackgroundModule.Companion.sendEventFromAccessibilityServicePermission
import com.facebook.react.HeadlessJsTaskService

class BackgroundService : AccessibilityService() {
  private var _url: String? = ""
  private var _eventType: String? = ""
  private var _className: String? = ""
  private var _packageName: String? = ""
  private var _eventText: String? = ""
  private var _hide: String? = ""

  private val NOTIFICATION_TIMEOUT: Long = 300

  private val handler = Handler()
  private val runnableCode: Runnable = object : Runnable {
    override fun run() {
      val context = applicationContext
      val myIntent = Intent(context, BackgroundEventService::class.java)
      val bundle = Bundle()

      bundle.putString("url", _url)
      bundle.putString("eventType", _eventType)
      bundle.putString("className", _className)
      bundle.putString("packageName", _packageName)
      bundle.putString("eventText", _eventText)
      bundle.putString("hide", _hide)

      _hide = ""

      myIntent.putExtras(bundle)

      context.startService(myIntent)
      HeadlessJsTaskService.acquireWakeLockNow(context)
    }
  }

  private val previousUrlDetections: HashMap<String, Long> = HashMap()

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  override fun onServiceConnected() {
    val info = serviceInfo

    info.notificationTimeout = NOTIFICATION_TIMEOUT

    this.serviceInfo = info
  }

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  private fun captureUrl(info: AccessibilityNodeInfo, config: SupportedBrowserConfig): String? {
    if (chromeWebpageIsRendering(info)) {
      val nodes = info.findAccessibilityNodeInfosByViewId(config.addressBarId)
      if (nodes == null || nodes.size <= 0) {
        return null
      }
      val addressBarNodeInfo = nodes[0]
      var url: String? = null
      if (addressBarNodeInfo.text != null) {
        url = addressBarNodeInfo.text.toString()
      }
      addressBarNodeInfo.recycle()
      return url
    }
    return null
  }

  private fun getEventType(event: AccessibilityEvent): String? {
    when (event.eventType) {
      AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> return "TYPE_NOTIFICATION_STATE_CHANGED"
      AccessibilityEvent.TYPE_VIEW_CLICKED -> return "TYPE_VIEW_CLICKED"
      AccessibilityEvent.TYPE_VIEW_FOCUSED -> return "TYPE_VIEW_FOCUSED"
      AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> return "TYPE_VIEW_LONG_CLICKED"
      AccessibilityEvent.TYPE_VIEW_SELECTED -> return "TYPE_VIEW_SELECTED"
      AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> return "TYPE_WINDOW_STATE_CHANGED"
      AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> return "TYPE_VIEW_TEXT_CHANGED"
      AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> return "TYPE_VIEW_TEXT_SELECTION_CHANGED"
    }
    return "default"
  }

  private fun getEventText(event: AccessibilityEvent): String? {
    val sb = StringBuilder()
    for (s in event.text) {
      sb.append(s)
    }
    return sb.toString()
  }

  fun isLauncherPackage(packageName: CharSequence): Boolean {
    return "com.android.systemui" == packageName || "com.android.launcher3" == packageName
  }

  private fun chromeWebpageIsRendering(info: AccessibilityNodeInfo): Boolean {
    return info.childCount > 0 && 
      info.getChild(0).className.toString() == "android.widget.LinearLayout" && 
      info.childCount > 1 && 
      info.getChild(1).className.toString() != "android.widget.EditText"
  }

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  override fun onAccessibilityEvent(event: AccessibilityEvent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (canDrawOverlays(applicationContext)) {
        if (
          event.getClassName() != null && 
          event!!.getPackageName() != null && 
          !isLauncherPackage(event!!.getPackageName())
        ) {
          if (event.getClassName().toString() == "com.android.inputmethod.keyboard.Key") {
            _hide = "true"
            _url = ""
            _eventType = getEventType(event)
            _className = event.getClassName().toString()
            _packageName = event.getPackageName().toString()
            _eventText = getEventText(event)
            handler.post(runnableCode)
            return
          }
          val parentNodeInfo = event.source ?: return
          val packageName = event.packageName.toString()
          var browserConfig: SupportedBrowserConfig? = null

            for (supportedConfig in getSupportedBrowsers()) {
              if (supportedConfig.packageName == packageName) {
                browserConfig = supportedConfig
              }
            }

            // this is not supported browser, so exit
            if (browserConfig == null) {
              return
            }
            if (AccessibilityEvent.eventTypeToString(event.getEventType()).contains("WINDOW")) {

              val capturedUrl = captureUrl(parentNodeInfo, browserConfig)

              // we can't find an url. Browser either was updated or opened page without url text field
              if (capturedUrl == null) {
                return
              }

              parentNodeInfo.recycle()

              val eventTime = event.eventTime
              val detectionId = "$packageName"
              val lastRecordedTime =
                if (previousUrlDetections.containsKey(detectionId)) {
                  previousUrlDetections[detectionId]!!
                } else 0.toLong()

              // some kind of redirect throttling
              if (eventTime - lastRecordedTime > NOTIFICATION_TIMEOUT) {
                Log.d("Notification", "POST WITH URL")
                previousUrlDetections[detectionId] = eventTime

                _url = capturedUrl
                _eventType = getEventType(event)
                _className = event.getClassName().toString()
                _packageName = event.getPackageName().toString()
                _eventText = getEventText(event)
                _hide = "false"
                handler.post(runnableCode)
              }
            }
          }
        }
      }
    }
  }

  override fun onInterrupt() {
    sendEventFromAccessibilityServicePermission("false");
  }

  private class SupportedBrowserConfig(var packageName: String, var addressBarId: String)

  /** @return a list of supported browser configs
   * This list could be instead obtained from remote server to support future browser updates without updating an app
   */
  private fun getSupportedBrowsers(): List<SupportedBrowserConfig> {
    val browsers: MutableList<SupportedBrowserConfig> = ArrayList()
    browsers.add(SupportedBrowserConfig("com.android.chrome", "com.android.chrome:id/url_bar"))
    return browsers
  }

  override fun onCreate() {
    super.onCreate()
  }

  override fun onDestroy() {
    super.onDestroy()

    sendEventFromAccessibilityServicePermission("false");
    handler.removeCallbacks(runnableCode)
  }
}
