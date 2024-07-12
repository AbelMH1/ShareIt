package uniovi.eii.shareit.model

import java.time.LocalDateTime

data class UserAlbum(
    val albumID: String = "",
    var name: String = "",
    var coverImage: String = "",
    var creatorName: String = "",
    var creationDate: LocalDateTime = LocalDateTime.now(),
    var lastUpdate: LocalDateTime = LocalDateTime.now(),
)
