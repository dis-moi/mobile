package com.dismoi.scout.floating

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.dismoi.scout.floating.FloatingService.FloatingServiceBinder
import com.dismoi.scout.floating.layout.Bubble
import com.dismoi.scout.floating.layout.Message

class Manager private constructor(private val context: Context) {
  private var bounded = false
  private var bubblesService: FloatingService? = null
  private var trashLayoutResourceId = 0
  private var listener: OnCallback? = null
  private val disMoiServiceConnection: ServiceConnection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName, service: IBinder) {
      val binder = service as FloatingServiceBinder
      bubblesService = binder.service
      configureBubblesService()
      bounded = true
      if (listener != null) {
        listener!!.onInitialized()
      }
    }

    override fun onServiceDisconnected(name: ComponentName) {
      bounded = false
    }
  }

  private fun configureBubblesService() {
    bubblesService!!.addTrash(trashLayoutResourceId)
  }

  fun initialize() {
    context.bindService(
      Intent(context, FloatingService::class.java),
      disMoiServiceConnection,
      Context.BIND_AUTO_CREATE
    )
  }

  fun recycle() {
    context.unbindService(disMoiServiceConnection)
  }

  fun addDisMoiBubble(bubble: Bubble?, x: Int, y: Int) {
    if (bounded) {
      bubblesService!!.addDisMoiBubble(bubble!!, x, y)
    }
  }

  fun addDisMoiMessage(message: Message?, y: Int) {
    if (bounded) {
      bubblesService!!.addDisMoiMessage(message, y)
    }
  }

  fun removeBubble(bubble: Bubble?) {
    if (bounded) {
      bubblesService!!.removeBubble(bubble!!)
    }
  }

  fun removeDisMoiMessage(message: Message?) {
    if (bounded) {
      bubblesService!!.removeMessage(message)
    }
  }

  class Builder(context: Context) {
    private val disMoiManager: Manager
    fun setInitializationCallback(listener: OnCallback?): Builder {
      disMoiManager.listener = listener
      return this
    }

    fun setTrashLayout(trashLayoutResourceId: Int): Builder {
      disMoiManager.trashLayoutResourceId = trashLayoutResourceId
      return this
    }

    fun build(): Manager {
      return disMoiManager
    }

    init {
      disMoiManager = getInstance(context)
    }
  }

  companion object {
    private fun getInstance(context: Context): Manager {
      return Manager(context)
    }
  }
}
