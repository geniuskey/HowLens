package kr.howlens.app.camera

internal fun isResolutionWithinPhotoLimit(width: Int, height: Int): Boolean =
    width > 0 && height > 0 && width.toLong() * height.toLong() <= MAX_IMAGE_PIXELS

internal fun isJpegSizeWithinPhotoLimit(bytes: Long): Boolean =
    bytes in 1L..MAX_IMAGE_BYTES

internal data class CameraPermissionUiState(
    val granted: Boolean,
    val denied: Boolean,
    val permanentlyDenied: Boolean,
)

internal fun resolveCameraPermissionUiState(
    granted: Boolean,
    requestWasMade: Boolean,
    shouldShowRationale: Boolean,
): CameraPermissionUiState = CameraPermissionUiState(
    granted = granted,
    denied = !granted && requestWasMade,
    permanentlyDenied = !granted && requestWasMade && !shouldShowRationale,
)

internal class CaptureGeneration {
    private var generation = 0

    fun begin(): Int = ++generation

    fun invalidate() {
        generation++
    }

    fun isCurrent(requestGeneration: Int): Boolean = requestGeneration == generation
}

private const val MAX_IMAGE_PIXELS = 20_000_000L
private const val MAX_IMAGE_BYTES = 10L * 1024L * 1024L
