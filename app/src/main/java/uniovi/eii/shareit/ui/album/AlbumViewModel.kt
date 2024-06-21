package uniovi.eii.shareit.ui.album

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uniovi.eii.shareit.model.ChatMessage
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.ui.album.placeholder.PlaceholderContent

class AlbumViewModel : ViewModel() {

    private val _imageList = MutableLiveData<List<Image>>(emptyList())
    val imageList: LiveData<List<Image>> = _imageList

    private val _messageList = MutableLiveData<List<ChatMessage>>(emptyList())
    val messageList: LiveData<List<ChatMessage>> = _messageList

    init {
        updateImageList(PlaceholderContent.getImagesList(25))
        updateChatMessageList(PlaceholderContent.getChatMessageList(25))
    }

    fun updateImageList(newImages: List<Image>) {
        _imageList.value = newImages
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