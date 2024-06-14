package uniovi.eii.shareit.ui.album.information.display

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumInformationGeneralBinding
import uniovi.eii.shareit.ui.album.information.AlbumInformationViewModel

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

        binding.editFAB.setOnClickListener {
            enableEdition(true)
            binding.editFAB.hide()
            binding.saveFAB.show()
        }
        binding.saveFAB.setOnClickListener {
            enableEdition(false)
            binding.editFAB.show()
            binding.saveFAB.hide()
        }

        binding.dateToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
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

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.editFAB.show()
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
}