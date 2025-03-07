package uniovi.eii.shareit.model

data class Participant(
    val participantId: String = "",
    var name: String = "",
    var email: String = "",
    var imagePath: String = "",
    var role: Role = Role.GUEST
) {
    enum class Role {
        OWNER,
        GUEST,
        MEMBER,
        NONE
    }
}