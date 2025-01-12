package uniovi.eii.shareit.model.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.model.Participant
import uniovi.eii.shareit.model.User
import uniovi.eii.shareit.model.UserAlbum
import uniovi.eii.shareit.model.realTimeListener.AlbumDataListener
import uniovi.eii.shareit.viewModel.AlbumInformationViewModel.GeneralValidationResult
import java.util.Date

object FirestoreAlbumService {

    private const val TAG = "FirestoreAlbumService"

    /**
     * Asigna [currentUser] como creador del [album] dado y crea dicho [album] en la colección
     * albumes de firestore.
     */
    suspend fun createAlbum(album: Album, currentUser: User): Boolean {
        val db = Firebase.firestore
        return try {
            val docRef = db.collection("albums").document()
            album.apply {
                albumId = docRef.id
                creatorId = currentUser.userId
                creatorName = currentUser.name
                creationDate = Date()
                lastUpdate = Date()
            }
            with(docRef.set(album).await()) {
                addUserAsOwnerInAlbum(album.albumId, currentUser.toParticipant())
                createUserAlbumDenormalizedData(album.toUserAlbum())
                Log.d(TAG, "createAlbum:success")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "createAlbum:failure", e)
            false
        }
    }

    /**
     * Adición del usuario ([owner]) con rol de propietario bajo la colección de participantes del
     * álbum con el [albumId] dado en firestore.
     */
    private suspend fun addUserAsOwnerInAlbum(albumId: String, owner: Participant) {
        val db = Firebase.firestore
        owner.role = Participant.OWNER
        try {
            db.collection("albums")
                .document(albumId)
                .collection("participants")
                .document(owner.participantId)
                .set(owner).await()
            Log.d(TAG, "addUserAsOwnerInAlbum:success")
        } catch (e: Exception) {
            Log.e(TAG, "addUserAsOwnerInAlbum:failure")
            throw e
        }
    }

    /**
     * Adición del álbum ([userAlbum]) bajo la colección de álbumes del usuario en firestore
     */
    private suspend fun createUserAlbumDenormalizedData(userAlbum: UserAlbum) {
        val db = Firebase.firestore
        try {
            db.collection("users")
                .document(userAlbum.creatorId)
                .collection("albums")
                .document(userAlbum.albumId)
                .set(userAlbum).await()
            Log.d(TAG, "createUserAlbumDenormalizedData:success")
        } catch (e: Exception) {
            Log.e(TAG, "createUserAlbumDenormalizedData:failure")
            throw e
        }
    }

    /**
     * Enlazamiento de un objeto de escucha en tiempo real para el album [albumId] con el viewmodel
     * correspondiente según lo especificado mediante la función [updateVMEvent].
     * Se hace uso de la clase [AlbumDataListener].
     */
    fun getAlbumDataRegistration(
        albumId: String, updateVMEvent: (newData: Album) -> Unit
    ): ListenerRegistration {
        val db = Firebase.firestore
        return db.collection("albums")
            .document(albumId)
            .addSnapshotListener(AlbumDataListener(updateVMEvent))
    }

    /**
     * Actualización de los campos especificados en [newAlbumData] para el álbum [albumId] en firestore.
     */
    suspend fun updateCurrentAlbumData(
        albumId: String, newAlbumData: HashMap<String, Any?>
    ): GeneralValidationResult {
        val db = Firebase.firestore
        Log.d(TAG, "updatingCurrentAlbumData:\n Album: $albumId \n Data: $newAlbumData")
        return try {
            with(
                db.collection("albums").document(albumId).update(newAlbumData).await()
            ) {
                Log.d(TAG, "updateCurrentAlbumData:success")
                GeneralValidationResult(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateCurrentAlbumData:failure", e)
            GeneralValidationResult(firestoreError = e.message)
        }
    }
}