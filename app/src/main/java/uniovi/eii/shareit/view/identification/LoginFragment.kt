package uniovi.eii.shareit.view.identification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentLoginBinding
import uniovi.eii.shareit.view.MainActivity.ErrorCleaningTextWatcher
import uniovi.eii.shareit.viewModel.LoginViewModel


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by activityViewModels()

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
                binding.passwordEditText.text?.toString() ?: "",
                binding.checkboxRememberMe.isChecked
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

        viewModel.loginAttempt.observe(viewLifecycleOwner) {
            if (it.isUserLogged) {
                findNavController().navigate(LoginFragmentDirections.actionNavLoginToNavHome())
                Toast.makeText(context, getString(R.string.toast_successful_login), Toast.LENGTH_SHORT).show()
            } else {
                enableForm(true)
                updateErrors(it.emailError, it.passwordError)
            }
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

    private fun updateErrors(emailError: Int?, passwordError: Int?) {
        if (emailError != null) {
            binding.emailLayout.error = resources.getString(emailError)
            binding.emailEditText.requestFocus()
        }
        if (passwordError != null) {
            binding.passwordLayout.error = resources.getString(passwordError)
            binding.passwordEditText.requestFocus()
        }
    }

    private fun updateHint(etLayout: TextInputLayout, hasFocus: Boolean, label: Int, placeholder: Int) {
        if (hasFocus || etLayout.editText?.text?.isNotEmpty() == true)
            etLayout.hint = resources.getString(label)
        else etLayout.hint = resources.getString(placeholder)
    }

}