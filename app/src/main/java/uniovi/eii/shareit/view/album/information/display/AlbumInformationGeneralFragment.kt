package uniovi.eii.shareit.view.album.information.display

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumInformationGeneralBinding
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.model.Participant
import uniovi.eii.shareit.utils.toDate
import uniovi.eii.shareit.utils.toFormattedString
import uniovi.eii.shareit.view.MainActivity.ErrorCleaningTextWatcher
import uniovi.eii.shareit.view.album.information.AlbumInformationFragmentDirections
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
    private val viewModel: AlbumInformationViewModel by activityViewModels()
    private val albumViewModel: AlbumViewModel by activityViewModels()

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
            if (it != Participant.NONE) {
                updateRoleUI(it == Participant.OWNER)
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (albumViewModel.isCurrentUserOwner()) binding.editFAB.show()
        updateAlbumUI(albumViewModel.getAlbumInfo())
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
                        findNavController().navigate(AlbumInformationFragmentDirections.actionNavAlbumInformationToNavHome())
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
                showMenu(it, R.menu.album_cover_options)
            }
        } else {
            binding.dropAlbumButton.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.warn_drop_album_title))
                    .setMessage(resources.getString(R.string.warn_drop_album_message))
                    .setNeutralButton(resources.getString(R.string.btn_cancel), null)
                    .setPositiveButton(resources.getString(R.string.btn_accept)) { _, _ ->
                        viewModel.dropAlbum()
                        findNavController().navigate(AlbumInformationFragmentDirections.actionNavAlbumInformationToNavHome())
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

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            // TODO: Respond to menu item click.
            when (menuItem.itemId) {
                R.id.action_most_liked -> {
                    Toast.makeText(context, "Most liked", Toast.LENGTH_SHORT).show()
                }
                R.id.action_choose_one -> {
                    Toast.makeText(context, "Choose cover", Toast.LENGTH_SHORT).show()
                }
                else -> return@setOnMenuItemClickListener false
            }
            true
        }
        popup.setOnDismissListener {
            // Respond to popup being dismissed.
        }
        // Show the popup menu.
        popup.show()
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
}