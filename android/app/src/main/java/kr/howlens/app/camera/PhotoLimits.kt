package kr.howlens.app.camera

internal fun isResolutionWithinPhotoLimit(width: Int, height: Int): Boolean =
    width > 0 && height > 0 && width.toLong() * height.toLong() <= MAX_IMAGE_PIXELS

internal fun isJpegSizeWithinPhotoLimit(bytes: Long): Boolean =
    bytes in 1L..MAX_IMAGE_BYTES

private const val MAX_IMAGE_PIXELS = 20_000_000L
private const val MAX_IMAGE_BYTES = 10L * 1024L * 1024L
