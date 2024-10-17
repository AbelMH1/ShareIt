package uniovi.eii.shareit.model.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import uniovi.eii.shareit.model.Album

object FirestoreAlbumService {

    private const val TAG = "FirestoreAlbumService"

    suspend fun createAlbum(album: Album): Boolean {
        val db = Firebase.firestore
        return try {
            val docRef = db.collection("albums").document()
            album.albumId = docRef.id
            with(docRef.set(album).await()) {
                Log.d(TAG, "createAlbum:success")
                // TODO: Adición del álbum bajo la colección de álbumes del usuario
                // TODO: Adición del usuario bajo la colección de participantes del álbum
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "createAlbum:failure", e)
            false
        }
    }
}