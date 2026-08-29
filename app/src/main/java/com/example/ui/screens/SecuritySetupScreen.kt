package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.BiometricHardwareStatus
import com.example.security.SecurityRepository
import com.example.ui.components.CyberCard
import com.example.ui.components.PinDotsIndicator
import com.example.ui.components.PinKeypad
import com.example.ui.theme.*

private const val PIN_LENGTH = 4

enum class SetupStep {
    CREATE_PIN,
    CONFIRM_PIN,
    BIOMETRICS
}

@Composable
fun SecuritySetupScreen(
    securityRepository: SecurityRepository,
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(SetupStep.CREATE_PIN) }
    var enteredPin by remember { mutableStateOf("") }
    var confirmedPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var enableBiometrics by remember { mutableStateOf(true) }

    val biometricStatus = remember { securityRepository.checkBiometricStatus() }

    fun handleDigit(digit: String) {
        isError = false
        errorMessage = null

        when (currentStep) {
            SetupStep.CREATE_PIN -> {
                if (enteredPin.length < PIN_LENGTH) {
                    enteredPin += digit
                    if (enteredPin.length == PIN_LENGTH) {
                        // Move to confirm step
                        currentStep = SetupStep.CONFIRM_PIN
                    }
                }
            }
            SetupStep.CONFIRM_PIN -> {
                if (confirmedPin.length < PIN_LENGTH) {
                    confirmedPin += digit
                    if (confirmedPin.length == PIN_LENGTH) {
                        if (confirmedPin == enteredPin) {
                            // PIN verified successfully
                            currentStep = SetupStep.BIOMETRICS
                        } else {
                            // Mismatch error
                            isError = true
                            errorMessage = "PINs do not match. Please try again."
                            confirmedPin = ""
                            enteredPin = ""
                            currentStep = SetupStep.CREATE_PIN
                        }
                    }
                }
            }
            SetupStep.BIOMETRICS -> {
                // Keypad not active in biometrics step
            }
        }
    }

    fun handleDelete() {
        isError = false
        errorMessage = null
        when (currentStep) {
            SetupStep.CREATE_PIN -> {
                if (enteredPin.isNotEmpty()) {
                    enteredPin = enteredPin.dropLast(1)
                }
            }
            SetupStep.CONFIRM_PIN -> {
                if (confirmedPin.isNotEmpty()) {
                    confirmedPin = confirmedPin.dropLast(1)
                } else {
                    // Back to create step
                    enteredPin = ""
                    currentStep = SetupStep.CREATE_PIN
                }
            }
            SetupStep.BIOMETRICS -> {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBlack)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Sleek Shield & Progress
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Glowing Security Orb
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(NeonCyan.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                        .border(1.5.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (currentStep == SetupStep.BIOMETRICS) Icons.Default.Fingerprint else Icons.Default.Shield,
                        contentDescription = "Security Setup",
                        tint = NeonCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "VAULT SECURITY SETUP",
                    style = MaterialTheme.typography.titleMedium.copy(
                        brush = SleekBrandGradient,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        fontSize = 18.sp
                    )
                )

                // Step Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SetupStep.values().forEach { step ->
                        val isActive = step == currentStep
                        val isDone = step.ordinal < currentStep.ordinal
                        Box(
                            modifier = Modifier
                                .width(if (isActive) 32.dp else 12.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    when {
                                        isActive -> NeonCyan
                                        isDone -> NeonEmerald
                                        else -> Color(0x26FFFFFF)
                                    }
                                )
                        )
                    }
                }
            }

            // Step Content
            when (currentStep) {
                SetupStep.CREATE_PIN -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Create 4-Digit Master PIN",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = "This PIN will be required to open your financial ledger and vault.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        PinDotsIndicator(
                            pinLength = PIN_LENGTH,
                            enteredLength = enteredPin.length,
                            isError = isError
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NeonCrimson,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    PinKeypad(
                        onDigitClick = { handleDigit(it) },
                        onDeleteClick = { handleDelete() },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                SetupStep.CONFIRM_PIN -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Confirm Master PIN",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = "Re-enter your 4-digit PIN to confirm accuracy.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        PinDotsIndicator(
                            pinLength = PIN_LENGTH,
                            enteredLength = confirmedPin.length,
                            isError = isError
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NeonCrimson,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    PinKeypad(
                        onDigitClick = { handleDigit(it) },
                        onDeleteClick = { handleDelete() },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                SetupStep.BIOMETRICS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Biometric Authentication",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = "Enable Android BiometricPrompt for instant fingerprint & facial unlocking.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )
                        )

                        // Hardware Sensor Status Card
                        CyberCard(
                            borderColor = if (biometricStatus.isAvailable) NeonEmerald.copy(alpha = 0.4f) else NeonAmber.copy(alpha = 0.4f),
                            glowColor = if (biometricStatus.isAvailable) NeonEmeraldGlow else NeonAmberGlow
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (biometricStatus.isAvailable) NeonEmerald.copy(alpha = 0.15f) else NeonAmber.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (biometricStatus.isAvailable) Icons.Default.CheckCircle else Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (biometricStatus.isAvailable) NeonEmerald else NeonAmber,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "HARDWARE SENSOR TELEMETRY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSecondary,
                                            fontSize = 9.sp,
                                            letterSpacing = 0.8.sp
                                        )
                                    )
                                    Text(
                                        text = biometricStatus.label,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = if (biometricStatus.isAvailable) NeonEmerald else NeonAmber,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        // Toggle Biometrics Card
                        CyberCard(borderColor = SurfaceCardBorder) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Require Biometrics on Open",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Prompt for Fingerprint or Face ID when launching Nexura",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Switch(
                                    checked = enableBiometrics,
                                    onCheckedChange = { enableBiometrics = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = NeonCyan,
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = SurfaceElevated
                                    ),
                                    modifier = Modifier.testTag("biometric_enable_switch")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Complete Setup Button
                        Button(
                            onClick = {
                                securityRepository.completeSetup(
                                    pin = enteredPin,
                                    enableBiometric = enableBiometrics
                                )
                                onSetupComplete()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("complete_security_setup_button")
                        ) {
                            Text(
                                text = "COMPLETE SETUP & ENTER VAULT",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
