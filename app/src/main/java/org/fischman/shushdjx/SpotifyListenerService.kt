package org.fischman.shushdjx

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.util.Log

class SpotifyListenerService : NotificationListenerService() {

    private var spotifyController: MediaController? = null
    private var lastMetadataTitle: String? = null
    private var lastSkipTime = 0L

    companion object {
        private const val TAG = "ShushDJX"
        private const val SPOTIFY_PKG = "com.spotify.music"
        private const val MAX_SKIP_ATTEMPTS = 6
        private const val RETRY_INTERVAL_MS = 300L
        private const val COOLDOWN_MS = 2000L
    }

    private val metadataCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: return
            if (title == lastMetadataTitle) return
            lastMetadataTitle = title

            if (!title.equals("Up next", ignoreCase = true)) return

            val now = System.currentTimeMillis()
            if (now - lastSkipTime < COOLDOWN_MS) return

            Log.i(TAG, "DJ Next up detected; skipping.")
            attemptSkip(1)
        }
    }

    private fun attemptSkip(attempt: Int) {
        val ctrl = spotifyController ?: return // Might be null on a postDelayed firing.

        if (attempt > 1) {
            val currentTitle = ctrl.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
            if (!currentTitle.equals("Up next", ignoreCase = true)) {
                Log.i(TAG, "Skip succeeded on attempt ${attempt - 1}")
                return
            }
        }

        ctrl.transportControls?.skipToNext()
        lastSkipTime = System.currentTimeMillis()
        Log.d(TAG, "skipToNext attempt $attempt/$MAX_SKIP_ATTEMPTS  state=${ctrl.playbackState?.state}")

        if (attempt < MAX_SKIP_ATTEMPTS) {
            android.os.Handler(mainLooper).postDelayed({ attemptSkip(attempt + 1) }, RETRY_INTERVAL_MS)
        }
    }

    private val sessionsChangedListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            val ctrl = controllers?.firstOrNull { it.packageName == SPOTIFY_PKG }
            if (ctrl != null && ctrl != spotifyController) {
                spotifyController?.unregisterCallback(metadataCallback)
                spotifyController = ctrl
                ctrl.registerCallback(metadataCallback)
                Log.d(TAG, "Attached to Spotify MediaSession")
            } else if (ctrl == null && spotifyController != null) {
                spotifyController?.unregisterCallback(metadataCallback)
                spotifyController = null
                Log.d(TAG, "Spotify MediaSession gone")
            }
        }

    override fun onListenerConnected() {
        super.onListenerConnected()
        migrateNotificationFilter(0, null)
        val cn = ComponentName(this, SpotifyListenerService::class.java)
        val msm = getSystemService(MediaSessionManager::class.java)
        msm.addOnActiveSessionsChangedListener(sessionsChangedListener, cn)
        // Fire once for sessions that already exist.
        sessionsChangedListener.onActiveSessionsChanged(msm.getActiveSessions(cn))
    }

    override fun onListenerDisconnected() {
        getSystemService(MediaSessionManager::class.java)
            .removeOnActiveSessionsChangedListener(sessionsChangedListener)
        spotifyController?.unregisterCallback(metadataCallback)
        spotifyController = null
        super.onListenerDisconnected()
    }
}
