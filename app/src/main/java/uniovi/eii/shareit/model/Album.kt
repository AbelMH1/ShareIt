package uniovi.eii.shareit.model

import com.google.android.gms.maps.model.LatLng
import java.util.Date

data class Album(
    var name: String = "",
    var creatorName: String = "",
    var startDate: Date? = null,
    var endDate: Date? = null,
    var location: LatLng? = null,
    var shared: Boolean = false,
    var membersImagesPermission: String? = null,
    var membersChatPermission: String? = null,
    var guestsImagesPermission: String? = null,
    var guestsChatPermission: String? = null,
    var invitationLinkEnabled: Boolean = false,
    var invitationLink: String? = null,
    var participants: MutableList<String>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Album

        if (name != other.name) return false
        if (creatorName != other.creatorName) return false
        if (startDate != other.startDate) return false
        if (endDate != other.endDate) return false
        if (location != other.location) return false
        if (shared != other.shared) return false
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
        result = 31 * result + shared.hashCode()
        result = 31 * result + (membersImagesPermission?.hashCode() ?: 0)
        result = 31 * result + (membersChatPermission?.hashCode() ?: 0)
        result = 31 * result + (guestsImagesPermission?.hashCode() ?: 0)
        result = 31 * result + (guestsChatPermission?.hashCode() ?: 0)
        result = 31 * result + invitationLinkEnabled.hashCode()
        result = 31 * result + (invitationLink?.hashCode() ?: 0)
        return result
    }
}
