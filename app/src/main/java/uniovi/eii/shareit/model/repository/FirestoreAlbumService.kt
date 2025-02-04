package uniovi.eii.shareit.model.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.model.Participant
import uniovi.eii.shareit.model.User
import uniovi.eii.shareit.model.UserAlbum
import uniovi.eii.shareit.model.realTimeListener.AlbumDataListener
import uniovi.eii.shareit.model.realTimeListener.AlbumParticipantsListener
import uniovi.eii.shareit.model.realTimeListener.AlbumUserRoleListener
import uniovi.eii.shareit.viewModel.AlbumInformationViewModel.ParticipantValidationResult
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
                val owner = currentUser.toParticipant()
                owner.role = Participant.OWNER
                var error = addParticipantToAlbum(album.albumId, owner)
                if (error != null) throw Exception(error)
                val userAlbum = album.toUserAlbum()
                error = createUserAlbumDenormalizedData(userAlbum, userAlbum.creatorId)
                if (error != null) throw Exception(error)
                Log.d(TAG, "createAlbum:success")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "createAlbum:failure", e)
            false
        }
    }

    fun deleteAlbum(albumId: String, currentUserId: String) {
        val db = Firebase.firestore
        val docRef = db.collection("albums").document(albumId)
        deleteFullCollection(docRef, "participants")
//        deleteFullCollection(docRef, "images")
        deleteFullCollection(docRef, "chat")
        docRef.delete().addOnSuccessListener {
            Log.d(TAG, "deleteAlbum $albumId: Success")
            eliminateUserAlbumFromParticipant(albumId, currentUserId)
        }.addOnFailureListener {
            Log.w(TAG, "deleteAlbum $albumId: Failure")
        }
    }

    private fun deleteFullCollection(docRef: DocumentReference, collectionPath: String) {
        Log.d(TAG, "deleteFullCollection: $collectionPath")
        docRef.collection(collectionPath).get()
            .addOnSuccessListener {
                for (doc in it) {
                    doc.reference.delete()
                        .addOnFailureListener { e -> Log.w(TAG, "Error deleting document ${doc.id}", e) }
                }
            }
    }

    /**
     * Adición del usuario con email [participantEmail] y con rol de miembro a los participantes
     * del [album] dado en firestore.
     */
    suspend fun addNewMemberToAlbum(album: Album, participantEmail: String) : ParticipantValidationResult {
        val participantResult = searchUserByEmail(participantEmail)
        if (participantResult.value == null) {
            return ParticipantValidationResult(firestoreError = participantResult.firestoreError)
        }
        val participant = participantResult.value!!.toParticipant()
        participant.role = Participant.MEMBER
        var error = addParticipantToAlbum(album.albumId, participant)
        if (error != null) return ParticipantValidationResult(firestoreError = error)
        error = createUserAlbumDenormalizedData(album.toUserAlbum(), participant.participantId)
        if (error != null) return ParticipantValidationResult(firestoreError = error)
        return ParticipantValidationResult(true)
    }

    /**
     * Adición del participante [participant] bajo la subcolección de participantes del álbum
     * con el [albumId] dado en firestore.
     */
    private suspend fun addParticipantToAlbum(albumId: String, participant: Participant) : String? {
        val db = Firebase.firestore
        return try {
            db.collection("albums")
                .document(albumId)
                .collection("participants")
                .document(participant.participantId)
                .set(participant).await()
            Log.d(TAG, "addParticipantToAlbum:success")
            null
        } catch (e: Exception) {
            Log.e(TAG, "addParticipantToAlbum:failure")
            e.message
        }
    }

    /**
     * Busca y devuelve en un [SearchUserResult] el resultado de buscar un usuario por su [userEmail].
     * En caso de no encontrarlo devuelve el error encontrado también dentro del objeto mencionado.
     */
    private suspend fun searchUserByEmail(userEmail : String) : SearchUserResult {
        val db = Firebase.firestore
        Log.d(TAG, "searchingUserByEmail: $userEmail")
        return try {
            with(
                db.collection("users").whereEqualTo("email", userEmail).get().await()
            ) {
                if(this.isEmpty) {
                    Log.d(TAG, "searchUserByEmail: notFound")
                    SearchUserResult(firestoreError = "User not found")
                } else {
                    Log.d(TAG, "searchUserByEmail: success")
                    SearchUserResult(this.documents.first().toObject(User::class.java))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "searchUserByEmail:failure", e)
            SearchUserResult(firestoreError = e.message)
        }
    }

    data class SearchUserResult(
        var value: User? = null,
        var firestoreError: String? = null
    )

    /**
     * Adición del álbum ([userAlbum]) bajo la colección de álbumes del usuario en firestore.
     */
    private suspend fun createUserAlbumDenormalizedData(userAlbum: UserAlbum, userId: String) : String? {
        val db = Firebase.firestore
        return try {
            db.collection("users")
                .document(userId)
                .collection("albums")
                .document(userAlbum.albumId)
                .set(userAlbum).await()
            Log.d(TAG, "createUserAlbumDenormalizedData:success")
            null
        } catch (e: Exception) {
            Log.e(TAG, "createUserAlbumDenormalizedData:failure")
            e.message
        }
    }

    /**
     * Eliminación del participante con el [participantId] pasado como parámetro de la subcolección de
     * participantes del álbum con el [albumId] dado en firestore.
     */
    fun eliminateParticipantFromAlbum(albumId: String, participantId: String) {
        val db = Firebase.firestore
        try {
            db.collection("albums")
                .document(albumId)
                .collection("participants")
                .document(participantId)
                .delete()
                .addOnSuccessListener { Log.d(TAG, "eliminateParticipantFromAlbum:success") }
                .addOnFailureListener { e -> Log.w(TAG, "eliminateParticipantFromAlbum:failure", e) }
        } catch (e: Exception) {
            Log.e(TAG, "eliminateParticipantFromAlbum:failure", e)
            e.message
        }
    }

    /**
     * Eliminación del [UserAlbum] con el id [albumId] de la subcolección de álbumes del
     * participante con el [participantId] pasado como parámetro en firestore.
     */
    fun eliminateUserAlbumFromParticipant(albumId: String, participantId: String) {
        val db = Firebase.firestore
        try {
            db.collection("users")
                .document(participantId)
                .collection("albums")
                .document(albumId)
                .delete()
                .addOnSuccessListener { Log.d(TAG, "eliminateUserAlbumFromParticipant:success") }
                .addOnFailureListener { e -> Log.w(TAG, "eliminateUserAlbumFromParticipant:failure", e) }
        } catch (e: Exception) {
            Log.e(TAG, "eliminateUserAlbumFromParticipant:failure", e)
            e.message
        }
    }

    /**
     * Actualización del rol del participante con id [participantId] pasado como parámetro de la subcolección de
     * participantes del álbum con el [albumId] dado al nuevo valor especificado en [newRole], en firestore.
     */
    fun updateParticipantRoleInAlbum(albumId: String, participantId: String, newRole: HashMap<String, Any?>) {
        val db = Firebase.firestore
        try {
            db.collection("albums")
                .document(albumId)
                .collection("participants")
                .document(participantId)
                .update(newRole)
                .addOnSuccessListener { Log.d(TAG, "updateParticipantRoleInAlbum:success") }
                .addOnFailureListener { e -> Log.w(TAG, "updateParticipantRoleInAlbum:failure", e) }
        } catch (e: Exception) {
            Log.e(TAG, "updateParticipantRoleInAlbum:failure", e)
            e.message
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
     * Enlazamiento de un objeto de escucha en tiempo real para los participantes del
     * album [albumId] con el viewmodel correspondiente según lo especificado mediante
     * la función [updateEvent].
     * Se hace uso de la clase [AlbumParticipantsListener].
     */
    fun getAlbumParticipantsRegistration(
        albumId: String, updateEvent: (newAlbumParticipants: List<Participant>) -> Unit
    ): ListenerRegistration {
        val db = Firebase.firestore
        return db.collection("albums")
            .document(albumId)
            .collection("participants")
            .addSnapshotListener(MetadataChanges.INCLUDE, AlbumParticipantsListener(updateEvent))
    }

    /**
     * Enlazamiento de un objeto de escucha en tiempo real para el rol del participante con
     * id [currentUserId] en el album [albumId] con el viewmodel correspondiente según lo
     * especificado mediante la función [updateEvent].
     * Se hace uso de la clase [AlbumUserRoleListener].
     */
    fun getCurrentUserRoleInAlbumRegistration(
        albumId: String, currentUserId: String, updateEvent: (newRole: String) -> Unit
    ): ListenerRegistration {
        val db = Firebase.firestore
        return db.collection("albums")
            .document(albumId)
            .collection("participants")
            .document(currentUserId)
            .addSnapshotListener(MetadataChanges.INCLUDE, AlbumUserRoleListener(updateEvent))
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