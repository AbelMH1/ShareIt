package uniovi.eii.shareit.model

import com.google.android.gms.maps.model.LatLng
import java.util.Date

data class Album(
    var albumId: String = "",
    var creatorId: String = "",
    var creatorName: String = "",
    var name: String = "",
    var coverImage: String = "",
    var creationDate: Date = Date(),
    var lastUpdate: Date = Date(),
    var startDate: Date? = null,
    var endDate: Date? = null,
    var location: LatLng? = null,
    var visibility: String = PRIVATE,
    var membersImagesPermission: Int? = null,
    var membersChatPermission: Int? = null,
    var guestsImagesPermission: Int? = null,
    var guestsChatPermission: Int? = null,
    var invitationLinkEnabled: Boolean = false,
    var invitationLink: String? = null
) {

    fun toUserAlbum(): UserAlbum {
        return UserAlbum(
            albumId, creatorId, creatorName, name, coverImage, creationDate, lastUpdate
        )
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Album

        if (name != other.name) return false
        if (creatorName != other.creatorName) return false
        if (startDate != other.startDate) return false
        if (endDate != other.endDate) return false
        if (location != other.location) return false
        if (visibility != other.visibility) return false
        if (membersImagesPermission != other.membersImagesPermission) return false
        if (membersChatPermission != other.membersChatPermission) return false
        if (guestsImagesPermission != other.guestsImagesPermission) return false
        if (guestsChatPermission != other.guestsChatPermission) return false
        if (invitationLinkEnabled != other.invitationLinkEnabled) return false
        if (invitationLink != other.invitationLink) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + creatorName.hashCode()
        result = 31 * result + (startDate?.hashCode() ?: 0)
        result = 31 * result + (endDate?.hashCode() ?: 0)
        result = 31 * result + (location?.hashCode() ?: 0)
        result = 31 * result + visibility.hashCode()
        result = 31 * result + (membersImagesPermission?.hashCode() ?: 0)
        result = 31 * result + (membersChatPermission?.hashCode() ?: 0)
        result = 31 * result + (guestsImagesPermission?.hashCode() ?: 0)
        result = 31 * result + (guestsChatPermission?.hashCode() ?: 0)
        result = 31 * result + invitationLinkEnabled.hashCode()
        result = 31 * result + (invitationLink?.hashCode() ?: 0)
        return result
    }

    companion object Visibility {
        const val PUBLIC = "Public"
        const val PRIVATE = "Private"
        const val SHARED = "Shared"
    }
}
