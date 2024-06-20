package uniovi.eii.shareit.ui.album.placeholder

import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.Participant
import java.util.Date

class PlaceholderContent {

    companion object {
        fun getImagesList(count: Int): MutableList<Image> {
            val items: MutableList<Image> = ArrayList()
            for (i in 1..count) {
                val time: Long = 86400000
                items.add(Image("Author $i", creationDate = Date(time*i)))
            }
            return items
        }

        fun getParticipantsList(count: Int): MutableList<Participant> {
            val items: MutableList<Participant> = ArrayList()
            for (i in 1..count) {
                items.add(Participant("Participant $i", "participant$i@gmail.com", "Guest"))
            }
            return items
        }
    }

}