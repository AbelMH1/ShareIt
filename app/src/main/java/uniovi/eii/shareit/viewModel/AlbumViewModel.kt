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
import uniovi.eii.shareit.model.Album.ChatPermission
import uniovi.eii.shareit.model.Album.ImagePermission
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

    fun getAlbumInfo(): Album {
        return album.value?.copy() ?: Album()
    }

    fun updateCurrentAlbum(albumID: String, albumName: String, albumCoverImage: String) {
        _album.value = Album(albumID, name = albumName, coverImage = albumCoverImage)
    }

    private fun updateAlbumData(newAlbumData: Album) {
        _album.postValue(newAlbumData)
    }

    private fun updateCurrentUserRole(newRole: Role) {
        Log.d("updateCurrentUserRole", newRole.name)
        _currentUserRole.postValue(newRole)
    }

    fun registerAlbumDataListener(
        albumId: String,
        updateAlbumFunc: (Album) -> Unit = {}
    ) {
        Log.d(TAG, "albumDataListener: START")
        val updateEvent: (newData: Album) -> Unit = {
            updateAlbumData(it)
            updateAlbumFunc(it)
        }
        albumDataListenerRegistration = FirestoreAlbumService.getAlbumDataRegistration(albumId, updateEvent)
    }

    fun unregisterAlbumDataListener() {
        Log.d(TAG, "albumDataListener: STOP")
        albumDataListenerRegistration?.remove()
    }

    fun registerUserRoleListener(
        albumId: String
    ) {
        Log.d(TAG, "userRoleListener: START")
        val updateEvent: (newData: Role) -> Unit = {
            updateCurrentUserRole(it)
        }
        val userId = FirebaseAuthService.getCurrentUser()!!.uid
        userRoleListenerRegistration = FirestoreAlbumService.getCurrentUserRoleInAlbumRegistration(albumId, userId, updateEvent)
    }

    fun unregisterUserRoleListener() {
        Log.d(TAG, "userRoleListener: STOP")
        userRoleListenerRegistration?.remove()
        resetCurrentUserRole()
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

    private fun resetCurrentUserRole() {
        _currentUserRole.postValue(Role.GUEST)
    }

    fun isAlbumPrivate(): Boolean {
        return _album.value!!.visibility == Album.PRIVATE
    }

    fun hasChatSeePermission(): Boolean {
        when (_currentUserRole.value) {
            Role.OWNER -> return true
            Role.MEMBER -> {
                val chatPermission = _album.value!!.membersChatPermission
                return chatPermission == ChatPermission.SEE || chatPermission == ChatPermission.COMMENT
            }
            Role.GUEST -> {
                val chatPermission = _album.value!!.guestsChatPermission
                return chatPermission == ChatPermission.SEE || chatPermission == ChatPermission.COMMENT
            }
            else -> return false
        }
    }

    fun hasChatCommentPermission(): Boolean {
        when (_currentUserRole.value) {
            Role.OWNER -> return true
            Role.MEMBER -> {
                val chatPermission = _album.value!!.membersChatPermission
                return chatPermission == ChatPermission.COMMENT
            }
            Role.GUEST -> {
                val chatPermission = _album.value!!.guestsChatPermission
                return chatPermission == ChatPermission.COMMENT
            }
            else -> return false
        }
    }

    fun hasImagesAddPermission(): Boolean {
        when (_currentUserRole.value) {
            Role.OWNER -> return true
            Role.MEMBER -> {
                val imagesPermission = _album.value!!.membersImagesPermission
                return imagesPermission == ImagePermission.ADD
            }
            Role.GUEST -> {
                val imagesPermission = _album.value!!.guestsImagesPermission
                return imagesPermission == ImagePermission.ADD
            }
            else -> return false
        }
    }
}