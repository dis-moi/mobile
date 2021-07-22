package com.dismoi.scout.accessibility

import android.accessibilityservice.AccessibilityService

/* 
  The configuration of an accessibility service is contained in the 
  AccessibilityServiceInfo class
*/
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings.canDrawOverlays
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import com.dismoi.scout.accessibility.BackgroundModule.Companion.sendEventFromAccessibilityServicePermission
import com.facebook.react.HeadlessJsTaskService

class BackgroundService : AccessibilityService() {
  private var _url: String? = ""
  private var _hide: String? = ""

  private val NOTIFICATION_TIMEOUT: Long = 300

  private val handler = Handler(Looper.getMainLooper())
  private val runnableCode: Runnable = object : Runnable {
    override fun run() {
      val context = applicationContext
      val myIntent = Intent(context, BackgroundEventService::class.java)
      val bundle = Bundle()

      bundle.putString("url", _url)
      bundle.putString("hide", _hide)

      myIntent.putExtras(bundle)

      context.startService(myIntent)
      HeadlessJsTaskService.acquireWakeLockNow(context)
    }
  }

  private val previousUrlDetections: HashMap<String, Long> = HashMap()

  /* 
    This system calls this method when it successfully connects to your accessibility service
  */
  // configure my service in there
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  override fun onServiceConnected() {

    val info = serviceInfo
    
    // Set the type of events that this service wants to listen to. Others
    // won't be passed to this service
    /* 
      Represents the event of changing the content of a window and more specifically 
      the sub-tree rooted at the event's source
    */
    info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED

    /* 
      info.packageNames is not set because we want to receive event from
      all packages
    */

    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL
    info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS

    /* 
      The minimal period in milliseconds between two accessibility events of 
      the same type are sent to this service
    */
    info.notificationTimeout = NOTIFICATION_TIMEOUT

    this.serviceInfo = info
  }

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  private fun captureUrl(info: AccessibilityNodeInfo, config: SupportedBrowserConfig): String? {
    // Can get URL with FLAG_REPORT_VIEW_IDS
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
    return AccessibilityEvent.eventTypeToString(event.eventType).contains("WINDOW")
  }

  /*
    This method is called back by the system when it detects an 
    AccessibilityEvent that matches the event filtering parameters 
    specified by your accessibility service
   */
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  override fun onAccessibilityEvent(event: AccessibilityEvent) {
    if (getRootInActiveWindow() == null) {
      return
    }

    val parentNodeInfo: AccessibilityNodeInfo = event.source ?: return

    if (overlayIsActivated(applicationContext) && isWindowChangeEvent(event)) {
      val packageName = event.packageName.toString()

      if (isLauncherPackage(packageName) && parentNodeInfo.className.toString() != "android.widget.FrameLayout") {
        _hide = "true"
        handler.post(runnableCode)
        return
      }

      if (outsideChrome(parentNodeInfo)) {
        _hide = "true"
        handler.post(runnableCode)
        return
      }

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
    sendEventFromAccessibilityServicePermission("false")
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

  override fun onDestroy() {
    super.onDestroy()

    sendEventFromAccessibilityServicePermission("false")
    handler.removeCallbacks(runnableCode)
  }
}
