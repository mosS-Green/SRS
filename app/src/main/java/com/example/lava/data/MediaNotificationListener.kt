package com.example.lava.data

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Singleton object to hold current media state so UI can observe it.
 * In a real architecture this would be injected, but for simplicity we use a global object
 * or we'd need to bind the service to the activity.
 * To stay "compilation ready" and simple, we'll expose a static flow.
 */
object MediaStateRepository {
    data class MediaInfo(
        val title: String = "No Track",
        val artist: String = "Unknown Artist",
        val isPlaying: Boolean = false,
        val albumArt: Bitmap? = null,
        val duration: Long = 0L,
        val position: Long = 0L
    )

    private val _mediaInfo = MutableStateFlow(MediaInfo())
    val mediaInfo: StateFlow<MediaInfo> = _mediaInfo.asStateFlow()

    fun updateInfo(
        title: String? = null, 
        artist: String? = null, 
        isPlaying: Boolean? = null,
        albumArt: Bitmap? = null,
        duration: Long? = null,
        position: Long? = null
    ) {
        _mediaInfo.update { current ->
            current.copy(
                title = title ?: current.title,
                artist = artist ?: current.artist,
                isPlaying = isPlaying ?: current.isPlaying,
                albumArt = albumArt ?: current.albumArt,
                duration = duration ?: current.duration,
                position = position ?: current.position
            )
        }
    }
    
    // Callbacks to control media
    var activeController: MediaController? = null
}

class MediaNotificationListener : NotificationListenerService() {

    private lateinit var mediaSessionManager: MediaSessionManager

    override fun onCreate() {
        super.onCreate()
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        
        // Listen for active sessions
        val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            updateActiveSession(controllers)
        }
        
        try {
            val componentName = ComponentName(this, MediaNotificationListener::class.java)
            mediaSessionManager.addOnActiveSessionsChangedListener(sessionListener, componentName)
            // Initial check
            updateActiveSession(mediaSessionManager.getActiveSessions(componentName))
        } catch (e: SecurityException) {
            // Permission not granted yet
        }
    }

    private fun updateActiveSession(controllers: List<MediaController>?) {
        val controller = controllers?.firstOrNull() ?: return
        MediaStateRepository.activeController = controller
        
        registerCallback(controller)
        
        // Initial Read
        extractMetadata(controller.metadata)
        extractPlaybackState(controller.playbackState)
    }

    private fun registerCallback(controller: MediaController) {
        controller.registerCallback(object : MediaController.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackState?) {
                extractPlaybackState(state)
            }

            override fun onMetadataChanged(metadata: MediaMetadata?) {
                extractMetadata(metadata)
            }
        })
    }

    private fun extractMetadata(metadata: MediaMetadata?) {
        metadata?.let {
            val title = it.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown Title"
            val artist = it.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown Artist"
            val bitmap = it.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) 
                ?: it.getBitmap(MediaMetadata.METADATA_KEY_ART)
            val duration = it.getLong(MediaMetadata.METADATA_KEY_DURATION)

            MediaStateRepository.updateInfo(
                title = title, 
                artist = artist, 
                albumArt = bitmap,
                duration = duration
            )
        }
    }

    private fun extractPlaybackState(state: PlaybackState?) {
        state?.let {
            val isPlaying = it.state == PlaybackState.STATE_PLAYING
            MediaStateRepository.updateInfo(
                isPlaying = isPlaying,
                position = it.position
            )
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            val componentName = ComponentName(this, MediaNotificationListener::class.java)
            val controllers = mediaSessionManager.getActiveSessions(componentName)
            updateActiveSession(controllers)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
