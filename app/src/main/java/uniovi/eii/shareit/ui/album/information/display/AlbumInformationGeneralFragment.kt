package uniovi.eii.shareit.ui.album.information.display

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputLayout
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumInformationGeneralBinding
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.ui.album.information.AlbumInformationViewModel
import java.text.ParseException
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumInformationGeneralBinding.inflate(inflater, container, false)
        enableEdition(false)
        setUpListeners()
        viewModel.album.observe(viewLifecycleOwner) {
            updateUI(it)
        }
        return binding.root
    }

    private fun setUpListeners() {
        binding.editFAB.setOnClickListener {
            enableEdition(true)
            binding.editFAB.hide()
            binding.saveFAB.show()
        }
        binding.saveFAB.setOnClickListener {
            val album = checkData() ?: return@setOnClickListener
            enableEdition(false)
            binding.editFAB.show()
            binding.saveFAB.hide()
            viewModel.saveAlbumInfo(album)
        }
        binding.dateToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) toggleDatesEditTexts(checkedId)
        }
        binding.dateStartLayout.setEndIconOnClickListener {
            // Show date picker
        }
        binding.dateEndLayout.setEndIconOnClickListener {
            // Show date picker
        }
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
        // To remove error messages
        binding.nameEditText.addTextChangedListener(ValidationTextWatcher(binding.nameLayout))
        binding.dateStartEditText.addTextChangedListener(ValidationTextWatcher(binding.dateStartLayout))
        binding.dateEndEditText.addTextChangedListener(ValidationTextWatcher(binding.dateEndLayout))
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
            }

            R.id.toggleRange -> {
                binding.dateStartLayout.visibility = View.VISIBLE
                binding.dateEndLayout.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.editFAB.show()
        updateUI(viewModel.getAlbumInfo())
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
            binding.editFAB.show()
            binding.saveFAB.hide()
        }
    }

    private fun checkData(): Album? {
        val albumData = viewModel.getAlbumInfo()
        albumData.name = validateTextField(binding.nameLayout) ?: return null
        when (binding.dateToggleButton.checkedButtonId) {
            R.id.toggleNone -> {
                albumData.startDate = null
                albumData.endDate = null
            }
            R.id.toggleSingle -> {
                albumData.startDate = validateDate(binding.dateStartLayout) ?: return null
                albumData.endDate = null
            }
            R.id.toggleRange -> {
                val dateStart = validateDate(binding.dateStartLayout) ?: return null
                val dateEnd = validateDate(binding.dateEndLayout) ?: return null
                if (!dateEnd.after(dateStart)) {
                    binding.dateEndLayout.error =
                        resources.getString(R.string.err_invalid_later_date)
                    binding.dateEndEditText.requestFocus()
                    return null
                }
                albumData.startDate = dateStart
                albumData.endDate = dateEnd
            }
        }
        if (binding.switchLocationSelection.isChecked) {
            // TODO: Obtener ubicación del mapa
            albumData.location = LatLng(0.0, 0.0)
        } else albumData.location = null
        return albumData
    }

    private fun validateDate(etLayout: TextInputLayout): Date? {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false
        val date: Date? = try {
            sdf.parse(etLayout.editText?.text.toString().trim())
        } catch (e: ParseException) {
            etLayout.error = resources.getString(R.string.err_invalid_date)
            etLayout.editText?.requestFocus()
            return null
        }
        return date
    }

    private fun validateTextField(etLayout: TextInputLayout): String? {
        val str = etLayout.editText?.text?.toString()?.trim()
        if (str.isNullOrBlank()) {
            etLayout.error = resources.getString(R.string.err_empty_field)
            etLayout.editText?.requestFocus()
            return null
        }
        return str
    }

    private fun updateUI(album: Album) {
        binding.albumName.text = album.name
        binding.nameEditText.setText(album.name)
        binding.switchLocationSelection.isChecked = album.location != null
        if (album.location != null) {
            // TODO: Establecer ubicación en el mapa
        }
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false
        if (album.endDate != null && album.startDate != null) {
            binding.dateStartEditText.setText(sdf.format(album.startDate!!))
            binding.dateEndEditText.setText(sdf.format(album.endDate!!))
            binding.dateToggleButton.check(R.id.toggleRange)
        } else if (album.startDate != null) {
            binding.dateStartEditText.setText(sdf.format(album.startDate!!))
            binding.dateEndEditText.setText("")
            binding.dateToggleButton.check(R.id.toggleSingle)
        } else {
            binding.dateStartEditText.setText("")
            binding.dateEndEditText.setText("")
            binding.dateToggleButton.check(R.id.toggleNone)
        }
    }

    private class ValidationTextWatcher constructor(private val etLayout: TextInputLayout) :
        TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {
            etLayout.error = null
        }
    }
}