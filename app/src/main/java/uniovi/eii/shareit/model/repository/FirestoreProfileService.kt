package uniovi.eii.shareit.model.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import uniovi.eii.shareit.viewModel.ProfileViewModel.ValidationResult

object FirestoreProfileService {

    private const val TAG = "FirestoreProfileService"

    fun getUserDataRegistration(
        userId: String, listener: EventListener<DocumentSnapshot>
    ): ListenerRegistration {
        val db = Firebase.firestore
        return db.collection("users").document(userId).addSnapshotListener(listener)
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
                ValidationResult(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateCurrentUserData:failure", e)
            ValidationResult(firestoreError = e.message)
        }
    }

}