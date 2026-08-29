package com.example.security

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

enum class BiometricHardwareStatus(val label: String, val isAvailable: Boolean) {
    AVAILABLE("Biometric Sensor Ready", true),
    NOT_ENROLLED("No Biometrics Registered on Device", false),
    NO_HARDWARE("No Biometric Sensor Detected", false),
    UNAVAILABLE("Biometric Sensor Temporarily Unavailable", false),
    UNKNOWN("Status Unknown", false)
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Failed : AuthResult()
}

class SecurityRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("nexura_security_prefs", Context.MODE_PRIVATE)

    private val _isSetupCompleted = MutableStateFlow(prefs.getBoolean(KEY_SETUP_COMPLETED, false))
    val isSetupCompleted: StateFlow<Boolean> = _isSetupCompleted.asStateFlow()

    private val _isUnlocked = MutableStateFlow(!_isSetupCompleted.value)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()

    private val _lockoutUntil = MutableStateFlow<Long>(0)
    val lockoutUntil: StateFlow<Long> = _lockoutUntil.asStateFlow()

    companion object {
        private const val KEY_SETUP_COMPLETED = "key_security_setup_completed"
        private const val KEY_PIN_HASH = "key_security_pin_hash"
        private const val KEY_PIN_SALT = "key_security_pin_salt"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val MAX_ATTEMPTS_BEFORE_LOCKOUT = 5
        private const val LOCKOUT_DURATION_MS = 30_000L // 30 seconds
    }

    fun checkBiometricStatus(): BiometricHardwareStatus {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                             BiometricManager.Authenticators.BIOMETRIC_WEAK or 
                             BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricHardwareStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricHardwareStatus.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricHardwareStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricHardwareStatus.UNAVAILABLE
            else -> BiometricHardwareStatus.UNKNOWN
        }
    }

    fun completeSetup(pin: String, enableBiometric: Boolean) {
        val salt = generateSalt()
        val hash = hashPin(pin, salt)

        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putString(KEY_PIN_SALT, salt)
            .putBoolean(KEY_BIOMETRIC_ENABLED, enableBiometric)
            .putBoolean(KEY_SETUP_COMPLETED, true)
            .apply()

        _isBiometricEnabled.value = enableBiometric
        _isSetupCompleted.value = true
        _isUnlocked.value = true
        _failedAttempts.value = 0
    }

    fun verifyPin(enteredPin: String): Boolean {
        if (isLockedOut()) {
            return false
        }

        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val storedSalt = prefs.getString(KEY_PIN_SALT, "") ?: ""
        val enteredHash = hashPin(enteredPin, storedSalt)

        val isValid = storedHash == enteredHash
        if (isValid) {
            _isUnlocked.value = true
            _failedAttempts.value = 0
        } else {
            val newAttempts = _failedAttempts.value + 1
            _failedAttempts.value = newAttempts
            if (newAttempts >= MAX_ATTEMPTS_BEFORE_LOCKOUT) {
                _lockoutUntil.value = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            }
        }
        return isValid
    }

    fun isLockedOut(): Boolean {
        val now = System.currentTimeMillis()
        if (_lockoutUntil.value > now) {
            return true
        }
        if (_lockoutUntil.value > 0 && now >= _lockoutUntil.value) {
            _lockoutUntil.value = 0
            _failedAttempts.value = 0
        }
        return false
    }

    fun getRemainingLockoutSeconds(): Int {
        val diff = _lockoutUntil.value - System.currentTimeMillis()
        return if (diff > 0) (diff / 1000).toInt() + 1 else 0
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    fun unlockWithBiometrics() {
        _isUnlocked.value = true
        _failedAttempts.value = 0
    }

    fun lockApp() {
        if (_isSetupCompleted.value) {
            _isUnlocked.value = false
        }
    }

    fun resetSecurity() {
        prefs.edit().clear().apply()
        _isSetupCompleted.value = false
        _isUnlocked.value = false
        _isBiometricEnabled.value = true
        _failedAttempts.value = 0
        _lockoutUntil.value = 0
    }

    fun authenticateWithBiometrics(
        activity: FragmentActivity,
        title: String = "Nexura Vault Security",
        subtitle: String = "Verify your biometric identity to unlock",
        onResult: (AuthResult) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                BiometricManager.Authenticators.BIOMETRIC_WEAK or 
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    unlockWithBiometrics()
                    onResult(AuthResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onResult(AuthResult.Error(errString.toString()))
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onResult(AuthResult.Failed)
                }
            }
        )

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onResult(AuthResult.Error(e.localizedMessage ?: "Biometric prompt error"))
        }
    }

    private fun hashPin(pin: String, salt: String): String {
        val input = pin + salt
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun generateSalt(): String {
        return (1..16)
            .map { "0123456789abcdef"[kotlin.random.Random.nextInt(16)] }
            .joinToString("")
    }
}
