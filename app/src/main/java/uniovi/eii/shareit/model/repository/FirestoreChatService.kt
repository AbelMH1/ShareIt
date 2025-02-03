package uniovi.eii.shareit.model.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import uniovi.eii.shareit.model.ChatMessage
import uniovi.eii.shareit.model.realTimeListener.AlbumChatListener

object FirestoreChatService {

    private const val TAG = "FirestoreChatService"

    /**
     * Adición del mensaje [message] bajo la subcolección de chat del álbum
     * con el [albumId] dado en firestore.
     */
    suspend fun addMessageToChat(albumId: String, message: ChatMessage) : String? {
        val db = Firebase.firestore
        return try {
            db.collection("albums")
                .document(albumId)
                .collection("chat")
                .add(message)
                .await()
            Log.d(TAG, "addMessageToChat:success")
            null
        } catch (e: Exception) {
            Log.e(TAG, "addMessageToChat:failure")
            e.message
        }
    }

    /**
     * Eliminación del mensaje con el [messageId] pasado como parámetro de la subcolección de
     * chat del álbum con el [albumId] dado en firestore.
     */
    fun eliminateMessageFromChat(albumId: String, messageId: String) {
        val db = Firebase.firestore
        try {
            db.collection("albums")
                .document(albumId)
                .collection("chat")
                .document(messageId)
                .delete()
                .addOnSuccessListener { Log.d(TAG, "eliminateMessageFromChat:success") }
                .addOnFailureListener { e -> Log.w(TAG, "eliminateMessageFromChat:failure", e) }
        } catch (e: Exception) {
            Log.e(TAG, "eliminateMessageFromChat:failure", e)
            e.message
        }
    }

    /**
     * Enlazamiento de un objeto de escucha en tiempo real para los mensajes del chat del
     * album [albumId] con el viewmodel correspondiente según lo especificado mediante
     * la función [updateVMEvent].
     * Se hace uso de la clase [AlbumChatListener].
     */
    fun getChatMessagesRegistration(
        albumId: String, updateVMEvent: (newData: List<ChatMessage>) -> Unit
    ): ListenerRegistration {
        val db = Firebase.firestore
        return db.collection("albums")
            .document(albumId)
            .collection("chat")
            .orderBy("timestamp")
            .addSnapshotListener(AlbumChatListener(updateVMEvent))
    }
}