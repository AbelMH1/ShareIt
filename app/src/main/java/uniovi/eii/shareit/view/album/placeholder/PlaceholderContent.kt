package uniovi.eii.shareit.view.album.placeholder

import uniovi.eii.shareit.model.ChatMessage
import uniovi.eii.shareit.model.Image
import java.time.LocalDateTime
import java.time.ZoneOffset

class PlaceholderContent {

    companion object {

        fun getImagesList(count: Int): MutableList<Image> {
            val items: MutableList<Image> = ArrayList()
            for (i in 1..count) {
                val time: Long = 15768000
                val likes = List(100*i) { index -> "$index" }
                items.add(Image("Author ${i%7}", "${i%3}", creationDate = LocalDateTime.ofEpochSecond(time*i, 0, ZoneOffset.UTC), likes = likes))
            }
            return items
        }

        fun getChatMessageList(count: Int): MutableList<ChatMessage> {
            val items: MutableList<ChatMessage> = ArrayList()
            val time: Long = 4200
            for (i in 1..count/2) {
                items.add(ChatMessage("This is message nº$i", "${i%2}", "User${(i%2)+1}", LocalDateTime.ofEpochSecond(time*i, 0, ZoneOffset.UTC)))
            }
            for (i in count/2..count) {
                val id = if (i%4<2) "0" else "1"
                items.add(ChatMessage("This is message nº$i", id, "User${id}", LocalDateTime.ofEpochSecond(time*i, 0, ZoneOffset.UTC)))
            }
            items.add(ChatMessage("This is message nº${count-1}", "1", "User2", LocalDateTime.ofEpochSecond(time*(count-1), 0, ZoneOffset.UTC)))
            items.add(ChatMessage("This is message nº$count", "1","User3", LocalDateTime.ofEpochSecond(time*count, 0, ZoneOffset.UTC)))
            return items
        }
    }

}