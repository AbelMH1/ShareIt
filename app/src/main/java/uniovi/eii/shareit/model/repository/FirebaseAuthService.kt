package uniovi.eii.shareit.model.repository

import android.net.Uri
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
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
                saveCreatedUserInFirestore(user!!.uid, email, email.substringBefore("@"))
                auth.signOut()
                SignUpResult(true)
            }
        } catch (e: Exception) {
            Log.w(TAG, "createUserWithEmail:failure", e)
            SignUpResult(firebaseError = e.message)
        }
    }

    private suspend fun saveCreatedUserInFirestore(userId: String, email: String, name: String) {
        val db = Firebase.firestore
        val user = User(
            userId,
            name,
            email.lowercase(Locale.getDefault()),
            FirebaseStorageService.getStorageReferenceStringForUser(userId),
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
                Log.d(TAG, "signInWithEmailAndPassword:success\nLogged User ID: ${user!!.uid}")
                LoginResult(true)
            }
        } catch (e: Exception) {
            Log.w(TAG, "signInWithEmail:failure", e)
            LoginResult(firebaseError = e.message)
        }
    }

    suspend fun signInWithGoogle(idToken: String): LoginResult {
        return try {
            with(auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()) {
                Log.d(TAG, "signInWithGoogle:success\nLogged User ID: ${user!!.uid}")
                if (additionalUserInfo?.isNewUser == true) {
                    Log.d(TAG, "signInWithGoogle: Is New User")
                    saveCreatedUserInFirestore(user!!.uid, user!!.email!!, user!!.displayName!!)
                    if (user!!.photoUrl != null) {
                        val imageFile = FirebaseStorageService.downloadImageToTempFile(user!!.photoUrl!!.toString())
                        if (imageFile != null) {
                            FirebaseStorageService.uploadUserImage(user!!.uid, Uri.fromFile(imageFile))
                            imageFile.delete()
                        }
                    }
                    Log.d(TAG, "signInWithGoogle: User image: ${user!!.photoUrl}")
                }
                LoginResult(true)
            }
        } catch (e: Exception) {
            Log.w(TAG, "signInWithGoogle:failure", e)
            LoginResult(firebaseError = e.message)
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun signOut(): FirebaseUser? {
        auth.signOut()
        val credentialManager = CredentialManager.create(FirebaseApp.getInstance().applicationContext)
        try {
            val clearRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearRequest)
        } catch (e: ClearCredentialException) {
            Log.e(TAG, "Couldn't clear user credentials: ${e.localizedMessage}")
        }
        return null
    }

}