package uniovi.eii.shareit.model.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import uniovi.eii.shareit.viewModel.LoginViewModel.LoginResult
import uniovi.eii.shareit.viewModel.SignUpViewModel.SignUpResult

object FirebaseAuthService {

    private const val TAG = "FirebaseAuthService"
    private val auth = Firebase.auth
    suspend fun createUserWithEmailAndPassword(email: String, password: String): SignUpResult {
        return try {
            with(auth.createUserWithEmailAndPassword(email, password).await()) {
                Log.d(TAG, "createUserWithEmail:success\nCreated User ID: ${user?.uid}")
                auth.signOut()
                SignUpResult(true)
            }
        } catch (e: Exception) {
            Log.w(TAG, "createUserWithEmail:failure", e)
            SignUpResult(firebaseError = e.message)
        }
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): LoginResult {
        return try {
            with(auth.signInWithEmailAndPassword(email, password).await()) {
                Log.d(TAG, "signInWithEmail:success\nLoged User ID: ${user?.uid}")
                LoginResult(true)
            }
        } catch (e: Exception) {
            Log.w(TAG, "signInWithEmail:failure", e)
            LoginResult(firebaseError = e.message)
        }
    }

}