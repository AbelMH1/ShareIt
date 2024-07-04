package uniovi.eii.shareit.view.identification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import uniovi.eii.shareit.databinding.FragmentLoginBinding
import uniovi.eii.shareit.viewModel.SignUpViewModel


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val loginViewModel =
            ViewModelProvider(this)[SignUpViewModel::class.java]

        binding.btSwitchToSignUp.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionNavLoginToNavSignup())
        }

        binding.btLogIn.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionNavLoginToNavHome())
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}