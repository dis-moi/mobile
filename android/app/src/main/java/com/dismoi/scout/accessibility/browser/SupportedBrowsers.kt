package com.dismoi.scout.accessibility.browser

val supportedBrowsers = listOf(
  SupportedBrowserConfig("com.android.chrome", "com.android.chrome:id/url_bar"),
  SupportedBrowserConfig("org.mozilla.firefox", "org.mozilla.firefox:id/url_bar")
)

object SupportedBrowsers {
  fun getList(): List<SupportedBrowserConfig> {
    return supportedBrowsers
  }

  fun find(packageName: String): SupportedBrowserConfig? {
    return supportedBrowsers.find { it.packageName == packageName }
  }

  fun isSupported(packageName: String): Boolean {
    return find(packageName) != null
  }
}
