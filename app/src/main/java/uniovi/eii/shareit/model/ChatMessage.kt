package uniovi.eii.shareit.model

import java.time.LocalDateTime

data class ChatMessage(
    var message: String = "",
    var senderId: String = "",
    var senderName: String = "",
    var timestamp: LocalDateTime = LocalDateTime.now(),
)
