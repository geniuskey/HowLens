package kr.howlens.app.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.util.concurrent.Executor

/**
 * CameraX still-photo capture pane. The caller owns the gallery picker and decides whether
 * the returned cache URI is accepted by its downstream image validation.
 */
@Composable
fun CameraCapturePane(
    isActive: Boolean,
    onPhotoCaptured: (Uri) -> Unit,
    onError: (String) -> Unit,
    onChooseFromGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor: Executor = remember(context) { ContextCompat.getMainExecutor(context) }
    val currentOnPhotoCaptured by rememberUpdatedState(onPhotoCaptured)
    val currentOnError by rememberUpdatedState(onError)
    val currentOnChooseFromGallery by rememberUpdatedState(onChooseFromGallery)
    val buttonColors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052FF))

    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }
    var cameraBlocked by remember { mutableStateOf(false) }
    var capturePending by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionDenied = !granted
        cameraBlocked = !granted && hasRequestedPermission &&
            findActivity(context)?.let {
                !androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.CAMERA,
                )
            } == true
        if (granted) {
            cameraBlocked = false
            cameraError = null
        }
    }

    val cameraGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA,
    ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(cameraGranted) {
        if (cameraGranted) {
            permissionDenied = false
            cameraBlocked = false
        }
    }

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            factory = { viewContext ->
                PreviewView(viewContext).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewView = this
                }
            },
            update = { previewView = it },
        )

        DisposableEffect(isActive, cameraGranted, lifecycleOwner, previewView) {
            val view = previewView
            if (!isActive || !cameraGranted || view == null) {
                imageCapture = null
                onDispose { }
            } else {
                var disposed = false
                var provider: ProcessCameraProvider? = null
                var boundPreview: Preview? = null
                var boundCapture: ImageCapture? = null
                cameraError = null

                val providerFuture = ProcessCameraProvider.getInstance(context)
                providerFuture.addListener({
                    if (disposed) return@addListener
                    try {
                        val cameraProvider = providerFuture.get()
                        provider = cameraProvider
                        val preview = Preview.Builder().build()
                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .setJpegQuality(JPEG_QUALITY)
                            .setResolutionSelector(
                                ResolutionSelector.Builder()
                                    .setResolutionFilter { supportedSizes, _ ->
                                        supportedSizes.filter { size ->
                                            isResolutionWithinPhotoLimit(size.width, size.height)
                                        }
                                    }
                                    .build(),
                            )
                            .setTargetRotation(view.display?.rotation ?: Surface.ROTATION_0)
                            .build()
                        preview.setSurfaceProvider(view.surfaceProvider)
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture,
                        )
                        boundPreview = preview
                        boundCapture = capture
                        imageCapture = capture
                    } catch (failure: Exception) {
                        if (!disposed) {
                            val message = "Unable to start the camera: ${failure.localizedMessage ?: "unknown error"}"
                            cameraError = message
                            currentOnError(message)
                        }
                    }
                }, mainExecutor)

                onDispose {
                    disposed = true
                    imageCapture = null
                    boundPreview?.let { preview ->
                        boundCapture?.let { capture ->
                            runCatching { provider?.unbind(preview, capture) }
                        }
                    }
                }
            }
        }

        when {
            !isActive -> Text("Camera paused")
            !cameraGranted && cameraBlocked -> Text("Camera access is blocked. Enable it in Settings or choose a photo.")
            !cameraGranted && permissionDenied -> Text("Camera permission is needed to take a photo.")
            !cameraGranted -> Text("Allow camera access to take a photo.")
            cameraError != null -> Text(cameraError!!)
        }

        if (isActive && !cameraGranted && !cameraBlocked) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = buttonColors,
                onClick = {
                    hasRequestedPermission = true
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
            ) { Text("Allow camera") }
        }

        if (isActive && cameraGranted) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = buttonColors,
                enabled = imageCapture != null && !capturePending,
                onClick = {
                    val capture = imageCapture ?: return@Button
                    if (capturePending) return@Button
                    capturePending = true
                    val outputFile = try {
                        File.createTempFile("howlens-camera-", ".jpg", context.cacheDir)
                    } catch (failure: Exception) {
                        capturePending = false
                        currentOnError("Unable to prepare a photo: ${failure.localizedMessage ?: "storage unavailable"}")
                        return@Button
                    }
                    val output = ImageCapture.OutputFileOptions.Builder(outputFile).build()
                    capture.takePicture(output, mainExecutor, object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                            capturePending = false
                            if (!isActive || imageCapture !== capture) {
                                outputFile.delete()
                                return
                            }
                            val jpegBytes = outputFile.length()
                            if (jpegBytes <= 0L) {
                                outputFile.delete()
                                currentOnError("The camera returned an empty photo.")
                                return
                            }
                            if (!isJpegSizeWithinPhotoLimit(jpegBytes)) {
                                outputFile.delete()
                                currentOnError("The photo is larger than 10 MiB. Try again with less detail in the frame.")
                                return
                            }
                            currentOnPhotoCaptured(Uri.fromFile(outputFile))
                        }

                        override fun onError(exception: ImageCaptureException) {
                            outputFile.delete()
                            capturePending = false
                            if (isActive && imageCapture === capture) {
                                currentOnError("Photo capture failed: ${exception.localizedMessage ?: exception.imageCaptureError}")
                            }
                        }
                    })
                },
            ) { Text(if (capturePending) "Taking photo…" else "Take photo") }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = buttonColors,
            onClick = { currentOnChooseFromGallery() },
        ) { Text("Choose photo") }
    }
}

private tailrec fun findActivity(context: Context): Activity? = when (context) {
    is Activity -> context
    is ContextWrapper -> findActivity(context.baseContext)
    else -> null
}

private const val JPEG_QUALITY = 85
