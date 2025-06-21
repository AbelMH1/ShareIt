package uniovi.eii.shareit.viewModel

import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniovi.eii.shareit.R
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.model.Album.Visibility
import uniovi.eii.shareit.model.Album.ImagePermission
import uniovi.eii.shareit.model.Album.ChatPermission
import uniovi.eii.shareit.model.Participant
import uniovi.eii.shareit.model.Participant.Role
import uniovi.eii.shareit.model.repository.FirestoreAlbumService
import uniovi.eii.shareit.model.repository.FirestoreUserService
import uniovi.eii.shareit.utils.toDate

class AlbumInformationViewModel : ViewModel() {

    companion object {
        private const val TAG = "AlbumInformationViewModel"
    }

    private var album: Album = Album()
    private var unsavedData: Album? = null

    private val _participants = MutableLiveData(emptyList<Participant>())
    val participants: LiveData<List<Participant>> = _participants
    private val _addParticipantAttempt = MutableLiveData(ParticipantValidationResult())
    val addParticipantAttempt: LiveData<ParticipantValidationResult> = _addParticipantAttempt

    private val _joinResultCorrect = MutableLiveData<Boolean>()
    val joinResultCorrect: LiveData<Boolean> = _joinResultCorrect

    private var albumParticipantsListenerRegistration: ListenerRegistration? = null

    fun updateAlbumData(newAlbumData: Album) {
        album = newAlbumData
    }

    private fun updateAlbumParticipants(newAlbumParticipants: List<Participant>) {
        _participants.postValue(newAlbumParticipants)
    }

    fun registerAlbumParticipantsListener(
        albumId: String
    ) {
        Log.d(TAG, "albumParticipantsListener: START")
        val updateParticipantsEvent: (newAlbumParticipants: List<Participant>) -> Unit = {
            updateAlbumParticipants(it)
        }
        albumParticipantsListenerRegistration = FirestoreAlbumService.getAlbumParticipantsRegistration(albumId, updateParticipantsEvent)
    }

    fun unregisterAlbumParticipantsListener() {
        Log.d(TAG, "albumParticipantsListener: STOP")
        albumParticipantsListenerRegistration?.remove()
    }

    fun isDisablingSharing(checkedButtonId: Int): Boolean {
        return checkedButtonId == R.id.togglePrivate && album.visibility != Visibility.PRIVATE
    }

    fun saveGeneralData(
        name: String,
        useLastImageAsCover: Boolean,
        imageUri: Uri?,
        startDate: String,
        endDate: String,
        toggleDateSelected: Int,
        tags: List<Int>
    ): GeneralValidationResult {
        val dataValidation = checkValidData(name, useLastImageAsCover, imageUri, startDate, endDate, toggleDateSelected, tags)
        if (dataValidation.isDataValid) {
            viewModelScope.launch(Dispatchers.IO) {
                FirestoreAlbumService.updateCurrentAlbumData(album.albumId, dataValidation.dataToUpdate)
            }
        }
        return dataValidation
    }

    fun saveSharedData(
        visibility: Visibility,
        membersImagesPermission: Int,
        membersChatPermission: Int,
        guestsImagesPermission: Int,
        guestsChatPermission: Int
    ): SharedValidationResult {
        val dataValidation = checkValidData(
            visibility,
            membersImagesPermission,
            membersChatPermission,
            guestsImagesPermission,
            guestsChatPermission
        )

        if (dataValidation.isDataValid) {
            viewModelScope.launch(Dispatchers.IO) {
                FirestoreAlbumService.updateCurrentAlbumData(album.albumId, dataValidation.dataToUpdate)
                if (dataValidation.dataToUpdate.getOrDefault("visibility", null) == Visibility.PRIVATE) {
                    val currentUserId = FirestoreUserService.getCurrentUserData()?.userId ?: ""
                    FirestoreAlbumService.deleteSharedAlbumData(album.albumId, currentUserId)
                }
            }
        }
        return dataValidation
    }

    fun addNewParticipant(
        participantEmail: String
    ) {
        if (checkValidData(participantEmail)) {
            viewModelScope.launch(Dispatchers.IO) {
                _addParticipantAttempt.postValue(
                    FirestoreAlbumService.addNewMemberToAlbum(album, participantEmail)
                )
            }
        }
    }

