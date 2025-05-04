package uniovi.eii.shareit.model.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.realTimeListener.AlbumImagesListener
import uniovi.eii.shareit.model.realTimeListener.ImageLikedListener

object FirestoreImageService {
    private const val TAG = "FirestoreImageService"
    private const val MAX_WHERE_IN_LIST_SIZE = 30

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
     * Enlazamiento de un objeto de escucha en tiempo real para las imágenes de los albumes del
     * usuario especificados en [userAlbums] con el viewmodel correspondiente según lo especificado
     * mediante la función [updateVMEvent]. Se hace uso de la clase [AlbumImagesListener].
     */
    fun getUserImagesRegistration(
        userAlbums: List<String>,
        updateVMEvent: (newData: List<Image>, isUpdateFromServer: Boolean) -> Unit
    ): ListenerRegistration {
        val db = Firebase.firestore
        val albumsIds =
            if (userAlbums.size > MAX_WHERE_IN_LIST_SIZE)
                userAlbums.subList(0, 30)
            else userAlbums
        return db.collectionGroup("images")
            .whereIn("albumId", albumsIds)
            .addSnapshotListener(MetadataChanges.INCLUDE, AlbumImagesListener(updateVMEvent))
    }

    /**
     * Obtiene el número de likes de cada imagen en la lista [images] pasada como parámetro.
     */
    suspend fun getLikesCountForImages(images: List<Image>): List<Image> = coroutineScope {
        try {
            val updatedImages = images.map { image ->
                async {
                    image.copy(likes = getLikesCountForImage(image))
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
     * Obtiene el número de likes de la imagen [image] pasada como parámetro.
     */
    suspend fun getLikesCountForImage(image: Image): Int {
        val db = Firebase.firestore
        return try {
            val likesCount = db.collection("albums")
                .document(image.albumId)
                .collection("images")
                .document(image.imageId)
                .collection("likes")
                .count()
                .get(AggregateSource.SERVER)
                .await()
                .count.toInt()

            Log.d(TAG, "getLikesCountForImage: success")
            likesCount
        } catch (e: Exception) {
            Log.e(TAG, "getLikesCountForImage: failure", e)
            0
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

    fun getImageLikeRegistration(
        image: Image,
        userId: String,
        updateEvent: (newData: Boolean) -> Unit
    ): ListenerRegistration {
        val db = Firebase.firestore
        return db.collection("albums")
                .document(image.albumId)
                .collection("images")
                .document(image.imageId)
                .collection("likes")
                .document(userId)
                .addSnapshotListener(ImageLikedListener(updateEvent))
    }

    suspend fun createImageLike(image: Image, userId: String): Boolean {
        val db = Firebase.firestore
        return try {
            db.collection("albums")
                .document(image.albumId)
                .collection("images")
                .document(image.imageId)
                .collection("likes")
                .document(userId)
                .set({})
                .await()
            Log.d(TAG, "likeImage:success")
            true
        } catch (e: Exception) {
            Log.e(TAG, "likeImage:failure", e)
            false
        }
    }

    suspend fun deleteImageLike(image: Image, userId: String): Boolean {
        val db = Firebase.firestore
        return try {
            db.collection("albums")
                .document(image.albumId)
                .collection("images")
                .document(image.imageId)
                .collection("likes")
                .document(userId)
                .delete()
                .await()
            Log.d(TAG, "unlikeImage:success")
            true
        } catch (e: Exception) {
            Log.e(TAG, "unlikeImage:failure", e)
            false
        }
    }

    suspend fun getLastAlbumImage(albumId: String): String {
        val db = Firebase.firestore
        return try {
            val snapshot = db.collection("albums")
                .document(albumId)
                .collection("images")
                .orderBy("creationDate", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                ""
            } else {
                Log.d(TAG, "getLastAlbumImage:success")
                snapshot.documents.first().getString("imagePath") ?: ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "getLastAlbumImage:failure", e)
            ""
        }
    }

    suspend fun deleteImage(image: Image): Boolean {
        val db = Firebase.firestore
        return try {
            db.collection("albums")
                .document(image.albumId)
                .collection("images")
                .document(image.imageId)
                .delete()
                .await()
            FirebaseStorageService.deleteAlbumImage(image)
            Log.d(TAG, "deleteImage:success")
            true
        } catch (e: Exception) {
            Log.e(TAG, "deleteImage:failure", e)
            false
        }
    }
}