package uniovi.eii.shareit.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.model.Album.Visibility
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.Participant.Role
import uniovi.eii.shareit.model.repository.FirebaseAuthService
import uniovi.eii.shareit.model.repository.FirestoreAlbumService
import uniovi.eii.shareit.model.repository.FirestoreUserService

class AlbumViewModel : ViewModel() {

    companion object {
        private const val TAG = "AlbumViewModel"
    }

    private val _album = MutableLiveData(Album())
    val album: LiveData<Album> = _album
    private val _currentUserRole = MutableLiveData(Role.GUEST)
    val currentUserRole: LiveData<Role> = _currentUserRole

    private var albumDataListenerRegistration: ListenerRegistration? = null
    private var userRoleListenerRegistration: ListenerRegistration? = null

    init {
        Log.d(TAG, "START")
    }

    override fun onCleared() {
        super.onCleared()
        unregisterUserRoleListener()
        unregisterAlbumDataListener()
        Log.d(TAG, "END")
    }

    fun getAlbumInfo(): Album {
        return album.value?.copy() ?: Album()
    }

    private fun updateAlbumData(newAlbumData: Album) {
        _album.postValue(newAlbumData)
    }

    private fun updateCurrentUserRole(newRole: Role) {
        Log.d("updateCurrentUserRole", newRole.name)
        _currentUserRole.postValue(newRole)
    }

    fun registerAlbumDataListener(
        albumId: String
    ) {
        if (albumDataListenerRegistration != null) return
        Log.d(TAG, "albumDataListener: START")
        albumDataListenerRegistration = FirestoreAlbumService.getAlbumDataRegistration(albumId, ::updateAlbumData)
    }

    private fun unregisterAlbumDataListener() {
        Log.d(TAG, "albumDataListener: STOP")
        albumDataListenerRegistration?.remove()
    }

    fun registerUserRoleListener(
        albumId: String
    ) {
        if (userRoleListenerRegistration != null) return
        Log.d(TAG, "userRoleListener: START")
        val updateEvent: (newData: Role) -> Unit = {
            updateCurrentUserRole(it)
        }
        val userId = FirebaseAuthService.getCurrentUser()!!.uid
        userRoleListenerRegistration = FirestoreAlbumService.getCurrentUserRoleInAlbumRegistration(albumId, userId, updateEvent)
    }

    private fun unregisterUserRoleListener() {
        Log.d(TAG, "userRoleListener: STOP")
        userRoleListenerRegistration?.remove()
    }

    fun deleteUserAlbum(albumID: String) {
        val currentUserId = FirestoreUserService.getCurrentUserData()?.userId ?: ""
        viewModelScope.launch(Dispatchers.IO) {
            FirestoreAlbumService.eliminateUserAlbumFromParticipant(albumID, currentUserId)
        }
    }

    fun isCurrentUserOwner(): Boolean {
        return _currentUserRole.value == Role.OWNER
    }

    fun isCurrentUserParticipant(): Boolean {
        return _currentUserRole.value != Role.NONE
    }

    fun isAlbumPrivate(): Boolean {
        return _album.value!!.visibility == Visibility.PRIVATE
    }

    fun isAlbumPublic(): Boolean {
        return _album.value!!.visibility == Visibility.PUBLIC
    }

    fun hasChatSeePermission()      = hasPermission { role -> chatPermissionFor(role)?.canSee  }
    fun hasChatCommentPermission()  = hasPermission { role -> chatPermissionFor(role)?.canComment  }
    fun hasImagesAddPermission()    = hasPermission { role -> imagePermissionFor(role)?.canAdd }
    fun hasImagesVotePermission()   = hasPermission { role -> imagePermissionFor(role)?.canVote }

    private fun hasPermission(check: Album.(Role) -> Boolean?): Boolean {
        val role  = _currentUserRole.value
        val album = _album.value               // LiveData puede ser null
        return when (role) {
            Role.OWNER  -> true                // El owner siempre puede
            Role.MEMBER,
            Role.GUEST  -> album?.check(role) ?: false
            else        -> false               // rol indefinido
        }
    }

    fun canDeleteImage(image: Image): Boolean {
        val userId = FirebaseAuthService.getCurrentUser()!!.uid
        return _currentUserRole.value == Role.OWNER || userId == image.authorId
    }
}