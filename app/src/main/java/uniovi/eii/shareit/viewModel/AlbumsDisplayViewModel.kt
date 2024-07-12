package uniovi.eii.shareit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uniovi.eii.shareit.R
import uniovi.eii.shareit.model.UserAlbum
import uniovi.eii.shareit.view.album.placeholder.PlaceholderContent

class AlbumsDisplayViewModel : ViewModel() {

    private val _albumList = MutableLiveData<List<UserAlbum>>(emptyList())
    val albumList: LiveData<List<UserAlbum>> = _albumList
    private val _displayAlbumList = MutableLiveData<List<UserAlbum>>(emptyList())
    val displayAlbumList: LiveData<List<UserAlbum>> = _displayAlbumList

    private val _currentOrder = MutableLiveData(R.id.action_order_creation_date)
    val currentOrder: LiveData<Int> = _currentOrder
    private val _currentOrderDirection = MutableLiveData(R.id.action_order_ascending)
    val currentOrderDirection: LiveData<Int> = _currentOrderDirection

    init {
        updateAlbumList(PlaceholderContent.getAlbumList(25))
    }

    fun updateAlbumList(newAlbums: List<UserAlbum>) {
        _albumList.value = newAlbums
        updateDisplayAlbumList(orderAlbumList(currentOrder.value!!, currentOrderDirection.value!!, newAlbums))
    }

    fun updateDisplayAlbumList(newAlbums: List<UserAlbum>) {
        _displayAlbumList.value = newAlbums
    }

    fun applyOrder(order: Int = currentOrder.value!!, direction: Int = currentOrderDirection.value!!) {
        if (order == currentOrder.value && direction == currentOrderDirection.value) return
        _currentOrder.value = order
        _currentOrderDirection.value = direction
        updateDisplayAlbumList(orderAlbumList(order, direction, displayAlbumList.value!!))
    }

    private fun orderAlbumList(order: Int, direction: Int, newAlbums: List<UserAlbum>): List<UserAlbum> {
        val orderedImages = when (order) {
            R.id.action_order_creation_date -> newAlbums.sortedBy { album -> album.creationDate }
            R.id.action_order_name -> newAlbums.sortedBy { album -> album.name }
            R.id.action_order_last_update -> newAlbums.sortedBy { album -> album.lastUpdate }
            else -> emptyList()
        }
        return if (direction == R.id.action_order_ascending) orderedImages else orderedImages.asReversed()
    }
}