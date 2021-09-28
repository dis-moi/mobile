package com.dismoi.scout.floating

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.dismoi.scout.R
import com.dismoi.scout.floating.layout.*
import com.dismoi.scout.floating.layout.Message
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

enum class State {
  HIDDEN, BUBBLE, MESSAGE
}

class FloatingService : Service() {
  private val TAG = "FloatingService"
  private lateinit var handler: Handler

  private lateinit var windowManager: WindowManager
  private lateinit var inflater: LayoutInflater
  private val binder = FloatingServiceBinder()

  private var noticesIds: List<Int> = listOf()
  private var liveNotices: List<MutableLiveData<JSONObject>> = mutableListOf()

  private lateinit var bubbleView: Bubble
  private lateinit var bubbleLayoutParams: WindowManager.LayoutParams

  private lateinit var messageView: Message
  private lateinit var messageLayoutParams: WindowManager.LayoutParams

  private lateinit var trashView: Trash
  private lateinit var trashLayoutParams: WindowManager.LayoutParams

  private var state: State = State.HIDDEN

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onCreate() {
    super.onCreate()

    windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
    handler = Handler(Looper.getMainLooper())

    createBubbleView()
    createMessageView()
    createTrashView()

    Log.d(TAG, "Service created")
  }


  override fun onBind(intent: Intent?): IBinder {
    return binder
  }

  // TODO Proper typed an mapped object !
  @RequiresApi(Build.VERSION_CODES.N)
  fun showNotices(noticesIds: List<Int>) {
    Log.d(TAG, "Receiving notices")
    if (this.noticesIds.size == noticesIds.size && this.noticesIds.containsAll(noticesIds)) {
      Log.d(TAG, "Same as before")
      if (state == State.HIDDEN) {
        Log.d(TAG, "current: HIDDEN -> Showing Bubble")
        showBubble(noticesIds.size)
      }
      return
    }

    Log.d(TAG, "Got new notices ${noticesIds.joinToString(",")}")

    liveNotices = noticesIds.map { noticeId ->
      val liveNotice = MutableLiveData<JSONObject>()
      fetchNotice(noticeId, liveNotice)
      liveNotice
    }

    messageView.notices = liveNotices
    showBubble(noticesIds.size)
  }

  fun hide() {
    if (state !== State.HIDDEN) {
      Log.d(TAG, "Hide everything !")
      hideBubble()
      hideMessage()
      hideTrash()
      state = State.HIDDEN
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun buildLayoutParamsForBubble() {
    bubbleLayoutParams = WindowManager.LayoutParams(
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
      PixelFormat.TRANSPARENT
    )
    bubbleLayoutParams.gravity = Gravity.TOP or Gravity.START
    bubbleLayoutParams.x = 10
    bubbleLayoutParams.y = 1500
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createBubbleView() {
    buildLayoutParamsForBubble()
    bubbleView = inflater.inflate(R.layout.bubble, null, false) as Bubble
    bubbleView.setViewParams(bubbleLayoutParams)
    bubbleView.setOnBubbleClickListener(object : Bubble.OnBubbleClickListener {
      override fun onBubbleClick(bubble: Bubble?) {
        onBubbleClick()
      }
    })
  }

  private fun showView(view: View, layoutParams: WindowManager.LayoutParams)
  {
    handler.post {
      try {
        windowManager.addView(view, layoutParams)
      } catch (exception: IllegalArgumentException) {
        Log.d("FloatingService", "${view.id} already in window : $exception : ${exception.stackTrace}")
      } catch (exception: IllegalStateException) {
        Log.d("FloatingService", "${view.id} already in window : $exception : ${exception.stackTrace}")
      }
    }
  }

  private fun removeView(view: View) {
    handler.post {
      try {
        windowManager.removeView(view)
      } catch (exception: IllegalArgumentException) {
        Log.d("FloatingService", "${view.id} not in view")
      } catch (exception: IllegalStateException) {
        Log.d("FloatingService", "${view.id} not in view")
      }
    }
  }

  private fun showBubble(count: Int) {
    Log.d(TAG, "Showing Bubble for $count")
    bubbleView.setCount(count)
    showView(bubbleView, bubbleLayoutParams)
    state = State.BUBBLE
  }

  private fun onBubbleClick() {
    showMessage()
    hideBubble()
  }

  private fun hideBubble() {
    removeView(bubbleView)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun buildLayoutParamsForMessage() {
    messageLayoutParams = WindowManager.LayoutParams(
      WindowManager.LayoutParams.MATCH_PARENT,
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
      WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
      PixelFormat.TRANSPARENT
    )
    messageLayoutParams.gravity = Gravity.END
    messageLayoutParams.y = 1500
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createMessageView() {
    buildLayoutParamsForMessage()
    messageView = inflater.inflate(R.layout.highlight_messages, null, false) as Message
    messageView.setViewParams(bubbleLayoutParams)
  }

  private fun showMessage() {
    showView(messageView, messageLayoutParams)
    state = State.MESSAGE
  }

  private fun hideMessage() {
    removeView(messageView)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun buildLayoutParamsForTrash() {
    trashLayoutParams = WindowManager.LayoutParams(
      WindowManager.LayoutParams.MATCH_PARENT,
      WindowManager.LayoutParams.MATCH_PARENT,
      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
      PixelFormat.TRANSPARENT
    )
    trashLayoutParams.x = 0
    trashLayoutParams.y = 0
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createTrashView() {
    buildLayoutParamsForTrash()
    trashView = Trash(this)
    trashView.setViewParams(trashLayoutParams)
    trashView.visibility = View.GONE
    inflater.inflate(R.layout.bubble_trash, trashView, true)
  }

  private fun showTrash() {
    showView(trashView, trashLayoutParams)
  }

  private fun hideTrash() {
    removeView(trashView)
  }

  // TODO To be moved to a repository
  private fun fetchNotice(noticeId: Int, liveNotice: MutableLiveData<JSONObject>) {
    val noticeEndpoint =  "https://notices.bulles.fr/api/v3/notices/$noticeId" // TODO to be moved to env configuration
    val request = Request.Builder().url(noticeEndpoint).build()
    val client = OkHttpClient()

    client.newCall(request).enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        Log.d(TAG, "Failed calling $noticeEndpoint")

      }

      @RequiresApi(Build.VERSION_CODES.N)
      override fun onResponse(call: Call, response: Response) {
        val body = response.body()?.string()
        if (body == null) {
          Log.e(TAG, "Empty response $noticeEndpoint")
          return
        }
        val notice = JSONObject(body)
        Log.d(TAG, "Fetched notice $noticeId")

        liveNotice.postValue(notice)
      }
    })
  }

  inner class FloatingServiceBinder : Binder() {
    val service: FloatingService
      get() = this@FloatingService
  }
}
