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

  fun checkIfChrome(packageName: String): Boolean {
    return packageName == "com.android.chrome"
  }

  fun isLauncherActivated(packageName: String, parentNodeInfo: AccessibilityNodeInfo): Boolean {
    return "com.android.launcher3" == packageName &&
      parentNodeInfo.className.toString() != "android.widget.FrameLayout"
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

      val chrome: Chrome = Chrome(packageName)

      chrome.parentNodeInfo = parentNodeInfo

      if (isLauncherActivated(packageName, parentNodeInfo)) {
        _hide = "true"
        handler.post(runnableCode)
        return
      }

      if (chrome.outsideChrome()) {
        _hide = "true"
        handler.post(runnableCode)
        return
      }

      if (checkIfChrome(packageName)) {
        val capturedUrl = chrome.captureUrl()

        if (chrome.chromeSearchBarEditingIsActivated()) {
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

  override fun onDestroy() {
    super<AccessibilityService>.onDestroy()

    sendEventFromAccessibilityServicePermission("false")
    handler.removeCallbacks(runnableCode)
  }
}
