package kr.howlens.app.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
    onOpenPhotoAnalysis: () -> Unit = onChooseFromGallery,
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
    var permissionUiState by remember(context) {
        mutableStateOf(readCameraPermissionUiState(context, requestWasMade = false))
    }
    var capturePending by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    val captureGeneration = remember { CaptureGeneration() }

    val refreshCameraPermission = remember(context) {
        {
            permissionUiState = readCameraPermissionUiState(
                context = context,
                requestWasMade = hasRequestedPermission,
            )
            if (permissionUiState.granted) cameraError = null
        }
    }

    DisposableEffect(lifecycleOwner, refreshCameraPermission) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshCameraPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            refreshCameraPermission()
        }
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            permissionUiState = resolveCameraPermissionUiState(
                granted = true,
                requestWasMade = true,
                shouldShowRationale = false,
            )
            cameraError = null
        } else {
            refreshCameraPermission()
        }
    }

    LaunchedEffect(isActive, permissionUiState, hasRequestedPermission) {
        if (isActive && !permissionUiState.granted && !permissionUiState.denied &&
            !permissionUiState.permanentlyDenied && !hasRequestedPermission
        ) {
            hasRequestedPermission = true
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val cameraGranted = permissionUiState.granted

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFF080E1A)).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.weight(1f).fillMaxWidth().semantics { contentDescription = "CameraX 미리보기" }) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                PreviewView(viewContext).apply {
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewView = this
                }
            },
            update = { previewView = it },
        )

        Canvas(Modifier.fillMaxSize().padding(28.dp)) {
            val length = 28.dp.toPx()
            val corners = listOf(Offset(0f, size.height * .18f), Offset(size.width, size.height * .18f),
                Offset(0f, size.height * .82f), Offset(size.width, size.height * .82f))
            corners.forEach { point ->
                val dx = if (point.x == 0f) length else -length
                val dy = if (point.y < size.height / 2f) length else -length
                drawLine(Color(0xFF367CFF), point, point + Offset(dx, 0f), 3.dp.toPx(), StrokeCap.Round)
                drawLine(Color(0xFF367CFF), point, point + Offset(0f, dy), 3.dp.toPx(), StrokeCap.Round)
            }
        }
        }
        Text("부품 전체가 보이게 촬영해 주세요.", color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally))

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
                            val message = "카메라를 시작할 수 없어요."
                            cameraError = message
                            currentOnError(message)
                        }
                    }
                }, mainExecutor)

                onDispose {
                    disposed = true
                    captureGeneration.invalidate()
                    capturePending = false
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
            !isActive -> Text("카메라 일시정지")
            !cameraGranted && permissionUiState.permanentlyDenied -> Text("카메라 권한이 필요해요. 설정에서 허용해 주세요.")
            !cameraGranted && permissionUiState.denied -> Text("촬영하려면 카메라 권한이 필요해요.")
            !cameraGranted -> Text("카메라 권한을 허용해 주세요.")
            cameraError != null -> Text(cameraError!!)
        }

        if (isActive && !cameraGranted && permissionUiState.permanentlyDenied) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = buttonColors,
                onClick = {
                    val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                },
            ) { Text("설정 열기") }
        }

        if (isActive && (!cameraGranted && permissionUiState.denied || cameraError != null)) {
            Button(
                modifier = Modifier.fillMaxWidth(), colors = buttonColors,
                onClick = onOpenPhotoAnalysis,
            ) { Text("사진으로 확인") }
        }

        if (isActive && !cameraGranted && !permissionUiState.permanentlyDenied && permissionUiState.denied) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    hasRequestedPermission = true
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
            ) { Text("권한 다시 요청") }
        } else if (isActive && !cameraGranted && !permissionUiState.permanentlyDenied && !hasRequestedPermission) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    hasRequestedPermission = true
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
            ) { Text("권한 허용") }
        }

        if (isActive && cameraGranted) {
            Button(
                modifier = Modifier.align(Alignment.CenterHorizontally).size(76.dp)
                    .border(3.dp, Color.White, CircleShape).padding(6.dp)
                    .semantics { contentDescription = "사진 촬영" },
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF0052FF)),
                enabled = imageCapture != null && !capturePending,
                onClick = {
                    val capture = imageCapture ?: return@Button
                    if (capturePending) return@Button
                    capturePending = true
                    val requestGeneration = captureGeneration.begin()
                    val outputFile = try {
                        File.createTempFile("howlens-camera-", ".jpg", context.cacheDir)
                    } catch (failure: Exception) {
                        capturePending = false
                        currentOnError("사진을 준비할 수 없어요.")
                        return@Button
                    }
                    val output = ImageCapture.OutputFileOptions.Builder(outputFile).build()
                    capture.takePicture(output, mainExecutor, object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                            if (!captureGeneration.isCurrent(requestGeneration) || !isActive || imageCapture !== capture) {
                                outputFile.delete()
                                return
                            }
                            capturePending = false
                            val jpegBytes = outputFile.length()
                            if (jpegBytes <= 0L) {
                                outputFile.delete()
                                currentOnError("사진을 촬영하지 못했어요.")
                                return
                            }
                            if (!isJpegSizeWithinPhotoLimit(jpegBytes)) {
                                outputFile.delete()
                                currentOnError("사진 용량이 10 MiB를 넘어요. 다시 촬영해 주세요.")
                                return
                            }
                            currentOnPhotoCaptured(Uri.fromFile(outputFile))
                        }

                        override fun onError(exception: ImageCaptureException) {
                            outputFile.delete()
                            if (!captureGeneration.isCurrent(requestGeneration) || !isActive || imageCapture !== capture) {
                                return
                            }
                            capturePending = false
                            currentOnError("사진을 촬영하지 못했어요.")
                        }
                    })
                },
            ) { if (capturePending) Text("촬영 중", color = Color(0xFF0052FF)) }
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            onClick = { currentOnChooseFromGallery() },
        ) { Text("사진 선택") }
    }
}

private tailrec fun findActivity(context: Context): Activity? = when (context) {
    is Activity -> context
    is ContextWrapper -> findActivity(context.baseContext)
    else -> null
}

private fun readCameraPermissionUiState(
    context: Context,
    requestWasMade: Boolean,
): CameraPermissionUiState {
    val granted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA,
    ) == PackageManager.PERMISSION_GRANTED
    val shouldShowRationale = findActivity(context)?.let {
        androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
            it,
            Manifest.permission.CAMERA,
        )
    } ?: false
    return resolveCameraPermissionUiState(granted, requestWasMade, shouldShowRationale)
}

private const val JPEG_QUALITY = 85
