package uniovi.eii.shareit.model.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.model.Album.Visibility.PUBLIC
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.Participant
import uniovi.eii.shareit.model.Participant.Role
import uniovi.eii.shareit.model.User
import uniovi.eii.shareit.model.UserAlbum
import uniovi.eii.shareit.model.realTimeListener.AlbumDataListener
import uniovi.eii.shareit.model.realTimeListener.AlbumParticipantsListener
import uniovi.eii.shareit.model.realTimeListener.AlbumUserRoleListener
import uniovi.eii.shareit.viewModel.AlbumInformationViewModel.GeneralValidationResult
import uniovi.eii.shareit.viewModel.AlbumInformationViewModel.ParticipantValidationResult
import java.util.Date

object FirestoreAlbumService {

    private const val TAG = "FirestoreAlbumService"
    const val PAGE_SIZE: Long = 12

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
                coverImage = FirebaseStorageService.getStorageReferenceStringForAlbum(albumId)
            }
            with(docRef.set(album).await()) {
                val owner = currentUser.toParticipant()
                owner.role = Role.OWNER
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
        docRef.delete().addOnSuccessListener {
            Log.d(TAG, "deleteAlbum $albumId: Success")
            eliminateUserAlbumFromParticipant(albumId, currentUserId)
        }.addOnFailureListener {
            Log.w(TAG, "deleteAlbum $albumId: Failure")
        }
    }

    fun deleteSharedAlbumData(albumId: String, currentUserId: String) {
        val db = Firebase.firestore
        val docRef = db.collection("albums").document(albumId)
        deleteFullCollection(docRef, "chat")
        deleteFullCollection(docRef, "participants", currentUserId)
        docRef.collection("images").get()
            .addOnSuccessListener {
                for (doc in it) {
                    val image = doc.toObject(Image::class.java)
                    if(image.authorId != currentUserId) {
                        doc.reference.delete()
                            .addOnFailureListener { e -> Log.w(TAG, "Error deleting image ${doc.id}", e) }
                    }
                }
            }
    }

    private fun deleteFullCollection(
        docRef: DocumentReference,
        collectionPath: String,
        except: String? = null
    ) {
        Log.d(TAG, "deleteFullCollection: $collectionPath")
        docRef.collection(collectionPath).get()
            .addOnSuccessListener {
                for (doc in it) {
                    if (except.equals(doc.id)) continue
                    doc.reference.delete()
                        .addOnFailureListener { e -> Log.w(TAG, "Error deleting document ${doc.id}", e) }
                }
            }
    }

    /**
     * Adición del usuario con email [participantEmail] y con rol de miembro a los participantes
     * del [album] dado en firestore.
     */
    suspend fun addNewMemberToAlbum(
        album: Album,
        participantEmail: String
    ): ParticipantValidationResult {
        val participantResult = searchUserByEmail(participantEmail)
        if (participantResult.value == null) {
            return ParticipantValidationResult(firestoreError = participantResult.firestoreError)
        }
        val participant = participantResult.value!!.toParticipant()
        participant.role = Role.MEMBER
        var error = addParticipantToAlbum(album.albumId, participant)
        if (error != null) return ParticipantValidationResult(firestoreError = error)
        error = createUserAlbumDenormalizedData(album.toUserAlbum(), participant.participantId)
        if (error != null) return ParticipantValidationResult(firestoreError = error)
        return ParticipantValidationResult(true)
    }

    /**
     * Adición del usuario [participant] a los participantes del [album] dado en firestore.
     * Se devuelve un booleano indicando si la operación fue exitosa o no. Usado cuando el
     * usuario se añade a sí mismo al álbum (albumes públicos).
     */
    suspend fun addNewGuestToAlbum(
        album: Album,
        participant: Participant
    ): Boolean {
        var error = addParticipantToAlbum(album.albumId, participant)
        if (error != null) return false
        error = createUserAlbumDenormalizedData(album.toUserAlbum(), participant.participantId)
        return error == null
    }

    /**
     * Adición del participante [participant] bajo la subcolección de participantes del álbum
     * con el [albumId] dado en firestore.
     */
    private suspend fun addParticipantToAlbum(albumId: String, participant: Participant): String? {
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
            Log.e(TAG, "addParticipantToAlbum:failure", e)
            e.message
        }
    }

    /**
     * Busca y devuelve en un [SearchUserResult] el resultado de buscar un usuario por su [userEmail].
     * En caso de no encontrarlo devuelve el error encontrado también dentro del objeto mencionado.
     */
    private suspend fun searchUserByEmail(userEmail: String): SearchUserResult {
        val db = Firebase.firestore
        Log.d(TAG, "searchingUserByEmail: $userEmail")
        return try {
            with(
                db.collection("users").whereEqualTo("email", userEmail).get().await()
            ) {
                if (this.isEmpty) {
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
    private suspend fun createUserAlbumDenormalizedData(
        userAlbum: UserAlbum,
        userId: String
    ): String? {
        val db = Firebase.firestore
        return try {
            db.collection("users")
                .document(userId)
                .collection("userAlbums")
                .document(userAlbum.albumId)
                .set(userAlbum).await()
            Log.d(TAG, "createUserAlbumDenormalizedData:success")
            null
        } catch (e: Exception) {
            Log.e(TAG, "createUserAlbumDenormalizedData:failure", e)
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
                .collection("userAlbums")
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
    fun updateParticipantRoleInAlbum(
        albumId: String,
        participantId: String,
        newRole: HashMap<String, Any?>
    ) {
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
            .addSnapshotListener(AlbumParticipantsListener(getDownloadImageUrlForParticipants(updateEvent)))
    }

    private fun getDownloadImageUrlForParticipants(
        updateEvent: (newAlbumParticipants: List<Participant>) -> Unit,
    ): (List<Participant>) -> Unit = { participants ->
        CoroutineScope(Dispatchers.IO).launch {
            val updatedParticipants = coroutineScope {
                participants.map { participant ->
                    async {
                        val downloadUrl = try {
                            FirebaseStorageService
                                .getStorageReference(participant.imagePath)
                                ?.downloadUrl
                                ?.await()
                                ?.toString()
                        } catch (e: Exception) {
                            Log.e(TAG, "getDownloadImageUrlForParticipant: failure - ${participant.participantId}")
                            ""
                        }
                        participant.copy(imagePath = downloadUrl ?: "")
                    }
                }.awaitAll()
            }

            withContext(Dispatchers.Default) {
                updateEvent(updatedParticipants)
            }
        }
    }


    /**
     * Enlazamiento de un objeto de escucha en tiempo real para el rol del participante con
     * id [currentUserId] en el album [albumId] con el viewmodel correspondiente según lo
     * especificado mediante la función [updateEvent].
     * Se hace uso de la clase [AlbumUserRoleListener].
     */
    fun getCurrentUserRoleInAlbumRegistration(
        albumId: String, currentUserId: String, updateEvent: (newRole: Role) -> Unit
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
        try {
            val albumCoverUpdate = checkCoverUpdate(albumId, newAlbumData)
            if (!albumCoverUpdate.isDataValid) {
                return GeneralValidationResult(firestoreError = albumCoverUpdate.firestoreError)
            }
            Log.d(TAG, "updatingCurrentAlbumData:\n Album: $albumId \n Data: $newAlbumData")
            db.collection("albums").document(albumId).update(newAlbumData).await()
            Log.d(TAG, "updateCurrentAlbumData:success")
            return GeneralValidationResult(true)
        } catch (e: Exception) {
            Log.e(TAG, "updateCurrentAlbumData:failure", e)
            return GeneralValidationResult(firestoreError = e.message)
        }
    }

    private suspend fun checkCoverUpdate(
        albumId: String, newAlbumData: HashMap<String, Any?>
    ): GeneralValidationResult {
        if (newAlbumData.getOrDefault("useLastImageAsCover", false) == true) {
            // Obtener URL de la última imagen
            val lastImageUrl = FirestoreImageService.getLastAlbumImage(albumId)
            if (lastImageUrl.isNotEmpty()) {
                // Descargar la imagen a un archivo temporal
                val imageFile = FirebaseStorageService.downloadImageToTempFile(lastImageUrl)
                    ?: return GeneralValidationResult(firestoreError = "Error downloading last image cover")
                // Subir el archivo temporal como portada
                FirebaseStorageService.uploadAlbumCover(albumId, Uri.fromFile(imageFile))
                    ?: return GeneralValidationResult(firestoreError = "Error uploading cover image")
                newAlbumData["lastUpdate"] = Date()
                // Eliminar archivo temporal
                imageFile.delete()
            }
        } else if (newAlbumData["coverImage"] != null) {
            val imageUri = newAlbumData["coverImage"] as Uri
            FirebaseStorageService.uploadAlbumCover(albumId, imageUri)
                ?: return GeneralValidationResult(firestoreError = "Error uploading cover image")
            newAlbumData.remove("coverImage")
            newAlbumData["lastUpdate"] = Date()
        }
        return GeneralValidationResult(true, dataToUpdate = newAlbumData)
    }

    /**
     * Obtención de los álbumes públicos de la colección de álbumes en firestore.
     * Se devuelve una lista de [UserAlbum] y el último álbum recibido para la paginación por
     * medio de los callbacks [onNewAlbumBatch], [onExistMoreAlbums] y [onLastAlbumReceived].
     */
    suspend fun getPublicAlbums(
        lastAlbumLoaded: DocumentSnapshot?,
        onNewAlbumBatch: (List<UserAlbum>) -> Unit,
        onExistMoreAlbums: (Boolean) -> Unit,
        onLastAlbumReceived: (DocumentSnapshot) -> Unit,
    ) {
        val db = Firebase.firestore
        try {
            var query = db.collection("albums")
                .whereEqualTo("visibility", PUBLIC)
                .orderBy("lastUpdate", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE)
            if (lastAlbumLoaded != null) {
                query = query.startAfter(lastAlbumLoaded)
            }

            val albumsDocs = query.get().await().documents
            val lastAlbum = albumsDocs.lastOrNull()

            if (lastAlbum == null) {
                Log.d(TAG, "getPublicAlbums: no more albums")
                onExistMoreAlbums(false)
            } else {
                if (albumsDocs.size < PAGE_SIZE) {
                    Log.d(TAG, "getPublicAlbumsSearch: no more albums")
                    onExistMoreAlbums(false)
                } else {
                    Log.d(TAG, "getPublicAlbums: more albums available")
                }
                onNewAlbumBatch(
                    albumsDocs
                    .mapNotNull { it.toObject(Album::class.java) }
                    .map { it.toUserAlbum() }
                )
                onLastAlbumReceived(lastAlbum)
            }
            Log.d(TAG, "getPublicAlbums:success")
        } catch (e: Exception) {
            Log.e(TAG, "getPublicAlbums:failure", e)
        }
    }

    /**
     * Obtención de los álbumes públicos de la colección de álbumes en firestore que cumplen con el
     * filtro de búsqueda [queryFilter]. Se devuelve una lista de [UserAlbum] y el último álbum
     * recibido para la paginación por medio de los callbacks [onNewAlbumBatch], [onExistMoreAlbums]
     * y [onLastAlbumReceived].
     */
    suspend fun getPublicAlbumsSearch(
        queryFilter: String,
        lastAlbumLoaded: DocumentSnapshot?,
        onNewAlbumBatch: (List<UserAlbum>) -> Unit,
        onExistMoreAlbums: (Boolean) -> Unit,
        onLastAlbumReceived: (DocumentSnapshot) -> Unit,
    ) {
        // TODO: Elegir opción de búsqueda
        //  - Busqueda actual, por correspondencia en los primeros carácteres del nombre.
        //          Ej: "El sol brillante" -> "El", "El sol", "El sol br"
        //  - Búsqueda por palabras completas, sin importar el orden. Crecimiento exponencial (máximo 5 palabras)
        //          Ej: "El sol brillante" -> "sol", "brillante", "El brillante sol"
        val db = Firebase.firestore
        try {
            var query = db.collection("albums")
                .whereEqualTo("visibility", PUBLIC)
                .where(Filter.or(
                    Filter.and(
                        Filter.greaterThanOrEqualTo("name", queryFilter),
                        Filter.lessThanOrEqualTo("name", queryFilter + "\uf8ff")
                    ),
                    Filter.and(
                        Filter.greaterThanOrEqualTo("creatorName", queryFilter),
                        Filter.lessThanOrEqualTo("creatorName", queryFilter + "\uf8ff")
                    )
                ))
                .limit(PAGE_SIZE)
            if (lastAlbumLoaded != null) {
                query = query.startAfter(lastAlbumLoaded)
            }

            val albumsDocs = query.get().await().documents
            val lastAlbum = albumsDocs.lastOrNull()

            if (lastAlbum == null) {
                Log.d(TAG, "getPublicAlbumsSearch: no more albums")
                onExistMoreAlbums(false)
                onNewAlbumBatch(emptyList())
            } else {
                if (albumsDocs.size < PAGE_SIZE) {
                    Log.d(TAG, "getPublicAlbumsSearch: no more albums")
                    onExistMoreAlbums(false)
                } else {
                    Log.d(TAG, "getPublicAlbumsSearch: more albums available")
                }
                onNewAlbumBatch(
                    albumsDocs
                        .mapNotNull { it.toObject(Album::class.java) }
                        .map { it.toUserAlbum() }
                )
                onLastAlbumReceived(lastAlbum)
            }
            Log.d(TAG, "getPublicAlbumsSearch:success")
        } catch (e: Exception) {
            Log.e(TAG, "getPublicAlbumsSearch:failure", e)
        }
    }
}