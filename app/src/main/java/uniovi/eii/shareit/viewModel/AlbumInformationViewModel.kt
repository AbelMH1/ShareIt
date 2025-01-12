package uniovi.eii.shareit.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniovi.eii.shareit.R
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.model.repository.FirestoreAlbumService
import uniovi.eii.shareit.utils.toDate

class AlbumInformationViewModel : ViewModel() {

    companion object {
        private const val TAG = "AlbumInformationViewModel"
    }

    private val _album = MutableLiveData(Album())
    val album: LiveData<Album> = _album

    private var albumDataListenerRegistration: ListenerRegistration? = null

    fun updateCurrentAlbum(albumID: String, albumName: String, albumCoverImage: String) {
        _album.value = Album(albumID, albumName, albumCoverImage)
    }

    fun getAlbumInfo(): Album {
        return album.value?.copy() ?: Album()
    }

    private fun updateAlbumData(newAlbumData: Album) {
        _album.postValue(newAlbumData)
    }

    fun registerAlbumDataListener(
        albumId: String
    ) {
        Log.d(TAG, "albumDataListener: START")
        val updateEvent: (newData: Album) -> Unit = {
            updateAlbumData(it)
        }
        albumDataListenerRegistration = FirestoreAlbumService.getAlbumDataRegistration(albumId, updateEvent)
    }

    fun unregisterAlbumDataListener() {
        Log.d(TAG, "albumDataListener: STOP")
        albumDataListenerRegistration?.remove()
    }

    fun saveGeneralData(
        name: String, startDate: String, endDate: String, toggleDateSelected: Int, location: Boolean
    ): GeneralValidationResult {
        val dataValidation = checkValidData(name, startDate, endDate, toggleDateSelected, location)
        if (dataValidation.isDataValid) {
            viewModelScope.launch(Dispatchers.IO) {
                FirestoreAlbumService.updateCurrentAlbumData(album.value!!.albumId, dataValidation.dataToUpdate)
            }
        }
        return dataValidation
    }

    fun saveSharedData(
        isShared: Boolean,
        membersImagesPermission: String,
        membersChatPermission: String,
        guestsImagesPermission: String,
        guestsChatPermission: String,
        invitationLinkEnabled: Boolean
    ): SharedValidationResult {
        val dataValidation = checkValidData(
            isShared,
            membersImagesPermission,
            membersChatPermission,
            guestsImagesPermission,
            guestsChatPermission,
            invitationLinkEnabled)

        if (dataValidation.isDataValid) {
            viewModelScope.launch(Dispatchers.IO) {
                FirestoreAlbumService.updateCurrentAlbumData(album.value!!.albumId, dataValidation.dataToUpdate)
            }
        }
        return dataValidation
    }

    private fun checkValidData(
        name: String, startDate: String, endDate: String, toggleDateSelected: Int, location: Boolean
    ): GeneralValidationResult {
        // Todo: location (LatLng(0.0, 0.0))
        val dataToUpdate: HashMap<String, Any?> = HashMap()
        if (name.isBlank()) {
            return GeneralValidationResult(nameError = R.string.err_empty_field)
        }
        if (name != album.value?.name) {
            dataToUpdate["name"] = name
        }
        if (toggleDateSelected == R.id.toggleNone) {
            if (album.value?.startDate != null) dataToUpdate["startDate"] = null
            if (album.value?.endDate != null) dataToUpdate["endDate"] = null
            return GeneralValidationResult(true, dataToUpdate=dataToUpdate)
        }
        val dateStart = startDate.toDate()
            ?: return GeneralValidationResult(dateStartError = R.string.err_invalid_date)
        if (dateStart != album.value?.startDate) {
            dataToUpdate["startDate"] = dateStart
        }
        if (toggleDateSelected == R.id.toggleRange) {
            val dateEnd = endDate.toDate()
                ?: return GeneralValidationResult(dateEndError = R.string.err_invalid_date)
            if (!dateEnd.after(dateStart)) {
                return GeneralValidationResult(dateEndError = R.string.err_invalid_later_date)
            }
            if (dateEnd != album.value?.endDate) {
                dataToUpdate["endDate"] = dateEnd
            }
        } else {
            if (album.value?.endDate != null) dataToUpdate["endDate"] = null
        }
        return GeneralValidationResult(true, dataToUpdate=dataToUpdate)
    }

    private fun checkValidData(shared: Boolean,
                               membersImagesPermission: String,
                               membersChatPermission: String,
                               guestsImagesPermission: String,
                               guestsChatPermission: String,
                               invitationLinkEnabled: Boolean
    ): SharedValidationResult {
        val dataToUpdate: HashMap<String, Any?> = HashMap()
        if (shared) {
            if (membersImagesPermission.isBlank()) {
                return SharedValidationResult(membersImagesPermissionError = R.string.err_empty_field)
            }
            if (membersImagesPermission != album.value?.membersImagesPermission) {
                dataToUpdate["membersImagesPermission"] = membersImagesPermission
            }
            if (membersChatPermission.isBlank()) {
                return SharedValidationResult(membersImagesPermissionError = R.string.err_empty_field)
            }
            if (membersChatPermission != album.value?.membersChatPermission) {
                dataToUpdate["membersChatPermission"] = membersChatPermission
            }
            if (guestsImagesPermission.isBlank()) {
                return SharedValidationResult(membersImagesPermissionError = R.string.err_empty_field)
            }
            if (guestsImagesPermission != album.value?.guestsImagesPermission) {
                dataToUpdate["guestsImagesPermission"] = guestsImagesPermission
            }
            if (guestsChatPermission.isBlank()) {
                return SharedValidationResult(membersImagesPermissionError = R.string.err_empty_field)
            }
            if (guestsChatPermission != album.value?.guestsChatPermission) {
                dataToUpdate["guestsChatPermission"] = guestsChatPermission
            }
        }
        val visibility = if(shared) Album.SHARED else Album.PRIVATE
        if (visibility != album.value?.visibility) {
            dataToUpdate["visibility"] = visibility
        }
        return SharedValidationResult(true, dataToUpdate=dataToUpdate)
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

}