package org.fischman.shushdjx

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.text.format.DateUtils

class MainActivity : Activity() {

    private var dialog: AlertDialog? = null
    private val handler = Handler(Looper.getMainLooper())
    private val refresher = Runnable { refreshActiveMessage() }
    private val skipListener: () -> Unit = { refreshActiveMessage() }

    override fun onResume() {
        super.onResume()
        if (isListenerEnabled()) {
            dialog = AlertDialog.Builder(this)
                .setTitle("ShushDJX Active")
                .setMessage(activeMessage())
                .setPositiveButton("OK") { _, _ -> finish() }
                .setCancelable(false)
                .show()
            SkipLog.addListener(skipListener)
            refreshActiveMessage()
        } else {
            dialog = AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("ShushDJX needs notification access to watch Spotify's MediaSession and skip \"DJ X\" interludes.\n\nTap OK to open Settings and then grant access.\nAfter this grant, the app unchecks all the boxes and never reads any notifications.")
                .setPositiveButton("OK") { _, _ ->
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
                        putExtra(
                            Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                            ComponentName(packageName, SpotifyListenerService::class.java.name).flattenToString())
                    }
                    startActivity(intent)
                }
                .setNegativeButton("Cancel") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }
    }

    override fun onPause() {
        SkipLog.removeListener(skipListener)
        handler.removeCallbacks(refresher)
        dialog?.dismiss()
        dialog = null
        super.onPause()
    }

    private fun refreshActiveMessage() {
        val d = dialog ?: return
        handler.removeCallbacks(refresher)
        val now = SystemClock.elapsedRealtime()
        d.setMessage(activeMessage(now))
        val delay = SkipLog.recent(now).minOfOrNull { msUntilDisplayChanges(now - it) } ?: return
        handler.postDelayed(refresher, delay)
    }

    private fun activeMessage(now: Long = SystemClock.elapsedRealtime()): String {
        val skips = SkipLog.recent(now)
        val history = if (skips.isEmpty()) {
            "No DJ X skips in the last day."
        } else {
            val wallNow = System.currentTimeMillis()
            "Recent DJ X skips:\n" + skips.joinToString("\n") {
                "• " + DateUtils.getRelativeTimeSpanString(
                    wallNow - (now - it), wallNow, 0) // 0 = finest resolution (seconds).
            }
        }
        return "Notification listener is enabled. The service is running.\n\nYou can close this app.\n\n$history"
    }

    // Time until a skip of the given age either ticks to the next displayed unit or expires.
    // DateUtils floors to seconds under a minute, minutes under an hour, and hours beyond that.
    private fun msUntilDisplayChanges(ageMs: Long): Long {
        val unit = when {
            ageMs < DateUtils.MINUTE_IN_MILLIS -> DateUtils.SECOND_IN_MILLIS
            ageMs < DateUtils.HOUR_IN_MILLIS -> DateUtils.MINUTE_IN_MILLIS
            else -> DateUtils.HOUR_IN_MILLIS
        }
        val untilTick = unit - ageMs % unit
        val untilExpiry = SkipLog.MAX_AGE_MS - ageMs
        return minOf(untilTick, untilExpiry) + 1 // +1 so we land just past the boundary.
    }

    private fun isListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (flat.isNullOrEmpty()) return false
        val me = ComponentName(this, SpotifyListenerService::class.java)
        return flat.contains(me.flattenToString())
    }
}
