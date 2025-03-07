package uniovi.eii.shareit.model.realTimeListener

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import uniovi.eii.shareit.model.Participant
import uniovi.eii.shareit.model.Participant.Role

class AlbumUserRoleListener(private val updateEvent: (newRole: Role) -> Unit) :
    EventListener<DocumentSnapshot> {
    companion object {
        private const val TAG = "AlbumUserRoleListener"
    }

    override fun onEvent(documentSnapshot: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        if (error != null) {
            Log.e(TAG, "getNewAlbumUserRole: failure", error)
            return
        }

        if (documentSnapshot != null && !documentSnapshot.metadata.isFromCache) {
            Log.d(TAG, "getNewAlbumUserRole: success")
            if (documentSnapshot.exists()) {
                val newUserRole = documentSnapshot.toObject(Participant::class.java)!!.role
                updateEvent(newUserRole)
            } else {
                updateEvent(Role.NONE)
            }
        }
    }
}