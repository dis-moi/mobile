package com.dismoi.scout.floating

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.dismoi.scout.R
import com.dismoi.scout.floating.layout.Bubble
import com.dismoi.scout.floating.layout.Message
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ViewListener
import java.net.URL

class FloatingModule(
  private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

  private var bubbleDisMoiView: Bubble? = null
  private var messageDisMoiView: Message? = null
  private var _size = 0
  private lateinit var _notices: ReadableArray
  private var _url = ""

  var bubbleInitialized: Boolean = false
  var messageInitialized: Boolean = false

  var verticalLayout: LinearLayout? = null

  private var bubblesManager: Manager? = null
  
  private var messagesManager: Manager? = null
  
  @ReactMethod
  fun reopenApp() {
    val launchIntent = reactContext.packageManager.getLaunchIntentForPackage(
      reactContext.packageName
    )
    if (launchIntent != null) {
      reactContext.startActivity(launchIntent)
    }
  }

  override fun getName(): String {
    return "FloatingModule"
  }

  @ReactMethod
  fun showFloatingDisMoiBubble(
    x: Int,
    y: Int,
    numberOfNotice: Int,
    url: String,
    promise: Promise
  ) {
    _url = url
    _size = numberOfNotice

    addNewFloatingDisMoiBubble(x, y, numberOfNotice.toString())
    promise.resolve("")
  }

  @ReactMethod
  fun showFloatingDisMoiMessage(notices: ReadableArray, y: Int, numberOfNotice: Int, promise: Promise) {
    try {
      _notices = notices
      _size = numberOfNotice

      removeDisMoiBubble()
      removeDisMoiMessage()
      addNewFloatingDisMoiMessage(y)
      promise.resolve("")
    } catch (e: Exception) {
      promise.reject("0", "")
    }
  }

  @ReactMethod
  fun hideFloatingDisMoiBubble(promise: Promise) {
    removeDisMoiBubble()
    bubblesManager = null
    promise.resolve("")
  }

  @ReactMethod
  fun openLink(url: String) {
    val sharingIntent = Intent(Intent.ACTION_VIEW)
    sharingIntent.data = Uri.parse(url)
    sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    sharingIntent.setPackage("com.android.chrome")

    val bundle = Bundle()

    reactContext.startActivityForResult(sharingIntent, 0, bundle)
  }

  @ReactMethod
  fun initializeBubblesManager(promise: Promise) {
    if (bubblesManager == null) {
      bubblesManager = Manager.Builder(reactContext).setTrashLayout(
        R.layout.bubble_trash
      ).setInitializationCallback(object : OnCallback {
        override fun onInitialized() {
          messagesManager = Manager.Builder(reactContext)
          .setInitializationCallback(object : OnCallback {
            override fun onInitialized() {
              promise.resolve("")
            }
          }).build()

          messagesManager!!.initialize()
        }
      }).build()

      bubblesManager!!.initialize()
    }

  }

  @ReactMethod
  fun hideFloatingDisMoiMessage(promise: Promise) {
    removeDisMoiMessage()
    promise.resolve("")
  }

  /**
   * Searches for all URLSpans in current text replaces them with our own ClickableSpans
   * forwards clicks to provided function.
   */
  fun TextView.handleUrlClicks(onClicked: ((String) -> Unit)? = null) {
    // create span builder and replaces current text with it
    text = SpannableStringBuilder.valueOf(text).apply {
      // search for all URL spans and replace all spans with our own clickable spans
      this.getSpans(0, length, URLSpan::class.java).forEach {
        // add new clickable span at the same position
        setSpan(
          object : ClickableSpan() {
            override fun onClick(widget: View) {
              onClicked?.invoke(it.url)
            }
          },
          getSpanStart(it),
          getSpanEnd(it),
          Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        // remove old URLSpan
        removeSpan(it)
      }
    }
    // make sure movement method is set
    movementMethod = LinkMovementMethod.getInstance()
  }

  private fun configureCarouselView(messageDisMoiView: Message?) {
    val carouselView = messageDisMoiView!!.findViewById(
      R.id.carouselView
    ) as CarouselView

    carouselView!!.pageCount = _size

    carouselView!!.setViewListener(viewListener)
  }

  private fun configureCloseButton(messageDisMoiView: Message?) {
    val imageButton = messageDisMoiView!!.findViewById<View>(R.id.close) as ImageButton
    imageButton.setOnClickListener {
      removeDisMoiMessage()
    }
  }

  private fun addNewFloatingDisMoiMessage(y: Int) {
    messageDisMoiView = LayoutInflater.from(reactContext).inflate(
      R.layout.highlight_messages, messageDisMoiView, false
    ) as Message

    configureCarouselView(messageDisMoiView)

    configureCloseButton(messageDisMoiView)

//    messagesManager!!.addDisMoiMessage(messageDisMoiView!!, y)
  }

  private fun configureNumberOfNoticeIcon(bubbleDisMoiView: Bubble?, numberOfNotice: String) {
    var textView: TextView? = bubbleDisMoiView!!.findViewById(R.id.number_of_notice)
    textView!!.text = numberOfNotice
  }

  private fun configureClickOnBubbleAction(bubbleDisMoiView: Bubble?) {
//    bubbleDisMoiView!!.setOnBubbleClickListener(object : OnBubbleClickListener {
//      override fun onBubbleClick(bubble: Bubble?) {
//        sendEventToReactNative("floating-dismoi-bubble-press", "")
//      }
//    })
  }

  private fun addNewFloatingDisMoiBubble(x: Int, y: Int, numberOfNotice: String) {
    bubbleDisMoiView = LayoutInflater.from(reactContext).inflate(
      R.layout.bubble, bubbleDisMoiView, false
    ) as Bubble

    configureNumberOfNoticeIcon(bubbleDisMoiView, numberOfNotice)

    configureClickOnBubbleAction(bubbleDisMoiView)

    bubbleDisMoiView!!.setShouldStickToWall(true)
//    bubblesManager!!.addDisMoiBubble(bubbleDisMoiView, x, y)
  }

  private fun removeDisMoiBubble() {
    if (bubbleDisMoiView != null) {
//      bubblesManager!!.removeDisMoiBubble(bubbleDisMoiView)
      bubbleDisMoiView = null
    }
  }

  private fun removeDisMoiMessage() {
    if (messageDisMoiView != null) {
//      messagesManager!!.removeDisMoiMessage(messageDisMoiView)
      messageDisMoiView = null
    }
  }

  private fun sendEventToReactNative(eventName: String, params: String) {
    reactContext
      .getJSModule(RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  fun String.toSpanned(): Spanned {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
      @Suppress("DEPRECATION")
      return Html.fromHtml(this)
    }
  }

  private fun configureImageProfile(customView: View, disMoiContributorNameMap: ReadableMap?) {
    var url: String? = disMoiContributorNameMap!!.getMap("avatar")!!.getMap("normal")!!.getString("url")
    val imageView: ImageView = customView!!.findViewById(R.id.contributorProfile) as ImageView

    val URL = URL(url)
    val bmp = BitmapFactory.decodeStream(URL.openConnection().getInputStream())
    imageView.setImageBitmap(bmp)
  }

  private fun configureDeleteNotice(customView: View, position: Int) {
    val imageButton = customView.findViewById<View>(R.id.closeNotice) as ImageView
    imageButton.setOnClickListener {
      sendEventToReactNative("DELETE_NOTICE", position.toString())
    }
  }

  private fun configureNoticeModificationDate(customView: View, message: ReadableMap?, typeBold: Typeface) {
    var modified: String? = message!!.getString("modified")
    var textViewDate: TextView? = customView.findViewById(R.id.date)

    textViewDate!!.typeface = typeBold

    textViewDate.text = modified
  }

  private fun configureContributorName(customView: View, disMoiContributorNameMap: ReadableMap?, typeBold: Typeface) {
    var disMoiContributorName: String? = disMoiContributorNameMap!!.getString("name")
    var textViewContributorName: TextView? = customView.findViewById(R.id.name)

    textViewContributorName!!.typeface = typeBold

    textViewContributorName.text = disMoiContributorName
  }

  private fun configureContributorContent(customView: View, message: ReadableMap?) {
    var disMoiMessage: String? = message!!.getString("message")
    val type = Typeface.createFromAsset(reactContext.assets, "fonts/Helvetica.ttf")
    var textView: TextView? = customView.findViewById(R.id.link)

    textView!!.typeface = type

    textView.text = disMoiMessage!!.toSpanned()

    textView.handleUrlClicks { urlLinkToClick ->
      sendEventToReactNative("URL_CLICK_LINK", Uri.parse(urlLinkToClick).toString())
    }
  }

  var viewListener: ViewListener = object : ViewListener {

    override fun setViewForPosition(position: Int): View? {
      val message: ReadableMap? = _notices.getMap(position)

      var disMoiContributorNameMap: ReadableMap? = message!!.getMap("contributor")

      val customView: View = LayoutInflater.from(reactContext).inflate(R.layout.message, null)
      val typeBold = Typeface.createFromAsset(reactContext.assets, "fonts/Helvetica-Bold.ttf")

      configureContributorContent(customView, message)

      configureContributorName(customView, disMoiContributorNameMap, typeBold)

      configureNoticeModificationDate(customView, message, typeBold)

      configureDeleteNotice(customView, position)

      val SDK_INT = Build.VERSION.SDK_INT
      if (SDK_INT > 8) {
        val policy = ThreadPolicy.Builder()
          .permitAll().build()
        StrictMode.setThreadPolicy(policy)
        configureImageProfile(customView, disMoiContributorNameMap)
      }

      return customView
    }
  }
}