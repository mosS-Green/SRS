package com.example.lava

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.session.MediaController
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lava.data.MediaStateRepository
import com.example.lava.ui.PlayerScreen
import com.example.lava.ui.SetupScreen
import com.example.lava.ui.components.AmbientAura
import com.example.lava.ui.theme.LavaTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // request no title
        
        // Keep screen on logic will be handled by compose state
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            LavaTheme {
                val context = this
                val hasPermission = remember { 
                    val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                    enabledListeners != null && enabledListeners.contains(context.packageName)
                }

                if (hasPermission) {
                    MainContent()
                } else {
                    SetupScreen()
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    val mediaInfo by MediaStateRepository.mediaInfo.collectAsStateWithLifecycle()
    
    // AOD Logic
    var userInteracted by remember { mutableStateOf(System.currentTimeMillis()) }
    var isDimmed by remember { mutableStateOf(false) }
    
    // Activity Monitor
    LaunchedEffect(mediaInfo.isPlaying, userInteracted) {
        if (!mediaInfo.isPlaying) {
             // Turn off after 10 seconds if paused
             delay(10000)
             // In a real app we would finish() or release wake lock, but here we just show black
             // or let the system sleep. To let system sleep we would remove flag, but we set it in onCreate.
             // We'll just dim to black.
             isDimmed = true
        } else {
            // Playing
            isDimmed = false
            delay(7000)
            isDimmed = true
        }
    }
    
    // Reset interaction on tap
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(interactionSource = interactionSource, indication = null) {
                userInteracted = System.currentTimeMillis()
                isDimmed = false
            }
    ) {
        if (!isDimmed) {
             // Show full UI
             AmbientAura(albumArt = mediaInfo.albumArt)
             
             PlayerScreen(
                 mediaInfo = mediaInfo,
                 onPlayPause = {
                     val controller = MediaStateRepository.activeController
                     if (mediaInfo.isPlaying) controller?.transportControls?.pause()
                     else controller?.transportControls?.play()
                 },
                 onSkipNext = { MediaStateRepository.activeController?.transportControls?.skipToNext() },
                 onSkipPrev = { MediaStateRepository.activeController?.transportControls?.skipToPrevious() },
                 onSeek = { pos -> MediaStateRepository.activeController?.transportControls?.seekTo(pos.toLong()) }
             )
        } else {
            // AOD Mode
            // "Show AOD... or keep screen on and reduce brightness"
            // We just show the Aura but very dim, or nothing?
            // "piques up on new song starting" -> mediaInfo change updates this.
            
            // We will dim the Aura
             Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                 AmbientAura(albumArt = mediaInfo.albumArt, modifier = Modifier.background(Color.Black.copy(alpha=0.8f)))
                 // minimal text
             }
        }
    }
}
