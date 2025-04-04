package uniovi.eii.shareit.view.album.placeholder

import uniovi.eii.shareit.model.Image
import java.util.Date

class PlaceholderContent {

    companion object {

        fun getImagesList(count: Int): MutableList<Image> {
            val items: MutableList<Image> = ArrayList()
            for (i in 1..count) {
                val time: Long = 15768000
                val likes = List(100*i) { index -> "$index" }
                items.add(Image("$i", "Author ${i%7}", "", "${i%3}", creationDate = Date(time*i), likes = likes))
            }
            return items
        }
    }

}