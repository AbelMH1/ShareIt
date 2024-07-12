package uniovi.eii.shareit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uniovi.eii.shareit.model.ChatMessage
import uniovi.eii.shareit.view.album.placeholder.PlaceholderContent

class AlbumChatViewModel : ViewModel() {

    private val _messageList = MutableLiveData<List<ChatMessage>>(emptyList())
    val messageList: LiveData<List<ChatMessage>> = _messageList

    init {
        updateChatMessageList(PlaceholderContent.getChatMessageList(25))
    }

    fun updateChatMessageList(newChatMessage: List<ChatMessage>) {
        _messageList.value = newChatMessage
    }

    fun sendMessage(message: String) {
        val m = ChatMessage(message, "0")
        val newMessageList = _messageList.value?.toMutableList().orEmpty().toMutableList()
        newMessageList.add(m)
        _messageList.value = newMessageList
    }

}