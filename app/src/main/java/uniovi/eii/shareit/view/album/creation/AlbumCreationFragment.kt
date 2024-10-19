package uniovi.eii.shareit.view.album.creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.util.Pair
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumCreationBinding
import uniovi.eii.shareit.utils.toDate
import uniovi.eii.shareit.utils.toFormattedString
import uniovi.eii.shareit.view.MainActivity.ErrorCleaningTextWatcher
import uniovi.eii.shareit.viewModel.AlbumCreationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlbumCreationFragment : Fragment() {

    private var _binding: FragmentAlbumCreationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: AlbumCreationViewModel by navGraphViewModels(R.id.navigation_album_creation)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumCreationBinding.inflate(inflater, container, false)

        binding.dateToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) toggleDatesEditTexts(checkedId)
        }
        binding.toggleNone.performClick()

        binding.switchLocationSelection.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    binding.mapView.visibility = View.VISIBLE
                }

                false -> {
                    binding.mapView.visibility = View.GONE
                }
            }
        }

        binding.switchSharedAlbum.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    binding.continueBtn.visibility = View.VISIBLE
                    binding.createBtn.visibility = View.GONE
                }

                false -> {
                    binding.continueBtn.visibility = View.GONE
                    binding.createBtn.visibility = View.VISIBLE
                }
            }
        }

        binding.continueBtn.setOnClickListener {
            if (validateData()) {
                findNavController().navigate(
                    AlbumCreationFragmentDirections.actionNavAlbumCreationToNavAlbumCreationShared()
                )
                enableForm(true)
            }
        }

        binding.createBtn.setOnClickListener {
            if (validateData()) {
                viewModel.createAlbum()
            }
        }

        viewModel.isCompletedAlbumCreation.observe(viewLifecycleOwner) {
            if (it) Toast.makeText(context, "Se ha creado el álbum", Toast.LENGTH_LONG).show()
            else Toast.makeText(context, "Se ha producido un error al crear el álbum", Toast.LENGTH_LONG).show()
            findNavController().navigate(
                AlbumCreationFragmentDirections.actionNavAlbumCreationToNavHome())
        }

        binding.nameEditText.addTextChangedListener(ErrorCleaningTextWatcher(binding.nameLayout))
        binding.dateStartEditText.addTextChangedListener(ErrorCleaningTextWatcher(binding.dateStartLayout))
        binding.dateEndEditText.addTextChangedListener(ErrorCleaningTextWatcher(binding.dateEndLayout))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureToolBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val toolbar = requireActivity().findViewById(R.id.toolbar) as MaterialToolbar
        toolbar.subtitle = null
        _binding = null
    }

    private fun configureToolBar() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                val toolbar = requireActivity().findViewById(R.id.toolbar) as MaterialToolbar
                toolbar.isTitleCentered = false
                toolbar.subtitle = resources.getString(R.string.menu_album_creation_general)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                menu.removeItem(R.id.action_account)
            }
        }, viewLifecycleOwner)
    }

    private fun toggleDatesEditTexts(checkedId: Int) {
        when (checkedId) {
            R.id.toggleNone -> {
                binding.dateStartLayout.visibility = View.GONE
                binding.dateEndLayout.visibility = View.GONE
            }

            R.id.toggleSingle -> {
                binding.dateStartLayout.visibility = View.VISIBLE
                binding.dateEndLayout.visibility = View.GONE
                setDatePicker()
            }

            R.id.toggleRange -> {
                binding.dateStartLayout.visibility = View.VISIBLE
                binding.dateEndLayout.visibility = View.VISIBLE
                setDateRangePicker()
            }
        }
    }

    private fun setDatePicker() {
        val startDate = binding.dateStartEditText.text.toString().toDate()?.time
            ?: MaterialDatePicker.todayInUtcMilliseconds()
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
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

    private fun validateData(): Boolean {
        enableForm(false)
        val dataValidationResult = viewModel.validateGeneralData(
            binding.nameEditText.text?.toString() ?: "",
            binding.dateStartEditText.text?.toString() ?: "",
            binding.dateEndEditText.text?.toString() ?: "",
            binding.dateToggleButton.checkedButtonId,
            binding.switchSharedAlbum.isChecked
        )
        if (!dataValidationResult.isDataValid) {
            updateErrors(
                dataValidationResult.nameError,
                dataValidationResult.dateStartError,
                dataValidationResult.dateEndError
            )
            enableForm(true)
        }
        return dataValidationResult.isDataValid
    }

    private fun enableForm(isEnabled: Boolean) {
        binding.nameLayout.isEnabled = isEnabled
        binding.dateToggleButton.isEnabled = isEnabled
        binding.dateStartLayout.isEnabled = isEnabled
        binding.dateEndLayout.isEnabled = isEnabled
        binding.switchLocationSelection.isEnabled = isEnabled
        binding.switchSharedAlbum.isEnabled = isEnabled
        binding.createBtn.isEnabled = isEnabled
        binding.continueBtn.isEnabled = isEnabled
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

}