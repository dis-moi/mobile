package com.dismoi.scout.floating

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.dismoi.scout.floating.FloatingService.FloatingServiceBinder

class Manager private constructor(private val context: Context) {
  private var bounded = false
  private var floatingService: FloatingService? = null
  private var trashLayoutResourceId = 0
  private var listener: OnCallback? = null

  private val disMoiServiceConnection: ServiceConnection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName, service: IBinder) {
      val binder = service as FloatingServiceBinder
      floatingService = binder.service
      bounded = true
      listener?.onInitialized()
    }

    override fun onServiceDisconnected(name: ComponentName) {
      bounded = false
    }
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

  class Builder(context: Context) {
    private val disMoiManager: Manager
    fun setInitializationCallback(listener: OnCallback): Builder {
      disMoiManager.listener = listener
      return this
    }

    fun setTrashLayout(trashLayoutResourceId: Int): Builder {
      Log.d("Notification", "set trash layout")
      Log.d("Notification", trashLayoutResourceId.toString())

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