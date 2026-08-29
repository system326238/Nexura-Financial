package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun PinDotsIndicator(
    pinLength: Int,
    enteredLength: Int,
    isError: Boolean,
    modifier: Modifier = Modifier,
    dotSize: Dp = 16.dp,
    activeColor: Color = NeonCyan,
    errorColor: Color = NeonCrimson
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(isError) {
        if (isError) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    (-20f) at 50
                    20f at 100
                    (-15f) at 150
                    15f at 200
                    (-10f) at 250
                    10f at 300
                    0f at 400
                }
            )
        }
    }

    Row(
        modifier = modifier
            .offset(x = shakeOffset.value.dp)
            .padding(vertical = 12.dp)
            .testTag("pin_dots_indicator"),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until pinLength) {
            val isFilled = i < enteredLength
            val currentColor = if (isError) errorColor else activeColor

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .then(
                        if (isFilled) {
                            Modifier.shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                ambientColor = currentColor,
                                spotColor = currentColor
                            )
                        } else Modifier
                    )
                    .clip(CircleShape)
                    .background(
                        if (isFilled) currentColor else Color.Transparent
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isFilled) currentColor else SurfaceCardBorderActive,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun PinKeypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: (() -> Unit)? = null,
    biometricIcon: ImageVector = Icons.Default.Fingerprint,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val keypadLayout = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("BIO", "0", "DEL")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keypadLayout.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    when (key) {
                        "BIO" -> {
                            if (onBiometricClick != null) {
                                KeypadIconButton(
                                    icon = biometricIcon,
                                    contentDescription = "Authenticate with Biometrics",
                                    tint = NeonCyan,
                                    enabled = enabled,
                                    onClick = onBiometricClick,
                                    testTag = "keypad_biometric_button"
                                )
                            } else {
                                Spacer(modifier = Modifier.size(72.dp))
                            }
                        }
                        "DEL" -> {
                            KeypadIconButton(
                                icon = Icons.Default.Backspace,
                                contentDescription = "Delete digit",
                                tint = TextSecondary,
                                enabled = enabled,
                                onClick = onDeleteClick,
                                testTag = "keypad_delete_button"
                            )
                        }
                        else -> {
                            KeypadDigitButton(
                                digit = key,
                                enabled = enabled,
                                onClick = { onDigitClick(key) },
                                testTag = "keypad_digit_$key"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadDigitButton(
    digit: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val size = 72.dp
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(SurfaceElevated)
            .border(1.dp, SurfaceCardBorder, CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = if (enabled) TextPrimary else TextMuted,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.SansSerif,
                fontSize = 28.sp
            )
        )
    }
}

@Composable
fun KeypadIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val size = 72.dp
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(SurfaceElevated.copy(alpha = 0.6f))
            .border(1.dp, SurfaceCardBorder, CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else TextMuted,
            modifier = Modifier.size(26.dp)
        )
    }
}
