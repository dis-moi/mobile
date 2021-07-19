package com.dismoi.scout.floating.layout

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.WindowManager
import com.dismoi.scout.R

import android.animation.*
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView

class Message(context: Context, attrs: AttributeSet?) : Layout(context, attrs) {

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    playAnimation()
  }

  private fun playAnimation() {
    val animator = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 600f, 0f)

    animator.start()
  }
}
