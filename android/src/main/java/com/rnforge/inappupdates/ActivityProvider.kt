package com.rnforge.inappupdates

import android.app.Activity
import android.content.Context

interface ActivityProvider {
    val applicationContext: Context?
    val currentActivity: Activity?
}

object DefaultActivityProvider : ActivityProvider {
    override val applicationContext: Context?
        get() = InAppUpdatesActivityProvider.applicationContext

    override val currentActivity: Activity?
        get() = InAppUpdatesActivityProvider.currentActivity
}
