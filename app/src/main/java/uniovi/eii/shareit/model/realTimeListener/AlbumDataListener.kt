package uniovi.eii.shareit.model.realTimeListener

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import uniovi.eii.shareit.model.Album

class AlbumDataListener(private val updateEvent: (newData: Album) -> Unit) :
    EventListener<DocumentSnapshot> {
    companion object {
        private const val TAG = "AlbumDataListener"
    }

    override fun onEvent(documentSnapshot: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        if (error != null) {
            Log.e(TAG, "getNewAlbumData:failure", error)
            return
        }
        if (documentSnapshot != null && documentSnapshot.exists()) {
            Log.d(TAG, "getNewAlbumData:success")
            val newAlbumData = documentSnapshot.toObject(Album::class.java) ?: Album()
            updateEvent(newAlbumData)
        } else {
            Log.e(TAG, "getNewAlbumData:nonexistent")
            return
        }
    }
}