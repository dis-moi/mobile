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
import com.dismoi.scout.floating.layout.*
import com.dismoi.scout.floating.layout.Message

class FloatingService : Service() {
  private val binder = FloatingServiceBinder()
  private var bubbles: MutableList<Bubble> = ArrayList()
  private var bubblesTrash: Trash? = null
  private var windowManager: WindowManager? = null
  private var layoutCoordinator: Coordinator? = null

  override fun onBind(intent: Intent): IBinder? {
    return binder
  }

  override fun onUnbind(intent: Intent): Boolean {
    for (bubble in bubbles) {
      recycleBubble(bubble)
    }
    bubbles.clear()
    return super.onUnbind(intent)
  }

  private fun recycleBubble(bubble: Bubble) {
    Handler(Looper.getMainLooper()).post {
      getWindowManager()!!.removeView(bubble)
      for (cachedBubble in bubbles) {
        if (cachedBubble === bubble) {
          bubble.notifyBubbleRemoved()
          bubbles.remove(cachedBubble)
          bubblesTrash = null
          break
        }
      }
    }
  }

  private fun recycleMessage(message: Message?) {
    Log.d("Notification", "recycle message")
    getWindowManager()!!.removeView(message)
    bubblesTrash = null
  }

  private fun getWindowManager(): WindowManager? {
    if (windowManager == null) {
      windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }
    return windowManager
  }

  fun addDisMoiBubble(bubble: Bubble, x: Int, y: Int) {
    val layoutParams = buildLayoutParamsForBubble(x, y)
    bubble.create(getWindowManager(), layoutParams, layoutCoordinator)

    bubbles.add(bubble)
    addViewToWindow(bubble)
  }

  fun addDisMoiMessage(message: Message?, y: Int) {
    val layoutParams = buildLayoutParamsForMessage(y)
    message!!.create(getWindowManager(), layoutParams, layoutCoordinator)

    getWindowManager()!!.addView(message, message.viewParams)
  }

  fun addTrash(trashLayoutResourceId: Int) {
    if (trashLayoutResourceId != 0) {
      bubblesTrash = Trash(this)
      bubblesTrash!!.windowManager = windowManager
      bubblesTrash!!.viewParams = buildLayoutParamsForTrash()
      bubblesTrash!!.visibility = View.GONE
      LayoutInflater.from(this).inflate(trashLayoutResourceId, bubblesTrash, true)
      addViewToWindow(bubblesTrash!!)
      initializeLayoutCoordinator()
    }
  }

  private fun initializeLayoutCoordinator() {
    layoutCoordinator = Coordinator.Builder(this)
      .setWindowManager(getWindowManager())
      .setTrashView(bubblesTrash)
      .build()
  }

  private fun addViewToWindow(view: Layout) {
    Handler(Looper.getMainLooper()).post {
      getWindowManager()!!.addView(view, view.viewParams)
    }
  }

  private fun buildLayoutParamsForBubble(x: Int, y: Int): WindowManager.LayoutParams? {
    var params: WindowManager.LayoutParams? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSPARENT
      )
    }
    params!!.gravity = Gravity.TOP or Gravity.START
    params.x = x
    params.y = y
    return params
  }

  private fun buildLayoutParamsForMessage(y: Int): WindowManager.LayoutParams? {
    var params: WindowManager.LayoutParams? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSPARENT
      )
    }
    params!!.gravity = Gravity.END
    params.y = y
    return params
  }

  private fun buildLayoutParamsForTrash(): WindowManager.LayoutParams? {
    val x = 0
    val y = 0
    var params: WindowManager.LayoutParams? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSPARENT
      )
    }
    params!!.x = x
    params.y = y
    return params
  }

  fun removeBubble(bubble: Bubble) {
    recycleBubble(bubble)
  }

  fun removeMessage(message: Message?) {
    recycleMessage(message)
  }

  inner class FloatingServiceBinder : Binder() {
    val service: FloatingService
      get() = this@FloatingService
  }
}
