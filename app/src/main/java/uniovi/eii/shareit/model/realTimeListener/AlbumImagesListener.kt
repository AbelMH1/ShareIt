package uniovi.eii.shareit.model.realTimeListener

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import uniovi.eii.shareit.model.Image

class AlbumImagesListener(
    private val updateEvent: (newAlbumImages: List<Image>, isUpdateFromServer: Boolean) -> Unit,
) : EventListener<QuerySnapshot> {
    companion object {
        private const val TAG = "AlbumImagesListener"
    }

    override fun onEvent(snapshots: QuerySnapshot?, error: FirebaseFirestoreException?) {
        if (error != null) {
            Log.e(TAG, "getNewAlbumImages:failure", error)
            return
        }

//        Log.d(TAG, "Origin cache: ${snapshots!!.metadata.isFromCache}; Changes: ${snapshots!!.documentChanges.size}")
//        snapshots!!.documentChanges.forEach { change ->
//            when (change.type) {
//                DocumentChange.Type.ADDED -> {
//                    Log.d(TAG, "added: ${change.document.id}")
//                }
//                DocumentChange.Type.MODIFIED -> {
//                    Log.d(TAG, "modified ${change.document.id}")
//                }
//                DocumentChange.Type.REMOVED -> {
//                    Log.d(TAG, "removed ${change.document.id}")
//                }
//            }
//        }
        val albumImages = mutableListOf<Image>()
        for (doc in snapshots!!) {
            val image = doc.toObject(Image::class.java)
            albumImages.add(image)
        }
        Log.d(TAG, "getNewAlbumImages:success")
        updateEvent(albumImages, !snapshots.metadata.isFromCache)
    }
}