package uniovi.eii.shareit.view.album.creation

import android.graphics.Color
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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.transition.MaterialContainerTransform
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition  = MaterialContainerTransform().apply {
//            isDrawDebugEnabled = true
            scrimColor = Color.TRANSPARENT
            containerColor = requireContext().getColor(R.color.md_theme_surface)
            startContainerColor = requireContext().getColor(R.color.md_theme_secondary)
            endContainerColor = requireContext().getColor(R.color.md_theme_surface)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumCreationBinding.inflate(inflater, container, false)

        binding.dateToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) toggleDatesEditTexts(checkedId)
        }
        binding.toggleNone.performClick()

        binding.visibilityToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val isPrivateAlbum = checkedId == R.id.togglePrivate
                binding.continueBtn.isVisible = !isPrivateAlbum
                binding.createBtn.isVisible = isPrivateAlbum
                val explanationResource = when (checkedId) {
                    R.id.togglePublic -> R.string.label_album_visibility_public_explanation
                    R.id.toggleShared -> R.string.label_album_visibility_shared_explanation
                    else /*R.id.togglePrivate*/ ->  R.string.label_album_visibility_private_explanation
                }
                binding.labelAlbumVisibilityExplanation.text = resources.getString(explanationResource)
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
            if (it) Toast.makeText(context, R.string.toast_album_created, Toast.LENGTH_LONG).show()
            else Toast.makeText(context, R.string.toast_error_creating_album, Toast.LENGTH_LONG).show()
            val extras = FragmentNavigatorExtras(binding.root to resources.getString(R.string.fab_add_album_desc))
            findNavController().navigate(
                AlbumCreationFragmentDirections.actionNavAlbumCreationToNavHome(), extras)
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
        val toolbar: MaterialToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.subtitle = null
        _binding = null
    }

    private fun configureToolBar() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                val toolbar: MaterialToolbar = requireActivity().findViewById(R.id.toolbar)
                toolbar.isTitleCentered = false
                toolbar.subtitle = resources.getString(R.string.menu_album_creation_general)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
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
            binding.visibilityToggleButton.checkedButtonId
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
        binding.visibilityToggleButton.isEnabled = isEnabled
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