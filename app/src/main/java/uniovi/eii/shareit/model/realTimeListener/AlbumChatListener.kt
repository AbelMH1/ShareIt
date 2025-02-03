package uniovi.eii.shareit.model.realTimeListener

import android.util.Log
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import uniovi.eii.shareit.model.ChatMessage

class AlbumChatListener(private val updateEvent: (newChatMessages: List<ChatMessage>) -> Unit) :
    EventListener<QuerySnapshot> {
    companion object {
        private const val TAG = "AlbumChatListener"
    }

    override fun onEvent(snapshots: QuerySnapshot?, error: FirebaseFirestoreException?) {
        if (error != null) {
            Log.e(TAG, "getNewChatMessages:failure", error)
            return
        }
        val chatMessages = mutableListOf<ChatMessage>()
        for (doc in snapshots!!) {
            chatMessages.add(doc.toObject(ChatMessage::class.java))
        }
        Log.d(TAG, "getNewChatMessages:success")
        updateEvent(chatMessages)
    }
}