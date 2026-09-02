package com.wopro.app.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

/**
 * Membuat FileProvider URI untuk hasil foto kamera.
 * File disimpan di cacheDir/images/ (sesuai @xml/file_paths).
 */
private fun createCameraUri(context: Context): Uri {
    val dir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(dir, "wo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
}

/**
 * Launcher kamera dengan permission request otomatis.
 * Panggil return-nya (ex: `openCamera()`) → minta izin CAMERA → buka kamera sistem.
 * Hasil foto dikembalikan via [onCaptured] sebagai string URI (content:// atau file://).
 */
@Composable
fun rememberCameraLauncher(onCaptured: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = pendingUri
        pendingUri = null
        if (ok && uri != null) {
            onCaptured(uri.toString())
        }
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createCameraUri(context)
            pendingUri = uri
            takePicture.launch(uri)
        }
    }

    return {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val uri = createCameraUri(context)
            pendingUri = uri
            takePicture.launch(uri)
        } else {
            permLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}
