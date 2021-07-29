package com.dismoi.scout.accessibility.browser

open class Browser() {
  var _packageName: String = ""

  open fun getBrowserConfig(): SupportedBrowserConfig? {
    return SupportedBrowsers.find(_packageName)
  }

  object SupportedBrowsers {
    val supportedBrowsers = listOf(
      SupportedBrowserConfig("com.android.chrome", "com.android.chrome:id/url_bar")
    )

    fun get(): List<SupportedBrowserConfig> {
      return supportedBrowsers
    }

    fun find(packageName: String): SupportedBrowserConfig? {
      return supportedBrowsers.find { it.packageName == packageName }
    }
  }
}