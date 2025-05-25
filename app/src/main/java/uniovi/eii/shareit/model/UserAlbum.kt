package uniovi.eii.shareit.model

import uniovi.eii.shareit.model.Album.Tags
import java.util.Date

data class UserAlbum(
    var albumId: String = "",
    var creatorId: String = "",
    var creatorName: String = "",
    var name: String = "",
    var coverImage: String = "",
    var creationDate: Date = Date(),
    var lastUpdate: Date = Date(),
    var tags: List<Tags> = emptyList(),
)
