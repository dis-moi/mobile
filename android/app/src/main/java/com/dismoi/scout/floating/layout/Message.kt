package com.dismoi.scout.floating.layout

import android.content.Context
import android.util.AttributeSet
import android.animation.*
import android.view.View

class Message(context: Context, attrs: AttributeSet?) : Layout(context, attrs) {

  override fun onAttachedToWindow() {
    super<Layout>.onAttachedToWindow()
    playAnimation()
  }

  private fun playAnimation() {
    val animator = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 600f, 0f)

    animator.start()
  }
}
