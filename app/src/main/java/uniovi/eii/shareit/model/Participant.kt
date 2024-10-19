package uniovi.eii.shareit.model

data class Participant(
    val participantId: String = "",
    var name: String = "",
    var email: String = "",
    var imagePath: String = "",
    var role: String = GUEST
) {
    companion object Role {
        const val OWNER = "Owner"
        const val GUEST = "Guest"
        const val MEMBER = "Member"
    }
}