    fun eliminateParticipant(participant: Participant) {
        viewModelScope.launch(Dispatchers.IO) {
            FirestoreAlbumService.eliminateParticipantFromAlbum(album.albumId, participant.participantId)
        }
    }

    fun promoteParticipant(participant: Participant) {
        participant.role = Role.MEMBER
        updateParticipantRole(participant)
    }

    fun demoteParticipant(participant: Participant) {
        participant.role = Role.GUEST
        updateParticipantRole(participant)
    }

    private fun updateParticipantRole(participant: Participant) {
        val newRole = hashMapOf<String,Any?>(
            "role" to participant.role
        )
        viewModelScope.launch(Dispatchers.IO) {
            FirestoreAlbumService.updateParticipantRoleInAlbum(album.albumId, participant.participantId, newRole)
        }
    }

    fun dropAlbum() {
        val currentUserId = FirestoreUserService.getCurrentUserData()?.userId ?: ""
        viewModelScope.launch(Dispatchers.IO) {
            FirestoreAlbumService.eliminateUserAlbumFromParticipant(album.albumId, currentUserId)
            FirestoreAlbumService.eliminateParticipantFromAlbum(album.albumId, currentUserId)
        }
    }

    fun deleteAlbum() {
        val currentUserId = FirestoreUserService.getCurrentUserData()?.userId ?: ""
        viewModelScope.launch(Dispatchers.IO) {
            FirestoreAlbumService.deleteAlbum(album.albumId, currentUserId)
        }
    }

    fun joinAlbum() {
        val currentUserAsParticipant = FirestoreUserService.getCurrentUserData()?.toParticipant()
        if (currentUserAsParticipant == null) {
            _joinResultCorrect.value = false
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            Thread.sleep(2000)
            _joinResultCorrect.postValue(
                FirestoreAlbumService.addNewGuestToAlbum(album, currentUserAsParticipant)
            )
        }
    }

    private fun checkValidData(
        name: String, useLastImageAsCover: Boolean, imageUri: Uri?, startDate: String, endDate: String, toggleDateSelected: Int, tags: List<Int>
    ): GeneralValidationResult {
        val dataToUpdate: HashMap<String, Any?> = HashMap()
        if (name.isBlank()) {
            return GeneralValidationResult(nameError = R.string.err_empty_field)
        }
        if (name != album.name) {
            dataToUpdate["name"] = name
        }
        if (useLastImageAsCover != album.useLastImageAsCover) {
            dataToUpdate["useLastImageAsCover"] = useLastImageAsCover
        }
        if (imageUri != null && !useLastImageAsCover) {
            dataToUpdate["coverImage"] = imageUri
        }
        val enumTags = tags.map { Album.Tags.entries[it] }
        if (album.tags != enumTags) {
            dataToUpdate["tags"] = enumTags
        }
        if (toggleDateSelected == R.id.toggleNone) {
            if (album.startDate != null) dataToUpdate["startDate"] = null
            if (album.endDate != null) dataToUpdate["endDate"] = null
            return GeneralValidationResult(true, dataToUpdate=dataToUpdate)
        }
        val dateStart = startDate.toDate()
            ?: return GeneralValidationResult(dateStartError = R.string.err_invalid_date)
        if (dateStart != album.startDate) {
            dataToUpdate["startDate"] = dateStart
        }
        if (toggleDateSelected == R.id.toggleRange) {
            val dateEnd = endDate.toDate()
                ?: return GeneralValidationResult(dateEndError = R.string.err_invalid_date)
            if (!dateEnd.after(dateStart)) {
                return GeneralValidationResult(dateEndError = R.string.err_invalid_later_date)
            }
            if (dateEnd != album.endDate) {
                dataToUpdate["endDate"] = dateEnd
            }
        } else {
            if (album.endDate != null) dataToUpdate["endDate"] = null
        }
        return GeneralValidationResult(true, dataToUpdate=dataToUpdate)
    }

