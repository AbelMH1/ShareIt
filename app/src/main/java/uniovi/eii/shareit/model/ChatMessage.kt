package uniovi.eii.shareit.model

import java.util.Date

data class ChatMessage(
    var message: String = "",
    var senderId: String = "",
    var senderName: String = "",
    var timestamp: Date = Date(),
)
