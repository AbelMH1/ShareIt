package uniovi.eii.shareit.model.realTimeListener

import android.util.Log
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import uniovi.eii.shareit.model.UserAlbum

class UserAlbumsListener(private val updateEvent: (newUserAlbums: List<UserAlbum>) -> Unit) :
    EventListener<QuerySnapshot> {
    companion object {
        private const val TAG = "UserAlbumsListener"
    }

    override fun onEvent(snapshots: QuerySnapshot?, error: FirebaseFirestoreException?) {
        if (error != null) {
            Log.e(TAG, "getNewUserAlbums:failure", error)
            return
        }
        val userAlbums = mutableListOf<UserAlbum>()
        for (doc in snapshots!!) {
            userAlbums.add(doc.toObject(UserAlbum::class.java))
        }
        Log.d(TAG, "getNewUserAlbums:success")
        updateEvent(userAlbums)
    }
}