    private fun checkValidData(
        visibility: Visibility,
        membersImagesPermissionPos: Int,
        membersChatPermissionPos: Int,
        guestsImagesPermissionPos: Int,
        guestsChatPermissionPos: Int
    ): SharedValidationResult {
        val dataToUpdate: HashMap<String, Any?> = HashMap()
        if (visibility != album.visibility) {
            dataToUpdate["visibility"] = visibility
        }
        if (visibility != Visibility.PRIVATE) {
            if (membersImagesPermissionPos < 0) {
                return SharedValidationResult(membersImagesPermissionError = R.string.err_empty_field)
            }
            val membersImagesPermission = ImagePermission.entries[membersImagesPermissionPos]
            if (membersImagesPermission != album.membersImagesPermission) {
                dataToUpdate["membersImagesPermission"] = membersImagesPermission
            }
            if (membersChatPermissionPos < 0) {
                return SharedValidationResult(membersImagesPermissionError = R.string.err_empty_field)
            }
            val membersChatPermission = ChatPermission.entries[membersChatPermissionPos]
            if (membersChatPermission != album.membersChatPermission) {
                dataToUpdate["membersChatPermission"] = membersChatPermission
            }
            if (guestsImagesPermissionPos < 0) {
                return SharedValidationResult(membersImagesPermissionError = R.string.err_empty_field)
            }
            val guestsImagesPermission = ImagePermission.entries[guestsImagesPermissionPos]
            if (guestsImagesPermission != album.guestsImagesPermission) {
                dataToUpdate["guestsImagesPermission"] = guestsImagesPermission
            }
            if (guestsChatPermissionPos < 0) {
                return SharedValidationResult(membersImagesPermissionError = R.string.err_empty_field)
            }
            val guestsChatPermission = ChatPermission.entries[guestsChatPermissionPos]
            if (guestsChatPermission != album.guestsChatPermission) {
                dataToUpdate["guestsChatPermission"] = guestsChatPermission
            }
        }
        return SharedValidationResult(true, dataToUpdate=dataToUpdate)
    }

    private fun checkValidData(participantEmail: String): Boolean {
        if (participantEmail.isBlank()) {
            _addParticipantAttempt.value = ParticipantValidationResult(emailError = R.string.err_empty_field)
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(participantEmail).matches()) {
            _addParticipantAttempt.value = ParticipantValidationResult(emailError = R.string.err_not_an_email)
            return false
        }
        val existingParticipant = participants.value!!.find { participant -> participant.email == participantEmail }
        if (existingParticipant != null) {
            _addParticipantAttempt.value = ParticipantValidationResult(emailError = R.string.err_participant_already_in_album)
            return false
        }
        return true
    }

    fun saveUnsavedData(
        name: String,
        useLastImageAsCover: Boolean,
        startDate: String?,
        endDate: String?,
        toggleDateSelected: Int,
        tags: MutableList<Album.Tags>
    ) {
        Log.d("saveUnsavedData", "saveUnsavedData")
        unsavedData = Album(
            name = name,
            coverImage = album.coverImage,
            useLastImageAsCover = useLastImageAsCover,
            startDate = if (toggleDateSelected != R.id.toggleNone) startDate?.toDate() else null,
            endDate = if (toggleDateSelected == R.id.toggleRange) endDate?.toDate() else null,
            tags = tags
        )
    }

    fun hasUnsavedChanges(): Boolean {
        return unsavedData != null
    }

    fun restoreUnsavedData(): Album {
        Log.d("restoreUnsavedData", "restoreUnsavedData")
        val restoredData = unsavedData
        unsavedData = null
        return restoredData ?: Album()
    }

    data class GeneralValidationResult(
        var isDataValid: Boolean = false,
        var nameError: Int? = null,
        var dateStartError: Int? = null,
        var dateEndError: Int? = null,
        var firestoreError: String? = null,
        var dataToUpdate: HashMap<String, Any?> = HashMap()
    )

    data class SharedValidationResult(
        var isDataValid: Boolean = false,
        var membersImagesPermissionError: Int? = null,
        var membersChatPermissionError: Int? = null,
        var guestsImagesPermissionError: Int? = null,
        var guestsChatPermissionError: Int? = null,
        var firestoreError: String? = null,
        var dataToUpdate: HashMap<String, Any?> = HashMap()
    )

    data class ParticipantValidationResult(
        var isDataValid: Boolean = false,
        var emailError: Int? = null,
        var firestoreError: String? = null
    )
}