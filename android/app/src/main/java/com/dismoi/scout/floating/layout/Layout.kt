package com.dismoi.scout.floating.layout

import android.content.Context
import android.util.AttributeSet
import android.view.WindowManager
import android.widget.FrameLayout

open class Layout : FrameLayout {
  var _windowManager: WindowManager? = null
  var _viewParams: WindowManager.LayoutParams? = null

  open fun setWindowManager(windowManager: WindowManager?) {
    _windowManager = windowManager
  }

  open fun getWindowManager(): WindowManager? {
    return _windowManager
  }

  open fun setViewParams(params: WindowManager.LayoutParams?) {
    _viewParams = params
  }

  open fun getViewParams(): WindowManager.LayoutParams? {
    return _viewParams
  }

  constructor(context: Context?) : super(context!!)
  constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context!!, attrs, defStyleAttr
  )
}
