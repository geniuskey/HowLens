package kr.howlens.app.data

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PhotoLoader {
    suspend fun load(resolver: ContentResolver, uri: Uri): Photo = withContext(Dispatchers.IO) {
        val bytes = resolver.openInputStream(uri)?.use { stream ->
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val read = stream.read(buffer)
                if (read < 0) break
                require(out.size() + read <= InputRules.MAX_BYTES) { "사진은 10 MiB 이하여야 합니다." }
                out.write(buffer, 0, read)
            }
            out.toByteArray()
        } ?: error("사진을 읽을 수 없습니다.")
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val photo = Photo(bytes, bounds.outMimeType.orEmpty(), bounds.outWidth, bounds.outHeight)
        require(InputRules.validate("server", "photo", photo) == null) { "디코딩 가능한 JPEG/PNG, 10 MiB·20MP 이하 사진이 필요합니다." }
        // Bounds alone can accept truncated headers. Decode a sampled bitmap to validate content.
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size,
            BitmapFactory.Options().apply { inSampleSize = 8 }) ?: error("손상된 사진입니다.")
        decoded.recycle()
        photo
    }
}
