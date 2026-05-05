package com.rnforge.inappupdates

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.margelo.nitro.core.Promise
import com.margelo.nitro.rnforge_inappupdates.OpenStorePageOptionsNative

/**
 * Opens the Play Store page for the current app.
 *
 * Tries market:// intent first, then falls back to https:// URL
 * if no Activity can handle the market scheme.
 */
class PlayCoreStoreService {

    fun openStorePage(options: OpenStorePageOptionsNative?): Promise<Unit> {
        val promise = Promise<Unit>()
        val context = InAppUpdatesActivityProvider.applicationContext

        if (context == null) {
            promise.reject(Exception("Context not available"))
            return promise
        }

        val packageName = context.packageName
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))

        val canHandleMarket = marketIntent.resolveActivity(context.packageManager) != null

        val intent = if (canHandleMarket) {
            marketIntent
        } else {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            context.startActivity(intent)
            promise.resolve(Unit)
        } catch (e: Exception) {
            promise.reject(e)
        }

        return promise
    }
}
