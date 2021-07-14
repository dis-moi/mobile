package com.dismoi.scout.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings.canDrawOverlays
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

      myIntent.putExtras(bundle)

      context.startService(myIntent)
      HeadlessJsTaskService.acquireWakeLockNow(context)
    }
  }

  private val previousUrlDetections: HashMap<String, Long> = HashMap()

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  override fun onServiceConnected() {
    val info = serviceInfo

    info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL
    info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
      AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS

    info.notificationTimeout = NOTIFICATION_TIMEOUT

    this.serviceInfo = info
  }

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  private fun captureUrl(info: AccessibilityNodeInfo, config: SupportedBrowserConfig): String? {
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

  fun isLauncherPackage(packageName: CharSequence): Boolean {
    return "com.android.systemui" == packageName || "com.android.launcher3" == packageName
  }

  private fun outsideChrome(info: AccessibilityNodeInfo): Boolean {
    return info.childCount > 0 &&
      info.className.toString() == "android.widget.FrameLayout" &&
      info.getChild(0).className.toString() == "android.view.View"
  }

  private fun chromeSearchBarEditingIsActivated(info: AccessibilityNodeInfo): Boolean {
    return info.childCount > 0 &&
      info.className.toString() == "android.widget.FrameLayout" &&
      info.getChild(0).className.toString() == "android.widget.EditText"
  }

  private fun overlayIsActivated(applicationContext: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      canDrawOverlays(applicationContext)
    } else {
      false
    }
  }

  private fun isWindowChangeEvent(event: AccessibilityEvent): Boolean {
    return AccessibilityEvent.eventTypeToString(event.getEventType()).contains("WINDOW")
  }

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  override fun onAccessibilityEvent(event: AccessibilityEvent) {
    val parentNodeInfo = event.source ?: return

    if (overlayIsActivated(applicationContext) && isWindowChangeEvent(event)) {
      if (outsideChrome(parentNodeInfo)) {
        _hide = "true"
        handler.post(runnableCode)
        return
      }
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
      val eventTime = event.eventTime
      val detectionId = "$packageName"
      val lastRecordedTime =
      if (previousUrlDetections.containsKey(detectionId)) {
        previousUrlDetections[detectionId]!!
      } else 0.toLong()
      if (eventTime - lastRecordedTime > NOTIFICATION_TIMEOUT) {
        previousUrlDetections[detectionId] = eventTime

        val capturedUrl = captureUrl(parentNodeInfo, browserConfig)

        if (chromeSearchBarEditingIsActivated(parentNodeInfo)) {
          _hide = "true"
          handler.post(runnableCode)
          return
        }

        if (capturedUrl == null) {
          return
        }

        _url = capturedUrl
        _hide = "false"
        handler.post(runnableCode)

        parentNodeInfo.recycle()

        return
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
