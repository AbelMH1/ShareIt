package uniovi.eii.shareit.model.repository
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FirebaseStorageService {

    private const val TAG = "FirebaseStorageService"
    private var storage = FirebaseStorage.getInstance().reference

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

    private suspend fun getImageDownloadUriString(imageRef: StorageReference): String? {
        return try {
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "getImageDownloadUriString: Error al obtener la URL de descarga", e)
            null
        }
    }

}