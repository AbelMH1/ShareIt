package uniovi.eii.shareit.model.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import uniovi.eii.shareit.model.User
import uniovi.eii.shareit.viewModel.LoginViewModel.LoginResult
import uniovi.eii.shareit.viewModel.SignUpViewModel.SignUpResult
import java.util.Locale

object FirebaseAuthService {

    private const val TAG = "FirebaseAuthService"
    private val auth = Firebase.auth
    suspend fun createUserWithEmailAndPassword(email: String, password: String): SignUpResult {
        return try {
            with(auth.createUserWithEmailAndPassword(email, password).await()) {
                Log.d(TAG, "createUserWithEmail:success\nCreated User ID: ${user!!.uid}")
                saveCreatedUserInFirestore(user!!.uid, email)
                auth.signOut()
                SignUpResult(true)
            }
        } catch (e: Exception) {
            Log.w(TAG, "createUserWithEmail:failure", e)
            SignUpResult(firebaseError = e.message)
        }
    }

    private suspend fun saveCreatedUserInFirestore(userId: String, email: String) {
        val db = Firebase.firestore
        val user = User(
            userId,
            email.substringBefore("@"),
            email.lowercase(Locale.getDefault())
        )
        db.collection("users")
            .document(userId)
            .set(user).addOnCompleteListener {
                if (it.isSuccessful)
                    Log.d(TAG, "saveCreatedUserInFirestore:success")
                else
                    Log.e(TAG, "saveCreatedUserInFirestore:failure", it.exception)
            }.await()
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): LoginResult {
        return try {
            with(auth.signInWithEmailAndPassword(email, password).await()) {
                Log.d(TAG, "signInWithEmail:success\nLoged User ID: ${user!!.uid}")
                LoginResult(true)
            }
        } catch (e: Exception) {
            Log.w(TAG, "signInWithEmail:failure", e)
            LoginResult(firebaseError = e.message)
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun signOut(): FirebaseUser? {
        auth.signOut()
        return null
    }

}