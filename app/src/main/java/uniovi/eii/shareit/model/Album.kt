package uniovi.eii.shareit.model

import java.util.Date

data class Album(
    var albumId: String = "",
    var creatorId: String = "",
    var creatorName: String = "",
    var name: String = "",
    var coverImage: String = "",
    var useLastImageAsCover: Boolean = true,
    var creationDate: Date = Date(),
    var lastUpdate: Date = Date(),
    var startDate: Date? = null,
    var endDate: Date? = null,
    var visibility: Visibility = Visibility.PRIVATE,
    var membersImagesPermission: ImagePermission? = null,
    var membersChatPermission: ChatPermission? = null,
    var guestsImagesPermission: ImagePermission? = null,
    var guestsChatPermission: ChatPermission? = null,
    var tags: List<Tags> = emptyList(),
) {
    enum class ChatPermission {
        HIDDEN, SEE, COMMENT;
        val canSee get() = this == SEE || this == COMMENT
        val canComment get() = this == COMMENT
    }

    enum class ImagePermission {
        SEE, VOTE, ADD;
        val canVote get() = this == VOTE || this == ADD
        val canAdd  get() = this == ADD
    }

    enum class Visibility {
        PRIVATE,
        SHARED,
        PUBLIC,
    }

    enum class Tags {
        PARTY,
        HUMOR,
        TRAVEL,
        NATURE,
        EVENTS,
        FOOD,
        ANIMALS,
        SPORTS,
        CULTURE,
        PEOPLE,
    }

    fun toUserAlbum(): UserAlbum {
        return UserAlbum(
            albumId, creatorId, creatorName, name, coverImage, creationDate, lastUpdate, tags
        )
    }

    fun chatPermissionFor(role: Participant.Role) = when (role) {
        Participant.Role.MEMBER -> membersChatPermission
        Participant.Role.GUEST  -> guestsChatPermission
        else -> ChatPermission.COMMENT // owner máximo permiso
    }

    fun imagePermissionFor(role: Participant.Role) = when (role) {
        Participant.Role.MEMBER -> membersImagesPermission
        Participant.Role.GUEST  -> guestsImagesPermission
        else -> ImagePermission.ADD // owner máximo permiso
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Album

        if (albumId != other.albumId) return false
        if (creatorId != other.creatorId) return false
        if (creatorName != other.creatorName) return false
        if (name != other.name) return false
        if (coverImage != other.coverImage) return false
        if (useLastImageAsCover != other.useLastImageAsCover) return false
        if (creationDate != other.creationDate) return false
        if (lastUpdate != other.lastUpdate) return false
        if (startDate != other.startDate) return false
        if (endDate != other.endDate) return false
        if (visibility != other.visibility) return false
        if (membersImagesPermission != other.membersImagesPermission) return false
        if (membersChatPermission != other.membersChatPermission) return false
        if (guestsImagesPermission != other.guestsImagesPermission) return false
        if (guestsChatPermission != other.guestsChatPermission) return false
        if (tags != other.tags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = albumId.hashCode()
        result = 31 * result + creatorId.hashCode()
        result = 31 * result + creatorName.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + coverImage.hashCode()
        result = 31 * result + useLastImageAsCover.hashCode()
        result = 31 * result + creationDate.hashCode()
        result = 31 * result + lastUpdate.hashCode()
        result = 31 * result + (startDate?.hashCode() ?: 0)
        result = 31 * result + (endDate?.hashCode() ?: 0)
        result = 31 * result + visibility.hashCode()
        result = 31 * result + (membersImagesPermission?.hashCode() ?: 0)
        result = 31 * result + (membersChatPermission?.hashCode() ?: 0)
        result = 31 * result + (guestsImagesPermission?.hashCode() ?: 0)
        result = 31 * result + (guestsChatPermission?.hashCode() ?: 0)
        result = 31 * result + tags.hashCode()
        return result
    }
}
