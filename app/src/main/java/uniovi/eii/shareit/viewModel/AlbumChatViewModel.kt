package uniovi.eii.shareit.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniovi.eii.shareit.model.ChatMessage
import uniovi.eii.shareit.model.repository.FirebaseAuthService
import uniovi.eii.shareit.model.repository.FirestoreChatService
import uniovi.eii.shareit.model.repository.FirestoreUserService

class AlbumChatViewModel : ViewModel() {

    companion object {
        private const val TAG = "AlbumChatViewModel"
    }

    private val _messageList = MutableLiveData<List<ChatMessage>>(emptyList())
    val messageList: LiveData<List<ChatMessage>> = _messageList

    private var chatMessagesListenerRegistration: ListenerRegistration? = null

    fun getCurrentUserId(): String {
        return FirebaseAuthService.getCurrentUser()!!.uid
    }

    private fun updateChatMessageList(newChatMessage: List<ChatMessage>) {
        _messageList.value = newChatMessage
    }

    fun registerChatMessagesListener(
        albumId: String
    ) {
        Log.d(TAG, "chatMessagesListener: START")
        val updateEvent: (newData: List<ChatMessage>) -> Unit = {
            updateChatMessageList(it)
        }
        chatMessagesListenerRegistration = FirestoreChatService.getChatMessagesRegistration(albumId, updateEvent)
    }

    fun unregisterChatMessagesListener() {
        Log.d(TAG, "chatMessagesListener: STOP")
        chatMessagesListenerRegistration?.remove()
    }

    fun sendMessage(messageText: String, albumID: String) {
        val currentUser = FirestoreUserService.getCurrentUserData()!!
        val message = ChatMessage(messageText, currentUser.userId, currentUser.name)
        viewModelScope.launch(Dispatchers.IO) {
            FirestoreChatService.addMessageToChat(albumID, message)
        }
    }

}