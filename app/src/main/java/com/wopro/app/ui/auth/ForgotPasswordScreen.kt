package com.wopro.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wopro.app.ui.components.ErrorBanner
import com.wopro.app.ui.components.SuccessBanner

@Composable
fun ForgotPasswordScreen(
    vm: AuthViewModel,
    onBack: () -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }

    AuthScaffold(
        title = "Reset Password",
        subtitle = "Enter your email and we'll send a one-time code"
    ) {
        ui.error?.let {
            ErrorBanner(it)
            Spacer(Modifier.height(8.dp))
        }
        ui.success?.let {
            SuccessBanner(it)
            Spacer(Modifier.height(8.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { vm.sendOtp(email) },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Send Code", style = MaterialTheme.typography.titleMedium)
            }
            if (ui.otpSent) {
                OtpSection(
                    email = email,
                    onVerify = { code -> vm.verifyOtp(code, email) },
                    onReset = { newPw -> vm.resetPassword(email, newPw) },
                    onDone = { onBack() }
                )
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Sign In")
            }
        }
    }
}

@Composable
private fun OtpSection(
    email: String,
    onVerify: (String) -> Unit,
    onReset: (String) -> Unit,
    onDone: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var newPw by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Code sent to $email (demo: any 6 digits)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.filter(Char::isDigit).take(6) },
            label = { Text("6-digit code") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = newPw,
            onValueChange = { newPw = it },
            label = { Text("New password (min 6 chars)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                onVerify(code)
                if (newPw.length >= 6) onReset(newPw)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify & Reset")
        }
    }
}
