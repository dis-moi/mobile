package com.dismoi.scout.accessibility

import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi

class Chrome(packageName: String): Browser(packageName) {
  var url: String? = ""
  var eventType: String? = ""
  var className: String? = ""
  var eventText: String? = ""
  var hide: String? = ""
  var eventTime: String? = ""
  lateinit var parentNodeInfo: AccessibilityNodeInfo

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  fun captureUrl(): String? {

    if (getBrowserConfig() == null) {
      return ""
    }
    // Can get URL with FLAG_REPORT_VIEW_IDS
    val nodes = parentNodeInfo.findAccessibilityNodeInfosByViewId(
      getBrowserConfig()!!.addressBarId
    )
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

  fun outsideChrome(): Boolean {
    return parentNodeInfo.childCount > 0 &&
      parentNodeInfo.className.toString() == "android.widget.FrameLayout" &&
      parentNodeInfo.getChild(0).className.toString() == "android.view.View"
  }

  fun chromeSearchBarEditingIsActivated(): Boolean {
    return parentNodeInfo.childCount > 0 &&
      parentNodeInfo.className.toString() == "android.widget.FrameLayout" &&
      parentNodeInfo.getChild(0).className.toString() == "android.widget.EditText"
  }
}