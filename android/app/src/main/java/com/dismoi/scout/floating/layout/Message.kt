package com.dismoi.scout.floating.layout

import android.animation.*
import android.app.Service
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.StrictMode
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.dismoi.scout.R
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ViewListener
import org.json.JSONObject
import java.net.URL

@RequiresApi(Build.VERSION_CODES.N)
class Message(context: Context, attrs: AttributeSet?) : Layout(context, attrs) {
  var eventListener: MessageViewListener? = null

  // TODO have correct class for notices
  var notices: List<MutableLiveData<JSONObject>> = listOf()
    set (notices) {
      val carouselView = findViewById<CarouselView>(R.id.carouselView)
      field = notices
      carouselView.pageCount = notices.size
    }

  // TODO Fix fonts usage
  //    val type = Typeface.createFromAsset(context.assets, "fonts/Helvetica.ttf")
  //    val typeBold = Typeface.createFromAsset(context.assets, "fonts/Helvetica-Bold.ttf")

  private var inflater: LayoutInflater = context.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater
  private var carouselListener = ViewListener { position ->
    val liveNotice = notices[position]

    // TODO Extract in fun
    val noticeView = inflater.inflate(R.layout.message, null)

    val noticeObserver = Observer<JSONObject> { noticeContent ->
      if (noticeContent == null) {
        return@Observer
      }

      val message = noticeContent.getString("message")
      val contributor = noticeContent.getJSONObject("contributor")
      val contributorName = contributor.getString("name") // TODO don’t crash if no name
      val contributorAvatarUrl =
        if (contributor.has("avatar") && !contributor.isNull("avatar"))
          URL(
            contributor.getJSONObject("avatar").getJSONObject("normal").getString("url")
          )
        else null

      val modified = noticeContent.getString("modified") // TODO should be parsed

      val messageView = noticeView.findViewById<TextView>(R.id.link)
      messageView.text = message.toSpanned()
//    messageView.typeface = type
      messageView.handleUrlClicks { urlLinkToClick ->
        Log.d("Message", "Clicked link")
        // TODO Legacy : sendEventToReactNative("URL_CLICK_LINK", Uri.parse(urlLinkToClick).toString())
      }

      val contributorNameView = noticeView.findViewById<TextView>(R.id.name)
      contributorNameView.text = contributorName
//    contributorNameView.typeface = typeBold // TODO Should be defined in view, right ?

      val modifiedDateView = noticeView.findViewById<TextView>(R.id.date)
      modifiedDateView.text = modified // TODO should be formatted correctly
//    modifiedDateView.typeface = typeBold // TODO Should be defined in view, right ?

      val deleteButton = noticeView.findViewById<ImageView>(R.id.closeNotice)
      deleteButton.setOnClickListener {
        // TODO What to do onClick
      }

      if (contributorAvatarUrl != null) {
        // TODO Do we need that ?
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val avatarView = noticeView.findViewById<ImageView>(R.id.contributorProfile)
        val avatarBitmap =
          BitmapFactory.decodeStream(contributorAvatarUrl.openConnection().getInputStream())
        avatarView.setImageBitmap(avatarBitmap)
      }
    }

    liveNotice.observeForever(noticeObserver) // TODO we should link to a LifecycleOwner

    noticeView
  }

  interface MessageViewListener {
    fun onClose()
    fun onClickOutside()
  }

  override fun onViewAdded(child: View?) {
    super.onViewAdded(child)

    val carouselView = findViewById<CarouselView>(R.id.carouselView)
    carouselView.setViewListener(carouselListener)

    findViewById<ImageButton>(R.id.close)
      .setOnClickListener(OnClickListener { eventListener?.onClose() })
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    playAnimation()
  }

  // TODO Use performClick ?
  override fun onTouchEvent(event: MotionEvent?): Boolean {
    if (event?.action == MotionEvent.ACTION_OUTSIDE) {
      eventListener?.onClickOutside()
    }

    return super.onTouchEvent(event)
  }

  /**
   * Searches for all URLSpans in current text replaces them with our own ClickableSpans
   * forwards clicks to provided function.
   */
  private fun TextView.handleUrlClicks(onClicked: ((String) -> Unit)? = null) {
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


  private fun playAnimation() {
    val animator = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 600f, 0f)

    animator.start()
  }

  fun String.toSpanned(): Spanned {
    return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
  }
}
