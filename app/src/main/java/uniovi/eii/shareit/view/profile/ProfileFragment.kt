package uniovi.eii.shareit.view.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentProfileBinding
import uniovi.eii.shareit.model.User
import uniovi.eii.shareit.view.MainActivity.ErrorCleaningTextWatcher
import uniovi.eii.shareit.viewModel.MainViewModel
import uniovi.eii.shareit.viewModel.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.btEditData.setOnClickListener {
            enableEdition(true)
        }

        binding.btSaveData.setOnClickListener {
            enableEdition(false)
            viewModel.updateData(
                binding.etUser.text?.toString() ?: "",
                binding.etEmail.text?.toString() ?: "",
                ""
            )
        }

        binding.imgProfile.setOnClickListener {
            Toast.makeText(context, "Select new image", Toast.LENGTH_SHORT).show()
        }

        binding.btCloseSession.setOnClickListener {
            val mainViewModel: MainViewModel by activityViewModels()
            mainViewModel.logOut()
            Toast.makeText(context, getString(R.string.toast_successful_logout), Toast.LENGTH_SHORT).show()
        }

        binding.btExit.setOnClickListener {
            finishAffinity(requireActivity())
        }

        binding.etUser.addTextChangedListener(ErrorCleaningTextWatcher(binding.outlinedTextFieldUser))

        viewModel.currentUser.observe(viewLifecycleOwner) {
            updateUI(it)
        }

        viewModel.dataValidation.observe(viewLifecycleOwner) {
            if (!it.isDataValid) {
                enableEdition(true)
                updateErrors(it.userError)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.wipeErrors()
        enableEdition(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI(user: User) {
        binding.etUser.setText(user.name)
        binding.etEmail.setText(user.email)
        user.imagePath // TODO: Cargar imagen
    }

    private fun enableEdition(enable: Boolean) {
        binding.btEditData.isVisible = !enable
        binding.btSaveData.isVisible = enable
        binding.outlinedTextFieldUser.isEnabled = enable
        binding.imgEditImgProfile.isVisible = enable
        binding.imgProfile.isClickable = enable
    }

    private fun updateErrors(userError: Int?) {
        if (userError != null) {
            binding.outlinedTextFieldUser.error = resources.getString(userError)
            binding.etUser.requestFocus()
        }
    }

}