package uniovi.eii.shareit.view.album.information.display

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
import androidx.core.net.toUri
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumInformationGeneralBinding
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.model.Participant.Role
import uniovi.eii.shareit.utils.compressImage
import uniovi.eii.shareit.utils.createTempImageFile
import uniovi.eii.shareit.utils.getRequiredGalleryPermissions
import uniovi.eii.shareit.utils.getSecureUriForFile
import uniovi.eii.shareit.utils.hasCameraPermission
import uniovi.eii.shareit.utils.hasGalleryPermission
import uniovi.eii.shareit.utils.loadImageIntoView
import uniovi.eii.shareit.utils.registerCameraPicker
import uniovi.eii.shareit.utils.registerGalleryPicker
import uniovi.eii.shareit.utils.registerPermissionRequest
import uniovi.eii.shareit.utils.registerPermissionsRequest
import uniovi.eii.shareit.utils.toDate
import uniovi.eii.shareit.utils.toFormattedString
import uniovi.eii.shareit.view.MainActivity.ErrorCleaningTextWatcher
import uniovi.eii.shareit.viewModel.AlbumInformationViewModel
import uniovi.eii.shareit.viewModel.AlbumViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlbumInformationGeneralFragment : Fragment() {

    companion object {
        fun newInstance() = AlbumInformationGeneralFragment()
    }

    private var _binding: FragmentAlbumInformationGeneralBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlbumInformationViewModel by navGraphViewModels(R.id.navigation_album)
    private val albumViewModel: AlbumViewModel by navGraphViewModels(R.id.navigation_album)

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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumInformationGeneralBinding.inflate(inflater, container, false)
        enableEdition(false)
        setUpListeners(albumViewModel.isCurrentUserOwner())
        albumViewModel.album.observe(viewLifecycleOwner) {
            updateAlbumUI(it)
        }
        albumViewModel.currentUserRole.observe(viewLifecycleOwner) {
            if (it != Role.NONE) {
                updateRoleUI(it == Role.OWNER)
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (albumViewModel.isCurrentUserOwner()) binding.editFAB.show()
        val albumInfo = if (viewModel.hasUnsavedChanges()) {
            enableEdition(true)
            viewModel.restoreUnsavedData()
        } else {
            albumViewModel.getAlbumInfo()
        }
        updateAlbumUI(albumInfo)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        enableEdition(false)
        binding.saveFAB.hide()
        binding.editFAB.hide()
    }

    private fun setUpListeners(currentUserOwner: Boolean) {
        binding.dateToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) toggleDatesEditTexts(checkedId)
        }
        binding.switchLocationSelection.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    binding.labelLocationNotEnabled.visibility = View.GONE
                    binding.mapView.visibility = View.VISIBLE
                }

                false -> {
                    binding.labelLocationNotEnabled.visibility = View.VISIBLE
                    binding.mapView.visibility = View.GONE
                }
            }
        }
        if (currentUserOwner) {
            binding.dropAlbumButton.setOnClickListener(null)
            binding.deleteAlbumButton.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.warn_delete_album_title))
                    .setMessage(resources.getString(R.string.warn_delete_album_message))
                    .setNeutralButton(resources.getString(R.string.btn_cancel), null)
                    .setPositiveButton(resources.getString(R.string.btn_accept)) { _, _ ->
                        viewModel.deleteAlbum()
                        findNavController().navigate(R.id.action_exit_album_to_nav_home)
                    }.show()
            }
            binding.editFAB.setOnClickListener {
                enableEdition(true)
                binding.editFAB.hide()
                binding.saveFAB.show()
            }
            binding.saveFAB.setOnClickListener {
                saveData()
            }
            binding.coverSettingsButton.setOnClickListener {
                showPickResourceDialog()
            }
        } else {
            binding.dropAlbumButton.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.warn_drop_album_title))
                    .setMessage(resources.getString(R.string.warn_drop_album_message))
                    .setNeutralButton(resources.getString(R.string.btn_cancel), null)
                    .setPositiveButton(resources.getString(R.string.btn_accept)) { _, _ ->
                        viewModel.dropAlbum()
                        findNavController().navigate(R.id.action_exit_album_to_nav_home)
                    }.show()
            }
            binding.deleteAlbumButton.setOnClickListener(null)
            binding.editFAB.setOnClickListener(null)
            binding.saveFAB.setOnClickListener(null)
            binding.coverSettingsButton.setOnClickListener(null)
        }

        // To remove error messages
        binding.nameEditText.addTextChangedListener(ErrorCleaningTextWatcher(binding.nameLayout))
        binding.dateStartEditText.addTextChangedListener(ErrorCleaningTextWatcher(binding.dateStartLayout))
        binding.dateEndEditText.addTextChangedListener(ErrorCleaningTextWatcher(binding.dateEndLayout))
    }

    private fun toggleDatesEditTexts(checkedId: Int) {
        when (checkedId) {
            R.id.toggleNone -> {
                binding.labelDateNotEnabled.visibility = View.VISIBLE
                binding.dateStartLayout.visibility = View.GONE
                binding.dateEndLayout.visibility = View.GONE
            }

            R.id.toggleSingle -> {
                binding.labelDateNotEnabled.visibility = View.GONE
                binding.dateStartLayout.visibility = View.VISIBLE
                binding.dateEndLayout.visibility = View.GONE
                setDatePicker()
            }

            R.id.toggleRange -> {
                binding.labelDateNotEnabled.visibility = View.GONE
                binding.dateStartLayout.visibility = View.VISIBLE
                binding.dateEndLayout.visibility = View.VISIBLE
                setDateRangePicker()
            }
        }
    }

    private fun setDatePicker() {
        val startDate = binding.dateStartEditText.text.toString().toDate()?.time
            ?: MaterialDatePicker.todayInUtcMilliseconds()
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(startDate)
            .setTextInputFormat(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()))
            .build()
        datePicker.addOnPositiveButtonClickListener {
            binding.dateStartEditText.setText(Date(it).toFormattedString())
        }
        binding.dateStartLayout.setEndIconOnClickListener {
            datePicker.show(childFragmentManager, "AlbumCreationFragment")
        }
    }

    private fun setDateRangePicker() {
        val startDate = binding.dateStartEditText.text.toString().toDate()?.time
            ?: MaterialDatePicker.todayInUtcMilliseconds()
        var endDate = binding.dateEndEditText.text.toString().toDate()?.time
        if (endDate == null || endDate <= startDate)
            endDate = startDate + (86400000 * 3) // Tres días después de startDate
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .setSelection(Pair(startDate, endDate))
                .setTextInputFormat(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()))
                .build()
        dateRangePicker.addOnPositiveButtonClickListener {
            binding.dateStartEditText.setText(Date(it.first).toFormattedString())
            binding.dateEndEditText.setText(Date(it.second).toFormattedString())
        }
        binding.dateStartLayout.setEndIconOnClickListener {
            dateRangePicker.show(childFragmentManager, "AlbumCreationFragment")
        }
        binding.dateEndLayout.setEndIconOnClickListener {
            dateRangePicker.show(childFragmentManager, "AlbumCreationFragment")
        }
    }

    private fun enableEdition(enable: Boolean) {
        val editView = if (enable) View.VISIBLE else View.GONE
        val displayView = if (enable) View.GONE else View.VISIBLE
        binding.displayNameLayout.visibility = displayView
        binding.editNameLayout.visibility = editView
        binding.dateToggleButton.visibility = editView
        binding.switchLocationSelection.visibility = editView
        binding.dateStartLayout.isEnabled = enable
        binding.dateEndLayout.isEnabled = enable
        binding.coverSettingsButton.visibility = editView
        if (enable) {
            binding.dateStartLayout.setEndIconDrawable(R.drawable.ic_calendar_24)
            binding.dateEndLayout.setEndIconDrawable(R.drawable.ic_calendar_24)
            binding.editFAB.hide()
            binding.saveFAB.show()
        } else {
            binding.dateStartLayout.endIconDrawable = null
            binding.dateEndLayout.endIconDrawable = null
            if (albumViewModel.isCurrentUserOwner()) binding.editFAB.show()
            binding.saveFAB.hide()
        }
    }

    private fun saveData() {
        enableEdition(false)
        val dataValidationResult = viewModel.saveGeneralData(
            binding.nameEditText.text?.toString() ?: "",
            binding.switchCoverLastImage.isChecked,
            imageUri,
            binding.dateStartEditText.text?.toString() ?: "",
            binding.dateEndEditText.text?.toString() ?: "",
            binding.dateToggleButton.checkedButtonId,
            binding.switchLocationSelection.isChecked
        )
        if (dataValidationResult.isDataValid) {
            binding.editFAB.show()
            binding.saveFAB.hide()
        }
        else {
            updateErrors(
                dataValidationResult.nameError,
                dataValidationResult.dateStartError,
                dataValidationResult.dateEndError
            )
            enableEdition(true)
        }
    }

    private fun updateErrors(nameError: Int?, dateStartError: Int?, dateEndError: Int?) {
        if (nameError != null) {
            binding.nameLayout.error = resources.getString(nameError)
            binding.nameEditText.requestFocus()
        }
        if (dateStartError != null) {
            binding.dateStartLayout.error = resources.getString(dateStartError)
            binding.dateStartEditText.requestFocus()
        }
        if (dateEndError != null) {
            binding.dateEndLayout.error = resources.getString(dateEndError)
            binding.dateEndEditText.requestFocus()
        }
    }

    private fun updateAlbumUI(album: Album) {
        binding.albumName.text = album.name
        binding.nameEditText.setText(album.name)
        binding.switchCoverLastImage.isChecked = album.useLastImageAsCover
        requireContext().loadImageIntoView(album.coverImage.toUri(), binding.albumCover)
        binding.switchLocationSelection.isChecked = album.location != null
        if (album.location != null) {
            // TODO: Establecer ubicación en el mapa
        }
        if (album.endDate != null && album.startDate != null) {
            binding.dateStartEditText.setText(album.startDate!!.toFormattedString())
            binding.dateEndEditText.setText(album.endDate!!.toFormattedString())
            binding.dateToggleButton.check(R.id.toggleRange)
        } else if (album.startDate != null) {
            binding.dateStartEditText.setText(album.startDate!!.toFormattedString())
            binding.dateEndEditText.setText("")
            binding.dateToggleButton.check(R.id.toggleSingle)
        } else {
            binding.dateStartEditText.setText("")
            binding.dateEndEditText.setText("")
            binding.dateToggleButton.check(R.id.toggleNone)
        }
    }

    private fun updateRoleUI(isOwner: Boolean) {
        setUpListeners(isOwner)
        binding.dropAlbumButton.isVisible = !isOwner
        binding.deleteAlbumButton.isVisible = isOwner
        if(!isOwner) {
            binding.editFAB.hide()
            binding.saveFAB.hide()
        } else if(!binding.editFAB.isVisible && !binding.saveFAB.isVisible) {
            binding.editFAB.show()
        }
    }

    private fun showPickResourceDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_select_source_menu, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.select_source_title))
            .setView(view)
            .create()

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
            saveUnsavedData()
            requestPermissions.launch(getRequiredGalleryPermissions())
        }
    }

    private fun openGallery() {
        saveUnsavedData()
        pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun checkCameraPermissions() {
        if (requireContext().hasCameraPermission()) {
            takePicture()
        } else {
            saveUnsavedData()
            requestCameraPermission.launch(CAMERA)
        }
    }

    private fun takePicture() {
        val image = requireContext().createTempImageFile("IMG_${System.currentTimeMillis()}")
        imageUri = requireContext().getSecureUriForFile(image)
        saveUnsavedData()
        captureImageLauncher.launch(imageUri)
    }

    private fun saveUnsavedData() {
        viewModel.saveUnsavedData(
            binding.nameEditText.text?.toString() ?: "",
            binding.switchCoverLastImage.isChecked,
            binding.dateStartEditText.text?.toString(),
            binding.dateEndEditText.text?.toString(),
            binding.dateToggleButton.checkedButtonId,
            binding.switchLocationSelection.isChecked
        )
    }

    private fun processImage(uri: Uri? = imageUri) {
        Log.d("Camera", "Selected URI: $uri")
        lifecycleScope.launch {
            val context = requireContext()
            val outputFile = context.createTempImageFile()
            val processed = context.compressImage(uri!!, outputFile)
            if (processed) {
                imageUri = context.getSecureUriForFile(outputFile)
                binding.switchCoverLastImage.isChecked = false
                context.loadImageIntoView(imageUri!!, binding.albumCover)
            } else {
                Toast.makeText(context, "Error picking image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}