package com.dismoi.scout.floating.layout

import android.content.Context
import android.util.AttributeSet
import android.view.WindowManager
import android.widget.FrameLayout

open class Layout : FrameLayout {
  var windowManager: WindowManager? = null
  var viewParams: WindowManager.LayoutParams? = null
  var layoutCoordinator: Coordinator? = null
  
  fun create(
    getWindowManager: WindowManager?,
    layoutParams: WindowManager.LayoutParams?,
    layoutCoordinator: Coordinator?
  ) {
    windowManager = getWindowManager
    viewParams = layoutParams
    this.layoutCoordinator = layoutCoordinator
  }

  constructor(context: Context?) : super(context!!)
  constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context!!, attrs, defStyleAttr
  )
}
