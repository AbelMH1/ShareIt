package uniovi.eii.shareit.model.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.realTimeListener.AlbumImagesListener

object FirestoreImageService {
    private const val TAG = "FirestoreImageService"

    /**
     * Obtiene la lista de imágenes del album [albumId] pasado como parámetro.
     * Se hace uso de la clase [Image].
     */
    suspend fun getAlbumImages(albumId: String): List<Image> {
        val db = Firebase.firestore
        return try {
            val images = db.collection("albums")
                .document(albumId)
                .collection("images")
                .get()
                .await()
                .toObjects(Image::class.java)
            Log.d(TAG, "getAlbumImages:success")
            images
        } catch (e: Exception) {
            Log.e(TAG, "getAlbumImages:failure", e)
            emptyList()
        }
    }

    /**
     * Enlazamiento de un objeto de escucha en tiempo real para las imágenes del album [albumId]
     * con el viewmodel correspondiente según lo especificado mediante la función [updateVMEvent].
     * Se hace uso de la clase [AlbumImagesListener].
     */
    fun getAlbumImagesRegistration(
        albumId: String, updateVMEvent: (newData: List<Image>, isUpdateFromServer: Boolean) -> Unit
    ): ListenerRegistration {
        val db = Firebase.firestore
        return db.collection("albums")
            .document(albumId)
            .collection("images")
            .addSnapshotListener(MetadataChanges.INCLUDE, AlbumImagesListener(updateVMEvent))
    }

    /**
     * Obtiene el número de likes de cada imagen en la lista [images] pasada como parámetro.
     * Se hace uso de la clase [Image].
     */
    suspend fun getLikesCountForImages(images: List<Image>): List<Image> = coroutineScope {
        val db = Firebase.firestore
        try {
            val updatedImages = images.map { image ->
                async {
                    val likesCount = db.collection("albums")
                        .document(image.albumId)
                        .collection("images")
                        .document(image.imageId)
                        .collection("likes")
                        .count()
                        .get(AggregateSource.SERVER)
                        .await()
                        .count

                    image.copy(likes = likesCount)
                }
            }.awaitAll()

            Log.d(TAG, "getLikesCountForImages: success")
            updatedImages
        } catch (e: Exception) {
            Log.e(TAG, "getLikesCountForImages: failure", e)
            images
        }
    }



    /**
     * Añade la [image] pasada como parámetro en la colección images del album
     * correspondiente en firestore si se ha conseguido subir la [imageUri] pasada.
     */
    suspend fun uploadImage(image: Image, imageUri: Uri): Boolean {
        val db = Firebase.firestore
        return try {
            val docRef = db.collection("albums")
                .document(image.albumId)
                .collection("images")
                .document()
            val imagePath = FirebaseStorageService.uploadAlbumImage(
                image.albumId,
                docRef.id,
                imageUri
            )
            if (imagePath == null) {
                Log.e(TAG, "uploadImage:failure")
                return false
            }
            image.apply {
                this.imageId = docRef.id
                this.imagePath = imagePath
            }
            docRef.set(image).await()
            Log.d(TAG, "uploadImage:success")
            true
        } catch (e: Exception) {
            Log.e(TAG, "uploadImage:failure", e)
            false
        }
    }
}