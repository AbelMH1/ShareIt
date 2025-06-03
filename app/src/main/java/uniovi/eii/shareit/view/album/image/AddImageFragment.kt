package uniovi.eii.shareit.view.album.image

import android.Manifest.permission.CAMERA
import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAddImageBinding
import uniovi.eii.shareit.model.Participant.Role
import uniovi.eii.shareit.model.service.ImageUploadService
import uniovi.eii.shareit.utils.compressImage
import uniovi.eii.shareit.utils.createTempImageFile
import uniovi.eii.shareit.utils.getRequiredGalleryPermissions
import uniovi.eii.shareit.utils.getSecureUriForFile
import uniovi.eii.shareit.utils.hasCameraPermission
import uniovi.eii.shareit.utils.hasGalleryPermission
import uniovi.eii.shareit.utils.hasNotificationPermission
import uniovi.eii.shareit.utils.registerCameraPicker
import uniovi.eii.shareit.utils.registerGalleryPicker
import uniovi.eii.shareit.utils.registerPermissionRequest
import uniovi.eii.shareit.utils.registerPermissionsRequest
import uniovi.eii.shareit.viewModel.AddImageViewModel
import uniovi.eii.shareit.viewModel.AlbumViewModel

class AddImageFragment : Fragment() {

    companion object {
        private const val TAG = "AddImageFragment"
    }

    private var _binding: FragmentAddImageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddImageViewModel by viewModels()
    private val albumViewModel: AlbumViewModel by navGraphViewModels(R.id.navigation_album)

    private var imageUri: Uri? = null
    private var exampleService: ImageUploadService? = null
    private var serviceBoundState = false

    // Lanzador para seleccionar la imagen de la galería
    private val pickImageLauncher = registerGalleryPicker(::processImage)
    // Lanzador para tomar una foto con la cámara
    private val captureImageLauncher = registerCameraPicker(::processImage)
    // Lanzador para solicitar el permiso de lectura del almacenamiento
    private val requestPermissions = registerPermissionsRequest(::openGallery, R.string.error_gallery_permission_denied)
    // Lanzador para solicitar el permiso de la cámara
    private val requestCameraPermission = registerPermissionRequest(::takePicture, R.string.error_camera_permission_denied)
    // Lanzador para solicitar el permiso de notificaciones (necesario para Android 13 y superior)
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        { // Si el permiso fue denegado, el servicio aún puede ejecutarse, solo que la notificación no será visible
            uploadImage()
        }

    // Necesario para vincular el servicio de carga de imágenes
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // Al vincularnos al servicio, obtenemos una instancia de él y actualizamos el estado del servicio.
            Log.d(TAG, "onServiceConnected")
            val binder = service as ImageUploadService.LocalBinder
            exampleService = binder.getService()

            onServiceConnected()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            // Se llama cuando la conexión con el servicio se ha desconectado. Limpieza.
            Log.d(TAG, "onServiceDisconnected")
            serviceBoundState = false
            exampleService = null
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

        viewModel.isServiceRunning.observe(viewLifecycleOwner) { isRunning ->
            Log.d(TAG, "Service running: $isRunning")
            if (isRunning) {
                enableUploadButton(false)
                if (!serviceBoundState){
                    Log.d(TAG, "Re-binding service")
                    bindService()
                }
            }
        }

        binding.selectImageButton.setOnClickListener {
            showPickResourceDialog()
        }

        binding.uploadImageButton.setOnClickListener {
            checkNotificationPermission()
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
        if (serviceBoundState) {
            unbindService()
        }
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
        Log.d("takePicture", "Image URI: $imageUri")
        captureImageLauncher.launch(imageUri)
    }

    private fun processImage(uri: Uri? = imageUri) {
        Log.d("processImage", "Selected URI: $uri")
        if (uri == null) {
            Toast.makeText(requireContext(), R.string.toast_error_loading_image, Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            val context = requireContext()
            val outputFile = context.createTempImageFile()
            val processed = context.compressImage(uri, outputFile)
            if (processed) {
                viewModel.loadImage(context.getSecureUriForFile(outputFile))
            } else {
                Toast.makeText(requireContext(), R.string.toast_error_processing_image, Toast.LENGTH_LONG).show()
            }
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

    /**
     *  Verifica si se tiene el permiso de notificaciones y, si es así, procede a subir la imagen.
     */
    private fun checkNotificationPermission() {
        if (requireContext().hasNotificationPermission()) {
            uploadImage()
        } else {
            if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU)
                requestNotificationPermission.launch(POST_NOTIFICATIONS)
        }
    }

    private fun onServiceConnected() {
        serviceBoundState = true
        viewModel.setServiceRunning(true)
        exampleService?.isUploadCompleted?.observe(viewLifecycleOwner) { isCompleted ->
            if (isCompleted != null) {
                viewModel.setServiceRunning(false)
                if (isCompleted) {
                    findNavController().navigateUp()
                } else {
                    enableUploadButton(true)
                }
                // Desvincularse después de recibir el resultado
                if (serviceBoundState) {
                    unbindService()
                    exampleService = null
                }
            }
        }
    }

    private fun uploadImage() {
        enableUploadButton(false)

        val image = viewModel.getImageToUpload(albumViewModel.getAlbumInfo())
        if (image == null) {
            Toast.makeText(requireContext(), resources.getString(R.string.error_uploading_image), Toast.LENGTH_SHORT).show()
            enableUploadButton(true)
            return
        }

        ImageUploadService.startService(requireContext(), image)

        // Vincular para recibir actualizaciones
        bindService()
    }

    private fun bindService() {
        Intent(requireContext(), ImageUploadService::class.java).also { intent ->
            requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unbindService() {
        requireContext().unbindService(connection)
        serviceBoundState = false
    }
}