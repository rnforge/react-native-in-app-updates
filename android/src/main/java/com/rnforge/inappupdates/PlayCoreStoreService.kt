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
class PlayCoreStoreService(
    private val activityProvider: ActivityProvider = DefaultActivityProvider
) {

    fun openStorePage(options: OpenStorePageOptionsNative?): Promise<Unit> {
        val promise = Promise<Unit>()
        openStorePage(
            onSuccess = { promise.resolve(Unit) },
            onFailure = { promise.reject(it) }
        )
        return promise
    }

    internal fun openStorePage(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val context = activityProvider.applicationContext

        if (context == null) {
            onFailure(Exception("Context not available"))
            return
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
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}
