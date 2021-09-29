package com.dismoi.scout.floating.layout

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import com.dismoi.scout.R

class Trash : Layout {
  private var magnetismApplied = false
  private var attachedToWindow = false
  private var isVibrateInThisSession = false

  constructor(context: Context?) : super(context)
  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context, attrs, defStyleAttr
  )

  override fun onAttachedToWindow() {
    super<Layout>.onAttachedToWindow()
    attachedToWindow = true
  }

  override fun onDetachedFromWindow() {
    super<Layout>.onDetachedFromWindow()
    attachedToWindow = false
  }

  override fun setVisibility(visibility: Int) {
    if (attachedToWindow) {
      if (visibility != getVisibility()) {
        if (visibility == VISIBLE) {
          playAnimation(R.animator.bubble_trash_shown_animator)
        } else {
          playAnimation(R.animator.bubble_trash_hide_animator)
        }
      }
    }
    super<Layout>.setVisibility(visibility)
  }

  fun applyMagnetism() {
    if (!magnetismApplied) {
      magnetismApplied = true
      playAnimation(R.animator.bubble_trash_shown_magnetism_animator)
    }
  }

  fun vibrate() {
    if (!isVibrateInThisSession) {
      val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        // TODO Deprecation
        @Suppress("DEPRECATION")
        vibrator.vibrate(200)
      }
      isVibrateInThisSession = true
    }
  }

  fun releaseMagnetism() {
    if (magnetismApplied) {
      magnetismApplied = false
      playAnimation(R.animator.bubble_trash_hide_magnetism_animator)
    }
    isVibrateInThisSession = false
  }

  private fun playAnimation(animationResourceId: Int) {
    if (!isInEditMode) {
      val animator = AnimatorInflater
        .loadAnimator(context, animationResourceId) as AnimatorSet
      animator.setTarget(getChildAt(0))
      animator.start()
    }
  }

  companion object {
    const val VIBRATION_DURATION_IN_MS = 70
  }
}
