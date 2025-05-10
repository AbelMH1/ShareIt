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

    fun hasImagesVotePermission(): Boolean {
        when (_currentUserRole.value) {
            Role.OWNER -> return true
            Role.MEMBER -> {
                val imagesPermission = _album.value!!.membersImagesPermission
                return imagesPermission == ImagePermission.VOTE || imagesPermission == ImagePermission.ADD
            }
            Role.GUEST -> {
                val imagesPermission = _album.value!!.guestsImagesPermission
                return imagesPermission == ImagePermission.VOTE || imagesPermission == ImagePermission.ADD
            }
            else -> return false
        }
    }

    fun canDeleteImage(image: Image): Boolean {
        val userId = FirebaseAuthService.getCurrentUser()!!.uid
        return _currentUserRole.value == Role.OWNER || userId == image.authorId
    }
}