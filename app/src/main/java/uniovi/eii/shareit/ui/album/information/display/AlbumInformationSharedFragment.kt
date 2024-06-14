package uniovi.eii.shareit.ui.album.information.display

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import uniovi.eii.shareit.databinding.FragmentAlbumInformationSharedBinding
import uniovi.eii.shareit.ui.album.information.AlbumInformationViewModel

class AlbumInformationSharedFragment : Fragment() {

    companion object {
        fun newInstance() = AlbumInformationSharedFragment()
    }

    private var _binding: FragmentAlbumInformationSharedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlbumInformationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumInformationSharedBinding.inflate(inflater, container, false)
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

        binding.switchSharedAlbum.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> binding.sharedSettings.visibility = View.VISIBLE
                false -> binding.sharedSettings.visibility = View.GONE
            }
        }

        binding.switchInvitationLink.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> binding.invitationLinkLayout.visibility = View.VISIBLE
                false -> binding.invitationLinkLayout.visibility = View.GONE
            }
        }

        binding.invitationLinkLayout.setEndIconOnClickListener {
            Toast.makeText(context, "Copy", Toast.LENGTH_SHORT).show()
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
        binding.switchInvitationLink.visibility = editView
        binding.switchSharedAlbum.isEnabled = enable
        binding.guestsChatPermission.isEnabled = enable
        binding.guestsImagesPermission.isEnabled = enable
        binding.membersChatPermission.isEnabled = enable
        binding.membersImagesPermission.isEnabled = enable
    }
}