package com.dismoi.scout.accessibility

import android.util.Log

open class Browser(packageName: String = "") {
  var _packageName = packageName

  class SupportedBrowserConfig(var packageName: String, var addressBarId: String)

  open fun getBrowserConfig(): SupportedBrowserConfig? {
    var browserConfig: SupportedBrowserConfig? = null
    for (supportedConfig in getSupportedBrowsers()) {
      if (supportedConfig.packageName == _packageName) {
        browserConfig = supportedConfig
      }
    }
    return browserConfig
  }

  /** @return a list of supported browser configs
   * This list could be instead obtained from remote server to support future browser updates without updating an app
   */
  private fun getSupportedBrowsers(): List<SupportedBrowserConfig> {
    val browsers: MutableList<SupportedBrowserConfig> = ArrayList()
    browsers.add(SupportedBrowserConfig("com.android.chrome", "com.android.chrome:id/url_bar"))
    return browsers
  }
}