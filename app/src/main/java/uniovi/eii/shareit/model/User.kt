package uniovi.eii.shareit.model

data class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var imagePath: String = ""
) {
    fun getChanges(newUserData: User): HashMap<String, Any> {
        val changedData = HashMap<String, Any>()
        if (this.name != newUserData.name) changedData["name"] = newUserData.name
        if (this.email != newUserData.email) changedData["email"] = newUserData.email
        if (this.imagePath != newUserData.imagePath) changedData["imagePath"] = newUserData.imagePath
        return changedData
    }
}
