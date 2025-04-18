package uniovi.eii.shareit.model.realTimeListener

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException

class ImageLikedListener(private val updateEvent: (newData: Boolean) -> Unit) :
    EventListener<DocumentSnapshot> {
    companion object {
        private const val TAG = "ImageLikedListener"
    }

    override fun onEvent(documentSnapshot: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        if (error != null) {
            Log.e(TAG, "getLikedByCurrentUser:failure", error)
            return
        }
        if (documentSnapshot != null && documentSnapshot.exists()) {
            Log.d(TAG, "getLikedByCurrentUser:success")
            updateEvent(true)
        } else {
            updateEvent(false)
        }
    }
}
