package com.dismoi.scout.floating

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.dismoi.scout.R
import com.dismoi.scout.floating.layout.Bubble
import com.dismoi.scout.floating.layout.Bubble.OnBubbleClickListener
import com.dismoi.scout.floating.layout.Bubble.OnBubbleRemoveListener
import com.dismoi.scout.floating.layout.Message
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ViewListener
import java.net.URL

class FloatingModule(
  private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

  private var bubblesService: FloatingService? = null

  var messageIsInitialized = false

  private var bubblesManager: Manager? = null

  private var messagesManager: Manager? = null

  private var bubbleDisMoiView: Bubble? = null
  private var messageDisMoiView: Message? = null
  private var _size = 0
  private lateinit var _notices: ReadableArray
  private var _url = ""

  var verticalLayout: LinearLayout? = null

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
  fun initialize(promise: Promise) {
    bubblesManager = Manager.Builder(reactContext).setTrashLayout(
      R.layout.bubble_trash
    ).setInitializationCallback(object : OnCallback {
      override fun onInitialized() {
      }
    }).build()

    messagesManager = Manager.Builder(reactContext)
    .setInitializationCallback(object : OnCallback {
      override fun onInitialized() {
        messageIsInitialized = true

        promise.resolve("")
      }
    }).build()

    bubblesManager!!.initialize()
    messagesManager!!.initialize()
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
      if (messageDisMoiView != null) {
        removeDisMoiMessage()
        messageDisMoiView = null
      }

      removeDisMoiBubble()
      _notices = notices
      _size = numberOfNotice

      addNewFloatingDisMoiMessage(y)
      promise.resolve("")
    } catch (e: Exception) {
      promise.reject("0", "")
    }
  }

  @ReactMethod
  fun hideFloatingDisMoiBubble(promise: Promise) {
    removeDisMoiBubble()

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
  fun hideFloatingDisMoiMessage() {
    removeDisMoiMessage()
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

  private fun addNewFloatingDisMoiMessage(y: Int) {
    messageDisMoiView = LayoutInflater.from(reactContext).inflate(
      R.layout.highlight_messages, messageDisMoiView, false
    ) as Message

    val carouselView = messageDisMoiView!!.findViewById(
      R.id.carouselView
    ) as CarouselView

    carouselView!!.pageCount = _size

    carouselView!!.setViewListener(viewListener)

    val imageButton = messageDisMoiView!!.findViewById<View>(R.id.close) as ImageButton
    imageButton.setOnClickListener {
      sendEventToReactNative("floating-dismoi-message-press", "")
    }

    messagesManager!!.addDisMoiMessage(messageDisMoiView!!, y)
  }

  private fun addNewFloatingDisMoiBubble(x: Int, y: Int, numberOfNotice: String) {
    bubbleDisMoiView = LayoutInflater.from(reactContext).inflate(
      R.layout.bubble, bubbleDisMoiView, false
    ) as Bubble

    var textView: TextView? = bubbleDisMoiView!!.findViewById(R.id.number_of_notice)
    textView!!.text = numberOfNotice

    bubbleDisMoiView!!.setOnBubbleRemoveListener(object : OnBubbleRemoveListener {
      override fun onBubbleRemoved(bubble: Bubble?) {
        bubbleDisMoiView = null
        sendEventToReactNative("floating-dismoi-bubble-remove", "")
      }
    })
    bubbleDisMoiView!!.setOnBubbleClickListener(object : OnBubbleClickListener {
      override fun onBubbleClick(bubble: Bubble?) {
        sendEventToReactNative("floating-dismoi-bubble-press", "")
      }
    })
    bubbleDisMoiView!!.setShouldStickToWall(true)

    bubblesManager!!.addDisMoiBubble(bubbleDisMoiView, x, y)
  }

  private fun removeDisMoiBubble() {
    if (bubbleDisMoiView != null) {
      bubblesManager!!.removeBubble(bubbleDisMoiView)
      bubbleDisMoiView = null
    }
  }

  private fun removeDisMoiMessage() {
    if (messageDisMoiView != null) {
      messagesManager!!.removeDisMoiMessage(messageDisMoiView)
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

  //set view attributes here
  var viewListener: ViewListener = object : ViewListener {

    override fun setViewForPosition(position: Int): View? {
      val message: ReadableMap? = _notices.getMap(position)

      var disMoiContributorNameMap: ReadableMap? = message!!.getMap("contributor")

      var url: String? = disMoiContributorNameMap!!.getMap("avatar")!!.getMap("normal")!!.getString("url")

      var modified: String? = message.getString("modified")

      var disMoiContributorName: String? = disMoiContributorNameMap.getString("name")

      var disMoiMessage: String? = message.getString("message")

      val customView: View = LayoutInflater.from(reactContext).inflate(R.layout.message, null)

      val type = Typeface.createFromAsset(reactContext.assets, "fonts/Helvetica.ttf")

      val typeBold = Typeface.createFromAsset(reactContext.assets, "fonts/Helvetica-Bold.ttf")
      var textView: TextView? = customView.findViewById(R.id.link)

      textView!!.typeface = type

      textView.text = disMoiMessage!!.toSpanned()

      var textViewContributorName: TextView? = customView.findViewById(R.id.name)

      textViewContributorName!!.typeface = typeBold

      var textViewDate: TextView? = customView.findViewById(R.id.date)

      textViewDate!!.typeface = typeBold

      textViewContributorName.text = disMoiContributorName

      textViewDate.text = modified

      textView.handleUrlClicks { urlLinkToClick ->
        sendEventToReactNative("URL_CLICK_LINK", Uri.parse(urlLinkToClick).toString())
      }

      val imageButton = customView!!.findViewById<View>(R.id.closeNotice) as ImageView
      imageButton.setOnClickListener {
        sendEventToReactNative("DELETE_NOTICE", position.toString())
      }

      val imageView: ImageView = customView!!.findViewById(R.id.contributorProfile) as ImageView

      val URL = URL(url)
      val bmp = BitmapFactory.decodeStream(URL.openConnection().getInputStream())
      imageView.setImageBitmap(bmp)

      return customView
    }
  }
}