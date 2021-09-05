package com.dismoi.scout.browser

import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi

val TAG = "Amazon"

object Amazon {
  @RequiresApi(Build.VERSION_CODES.KITKAT)
  fun getProductTitle(webviewNodeInfo: AccessibilityNodeInfo): String? {
    val titleView = Helpers.findFirstHeading(webviewNodeInfo)
    val title = titleView?.text ?: titleView?.contentDescription
    Log.d(TAG, "Found Amazon page title : $title")
    return title?.toString()
  }
}