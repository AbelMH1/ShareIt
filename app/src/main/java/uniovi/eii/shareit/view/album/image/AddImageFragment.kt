package uniovi.eii.shareit.view.album.image

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAddImageBinding
import uniovi.eii.shareit.model.Participant.Role
import uniovi.eii.shareit.utils.createTempImageFile
import uniovi.eii.shareit.utils.getSecureUriForFile
import uniovi.eii.shareit.viewModel.AddImageViewModel
import uniovi.eii.shareit.viewModel.AlbumViewModel

class AddImageFragment : Fragment() {

    private var _binding: FragmentAddImageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddImageViewModel by viewModels()
    private val albumViewModel: AlbumViewModel by navGraphViewModels(R.id.navigation_album)
    private var imageUri: Uri? = null

    // Lanzador para seleccionar la imagen de la galería
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                viewModel.processImage(uri)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    // Lanzador para tomar una foto con la cámara
    private val captureImageLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                Log.d("Camera", "Selected URI: $imageUri")
                viewModel.processImage(imageUri!!)
            } else {
                Log.d("Camera", "No media selected")
            }
        }

    // Lanzador para solicitar el permiso de lectura del almacenamiento
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            if (results.isNotEmpty() && results.values.all { it }) {
                openGallery()
            } else {
                Toast.makeText(requireContext(), resources.getString(R.string.error_gallery_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }

    // Lanzador para solicitar el permiso de la cámara
    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                takePicture()
            } else {
                Toast.makeText(requireContext(), resources.getString(R.string.error_camera_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddImageBinding.inflate(inflater, container, false)

        albumViewModel.album.observe(viewLifecycleOwner) {
            checkPermissions()
        }
        albumViewModel.currentUserRole.observe(viewLifecycleOwner) {
            if (it == Role.NONE) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.warn_eliminated_from_album_title))
                    .setMessage(resources.getString(R.string.warn_eliminated_from_album_message))
                    .setCancelable(false)
                    .setNeutralButton(resources.getString(R.string.btn_cancel)) { _, _ ->
                        findNavController().navigate(R.id.action_exit_album_to_nav_home)
                    }
                    .setPositiveButton(resources.getString(R.string.btn_accept)) { _, _ ->
                        albumViewModel.deleteUserAlbum(albumViewModel.getAlbumInfo().albumId)
                        findNavController().navigate(R.id.action_exit_album_to_nav_home)
                    }.show()
            } else {
                checkPermissions()
            }
        }

        viewModel.imageUri.observe(viewLifecycleOwner) { uri ->
            if (uri != null) {
                binding.imageView.setImageURI(uri)
                binding.uploadImageButton.isEnabled = true
            } else {
                binding.uploadImageButton.isEnabled = false
            }
        }

        viewModel.isCompletedImageLoad.observe(viewLifecycleOwner) { isCompleted ->
            if (!isCompleted) {
                Toast.makeText(requireContext(), resources.getString(R.string.error_loading_image), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isCompletedImageUpload.observe(viewLifecycleOwner) { isCompleted ->
            if (isCompleted) {
                Toast.makeText(requireContext(), resources.getString(R.string.info_success_uploading_image), Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), resources.getString(R.string.error_uploading_image), Toast.LENGTH_SHORT).show()
                enableUploadButton(true)
            }
        }

        binding.selectImageButton.setOnClickListener {
            showPickResourceDialog()
        }

        binding.uploadImageButton.setOnClickListener {
            enableUploadButton(false)
            with(albumViewModel.getAlbumInfo()) {
                viewModel.uploadImage(albumId, name)
            }
        }

        return binding.root
    }

    private fun enableUploadButton(enabled: Boolean) {
        val uploadImageButton = binding.uploadImageButton as MaterialButton
        uploadImageButton.isEnabled = enabled
        uploadImageButton.setTextColor(if (enabled) {
            resources.getColor(R.color.md_theme_onPrimary, null)
        } else {
            Color.TRANSPARENT
        })
        binding.selectImageButton.isEnabled = enabled
        binding.uploadProgressIndicator.isVisible = !enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkPermissions() {
        if (!albumViewModel.hasImagesAddPermission()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.warn_image_add_permission_revoked_title))
                .setMessage(resources.getString(R.string.warn_image_add_permission_revoked_message))
                .setCancelable(false)
                .setPositiveButton(resources.getString(R.string.btn_accept)) { _, _ ->
                    findNavController().navigateUp()
                }.show()
        }
    }

    private fun checkGalleryPermissions() {
        val hasPermission = when {
            // Full access on Android 13 (API level 33) or higher
            VERSION.SDK_INT >= VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(requireContext(), READ_MEDIA_IMAGES) == PERMISSION_GRANTED -> true
            // Partial access on Android 14 (API level 34) or higher
            VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE &&
                    ContextCompat.checkSelfPermission(requireContext(), READ_MEDIA_VISUAL_USER_SELECTED) == PERMISSION_GRANTED -> true
            // Full access up to Android 12 (API level 32)
            ContextCompat.checkSelfPermission(requireContext(), READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED -> true
            else -> false
        }

        if (hasPermission) {
            openGallery()
        } else {
            val permissions = when {
                VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE ->
                    arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VISUAL_USER_SELECTED)
                VERSION.SDK_INT >= VERSION_CODES.TIRAMISU ->
                    arrayOf(READ_MEDIA_IMAGES)
                else ->
                    arrayOf(READ_EXTERNAL_STORAGE)
            }
            requestPermissions.launch(permissions)
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), CAMERA) == PERMISSION_GRANTED) {
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


}