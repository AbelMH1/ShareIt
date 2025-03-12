package uniovi.eii.shareit.view.album.information.display

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumInformationSharedBinding
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.view.MainActivity.ErrorCleaningTextWatcher
import uniovi.eii.shareit.viewModel.AlbumInformationViewModel
import uniovi.eii.shareit.viewModel.AlbumViewModel

class AlbumInformationSharedFragment : Fragment() {

    companion object {
        fun newInstance() = AlbumInformationSharedFragment()
    }

    private var _binding: FragmentAlbumInformationSharedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlbumInformationViewModel by navGraphViewModels(R.id.navigation_album)
    private val albumViewModel: AlbumViewModel by navGraphViewModels(R.id.navigation_album)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumInformationSharedBinding.inflate(inflater, container, false)
        enableEdition(false)
        setUpListeners(albumViewModel.isCurrentUserOwner())
        binding.switchSharedAlbum.isEnabled = albumViewModel.isCurrentUserOwner()
        albumViewModel.album.observe(viewLifecycleOwner) {
            updateUI(it)
        }

        return binding.root
    }

    private fun setUpListeners(currentUserOwner: Boolean) {
        binding.switchInvitationLink.setOnCheckedChangeListener { _, isChecked ->
            binding.invitationLinkLayout.isVisible = isChecked
        }
        binding.invitationLinkLayout.setEndIconOnClickListener {
            Toast.makeText(context, "Copy", Toast.LENGTH_SHORT).show()
            // TODO: Copy to clipboard
        }
        if(!currentUserOwner) {
            binding.switchSharedAlbum.setOnCheckedChangeListener { _, isChecked ->
                binding.sharedSettings.isVisible = isChecked
            }
            return
        }
        binding.switchSharedAlbum.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.sharedSettings.visibility = View.VISIBLE
                saveData()
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.warn_disable_shared_title))
                    .setMessage(resources.getString(R.string.warn_disable_shared_message))
                    .setCancelable(false)
                    .setNeutralButton(resources.getString(R.string.btn_cancel)) { _, _ ->
                        binding.switchSharedAlbum.isChecked = true
                    }.setPositiveButton(resources.getString(R.string.btn_accept)) { _, _ ->
                        binding.sharedSettings.visibility = View.GONE
                        saveData()
                        binding.editFAB.hide()
                    }.show()
            }
        }
        binding.editFAB.setOnClickListener {
            enableEdition(true)
            binding.editFAB.hide()
            binding.saveFAB.show()
        }
        binding.saveFAB.setOnClickListener {
            saveData()
        }

        binding.membersImagesPermissionEditText.addTextChangedListener(ErrorCleaningTextWatcher(binding.membersImagesPermissionLayout))
        binding.membersChatPermissionEditText.addTextChangedListener(ErrorCleaningTextWatcher(binding.membersChatPermissionLayout))
        binding.guestsImagesPermissionEditText.addTextChangedListener(ErrorCleaningTextWatcher(binding.guestsImagesPermissionLayout))
        binding.guestsChatPermissionEditText.addTextChangedListener(ErrorCleaningTextWatcher(binding.guestsChatPermissionLayout))
    }

    private fun saveData() {
        enableEdition(false)
        val imagesPermissions = resources.getStringArray(R.array.ImagesPermission)
        val chatPermissions = resources.getStringArray(R.array.ChatPermission)
        val dataValidationResult = viewModel.saveSharedData(
            binding.switchSharedAlbum.isChecked,
            imagesPermissions.indexOf(binding.membersImagesPermissionEditText.text.toString()),
            chatPermissions.indexOf(binding.membersChatPermissionEditText.text.toString()),
            imagesPermissions.indexOf(binding.guestsImagesPermissionEditText.text.toString()),
            chatPermissions.indexOf(binding.guestsChatPermissionEditText.text.toString()),
            binding.switchInvitationLink.isChecked
        )
        if (dataValidationResult.isDataValid) {
            binding.editFAB.show()
            binding.saveFAB.hide()
        }
        else {
            updateErrors(
                dataValidationResult.membersImagesPermissionError,
                dataValidationResult.membersChatPermissionError,
                dataValidationResult.guestsImagesPermissionError,
                dataValidationResult.guestsChatPermissionError,
            )
            enableEdition(true)
        }
    }

    private fun updateErrors(
        membersImagesPermissionError: Int?,
        membersChatPermissionError: Int?,
        guestsImagesPermissionError: Int?,
        guestsChatPermissionError: Int?
    ) {
        if (membersImagesPermissionError != null) {
            binding.membersImagesPermissionLayout.error = resources.getString(membersImagesPermissionError)
            binding.membersImagesPermissionEditText.requestFocus()
        }
        if (membersChatPermissionError != null) {
            binding.membersChatPermissionLayout.error = resources.getString(membersChatPermissionError)
            binding.membersChatPermissionEditText.requestFocus()
        }
        if (guestsImagesPermissionError != null) {
            binding.guestsImagesPermissionLayout.error = resources.getString(guestsImagesPermissionError)
            binding.guestsImagesPermissionEditText.requestFocus()
        }
        if (guestsChatPermissionError != null) {
            binding.guestsChatPermissionLayout.error = resources.getString(guestsChatPermissionError)
            binding.guestsChatPermissionEditText.requestFocus()
        }
    }

    override fun onResume() {
        super.onResume()
        enableEdition(false)
        updateUI(albumViewModel.getAlbumInfo())
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
        binding.guestsChatPermissionLayout.isEnabled = enable
        binding.guestsImagesPermissionLayout.isEnabled = enable
        binding.membersChatPermissionLayout.isEnabled = enable
        binding.membersImagesPermissionLayout.isEnabled = enable
    }

    private fun updateUI(album: Album) {
        binding.switchSharedAlbum.isChecked = album.visibility == Album.SHARED
        if (album.visibility == Album.SHARED) {
            if (albumViewModel.isCurrentUserOwner()) binding.editFAB.show()
            val imagesPermissions = resources.getStringArray(R.array.ImagesPermission)
            val chatPermissions = resources.getStringArray(R.array.ChatPermission)
            binding.membersImagesPermissionEditText.setText(imagesPermissions[album.membersImagesPermission!!.ordinal], false)
            binding.membersChatPermissionEditText.setText(chatPermissions[album.membersChatPermission!!.ordinal], false)
            binding.guestsImagesPermissionEditText.setText(imagesPermissions[album.guestsImagesPermission!!.ordinal], false)
            binding.guestsChatPermissionEditText.setText(chatPermissions[album.guestsChatPermission!!.ordinal], false)
        } else {
            binding.membersImagesPermissionEditText.setText(getString(R.string.images_permission_see), false)
            binding.membersChatPermissionEditText.setText(getString(R.string.chat_permission_hidden), false)
            binding.guestsImagesPermissionEditText.setText(getString(R.string.images_permission_see), false)
            binding.guestsChatPermissionEditText.setText(getString(R.string.chat_permission_hidden), false)
        }
        if (album.visibility == Album.SHARED && album.invitationLinkEnabled) {
            binding.invitationLinkEditText.setText(album.invitationLink)
            binding.switchInvitationLink.isChecked = true
        } else {
            binding.invitationLinkEditText.text = null
            binding.switchInvitationLink.isChecked = false
        }
    }
}