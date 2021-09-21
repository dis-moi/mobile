package com.dismoi.scout.accessibility.browser

import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi

class SupportedBrowserConfig(var packageName: String, var addressBarId: String) {
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  public fun getCurrentUrlIn(node: AccessibilityNodeInfo): String? {
    var addressBars = node.findAccessibilityNodeInfosByViewId(addressBarId)

    if (addressBars == null || addressBars.size == 0) {
      throw NoUrlInBrowserException()
    }

    val addressBar = addressBars.first()

    val currentUrl = addressBar?.text?.toString() ?: "EMPTY URL"

    addressBar.recycle()

    return currentUrl
  }
}