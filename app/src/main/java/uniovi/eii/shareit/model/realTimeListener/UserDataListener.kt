package uniovi.eii.shareit.model.realTimeListener

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import uniovi.eii.shareit.model.User

class UserDataListener(private val updateEvent: (newData: User) -> Unit) :
    EventListener<DocumentSnapshot> {
    companion object {
        private const val TAG = "UserDataListener"
    }

    override fun onEvent(documentSnapshot: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        if (error != null) {
            Log.e(TAG, "getNewUserData:failure", error)
            return
        }
        if (documentSnapshot != null) {
            Log.d(TAG, "getNewUserData:success")
            val newUserData = documentSnapshot.toObject(User::class.java) ?: User()
            updateEvent(newUserData)
        }
    }
}