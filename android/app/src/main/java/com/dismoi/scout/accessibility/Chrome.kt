package com.dismoi.scout.accessibility

class Chrome {
  var _url: String? = ""
  var _eventType: String? = ""
  var _className: String? = ""
  var _packageName: String? = ""
  var _eventText: String? = ""
  var _hide: String? = ""
  val _eventTime: String? = ""
  val _parentNodeInfo: AccessibilityNodeInfo = null
  var _browserConfig: SupportedBrowserConfig = null

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  fun captureUrl(): String? {
    // Can get URL with FLAG_REPORT_VIEW_IDS
    val nodes = _parentNodeInfo.findAccessibilityNodeInfosByViewId(
      _browserConfig.addressBarId
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
    return _parentNodeInfo.childCount > 0 &&
      _parentNodeInfo.className.toString() == "android.widget.FrameLayout" &&
      _parentNodeInfo.getChild(0).className.toString() == "android.view.View"
  }

  fun chromeSearchBarEditingIsActivated(): Boolean {
    return _parentNodeInfo.childCount > 0 &&
      _parentNodeInfo.className.toString() == "android.widget.FrameLayout" &&
      _parentNodeInfo.getChild(0).className.toString() == "android.widget.EditText"
  }
}