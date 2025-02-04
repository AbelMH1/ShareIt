package uniovi.eii.shareit.model.realTimeListener

import android.util.Log
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import uniovi.eii.shareit.model.Participant

class AlbumParticipantsListener(private val updateParticipantsEvent: (newAlbumParticipants: List<Participant>) -> Unit) :
    EventListener<QuerySnapshot> {
    companion object {
        private const val TAG = "AlbumParticipantsListener"
    }

    override fun onEvent(snapshots: QuerySnapshot?, error: FirebaseFirestoreException?) {
        if (error != null) {
            Log.e(TAG, "getNewAlbumParticipants:failure", error)
            return
        }
        val participants = mutableListOf<Participant>()
        for (doc in snapshots!!) {
            participants.add(doc.toObject(Participant::class.java))
        }
        Log.d(TAG, "getNewAlbumParticipants:success")
        updateParticipantsEvent(participants)
    }
}