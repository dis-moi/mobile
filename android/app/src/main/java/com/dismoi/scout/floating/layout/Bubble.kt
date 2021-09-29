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
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.dismoi.scout.R

class Bubble(context: Context, attrs: AttributeSet?) : Layout(context, attrs) {
  private var _initialTouchX = 0f
  private var _initialTouchY = 0f
  private var _initialX = 0
  private var _initialY = 0
  private var _onBubbleRemoveListener: OnBubbleRemoveListener? = null
  private var _BubbleListener: BubbleListener? = null
  private var _lastTouchDown: Long = 0
  private val _animator: MoveAnimator
  private var _screenWidth = 0
  private var _shouldStickToWall = true

  fun setOnBubbleRemoveListener(listener: OnBubbleRemoveListener?) {
    _onBubbleRemoveListener = listener
  }

  fun setOnBubbleClickListener(listener: BubbleListener?) {
    _BubbleListener = listener
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

  fun setCount(count: Int) {
    var textView: TextView? = findViewById(R.id.number_of_notice)
    textView?.text = count.toString()
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        _initialX = getViewParams()!!.x
        _initialY = getViewParams()!!.y
        _initialTouchX = event.rawX
        _initialTouchY = event.rawY
        playAnimationClickDown()
        _lastTouchDown = System.currentTimeMillis()
        updateSize()
        _animator.stop()
      }
      MotionEvent.ACTION_MOVE -> {
        // TODO Needed ?
        val x = _initialX + (event.rawX - _initialTouchX).toInt()
        val y = _initialY + (event.rawY - _initialTouchY).toInt()
        getViewParams()!!.x = x
        getViewParams()!!.y = y
        getWindowManager()!!.updateViewLayout(this, getViewParams()); // TODO Use setX/setY ?

        _BubbleListener?.onBubbleMove()
      }
      MotionEvent.ACTION_UP -> {
        goToWall() // TODO Everytime ?
        playAnimationClickUp();

        // TODO if (event.eventTime - event.downTime)
        if (System.currentTimeMillis() - _lastTouchDown < TOUCH_TIME_THRESHOLD) {
          _BubbleListener?.onBubbleClick(this)
        } else {
          _BubbleListener?.onBubbleDrop()
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
    _windowManager!!.getDefaultDisplay().getMetrics(metrics);
    val display = getWindowManager()!!.getDefaultDisplay();
    val size = Point()
    display.getSize(size)
    _screenWidth = size.x - width
  }

  interface OnBubbleRemoveListener {
    fun onBubbleRemoved(bubble: Bubble?)
  }

  interface BubbleListener {
    fun onBubbleMove()
    fun onBubbleClick(bubble: Bubble?)
    fun onBubbleDrop()
  }

  fun goToWall() {
    if (_shouldStickToWall) {
      val middle = _screenWidth / 2
      val nearestXWall = if (getViewParams()!!.x >= middle) _screenWidth.toFloat() else 0.toFloat()
      _animator.start(nearestXWall, getViewParams()!!.y.toFloat())
    }
  }

  private fun move(deltaX: Float, deltaY: Float) {
    getViewParams()!!.x += deltaX.toInt()
    getViewParams()!!.y += deltaY.toInt()
    _windowManager!!.updateViewLayout(this, getViewParams());
  }

  fun magnetismOnTrash(trash: Trash) {
    val trashContentView: View = trash.getChildAt(0)
    val trashCenterX = trashContentView.left + trashContentView.measuredWidth / 2
    val trashCenterY = trashContentView.top + trashContentView.measuredHeight / 2
    _viewParams?.x = trashCenterX - measuredWidth / 2
    _viewParams?.y = trashCenterY - measuredHeight / 2
    // TODO Use setX and setY instead ?
    _windowManager!!.updateViewLayout(this, _viewParams)
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
        val deltaX = (destinationX - getViewParams()!!.x) * progress
        val deltaY = (destinationY - getViewParams()!!.y) * progress
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
    _windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    initializeView()
  }
}
