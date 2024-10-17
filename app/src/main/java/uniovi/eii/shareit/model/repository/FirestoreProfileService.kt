package uniovi.eii.shareit.model.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import uniovi.eii.shareit.model.User
import uniovi.eii.shareit.model.realTimeListener.UserDataListener
import uniovi.eii.shareit.viewModel.ProfileViewModel.ValidationResult

object FirestoreProfileService {

    private const val TAG = "FirestoreProfileService"
    private val auth = Firebase.auth
    private var currentUserData: User = User()

    fun getCurrentUserData(): User? {
        return if (auth.currentUser?.uid == currentUserData.userId) currentUserData
        else {
            Log.e(TAG, "getCurrentUserData:The current user does not match the Auth user\n" +
                    "Auth: ${auth.currentUser?.email}\n" +
                    "Current: ${currentUserData.userId}")
            null
        }
    }

    private fun updateLocalCurrentUserData(newUserData: User) {
        Log.d(TAG, "updateCurrentUserData: $newUserData")
        currentUserData = newUserData
    }

    fun getUserDataRegistration(
        userId: String, updateVMEvent: (newData: User) -> Unit
    ): ListenerRegistration {
        val db = Firebase.firestore
        val updateServiceEvent: (newData: User) -> Unit = {
            updateLocalCurrentUserData(it)
            updateVMEvent(it)
        }
        return db.collection("users").document(userId)
            .addSnapshotListener(UserDataListener(updateServiceEvent))
    }

    suspend fun updateCurrentUserData(
        userId: String, newUserData: HashMap<String, Any>
    ): ValidationResult {
        val db = Firebase.firestore
        return try {
            with(
                db.collection("users").document(userId).update(newUserData).await()
            ) {
                Log.d(TAG, "updateCurrentUserData:success")
                // TODO: Actualizar campos en todos los UserAlbums donde sea creador
                // TODO: Actualizar campos en todos los álbumes donde sea creador
                // TODO: Actualizar campos en todos los álbumes donde sea participante
                ValidationResult(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateCurrentUserData:failure", e)
            ValidationResult(firestoreError = e.message)
        }
    }

}