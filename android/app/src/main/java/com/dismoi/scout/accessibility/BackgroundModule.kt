package com.dismoi.scout.accessibility

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import org.htmlcleaner.CleanerProperties
import org.htmlcleaner.DomSerializer
import org.htmlcleaner.HtmlCleaner
import javax.annotation.Nonnull
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class BackgroundModule(@Nonnull reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
  private var reactContext: ReactApplicationContext = reactContext

  @Nonnull
  override fun getName(): String {
    return "Background"
  }

  private fun isAccessibilitySettingsOn(mContext: Context): Boolean {
    var accessibilityEnabled = Settings.Secure.getInt(
      mContext.applicationContext.contentResolver,
      Settings.Secure.ACCESSIBILITY_ENABLED
    )
    if (accessibilityEnabled == 1) {
      val settingValue = Settings.Secure.getString(
        mContext.applicationContext.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
      )
      if (settingValue != null) {
        if (settingValue.indexOf(string = reactContext.packageName) > -1) {
          return true
        }
      }
    }
    return false
  }

  private fun overlayHasPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Settings.canDrawOverlays(reactContext)
    } else true
  }

  @ReactMethod
  fun isAccessibilityEnabled(callback: Callback) {
    if (isAccessibilitySettingsOn(reactContext) == false) {
      callback.invoke("0", null)
    }
    if (isAccessibilitySettingsOn(reactContext) == true) {
      callback.invoke("1", null)
    }
  }

  @ReactMethod
  fun canDrawOverlay(callback: Callback) {
    if (overlayHasPermission() == true) {
      callback.invoke("1", null)
    }
    if (overlayHasPermission() == false) {
      callback.invoke("0", null)
    }
  }

  private fun requestPermissionAction() {
    var intent: Intent? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse(
          "package:" + reactContext.packageName
        )
      )
    }
    val bundle = Bundle()
    reactContext.startActivityForResult(intent, 0, bundle)
  }

  @ReactMethod
  fun redirectToAppAccessibilitySettings(promise: Promise) {
    val currentActivity = currentActivity
    currentActivity!!.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    promise.resolve("")
  }

  // @ReactMethod
  // fun testWithXpath(htmlString: String, xPathParser: String, promise: Promise) {
  //   Log.d("Notification", "begin test xpath")
  //   // create an instance of HtmlCleaner
  //   val tagNode = HtmlCleaner().clean(
  //     htmlString
  //   )
  //   val doc = DomSerializer(
  //     CleanerProperties()
  //   ).createDOM(tagNode)

  //   val xpath: XPath = XPathFactory.newInstance().newXPath()

  //   val str = xpath.evaluate(
  //     xPathParser,
  //     doc, XPathConstants.STRING
  //   ) as String

  //   Log.d("Notification", str)
  //   Log.d("Notification", "end test xpath")

  //   promise.resolve(str)
  // }

  @ReactMethod
  fun redirectToAppAdvancedSettings(promise: Promise) {
    requestPermissionAction()
    promise.resolve("")
  }

  companion object {
    private var reactContext: ReactApplicationContext? = null

    private fun sendEventToReactNative(
      reactContext: ReactContext?,
      eventName: String,
      params: String?
    ) {
      if (reactContext == null) {
        return
      }
      reactContext
        .getJSModule<DeviceEventManagerModule.RCTDeviceEventEmitter>(
          DeviceEventManagerModule.RCTDeviceEventEmitter::class.java
        ).emit(eventName, params)
    }

    @JvmStatic
    fun sendEventFromAccessibilityServicePermission(params: String?) {
      sendEventToReactNative(reactContext, "EventFromAccessibilityServicePermission", params)
    }

    @JvmStatic
    fun sendEventFromOverlayPermission(params: String?) {
      sendEventToReactNative(reactContext, "EventFromOverlayPermission", params)
    }
  }

  init {
    Companion.reactContext = reactContext
  }
}
