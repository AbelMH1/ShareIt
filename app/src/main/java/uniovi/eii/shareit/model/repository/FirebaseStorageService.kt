package uniovi.eii.shareit.model.repository
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import uniovi.eii.shareit.model.Image
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object FirebaseStorageService {

    private const val TAG = "FirebaseStorageService"
    private var storage = FirebaseStorage.getInstance().reference

    fun getStorageReference(fullUrl: String): StorageReference? {
        return try {
            FirebaseStorage.getInstance().getReferenceFromUrl(fullUrl)
        } catch (e: Exception) {
            Log.e(TAG, "getStorageReference: Error al obtener la referencia de almacenamiento")
            null
        }
    }

    fun getStorageReferenceStringForUser(userId: String): String {
        return storage.child("users/$userId").toString()
    }

    fun getStorageReferenceStringForAlbum(albumId: String): String {
        return storage.child("albums/$albumId/cover").toString()
    }

    suspend fun uploadAlbumImage(albumId: String, imageId: String, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            val imageRef = storage.child("albums/$albumId/images/${imageId}")
            try {
                imageRef.putFile(imageUri).await()
                getImageDownloadUriString(imageRef)
            } catch (e: Exception) {
                Log.e(TAG, "uploadAlbumImage: Error al subir la imagen", e)
                null
            }
        }
    }

    suspend fun uploadAlbumCover(albumId: String, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            val imageRef = storage.child("albums/$albumId/cover")
            try {
                imageRef.putFile(imageUri).await()
                imageRef.toString()
            } catch (e: Exception) {
                Log.e(TAG, "uploadAlbumCover: Error al subir la imagen", e)
                null
            }
        }
    }

    suspend fun uploadUserImage(userId: String, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            val imageRef = storage.child("users/$userId")
            try {
                imageRef.putFile(imageUri).await()
                imageRef.toString()
            } catch (e: Exception) {
                Log.e(TAG, "uploadUserImage: Error al subir la imagen", e)
                null
            }
        }
    }

    private suspend fun getImageDownloadUriString(imageRef: StorageReference): String? {
        return try {
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "getImageDownloadUriString: Error al obtener la URL de descarga", e)
            null
        }
    }

    // Función auxiliar para descargar una imagen desde una URL a un archivo temporal
    suspend fun downloadImageToTempFile(imageUrl: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val tempFile = File.createTempFile("cover_", ".jpg")
                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(tempFile)

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                Log.d(TAG, "Image downloaded to temp file: ${tempFile.absolutePath}")
                tempFile
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading image", e)
                null
            }
        }
    }

    suspend fun deleteAlbumImage(image: Image): Boolean {
        return withContext(Dispatchers.IO) {
            val imageRef = storage.child("albums/${image.albumId}/images/${image.imageId}")
            try {
                imageRef.delete().await()
                Log.d(TAG, "Image deleted successfully")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting image", e)
                false
            }
        }
    }

}