package com.dismoi.scout.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.*
import android.os.Build
import android.os.IBinder
import android.provider.Settings.canDrawOverlays
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import com.dismoi.scout.accessibility.browser.NoUrlInBrowserException
import com.dismoi.scout.accessibility.browser.SupportedBrowserConfig
import com.dismoi.scout.accessibility.browser.SupportedBrowsers
import com.dismoi.scout.floating.FloatingService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

interface SavedContributor {
  val id: String
}

class BackgroundService : AccessibilityService() {
  private val TAG = "Accessibility"
  private var bound: Boolean = false
  private var floatingService: FloatingService? = null
  private var jsonMatchingContexts: JSONArray = JSONArray("[]")
  private var lastNotices: List<Int> = listOf()
  private var eventsChannel: Channel<AccessibilityEvent> = Channel<AccessibilityEvent>(CONFLATED)

  private val disMoiServiceConnection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName, floatingServiceBinder: IBinder) {
      val binder = floatingServiceBinder as FloatingService.FloatingServiceBinder
      floatingService = binder.service
      bound = true
    }

    override fun onServiceDisconnected(name: ComponentName) {
      bound = false
      floatingService = null
    }
  }

  @RequiresApi(Build.VERSION_CODES.N)
  override fun onCreate() {
    super.onCreate()

    CoroutineScope(Dispatchers.Default).launch {
      eventsChannel.consumeAsFlow().debounce(500L).collect {
        fetchMatchingContexts()

        matchContext(it)
      }
    }

    bindService(
      Intent(applicationContext, FloatingService::class.java), disMoiServiceConnection, Context.BIND_AUTO_CREATE
    )
  }

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  override fun onServiceConnected() {
    val info = serviceInfo
    
    info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
    // info.packageNames is not set because we want to receive event from all packages
    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL
    info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS

    this.serviceInfo = info
  }

  @RequiresApi(Build.VERSION_CODES.N)
  override fun onAccessibilityEvent(event: AccessibilityEvent) {
    runBlocking { launch {
      eventsChannel.send(event)
    } }
  }

  @RequiresApi(Build.VERSION_CODES.N)
  private fun matchContext(event: AccessibilityEvent) {
    val eventType = AccessibilityEvent.eventTypeToString(event.eventType)
    Log.d(TAG, "Event : ${eventType}")
    Log.d(TAG, "Package: ${event.packageName}")

    if (!canDrawOverlays(applicationContext)) return

    val root = rootInActiveWindow
    val packageName = root?.packageName?.toString()
    Log.d(TAG, "Root Package: ${packageName}")

    if (root == null || packageName == null) {
      hide()
      return
    }

    if (packageName == "com.dismoi.scout") { // TODO extract
      // We’re interacting with our own UI
      return
    }

    if (SupportedBrowsers.isSupported(packageName)) {
      Log.d(TAG, "Supported window packageName : ${root.packageName}, className: ${root.className}")
    } else {
      Log.d(TAG, "Unknown window packageName : ${root.packageName}, className: ${root.className}")
      hide()
      return
    }

    // we have a supported browser active ! Yay !
    val activeBrowserConfig = SupportedBrowsers.find(packageName) as SupportedBrowserConfig

    try {
      val currentUrl = activeBrowserConfig.getCurrentUrlIn(root)

      if (currentUrl == null) return

      Log.d(TAG, "current URL is $currentUrl")

      val matchingContexts = getContextsMatchingUrl(currentUrl)

      Log.d(TAG, "found ${matchingContexts.size} contexts matching current url")

      if (matchingContexts.size > 0) {

        val noticesIds =
          matchingContexts
            .filter { context -> !context.has("xpath") || context.isNull("xpath") } // TODO Use domain fields instead of xpath
            .map { context -> context.getInt("noticeId") }
            .distinct()

        if (noticesIds.size == lastNotices.size && noticesIds.containsAll(lastNotices)) {
          return
        }

        show(noticesIds)
      } else {
        hide()
      }
    } catch (e: NoUrlInBrowserException)
    {
      // When page is scrolled, the URL is hidden, but we may still be on the same page …
    }

    // Just an example of code of how to search for a product name
//    val webview = Helpers.findWebview(root)
//    if (webview != null) {
//      Amazon.getProductTitle(webview)
//    }
  }

  private fun hide() {
    if (bound) {
      lastNotices = listOf()
      floatingService!!.hide()
    }

  }

  @RequiresApi(Build.VERSION_CODES.N)
  private fun show(noticesIds: List<Int>) {
    if (bound) {
      lastNotices = noticesIds
      floatingService!!.showNotices(noticesIds)
    }
  }

  // TODO To be moved to repository
  private fun fetchMatchingContexts() {
    val prefs = getSharedPreferences("wit_player_shared_preferences", Context.MODE_PRIVATE)
    val matchingContextsQueryString = prefs.getString("url", "")
    val matchingContextsEndpoint: String = "https://notices.bulles.fr/api/v3/matching-contexts?$matchingContextsQueryString"

    // TODO URL should be composed here
//    val allPrefs = prefs.all as Map<String, SavedContributor>
//    val subscribedContributorsId = allPrefs.values.map { it.id }
//    val matchingContextsEndpoint = // TODO endpoint to be moved to env configuration
//      "https://notices.bulles.fr/api/v3/matching-contexts?contributors[]=${
//        subscribedContributorsId.joinToString("&contributors[]=")
//      }"

    Log.d(TAG, "Fetching URL $matchingContextsEndpoint")

    val request = Request.Builder().url(matchingContextsEndpoint).build()
    val client = OkHttpClient()

    client.newCall(request).enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        Log.d(TAG, "Failed to fetch : ${e.toString()}")
      }

      override fun onResponse(call: Call, response: Response) {
        val body = response.body()?.string()

        jsonMatchingContexts = JSONArray(body)

        Log.d(TAG, "Fetched ${jsonMatchingContexts.length()} matching contexts")
      }
    })
  }

  // TODO To be moved to a repository
  private fun getContextsMatchingUrl(url: String): MutableList<JSONObject> {
    val wwwUrl = "www.$url"
    try {
      val contextsMatchingUrl = mutableListOf<JSONObject>()
      for (i in 0 until jsonMatchingContexts.length()) {
        val aMatchingContext = jsonMatchingContexts.getJSONObject(i)

        if (aMatchingContext.has("urlRegex")) {
          val regex = Regex(aMatchingContext.getString("urlRegex"))
          if (!regex.matches(url) && !regex.matches(wwwUrl)) {
            continue
          }
        }

        if (aMatchingContext.has("excludeUrlRegex")) {
          val excludeRegex = Regex(aMatchingContext.getString("excludeUrlRegex"))
          if (excludeRegex.matches(url) || excludeRegex.matches(wwwUrl)) {
            continue
          }
        }

        contextsMatchingUrl.add(aMatchingContext)
      }
      return contextsMatchingUrl
    }
    catch (exception: Exception) {
      Log.d(TAG, "oups : ${exception.toString()}")
    }
    return mutableListOf()
  }

  override fun onInterrupt() {}
}
