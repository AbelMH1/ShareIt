package uniovi.eii.shareit.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniovi.eii.shareit.R
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.model.repository.FirestoreAlbumService
import uniovi.eii.shareit.model.repository.FirestoreProfileService
import uniovi.eii.shareit.utils.toDate


class AlbumCreationViewModel : ViewModel() {

    private val _albumToCreate = Album()
    val isCompletedAlbumCreation = MutableLiveData<Boolean>()

    fun createAlbum() {
        val currentUser = FirestoreProfileService.getCurrentUserData()
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
        name: String, startDate: String, endDate: String, toggleDateSelected: Int, shared: Boolean
    ): GeneralValidationResult {
        val dataValidation = checkValidData(name, startDate, endDate, toggleDateSelected)
        if (dataValidation.isDataValid) {
            _albumToCreate.apply {
                this.name = name
                this.startDate = startDate.toDate()
                this.endDate = endDate.toDate()
                this.visibility = if (shared) Album.SHARED else Album.PRIVATE
            }
        }
        return dataValidation
    }

    fun validateSharedData(
        membersImagesPermission: String,
        membersChatPermission: String,
        guestsImagesPermission: String,
        guestsChatPermission: String
    ): SharedValidationResult {
        val dataValidation = checkValidData(
            membersImagesPermission,
            membersChatPermission,
            guestsImagesPermission,
            guestsChatPermission
        )
        if (dataValidation.isDataValid) {
            _albumToCreate.apply {
                this.membersImagesPermission = membersImagesPermission
                this.membersChatPermission = membersChatPermission
                this.guestsImagesPermission = guestsImagesPermission
                this.guestsChatPermission = guestsChatPermission
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
        membersImagesPermission: String,
        membersChatPermission: String,
        guestsImagesPermission: String,
        guestsChatPermission: String
    ): SharedValidationResult {
        if (membersImagesPermission.isBlank()) return SharedValidationResult(memberImagesError = R.string.err_empty_field)
        if (membersChatPermission.isBlank()) return SharedValidationResult(memberChatError = R.string.err_empty_field)
        if (guestsImagesPermission.isBlank()) return SharedValidationResult(guestImagesError = R.string.err_empty_field)
        if (guestsChatPermission.isBlank()) return SharedValidationResult(guestChatError = R.string.err_empty_field)
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