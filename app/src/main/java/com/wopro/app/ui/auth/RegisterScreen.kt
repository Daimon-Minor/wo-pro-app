package com.wopro.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wopro.app.ui.components.ErrorBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    vm: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onGoLogin: () -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var depExpanded by remember { mutableStateOf(false) }

    val departments = listOf(
        "ENGINEERING", "HOUSEKEEPING", "F&B", "General",
        "FO (FRONT OFFICE)", "Operator", "DUTY MANAGER"
    )

    AuthScaffold(
        title = "Create Account",
        subtitle = "Start managing your facility in minutes"
    ) {
        localError?.let {
            ErrorBanner(it)
            Spacer(Modifier.height(8.dp))
        }
        AuthStateBox(ui, onReady = { onRegisterSuccess() }) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Dropdown Departemen — sesuai daftar departemen hotel
                ExposedDropdownMenuBox(
                    expanded = depExpanded,
                    onExpandedChange = { depExpanded = it }
                ) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Department") },
                        placeholder = { Text("Select department") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = depExpanded) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = depExpanded,
                        onDismissRequest = { depExpanded = false }
                    ) {
                        departments.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = { department = d; depExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password (min 6 chars)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        localError = null
                        if (password != confirm) {
                            localError = "Passwords do not match"
                        } else {
                            vm.register(name, email, password, department)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Create Account", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onGoLogin,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Already have an account? Sign In")
                }
            }
        }
    }
}
