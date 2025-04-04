package uniovi.eii.shareit.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

fun Context.createTempImageFile(fileName: String = "temp_image", directory: File = filesDir): File {
    return File.createTempFile(fileName, ".jpg", directory)
}

fun Context.getSecureUriForFile(file: File): Uri {
    return FileProvider.getUriForFile(this, "${packageName}.FileProvider", file)
}

suspend fun Context.getBitmapFromUri(uri: Uri, maxImageSize: Int = 1280): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            Glide.with(this@getBitmapFromUri)
                .asBitmap()
                .load(uri)
                // Se establecen dimensiones máximas. Glide mantiene la relación de aspecto.
                .submit(maxImageSize, maxImageSize)
                .get()
        } catch (e: Exception) {
            null
        }
    }
}

private fun getCompressFormat(mimeType: String?): Bitmap.CompressFormat {
    return when (mimeType) {
        "image/png" -> Bitmap.CompressFormat.PNG
        "image/jpeg" -> Bitmap.CompressFormat.JPEG
        "image/webp" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSLESS
        } else Bitmap.CompressFormat.WEBP

        else -> Bitmap.CompressFormat.JPEG
    }
}

/**
 * Procesa la imagen referenciada por [imageUri]:
 * - Si su tamaño es ≤ 2 MB se copia directamente al archivo [outputFile].
 * - Si es mayor, se redimensiona y comprime.
 * - Si tras la redimensión la imagen sigue siendo mayor a 2 MB, se retorna false para notificar al usuario.
 */
suspend fun Context.compressImage(
    imageUri: Uri,
    outputFile: File,
    compressionThresholdMB: Long = 1 // MB
): Boolean {
    val compressionThreshold = compressionThresholdMB * 1024 * 1024
    return withContext(Dispatchers.IO) {
        // Obtiene el contenido original de la imagen
        val mimeType = contentResolver.getType(imageUri)
        val inputBytes = contentResolver.openInputStream(imageUri)?.use { inputStream ->
            inputStream.readBytes()
        } ?: return@withContext false

        // Si la imagen original ya es ≤ compressionThresholdMB MB, se copia directamente
        if (inputBytes.size <= compressionThreshold) {
            Log.d("processImage", "Image under ${compressionThresholdMB}MB threshold. Size: ${inputBytes.size}")
            outputFile.writeBytes(inputBytes)
            return@withContext true
        }
        Log.d("processImage", "Image over ${compressionThresholdMB}MB threshold. Size: ${inputBytes.size}\nCompressing...")

        ensureActive()

        // Si la imagen es mayor a compressionThresholdMB, se redimensiona y comprime
        withContext(Dispatchers.Default) inner@{
            val bitmap: Bitmap = this@compressImage.getBitmapFromUri(imageUri)
                ?: return@inner false
//            val bitmap = BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.size)
            ensureActive()

            val compressFormat = getCompressFormat(mimeType)

            var outputBytes: ByteArray
            var quality = 95
            do {
                ByteArrayOutputStream().use { outputStream ->
                    bitmap.compress(compressFormat, quality, outputStream)
                    outputBytes = outputStream.toByteArray()
                    Log.d("processImage", "Compressing with quality: $quality\nSize: ${outputBytes.size}")
                    quality -= 5
                }
            } while (isActive &&
                outputBytes.size > compressionThreshold &&
                quality > 5 &&
                compressFormat != Bitmap.CompressFormat.PNG // PNG is lossless, so we don't compress it
            )

            // Comprueba si el archivo comprimido cumple con el límite
            return@inner if (outputBytes.size <= compressionThreshold) {
                Log.d("processImage", "Image successfully compressed to ${outputBytes.size} bytes")
                outputFile.writeBytes(outputBytes)
                true
            } else {
                Log.d("processImage", "Image still over ${compressionThresholdMB}MB threshold. Size: ${outputBytes.size}")
                false
            }
        }
    }
}