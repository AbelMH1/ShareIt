package uniovi.eii.shareit.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniovi.eii.shareit.R
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.model.Album.Visibility
import uniovi.eii.shareit.model.Album.ImagePermission
import uniovi.eii.shareit.model.Album.ChatPermission
import uniovi.eii.shareit.model.repository.FirestoreAlbumService
import uniovi.eii.shareit.model.repository.FirestoreUserService
import uniovi.eii.shareit.utils.toDate


class AlbumCreationViewModel : ViewModel() {

    private val _albumToCreate = Album()
    val isCompletedAlbumCreation = MutableLiveData<Boolean>()

    fun createAlbum() {
        val currentUser = FirestoreUserService.getCurrentUserData()
        if (currentUser == null) {
            isCompletedAlbumCreation.value = false
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            isCompletedAlbumCreation.postValue(
                FirestoreAlbumService.createAlbum(_albumToCreate, currentUser)
            )
        }
    }

    fun validateGeneralData(
        name: String, startDate: String, endDate: String, toggleDateSelected: Int, visibilitySelected: Int,
    ): GeneralValidationResult {
        val dataValidation = checkValidData(name, startDate, endDate, toggleDateSelected)
        if (dataValidation.isDataValid) {
            _albumToCreate.apply {
                this.name = name
                this.startDate = startDate.toDate()
                this.endDate = endDate.toDate()
                this.visibility = when (visibilitySelected) {
                    R.id.togglePublic -> Visibility.PUBLIC
                    R.id.toggleShared -> Visibility.SHARED
                    else -> Visibility.PRIVATE
                }
            }
        }
        return dataValidation
    }

    fun validateSharedData(
        membersImagesPermission: Int,
        membersChatPermission: Int,
        guestsImagesPermission: Int,
        guestsChatPermission: Int
    ): SharedValidationResult {
        val dataValidation = checkValidData(
            membersImagesPermission,
            membersChatPermission,
            guestsImagesPermission,
            guestsChatPermission
        )
        if (dataValidation.isDataValid) {
            _albumToCreate.apply {
                this.membersImagesPermission = ImagePermission.entries[membersImagesPermission]
                this.membersChatPermission = ChatPermission.entries[membersChatPermission]
                this.guestsImagesPermission = ImagePermission.entries[guestsImagesPermission]
                this.guestsChatPermission = ChatPermission.entries[guestsChatPermission]
            }
        }
        return dataValidation
    }

    private fun checkValidData(
        name: String, startDate: String, endDate: String, toggleDateSelected: Int
    ): GeneralValidationResult {
        if (name.isBlank()) {
            return GeneralValidationResult(nameError = R.string.err_empty_field)
        }
        if (toggleDateSelected == R.id.toggleNone) return GeneralValidationResult(true)
        val dateStart = startDate.toDate()
            ?: return GeneralValidationResult(dateStartError = R.string.err_invalid_date)
        if (toggleDateSelected == R.id.toggleRange) {
            val dateEnd = endDate.toDate()
                ?: return GeneralValidationResult(dateEndError = R.string.err_invalid_date)
            if (!dateEnd.after(dateStart)) {
                return GeneralValidationResult(dateEndError = R.string.err_invalid_later_date)
            }
        }
        return GeneralValidationResult(true)
    }

    private fun checkValidData(
        membersImagesPermission: Int,
        membersChatPermission: Int,
        guestsImagesPermission: Int,
        guestsChatPermission: Int
    ): SharedValidationResult {
        if (membersImagesPermission < 0) return SharedValidationResult(memberImagesError = R.string.err_empty_field)
        if (membersChatPermission < 0) return SharedValidationResult(memberChatError = R.string.err_empty_field)
        if (guestsImagesPermission < 0) return SharedValidationResult(guestImagesError = R.string.err_empty_field)
        if (guestsChatPermission < 0) return SharedValidationResult(guestChatError = R.string.err_empty_field)
        return SharedValidationResult(true)
    }

    data class GeneralValidationResult(
        var isDataValid: Boolean = false,
        var isSharedAlbum: Boolean = false,
        var nameError: Int? = null,
        var dateStartError: Int? = null,
        var dateEndError: Int? = null
    )

    data class SharedValidationResult(
        var isDataValid: Boolean = false,
        var memberImagesError: Int? = null,
        var memberChatError: Int? = null,
        var guestImagesError: Int? = null,
        var guestChatError: Int? = null
    )
}