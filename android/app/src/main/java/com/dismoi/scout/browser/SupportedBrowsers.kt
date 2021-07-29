package com.dismoi.scout.browser

val supportedBrowsers = listOf(
  SupportedBrowserConfig("com.android.chrome", "com.android.chrome:id/url_bar")
)

object SupportedBrowsers {
  fun get(): List<SupportedBrowserConfig> {
    return supportedBrowsers
  }

  fun find(packageName: String): SupportedBrowserConfig? {
    return supportedBrowsers.find { it.packageName == packageName }
  }
}
