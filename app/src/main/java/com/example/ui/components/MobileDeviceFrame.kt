package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun MobileDeviceFrame(
    isFrameEnabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!isFrameEnabled) {
        // Native Edge to Edge View
        Box(modifier = modifier.fillMaxSize()) {
            content()
        }
    } else {
        // Sleek High-Fidelity Mobile Device Shell Wrap
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF000000))
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .testTag("android_device_frame_container"),
            contentAlignment = Alignment.Center
        ) {
            // Phone Chassis
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(24.dp, RoundedCornerShape(36.dp), spotColor = NeonCyanGlow)
                    .clip(RoundedCornerShape(36.dp))
                    .background(CanvasBlack)
                    .border(1.5.dp, Color(0x33FFFFFF), RoundedCornerShape(36.dp))
            ) {
                // Sleek Status Bar with Dynamic Island / Notch
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CanvasBlack)
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    // Left Time
                    Text(
                        text = "9:41",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextPrimary.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    // Center Dynamic Island Notch
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(80.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                            .background(Color.Black)
                            .border(
                                1.dp,
                                Color(0x1AFFFFFF),
                                RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
                            )
                    )

                    // Right Status Icons (5G pill + Battery)
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "5G",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextPrimary,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        // Battery Indicator
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(10.dp)
                                .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(2.dp))
                                .padding(1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.75f)
                                    .background(TextPrimary.copy(alpha = 0.85f))
                            )
                        }
                    }
                }

                // Internal Screen App Viewport
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    content()
                }

                // Sleek Gesture Navigation Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CanvasBlack)
                        .padding(bottom = 8.dp, top = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(112.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0x26FFFFFF))
                    )
                }
            }
        }
    }
}

