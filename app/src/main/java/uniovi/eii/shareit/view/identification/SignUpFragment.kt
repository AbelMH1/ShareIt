package uniovi.eii.shareit.view.identification

import android.os.Bundle
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
import uniovi.eii.shareit.databinding.FragmentSignupBinding
import uniovi.eii.shareit.view.MainActivity.ErrorCleaningTextWatcher
import uniovi.eii.shareit.viewModel.SignUpViewModel


class SignUpFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)

        binding.btSwitchToLogin.setOnClickListener {
            findNavController().navigate(SignUpFragmentDirections.actionNavSignupToNavLogin())
        }

        binding.btSignUp.setOnClickListener {
            enableForm(false)
            viewModel.attemptSignUp(
                binding.emailEditText.text?.toString() ?: "",
                binding.passwordEditText.text?.toString() ?: "",
                binding.passwordRepeatEditText.text?.toString() ?: ""
            )
        }

        binding.emailEditText.setOnFocusChangeListener { _, hasFocus ->
            updateHint(binding.emailLayout, hasFocus, R.string.label_email, R.string.placeholder_email)
        }
        binding.passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            updateHint(binding.passwordLayout, hasFocus, R.string.label_password, R.string.placeholder_password)
        }
        binding.passwordRepeatEditText.setOnFocusChangeListener { _, hasFocus ->
            updateHint(binding.passwordRepeatLayout, hasFocus, R.string.label_password_repeat, R.string.placeholder_password_repeat)
        }

        binding.emailEditText.addTextChangedListener(ErrorCleaningTextWatcher(binding.emailLayout))
        binding.passwordEditText.addTextChangedListener(ErrorCleaningTextWatcher(binding.passwordLayout))
        binding.passwordRepeatEditText.addTextChangedListener(ErrorCleaningTextWatcher(binding.passwordRepeatLayout))

        viewModel.signUpAttempt.observe(viewLifecycleOwner) {
            if (it.isUserCreated) {
                Toast.makeText(context, getString(R.string.toast_successful_signup), Toast.LENGTH_SHORT).show()
                findNavController().navigate(SignUpFragmentDirections.actionNavSignupToNavLogin())
            } else {
                enableForm(true)
                updateErrors(it.emailError, it.passwordError, it.passwordRepeatError)
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
        binding.passwordRepeatLayout.isEnabled = enabled
        binding.btSignUp.isEnabled = enabled
        binding.btSignUp.text = if (enabled) getString(R.string.btn_signUp) else ""
        binding.progressBar.isVisible = !enabled
        binding.btSwitchToLogin.isEnabled = enabled
    }

    private fun updateErrors(emailError: Int?, passwordError: Int?, passwordRepeatError: Int?) {
        if (emailError != null) {
            binding.emailLayout.error = resources.getString(emailError)
            binding.emailEditText.requestFocus()
        }
        if (passwordError != null) {
            binding.passwordLayout.error = resources.getString(passwordError)
            binding.passwordEditText.requestFocus()
        }
        if (passwordRepeatError != null) {
            binding.passwordRepeatLayout.error = resources.getString(passwordRepeatError)
            binding.passwordRepeatEditText.requestFocus()
        }
    }

    private fun updateHint(etLayout: TextInputLayout, hasFocus: Boolean, label: Int, placeholder: Int) {
        if (hasFocus || etLayout.editText?.text?.isNotEmpty() == true)
            etLayout.hint = resources.getString(label)
        else etLayout.hint = resources.getString(placeholder)
    }
}