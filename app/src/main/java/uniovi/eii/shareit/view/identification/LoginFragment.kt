package uniovi.eii.shareit.view.identification

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import uniovi.eii.shareit.BuildConfig
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentLoginBinding
import uniovi.eii.shareit.view.MainActivity.ErrorCleaningTextWatcher
import uniovi.eii.shareit.viewModel.LoginViewModel
import uniovi.eii.shareit.viewModel.MainViewModel


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.btSwitchToSignUp.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionNavLoginToNavSignup())
        }

        binding.btLogIn.setOnClickListener {
            enableForm(false)
            viewModel.attemptLogin(
                binding.emailEditText.text?.toString() ?: "",
                binding.passwordEditText.text?.toString() ?: ""
            )
        }

        binding.emailEditText.setOnFocusChangeListener { _, hasFocus ->
            updateHint(binding.emailLayout, hasFocus, R.string.label_email, R.string.placeholder_email)
        }
        binding.passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            updateHint(binding.passwordLayout, hasFocus, R.string.label_password, R.string.placeholder_password)
        }
        binding.emailEditText.addTextChangedListener(ErrorCleaningTextWatcher(binding.emailLayout))
        binding.passwordEditText.addTextChangedListener(ErrorCleaningTextWatcher(binding.passwordLayout))

        binding.btGoogleLogin.setOnClickListener {
            handleLoginWithGoogle()
        }

        viewModel.loginAttempt.observe(viewLifecycleOwner) {
            if (it.isUserLogged) {
                findNavController().navigate(LoginFragmentDirections.actionNavLoginToNavHome())
                Toast.makeText(context, getString(R.string.toast_successful_login), Toast.LENGTH_SHORT).show()
                val mainViewModel: MainViewModel by activityViewModels()
                mainViewModel.logIn()
            } else {
                enableForm(true)
                updateErrors(it.emailError, it.passwordError, it.firebaseError)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBottomSheetLogin()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun enableForm(enabled: Boolean) {
        binding.emailLayout.isEnabled = enabled
        binding.passwordLayout.isEnabled = enabled
        binding.btForgotPassword.isEnabled = enabled
        binding.btLogIn.isEnabled = enabled
        binding.btLogIn.text = if (enabled) getString(R.string.btn_logIn) else ""
        binding.progressBar.isVisible = !enabled
        binding.btGoogleLogin.isEnabled = enabled
        binding.btSwitchToSignUp.isEnabled = enabled
    }

    private fun updateErrors(emailError: Int?, passwordError: Int?, firebaseError: String?) {
        if (emailError != null) {
            binding.emailLayout.error = resources.getString(emailError)
            binding.emailEditText.requestFocus()
        }
        if (passwordError != null) {
            binding.passwordLayout.error = resources.getString(passwordError)
            binding.passwordEditText.requestFocus()
        }
        if (firebaseError != null) {
            Toast.makeText(context, "Authentication failed: $firebaseError", Toast.LENGTH_LONG,).show()
        }
    }

    private fun updateHint(etLayout: TextInputLayout, hasFocus: Boolean, label: Int, placeholder: Int) {
        if (hasFocus || etLayout.editText?.text?.isNotEmpty() == true)
            etLayout.hint = resources.getString(label)
        else etLayout.hint = resources.getString(placeholder)
    }

    private fun handleBottomSheetLogin() {
        // Instantiate a Google sign-in request
        val googleIdOption = GetGoogleIdOption.Builder()
            // Your server's client ID, not your Android client ID.
            .setServerClientId(BuildConfig.ServerClientId)
            // If the user has previously signed in, automatically select the account.
            .setAutoSelectEnabled(true)
            // Only show accounts previously used to sign in.
            .setFilterByAuthorizedAccounts(true)
            .build()

        // Create the Credential Manager request
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager. create(requireContext())

        lifecycleScope.launch {
            try {
                val response = credentialManager.getCredential(
                    // Important: use an Activity context to ensure that the system credential
                    // selector ui is launched within the same activity stack to avoid undefined UI
                    // transition behavior.
                    context = requireActivity(),
                    request = request
                )
                viewModel.attemptLoginWithGoogle(response.credential)
            } catch (e: GetCredentialException) {
                Log.e("LoginFragment","Error al obtener la credencial: ${e.localizedMessage}")
            }
        }
    }

    private fun handleLoginWithGoogle() {
        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(BuildConfig.ServerClientId)
            .setNonce("nonce")
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val credentialManager = CredentialManager. create(requireContext())

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = requireActivity(),
                    request = request
                )
                enableForm(false)
                viewModel.attemptLoginWithGoogle(result.credential)
            } catch (e: GetCredentialException) {
                Log.e("LoginFragment","Error al obtener la credencial: ${e.localizedMessage}")
                Toast.makeText(requireContext(), "Error al obtener las credenciales de Google", Toast.LENGTH_LONG).show()
            }
        }
    }
}