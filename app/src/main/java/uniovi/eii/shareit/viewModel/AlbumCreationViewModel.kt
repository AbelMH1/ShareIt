package uniovi.eii.shareit.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uniovi.eii.shareit.R
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.model.Participant
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class AlbumCreationViewModel : ViewModel() {

    private val albumToCreate = MutableLiveData(Album())

    fun createAlbum() {
        // TODO: Guardar en base de datos el albunToCreate
    }

    fun validateGeneralData(
        name: String, startDate: String, endDate: String, toggleDateSelected: Int
    ): GeneralValidationResult {
        val dataValidation = checkValidData(name, startDate, endDate, toggleDateSelected)
        if (dataValidation.isDataValid) {
            val updatedAlbum = albumToCreate.value?.apply {
                this.name = name
            }
            albumToCreate.value = updatedAlbum
        }
        return dataValidation
    }

    fun validateSharedData(
        membersImagesPermission: String,
        membersChatPermission: String,
        guestsImagesPermission: String,
        guestsChatPermission: String,
        participants: List<Participant>
    ): SharedValidationResult {
        val dataValidation = checkValidData(
            membersImagesPermission,
            membersChatPermission,
            guestsImagesPermission,
            guestsChatPermission
        )
        if (dataValidation.isDataValid) {
            val updatedAlbum = albumToCreate.value?.apply {
                this.membersImagesPermission = membersImagesPermission
                this.membersChatPermission = membersChatPermission
                this.guestsImagesPermission = guestsImagesPermission
                this.guestsChatPermission = guestsChatPermission
                this.participants =
                    participants.map { participant -> participant.name }.toMutableList()
            }
            albumToCreate.value = updatedAlbum
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
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false
        val dateStart = try {
            sdf.parse(startDate.trim())
        } catch (e: ParseException) {
            return GeneralValidationResult(dateStartError = R.string.err_invalid_date)
        }
        if (toggleDateSelected == R.id.toggleRange) {
            val dateEnd = try {
                sdf.parse(endDate.trim())
            } catch (e: ParseException) {
                return GeneralValidationResult(dateEndError = R.string.err_invalid_date)
            }
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