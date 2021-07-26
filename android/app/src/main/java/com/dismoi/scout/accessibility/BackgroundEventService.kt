package com.dismoi.scout.accessibility

import android.content.Intent
import android.util.Log
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig

class BackgroundEventService : HeadlessJsTaskService() {
  override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig? {
    val extras = intent.extras

    return HeadlessJsTaskConfig(
      "Background",
      if (extras != null) {
        Arguments.fromBundle(extras)
      } else Arguments.createMap(),
      5000, // timeout of the task
      false
    )
  }
}
