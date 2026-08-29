package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.security.AuthResult
import com.example.security.BiometricHardwareStatus
import com.example.security.SecurityRepository
import com.example.ui.components.CyberCard
import com.example.ui.components.PinDotsIndicator
import com.example.ui.components.PinKeypad
import com.example.ui.theme.*
import kotlinx.coroutines.delay

private const val PIN_LENGTH = 4

@Composable
fun SecurityLockScreen(
    securityRepository: SecurityRepository,
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var lockoutSeconds by remember { mutableStateOf(0) }

    val isBiometricEnabled by securityRepository.isBiometricEnabled.collectAsState()
    val failedAttempts by securityRepository.failedAttempts.collectAsState()
    val biometricStatus = remember { securityRepository.checkBiometricStatus() }

    // Lockout countdown timer loop
    LaunchedEffect(Unit) {
        while (true) {
            if (securityRepository.isLockedOut()) {
                lockoutSeconds = securityRepository.getRemainingLockoutSeconds()
            } else {
                lockoutSeconds = 0
            }
            delay(1000)
        }
    }

    // Function to trigger biometric prompt
    fun triggerBiometrics() {
        if (activity != null && isBiometricEnabled && biometricStatus.isAvailable) {
            securityRepository.authenticateWithBiometrics(
                activity = activity,
                title = "Nexura Vault Security",
                subtitle = "Touch fingerprint sensor or look at camera to unlock",
                onResult = { result ->
                    when (result) {
                        is AuthResult.Success -> {
                            onUnlocked()
                        }
                        is AuthResult.Error -> {
                            errorMessage = result.message
                        }
                        is AuthResult.Failed -> {
                            isError = true
                            errorMessage = "Biometric verification failed"
                        }
                    }
                }
            )
        }
    }

    // Automatically trigger biometrics once on open
    LaunchedEffect(Unit) {
        if (isBiometricEnabled && biometricStatus.isAvailable && activity != null) {
            delay(350)
            triggerBiometrics()
        }
    }

    fun handleDigit(digit: String) {
        if (lockoutSeconds > 0) return

        isError = false
        errorMessage = null

        if (enteredPin.length < PIN_LENGTH) {
            enteredPin += digit
            if (enteredPin.length == PIN_LENGTH) {
                // Verify entered PIN
                val success = securityRepository.verifyPin(enteredPin)
                if (success) {
                    onUnlocked()
                } else {
                    isError = true
                    val isNowLocked = securityRepository.isLockedOut()
                    if (isNowLocked) {
                        lockoutSeconds = securityRepository.getRemainingLockoutSeconds()
                        errorMessage = "Too many failed attempts. Locked for $lockoutSeconds s."
                    } else {
                        val remaining = 5 - (failedAttempts + 1)
                        errorMessage = "Incorrect PIN. $remaining attempts remaining."
                    }
                    enteredPin = ""
                }
            }
        }
    }

    fun handleDelete() {
        if (lockoutSeconds > 0) return
        isError = false
        errorMessage = null
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
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
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Vault Shield & Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 24.dp)
            ) {
                // Glowing Security Lock Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(NeonCyan.copy(alpha = 0.2f), Color.Transparent)
                            )
                        )
                        .border(1.5.dp, NeonCyan.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Vault Locked",
                        tint = NeonCyan,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Text(
                    text = "NEXURA FINANCIAL VAULT",
                    style = MaterialTheme.typography.titleLarge.copy(
                        brush = SleekBrandGradient,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        fontSize = 18.sp
                    )
                )

                Text(
                    text = if (lockoutSeconds > 0) "SECURITY LOCKOUT ACTIVE" else "ENTER MASTER PIN TO UNLOCK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (lockoutSeconds > 0) NeonCrimson else TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    )
                )

                if (lockoutSeconds > 0) {
                    Text(
                        text = "Cooldown active: $lockoutSeconds seconds",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = NeonCrimson,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else {
                    PinDotsIndicator(
                        pinLength = PIN_LENGTH,
                        enteredLength = enteredPin.length,
                        isError = isError
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NeonCrimson,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            // Keypad & Biometric Action
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                PinKeypad(
                    onDigitClick = { handleDigit(it) },
                    onDeleteClick = { handleDelete() },
                    onBiometricClick = if (isBiometricEnabled && biometricStatus.isAvailable) {
                        { triggerBiometrics() }
                    } else null,
                    enabled = lockoutSeconds == 0
                )

                // Quick Links (Biometric Prompt trigger & Reset)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isBiometricEnabled && biometricStatus.isAvailable) {
                        TextButton(
                            onClick = { triggerBiometrics() },
                            modifier = Modifier.testTag("use_biometrics_text_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Use Biometrics",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    TextButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.testTag("forgot_pin_button")
                    ) {
                        Text(
                            text = "Forgot PIN?",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = SurfaceElevated,
            title = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = NeonAmber
                    )
                    Text(
                        text = "Reset Vault Security?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            },
            text = {
                Text(
                    text = "Resetting vault security will clear your current PIN and biometric configuration. You will need to complete security setup again.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        securityRepository.resetSecurity()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCrimson,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Reset PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
