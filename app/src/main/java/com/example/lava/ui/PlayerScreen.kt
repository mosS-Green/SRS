package com.example.lava.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lava.data.MediaStateRepository

@Composable
fun PlayerScreen(
    mediaInfo: MediaStateRepository.MediaInfo,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrev: () -> Unit,
    onSeek: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title & Artist
            Text(
                text = mediaInfo.title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mediaInfo.artist,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Seek Bar
            val duration = mediaInfo.duration.toFloat().coerceAtLeast(1f)
            val position = mediaInfo.position.toFloat()
            
            Slider(
                value = position,
                onValueChange = { onSeek(it) },
                valueRange = 0f..duration,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlButton(icon = Icons.Default.SkipPrevious, onClick = onSkipPrev)
                
                // Custom Play/Pause Icon or standard
                val playIcon = if (mediaInfo.isPlaying) {
                     // Since I don't have a pause icon in standard filled without importing more, I need to check imports.
                     // Actually filled has basic ones.
                     // I will just use a Text or specific simple shapes if icons missing, 
                     // but Icons.Default should have PlayArrow and Pause.
                     // I need to make sure I import them.
                     // For compilation safety I will use a simple logical switch
                    Icons.Default.SkipNext // Placeholder, I'll fix imports below
                } else {
                    Icons.Default.SkipNext 
                }
                
                // Wait, I should use the correct icons.
                // I'll add PlayArrow and Pause to imports in the file content.
                
                Box(modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { onPlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    // Start/Pause symbol
                    // Drawing simple primitives or using vector
                    // I will use text for "target" simplicity if icons fail, but let's try to add proper icons
                   if (mediaInfo.isPlaying) {
                       // Pause symbol (2 bars)
                       Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                           Box(modifier = Modifier.size(6.dp, 24.dp).background(Color.White))
                           Box(modifier = Modifier.size(6.dp, 24.dp).background(Color.White))
                       }
                   } else {
                        // Play symbol (triangle)
                        // Canvas drawing
                        Canvas(modifier = Modifier.size(24.dp)) {
                            drawPath(
                                path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(size.width, size.height / 2)
                                    lineTo(0f, size.height)
                                    close()
                                },
                                color = Color.White
                            )
                        }
                   }
                }

                ControlButton(icon = Icons.Default.SkipNext, onClick = onSkipNext)
            }
        }
    }
}

@Composable
fun ControlButton(icon: ImageVector, onClick: () -> Unit) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier
            .size(48.dp)
            .clickable { onClick() }
    )
}
