package uniovi.eii.shareit.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import uniovi.eii.shareit.R
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.model.UserAlbum
import uniovi.eii.shareit.model.repository.FirebaseAuthService
import uniovi.eii.shareit.model.repository.FirestoreUserService

class AlbumsDisplayViewModel : ViewModel() {

    companion object {
        private const val TAG = "AlbumsDisplayViewModel"
    }

    private var _albumList = emptyList<UserAlbum>()
    private val _displayAlbumList = MutableLiveData<List<UserAlbum>>(emptyList())
    val displayAlbumList: LiveData<List<UserAlbum>> = _displayAlbumList

    private val _currentOrder = MutableLiveData(R.id.action_order_creation_date)
    val currentOrder: LiveData<Int> = _currentOrder
    private val _currentOrderDirection = MutableLiveData(R.id.action_order_ascending)
    val currentOrderDirection: LiveData<Int> = _currentOrderDirection

    private val _showFilterTags = MutableLiveData(false)
    val showFilterTags: LiveData<Boolean> = _showFilterTags
    private var _currentFilterTags = emptyList<Album.Tags>()

    private var userAlbumsListenerRegistration: ListenerRegistration? = null

    fun getAlbumListIds(): List<String> {
        return _albumList.map { it.albumId }
    }

    private fun updateAlbumList(newAlbums: List<UserAlbum>) {
        _albumList = newAlbums
        updateDisplayAlbumList(orderAlbumList(currentOrder.value!!, currentOrderDirection.value!!, filterAlbumList(_currentFilterTags, newAlbums)))
    }

    private fun updateDisplayAlbumList(newAlbums: List<UserAlbum>) {
        _displayAlbumList.value = newAlbums
    }

    fun registerUserAlbumsListener() {
        Log.d(TAG, "userAlbumsListener: START")
        val updateEvent: (newUserAlbums: List<UserAlbum>) -> Unit = {
            updateAlbumList(it)
        }
        val userId = FirebaseAuthService.getCurrentUser()!!.uid
        userAlbumsListenerRegistration = FirestoreUserService.getUserAlbumsRegistration(userId, updateEvent)
    }

    fun unregisterUserAlbumsListener() {
        Log.d(TAG, "userAlbumsListener: STOP")
        userAlbumsListenerRegistration?.remove()
    }

    fun applyOrder(order: Int = currentOrder.value!!, direction: Int = currentOrderDirection.value!!): Boolean {
        if (order == currentOrder.value && direction == currentOrderDirection.value) return false
        _currentOrder.value = order
        _currentOrderDirection.value = direction
        updateDisplayAlbumList(orderAlbumList(order, direction, displayAlbumList.value!!))
        return true
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

    fun applyFilter(filterTags: List<Album.Tags>) {
        if (filterTags == _currentFilterTags) return
        _currentFilterTags = filterTags
        updateDisplayAlbumList(orderAlbumList(currentOrder.value!!, currentOrderDirection.value!!, filterAlbumList(filterTags, _albumList)))
    }

    private fun filterAlbumList(filterTags: List<Album.Tags>, newAlbums: List<UserAlbum>): List<UserAlbum> {
        return if (filterTags.isEmpty()) {
            newAlbums
        } else {
            val filterTagsSet = filterTags.toSet()
            newAlbums.filter { album -> album.tags.intersect(filterTagsSet).isNotEmpty() }
        }
    }

    fun toggleShowFilterTags(newValue: Boolean) {
        _showFilterTags.value = newValue
    }
}