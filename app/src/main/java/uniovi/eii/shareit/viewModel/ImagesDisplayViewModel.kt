package uniovi.eii.shareit.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.VIEW_MODEL_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniovi.eii.shareit.R
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.Section
import uniovi.eii.shareit.model.repository.FirestoreImageService
import uniovi.eii.shareit.utils.toFormattedChatDateString


class ImagesDisplayViewModel(private val instanceKey: String) : ViewModel() {

    companion object {
        private const val TAG = "ImagesDisplayViewModel"

        const val ALBUM_VIEW = "album"
        const val GENERAL_VIEW = "general"

        @Suppress("UNCHECKED_CAST") // Añadir el resto de parámetros necesarios en el constructor de la clase: ImagesDisplayViewModelFactory(private val extraParams: String)
        class ImagesDisplayViewModelFactory : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val instanceKey = checkNotNull(extras[VIEW_MODEL_KEY])
                if (modelClass.isAssignableFrom(ImagesDisplayViewModel::class.java)) {
                    return ImagesDisplayViewModel(instanceKey) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private val _allImageList = MutableLiveData<List<Image>>(emptyList())
    val allImageList: LiveData<List<Image>> = _allImageList

    private val _displayImageList = MutableLiveData<List<Image>>(emptyList())
    val displayImageList: LiveData<List<Image>> = _displayImageList
    private val _displaySectionList = MutableLiveData<List<Section>>(emptyList())
    val displaySectionList: LiveData<List<Section>> = _displaySectionList

    private val _currentFilter = MutableLiveData(R.id.action_filter_all)
    val currentFilter: LiveData<Int> = _currentFilter
    private val _currentOrder = MutableLiveData(R.id.action_order_date)
    val currentOrder: LiveData<Int> = _currentOrder
    private val _currentOrderDirection = MutableLiveData(R.id.action_order_ascending)
    val currentOrderDirection: LiveData<Int> = _currentOrderDirection

    private var albumImagesListenerRegistration: ListenerRegistration? = null

    fun getKey(): String {
        return instanceKey
    }

    fun loadImageList(albumID: String) {
        viewModelScope.launch(Dispatchers.IO){
            updateAllImageList(FirestoreImageService.getAlbumImages(albumID))
        }
    }

    private fun updateImageList(newImages: List<Image>, isUpdateFromServer: Boolean = false) {
        updateAllImageList(newImages)
        if (isUpdateFromServer) {
            unregisterAlbumImagesListener()
            if (_currentOrder.value == R.id.action_order_likes) {
                loadLikeCountForImages(newImages)
            }
        }

    }

    private fun loadLikeCountForImages(newImages: List<Image> = allImageList.value!!) {
        viewModelScope.launch(Dispatchers.IO) {
            updateAllImageList(FirestoreImageService.getLikesCountForImages(newImages))
        }
    }

    private fun updateAllImageList(newImages: List<Image>) {
        _allImageList.postValue(newImages)
        updateDisplayImageList(orderImageList(currentOrder.value!!, currentOrderDirection.value!!, filterImageList(currentFilter.value!!, newImages)))
    }

    fun registerAlbumImagesListener(albumID: String) {
        Log.d(TAG, "albumImagesListener: START")
        val updateEvent: (newAlbumImages: List<Image>, isUpdateFromServer: Boolean) -> Unit = { images, b ->
            updateImageList(images, b)
        }
        albumImagesListenerRegistration = FirestoreImageService.getAlbumImagesRegistration(albumID, updateEvent)
    }

    private fun unregisterAlbumImagesListener() {
        Log.d(TAG, "albumImagesListener: STOP")
        albumImagesListenerRegistration?.remove()
    }

    private fun updateDisplayImageList(newImages: List<Image>) {
        Log.d("ImagesDisplayViewModel", "New value for displayImageList (size: ${newImages.size}):  $newImages")
        _displayImageList.postValue(newImages)
        updateSectionList(newImages)
    }

    private fun updateSectionList(newImages: List<Image>) {
        val newSectionList: List<Section> =
            when (currentOrder.value) {
                R.id.action_order_date ->
                    newImages.groupBy { image: Image -> image.creationDate.toFormattedChatDateString() }
                R.id.action_order_album ->
                    newImages.groupBy { image: Image -> image.albumId }
                else -> return
            }.map { entry -> Section(entry.key, entry.value) }
        _displaySectionList.postValue(newSectionList)
    }

    fun applyFilter(filter: Int) {
        if (filter == currentFilter.value) return
        _currentFilter.value = filter
        updateDisplayImageList(orderImageList(currentOrder.value!!, currentOrderDirection.value!!, filterImageList(filter, allImageList.value!!)))
    }

    private fun filterImageList(filter: Int, newImages: List<Image>): List<Image> {
        return when (filter) {
            R.id.action_filter_all -> newImages
            R.id.action_filter_mine -> newImages.filter { img -> img.authorName == "Author 0" }
            else -> emptyList()
        }
    }

    fun applyOrder(order: Int = currentOrder.value!!, direction: Int = currentOrderDirection.value!!) {
        if (order == currentOrder.value && direction == currentOrderDirection.value) return
        _currentOrder.value = order
        _currentOrderDirection.value = direction
        updateDisplayImageList(orderImageList(order, direction, displayImageList.value!!))
    }

    private fun orderImageList(order: Int, direction: Int, newImages: List<Image>): List<Image> {
        val orderedImages = when (order) {
            R.id.action_order_date -> newImages.sortedBy { img -> img.creationDate }
            R.id.action_order_album -> newImages.sortedBy { img -> img.albumId }
            R.id.action_order_likes -> newImages.sortedBy { img -> img.likes }
            else -> emptyList()
        }
        return if (direction == R.id.action_order_ascending) orderedImages else orderedImages.asReversed()
    }

    fun shouldDisplaySections(): Boolean {
        return currentOrder.value == R.id.action_order_date || currentOrder.value == R.id.action_order_album
    }

}

