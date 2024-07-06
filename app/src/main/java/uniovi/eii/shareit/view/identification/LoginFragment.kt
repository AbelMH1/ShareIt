package uniovi.eii.shareit.view.identification

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentLoginBinding
import uniovi.eii.shareit.viewModel.SignUpViewModel


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.btSwitchToSignUp.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionNavLoginToNavSignup())
        }

        binding.btLogIn.setOnClickListener {
            enableForm(false)
            val email = binding.emailEditText.text?.toString() ?: ""
            val password = binding.passwordEditText.text?.toString() ?: ""
            val rememberMe = binding.checkboxRememberMe.isChecked
            if (checkValidData(email, password)){
                viewModel.attemptLogin(email, password, rememberMe)
            } else enableForm(true)
        }

        binding.emailEditText.addTextChangedListener(ValidationTextWatcher(binding.emailLayout))
        binding.passwordEditText.addTextChangedListener(ValidationTextWatcher(binding.passwordLayout))

        viewModel.isUserLogged.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(context, getString(R.string.toast_successful_login), Toast.LENGTH_SHORT).show()
                findNavController().navigate(LoginFragmentDirections.actionNavLoginToNavHome())
            } else enableForm(true)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun enableForm(enabled: Boolean) {
        binding.emailLayout.isEnabled = enabled
        binding.passwordLayout.isEnabled = enabled
        binding.checkboxRememberMe.isEnabled = enabled
        binding.btForgotPassword.isEnabled = enabled
        binding.btLogIn.isEnabled = enabled
        binding.btLogIn.text = if (enabled) getString(R.string.btn_logIn) else ""
        binding.progressBar.isVisible = !enabled
        binding.btGoogleLogin.isEnabled = enabled
        binding.btFacebookLogin.isEnabled = enabled
        binding.btTwitterLogin.isEnabled = enabled
        binding.btSwitchToSignUp.isEnabled = enabled
    }

    private fun checkValidData(email: String, password: String): Boolean {
        if (email.isBlank()) {
            binding.emailLayout.error = resources.getString(R.string.err_empty_field)
            binding.emailEditText.requestFocus()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = resources.getString(R.string.err_not_an_email)
            binding.emailEditText.requestFocus()
            return false
        }
        if (password.isBlank()) {
            binding.passwordLayout.error = resources.getString(R.string.err_empty_field)
            binding.passwordEditText.requestFocus()
            return false
        }
        return true
    }

    private class ValidationTextWatcher (private val etLayout: TextInputLayout) :
        TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {
            etLayout.error = null
        }
    }

}