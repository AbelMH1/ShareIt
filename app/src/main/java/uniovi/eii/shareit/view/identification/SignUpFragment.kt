package uniovi.eii.shareit.view.identification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import uniovi.eii.shareit.databinding.FragmentSignupBinding
import uniovi.eii.shareit.viewModel.SignUpViewModel


class SignUpFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        val signUpViewModel =
            ViewModelProvider(this)[SignUpViewModel::class.java]

        binding.btSignUp.setOnClickListener{
            findNavController().navigate(SignUpFragmentDirections.actionNavSignupToNavHome())
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}