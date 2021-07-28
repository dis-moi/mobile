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
  private var _hide: String? = ""
  private var _eventTime: String? = ""
  val chrome: Chrome = Chrome()

  private val NOTIFICATION_TIMEOUT: Long = 500

  private val handler = Handler(Looper.getMainLooper())
  private val runnableCode: Runnable = object : Runnable {
    override fun run() {
      val context = applicationContext
      val myIntent = Intent(context, BackgroundEventService::class.java)
      val bundle = Bundle()

      bundle.putString("url", chrome._url)
      bundle.putString("hide", _hide)
      bundle.putString("eventTime", _eventTime)


      myIntent.putExtras(bundle)

      context.startService(myIntent)
      HeadlessJsTaskService.acquireWakeLockNow(context)
    }
  }

  private fun getEventType(event: AccessibilityEvent): String? {
    when (event.eventType) {
      AccessibilityEvent.TYPE_VIEW_CLICKED -> return "TYPE_VIEW_CLICKED"
      AccessibilityEvent.TYPE_VIEW_FOCUSED -> return "TYPE_VIEW_FOCUSED"
      AccessibilityEvent.TYPE_VIEW_SELECTED -> return "TYPE_VIEW_SELECTED"
      AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> return "TYPE_WINDOW_STATE_CHANGED"
      AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> return "TYPE_WINDOW_CONTENT_CHANGED"
      AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> return "TYPE_VIEW_TEXT_CHANGED"
      AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> return "TYPE_VIEW_TEXT_SELECTION_CHANGED"
      AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED -> return "TYPE_VIEW_ACCESSIBILITY_FOCUSED"
    }
    return event.eventType.toString()
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
    info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED

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

  private fun chromeSearchBarEditingIsActivated(info: AccessibilityNodeInfo): Boolean {
    return info.childCount > 0 &&
      info.className.toString() == "android.widget.FrameLayout" &&
      info.getChild(0).className.toString() == "android.widget.EditText"
  }

  fun checkIfChrome(packageName: String): Boolean {
    return packageName == "com.android.chrome"
  }

  fun isLauncherActivated(packageName: String, parentNodeInfo: AccessibilityNodeInfo): Boolean {
    return "com.android.launcher3" == packageName
  }

  /*
    This method is called back by the system when it detects an 
    AccessibilityEvent that matches the event filtering parameters 
    specified by your accessibility service
   */
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  override fun onAccessibilityEvent(event: AccessibilityEvent) {
    val packageName = event.packageName.toString()
    val parentNodeInfo: AccessibilityNodeInfo = event.source ?: return

    if (getRootInActiveWindow() == null) {
      return
    }



    if (getEventType(event) == "TYPE_WINDOW_STATE_CHANGED") {

      if (packageName.contains("com.google.android.inputmethod")) {
        Log.d("Notification", "INSIDE INPUT METHOD")
        _hide = "true"
        handler.post(runnableCode)
        return
      }
    }

    //if (getEventType(event) == "TYPE_VIEW_CLICKED") {
      //Log.d("Notification", "INSIDE TYPE VIEW CLICKED")
      //_hide = "true"
      //chrome._url = ""
      //handler.post(runnableCode)
      //return
    //}

    if (isLauncherActivated(packageName, parentNodeInfo)) {
      Log.d("Notification", "INSIDE HIDE TRUE")
      _hide = "true"
      handler.post(runnableCode)
      return
    }

    if (checkIfChrome(packageName)) {
      chrome.parentNodeInfo = parentNodeInfo
      chrome._packageName = packageName

      if (getEventType(event) == "TYPE_WINDOW_STATE_CHANGED" || getEventType(event) == "TYPE_WINDOW_CONTENT_CHANGED") {
        chrome.captureUrl()
        if (chrome.chromeSearchBarEditingIsActivated()) {

          _hide = "true"
          handler.post(runnableCode)
          return
        }
      }



      if (getEventType(event) != "TYPE_WINDOW_STATE_CHANGED" && getEventType(event) != "TYPE_WINDOW_CONTENT_CHANGED") {
        Log.d("Notification", "POST hide false")
        Log.d("Notification", getEventType(event).toString())
        _eventTime = event.eventTime.toString()
        _hide = "false"
        handler.post(runnableCode)
      }

      parentNodeInfo.recycle()
    }

    return
  }

    // if (overlayIsActivated(applicationContext)) {
    //





    //   if (chrome.outsideChrome()) {
    //     _hide = "true"
    //     _url = ""
    //     handler.post(runnableCode)
    //     return
    //   }


  

  override fun onInterrupt() {
    sendEventFromAccessibilityServicePermission("false")
  }

  override fun onDestroy() {
    super<AccessibilityService>.onDestroy()

    sendEventFromAccessibilityServicePermission("false")
    handler.removeCallbacks(runnableCode)
  }
}
