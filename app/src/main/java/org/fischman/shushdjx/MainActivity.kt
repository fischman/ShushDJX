package org.fischman.shushdjx

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings

class MainActivity : Activity() {

    override fun onResume() {
        super.onResume()
        if (isListenerEnabled()) {
            AlertDialog.Builder(this)
                .setTitle("ShushDJX Active")
                .setMessage("Notification listener is enabled. The service is running.\n\nYou can close this app.")
                .setPositiveButton("OK") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("ShushDJX needs notification access to watch Spotify's now-playing notification and skip \"DJ X\" interludes.\n\nTap OK to open Settings and then grant access.")
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

    private fun isListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (flat.isNullOrEmpty()) return false
        val me = ComponentName(this, SpotifyListenerService::class.java)
        return flat.contains(me.flattenToString())
    }
}
