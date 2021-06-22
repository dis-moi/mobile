package com.dismoi.scout.floating.layout

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import com.dismoi.scout.R

class Bubble(context: Context, attrs: AttributeSet?) : Layout(context, attrs) {
  private var _initialTouchX = 0f
  private var _initialTouchY = 0f
  private var _initialX = 0
  private var _initialY = 0
  private var _onBubbleRemoveListener: OnBubbleRemoveListener? = null
  private var _onBubbleClickListener: OnBubbleClickListener? = null
  private var _lastTouchDown: Long = 0
  private val _animator: MoveAnimator
  private var _screenWidth = 0
  private var _shouldStickToWall = true

  fun setOnBubbleRemoveListener(listener: OnBubbleRemoveListener?) {
    _onBubbleRemoveListener = listener
  }

  fun setOnBubbleClickListener(listener: OnBubbleClickListener?) {
    _onBubbleClickListener = listener
  }

  fun setShouldStickToWall(shouldStick: Boolean) {
    _shouldStickToWall = shouldStick
  }

  fun notifyBubbleRemoved() {
    if (_onBubbleRemoveListener != null) {
      _onBubbleRemoveListener!!.onBubbleRemoved(this)
    }
  }

  private fun initializeView() {
    isClickable = true
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    playAnimation()
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        _initialX = viewParams!!.x
        _initialY = viewParams!!.y
        _initialTouchX = event.rawX
        _initialTouchY = event.rawY
        playAnimationClickDown()
        _lastTouchDown = System.currentTimeMillis()
        updateSize()
        _animator.stop()
      }
      MotionEvent.ACTION_MOVE -> {
        val x = _initialX + (event.rawX - _initialTouchX).toInt()
        val y = _initialY + (event.rawY - _initialTouchY).toInt()
        viewParams!!.x = x
        viewParams!!.y = y
        windowManager!!.updateViewLayout(this, viewParams)
        if (layoutCoordinator != null) {
          layoutCoordinator!!.notifyBubblePositionChanged(this)
        }
      }
      MotionEvent.ACTION_UP -> {
        goToWall()
        if (layoutCoordinator != null) {
          layoutCoordinator!!.notifyBubbleRelease(this)
          playAnimationClickUp()
        }
        if (System.currentTimeMillis() - _lastTouchDown < TOUCH_TIME_THRESHOLD) {
          if (_onBubbleClickListener != null) {
            _onBubbleClickListener!!.onBubbleClick(this)
          }
        }
      }
    }
    return super.onTouchEvent(event)
  }

  private fun playAnimation() {
    if (!isInEditMode) {
      val animator = AnimatorInflater
        .loadAnimator(context, R.animator.bubble_shown_animator) as AnimatorSet
      animator.setTarget(this)
      animator.start()
    }
  }

  private fun playAnimationClickDown() {
    if (!isInEditMode) {
      val animator = AnimatorInflater
        .loadAnimator(context, R.animator.bubble_down_click_animator) as AnimatorSet
      animator.setTarget(this)
      animator.start()
    }
  }

  private fun playAnimationClickUp() {
    if (!isInEditMode) {
      val animator = AnimatorInflater
        .loadAnimator(context, R.animator.bubble_up_click_animator) as AnimatorSet
      animator.setTarget(this)
      animator.start()
    }
  }

  private fun updateSize() {
    val metrics = DisplayMetrics()
    windowManager!!.defaultDisplay.getMetrics(metrics)
    val display = windowManager!!.defaultDisplay
    val size = Point()
    display.getSize(size)
    _screenWidth = size.x - getWidth()
  }

  interface OnBubbleRemoveListener {
    fun onBubbleRemoved(bubble: Bubble?)
  }

  interface OnBubbleClickListener {
    fun onBubbleClick(bubble: Bubble?)
  }

  fun goToWall() {
    if (_shouldStickToWall) {
      val middle = _screenWidth / 2
      val nearestXWall = if (viewParams!!.x >= middle) _screenWidth.toFloat() else 0.toFloat()
      _animator.start(nearestXWall, viewParams!!.y.toFloat())
    }
  }

  private fun move(deltaX: Float, deltaY: Float) {
    viewParams!!.x += deltaX.toInt()
    viewParams!!.y += deltaY.toInt()
    windowManager!!.updateViewLayout(this, viewParams)
  }

  private inner class MoveAnimator : Runnable {
    private val handler = Handler(Looper.getMainLooper())
    private var destinationX = 0f
    private var destinationY = 0f
    private var startingTime: Long = 0
    fun start(x: Float, y: Float) {
      destinationX = x
      destinationY = y
      startingTime = System.currentTimeMillis()
      handler.post(this)
    }

    override fun run() {
      if (rootView != null && rootView.parent != null) {
        val progress = Math.min(1f, (System.currentTimeMillis() - startingTime) / 400f)
        val deltaX = (destinationX - viewParams!!.x) * progress
        val deltaY = (destinationY - viewParams!!.y) * progress
        move(deltaX, deltaY)
        if (progress < 1) {
          handler.post(this)
        }
      }
    }

    fun stop() {
      handler.removeCallbacks(this)
    }
  }

  companion object {
    private const val TOUCH_TIME_THRESHOLD = 150
  }

  init {
    _animator = MoveAnimator()
    windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    initializeView()
  }
}
