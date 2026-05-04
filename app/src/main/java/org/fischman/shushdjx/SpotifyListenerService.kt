package org.fischman.shushdjx

import android.app.Notification
import android.media.session.MediaController
import android.media.session.MediaSession
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class SpotifyListenerService : NotificationListenerService() {

    private var lastKey = ""

    companion object {
        private const val TAG = "ShushDJX"
        private const val SPOTIFY_PKG = "com.spotify.music"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val sbnNotNull = sbn ?: return
        if (sbnNotNull.packageName != SPOTIFY_PKG) return

        val n = sbnNotNull.notification
        val extras = n.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""

        val key = "$title|$text"
        if (key == lastKey) return
        lastKey = key

        if (BuildConfig.DEBUG) Log.d(TAG, "Spotify notification: $key")

        if (key != "Up next|DJ X") return

        Log.i(TAG, "DJ interlude detected — skipping")
        n.extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)?.let {
            val controller = MediaController(this, it)
            controller.transportControls?.skipToNext()
            Log.i(TAG, "Sent skipToNext")
        }
    }
}
