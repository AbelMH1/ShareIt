package uniovi.eii.shareit.view.profile

import android.Manifest.permission.CAMERA
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentProfileBinding
import uniovi.eii.shareit.model.User
import uniovi.eii.shareit.utils.compressImage
import uniovi.eii.shareit.utils.createTempImageFile
import uniovi.eii.shareit.utils.getRequiredGalleryPermissions
import uniovi.eii.shareit.utils.getSecureUriForFile
import uniovi.eii.shareit.utils.hasCameraPermission
import uniovi.eii.shareit.utils.hasGalleryPermission
import uniovi.eii.shareit.utils.loadCircularImageIntoView
import uniovi.eii.shareit.utils.loadProfileImageIntoView
import uniovi.eii.shareit.utils.registerCameraPicker
import uniovi.eii.shareit.utils.registerGalleryPicker
import uniovi.eii.shareit.utils.registerPermissionRequest
import uniovi.eii.shareit.utils.registerPermissionsRequest
import uniovi.eii.shareit.view.MainActivity.ErrorCleaningTextWatcher
import uniovi.eii.shareit.viewModel.MainViewModel
import uniovi.eii.shareit.viewModel.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by activityViewModels()
    private var imageUri: Uri? = null

    // Lanzador para seleccionar la imagen de la galería
    private val pickImageLauncher = registerGalleryPicker(::processImage)
    // Lanzador para tomar una foto con la cámara
    private val captureImageLauncher = registerCameraPicker(::processImage)
    // Lanzador para solicitar el permiso de lectura del almacenamiento
    private val requestPermissions = registerPermissionsRequest(::openGallery, R.string.error_gallery_permission_denied)
    // Lanzador para solicitar el permiso de la cámara
    private val requestCameraPermission = registerPermissionRequest(::takePicture, R.string.error_camera_permission_denied)


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
            viewModel.attemptDataUpdate(
                binding.etUser.text?.toString() ?: "",
                binding.etEmail.text?.toString() ?: "",
                imageUri
            )
        }

        binding.imgProfile.setOnClickListener {
            showPickResourceDialog()
        }
        binding.btEditImgProfile.setOnClickListener {
            showPickResourceDialog()
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
        lifecycleScope.launch {
            requireContext().loadProfileImageIntoView(
                user.imagePath,
                user.lastUpdatedImage.time,
                binding.imgProfile,
            )
        }
    }

    private fun enableEdition(enable: Boolean) {
        binding.btEditData.isVisible = !enable
        binding.btSaveData.isVisible = enable
        binding.outlinedTextFieldUser.isEnabled = enable
        binding.btEditImgProfile.isVisible = enable
        binding.imgProfile.isClickable = enable
    }

    private fun updateErrors(userError: Int?) {
        if (userError != null) {
            binding.outlinedTextFieldUser.error = resources.getString(userError)
            binding.etUser.requestFocus()
        }
    }

    private fun showPickResourceDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_select_source_menu, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.select_source_title))
            .setView(view)
            .create()

        // Configura los clics para cada opción, cerrando el diálogo antes de llamar a la acción correspondiente
        view.findViewById<View>(R.id.option_camera).setOnClickListener {
            dialog.dismiss()
            checkCameraPermissions()
        }
        view.findViewById<View>(R.id.option_gallery).setOnClickListener {
            dialog.dismiss()
            checkGalleryPermissions()
        }

        dialog.show()
    }

    private fun checkGalleryPermissions() {
        if (requireContext().hasGalleryPermission()) {
            openGallery()
        } else {
            requestPermissions.launch(getRequiredGalleryPermissions())
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun checkCameraPermissions() {
        if (requireContext().hasCameraPermission()) {
            takePicture()
        } else {
            requestCameraPermission.launch(CAMERA)
        }
    }

    private fun takePicture() {
        val image = requireContext().createTempImageFile("IMG_${System.currentTimeMillis()}")
        imageUri = requireContext().getSecureUriForFile(image)
        captureImageLauncher.launch(imageUri)
    }

    private fun processImage(uri: Uri? = imageUri) {
        Log.d("Camera", "Selected URI: $uri")
        lifecycleScope.launch {
            val context = requireContext()
            val outputFile = context.createTempImageFile()
            val processed = context.compressImage(uri!!, outputFile)
            if (processed) {
                imageUri = context.getSecureUriForFile(outputFile)
                context.loadCircularImageIntoView(
                    imageUri!!,
                    binding.imgProfile,
                    R.drawable.ic_person_24,
                    R.drawable.ic_person_24)
            } else {
                Toast.makeText(context, "Error picking image", Toast.LENGTH_SHORT).show()
            }
        }
    }



}