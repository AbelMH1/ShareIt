package uniovi.eii.shareit.model

import java.util.Date

data class User(
    var userId: String = "",
    var name: String = "",
    var email: String = "",
    var imagePath: String = "",
    var lastUpdatedImage: Date = Date(),
) {
    fun toParticipant(): Participant {
        return Participant(userId, name, email, imagePath)
    }
    fun getChanges(newUserData: User): HashMap<String, Any> {
        val changedData = HashMap<String, Any>()
        if (this.name != newUserData.name) changedData["name"] = newUserData.name
        if (this.email != newUserData.email) changedData["email"] = newUserData.email
        return changedData
    }
}
