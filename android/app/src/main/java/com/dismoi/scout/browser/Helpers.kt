package com.dismoi.scout.browser

import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi

object Helpers {

  private val TAG: String = "WebviewHelpers"

  @RequiresApi(Build.VERSION_CODES.KITKAT)
  fun findByClassName(node: AccessibilityNodeInfo, className: String, level: Int = 0): AccessibilityNodeInfo? {
    node.refresh()
    val count = node.childCount
    for (i in 0 until count) {
      val child = node.getChild(i)
      if (child != null) {
        if (child.className.toString() == className) {
          return child
        }
        val foundInChild = findByClassName(child, className, level + 1)
        if (foundInChild != null) return foundInChild
      }
    }
    return null
  }

  @RequiresApi(Build.VERSION_CODES.KITKAT)
  fun nodeIsHeading(node: AccessibilityNodeInfo, headingLevel: Int? = null): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      if (node.isHeading) return true
    }

    if (node.extras == null) return false

    val roleDescription = node.extras?.get("AccessibilityNodeInfo.roleDescription")?.toString() ?: return false

    if (roleDescription.startsWith("heading", true)) {
      if(headingLevel==null || roleDescription.equals("heading"))
        return true

      return roleDescription.equals("heading ${headingLevel}")
    }

    return false
  }

  @RequiresApi(Build.VERSION_CODES.KITKAT)
  fun findFirstHeading(node: AccessibilityNodeInfo, level: Int = 0): AccessibilityNodeInfo? {
    node.refresh()
    val count = node.childCount
    for (i in 0 until count) {
      val child = node.getChild(i)
      if (child != null) {
        if (nodeIsHeading(child, 1)) {
          return child
        }
        val foundInChild = findFirstHeading(child, level + 1)
        if (foundInChild != null) return foundInChild
      }
    }
    return null
  }

  @RequiresApi(Build.VERSION_CODES.KITKAT)
  fun findById(node: AccessibilityNodeInfo, id: String, level: Int = 0): AccessibilityNodeInfo? {
    val count = node.childCount
    for (i in 0 until count) {
      val child = node.getChild(i)

      if (child != null) {
        if (child?.viewIdResourceName?.toString() == id) {
          return child
        }
        val foundInChild = findById(child, id, level + 1)
        if (foundInChild != null) return foundInChild
      }
    }
    return null
  }


  @RequiresApi(Build.VERSION_CODES.KITKAT)
  fun findTexts(node: AccessibilityNodeInfo, level: Int = 0): String {
    val count = node.childCount
    var texts = ""
    for (i in 0 until count) {
      val child = node.getChild(i)

      texts += child.text?.toString() ?: child.contentDescription?.toString() ?: ""
      texts += "\n" + findTexts(child, level + 1) + "\n"
    }
    return texts
  }

  @RequiresApi(Build.VERSION_CODES.KITKAT)
  fun findWebview(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
    return findByClassName(node, "android.webkit.WebView")
  }

  @RequiresApi(Build.VERSION_CODES.KITKAT)
  fun logHierarchy(node: AccessibilityNodeInfo, level: Int = 0) {
    node.refresh()
    val id = node.viewIdResourceName
    val text = node.text?.toString()
    val content = node.contentDescription?.toString()
//    val avExtras = node.availableExtraData?.joinToString(", ") Needs API level 26
    val count = node.childCount

    val extras = node.extras
    val extrasList = mutableListOf<String>()
    for (key in extras.keySet()) {
      extrasList.add("$key: ${extras.get(key)}")
    }
    val allExtras = extrasList.joinToString(" / ")

    Log.d(TAG, "${"  ".repeat(level)} ($level) " +
      "className: ${node.className}, " +
      "id: ${id ?: "NO ID"}, " +
      "text: $text, " +
      "content: $content, " +
      "extras: $allExtras, " +
//      "avExtras: $avExtras, " + Needs API level 26
//      "hint: ${node.hintText}, " + Needs API level 26
//      "heading: ${node.isHeading}, " + Needs API 30
      "inputType: ${node.inputType}, "
//      "state: ${node.stateDescription}" Needs API 30
    )

    for (i in 0 until count)  {
      val child = node.getChild(i)
      if (child != null) {
        logHierarchy(child, level + 1)
      }
    }
  }

  fun getNodeTextOrContent(node: AccessibilityNodeInfo?): String? {
    return if (node != null)  "${node.text?.toString()} / ${node.contentDescription?.toString()}" else null
  }
}