package uniovi.eii.shareit.view.album.information.display

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumInformationSharedBinding
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.view.album.information.AlbumInformationViewModel

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
            if (viewModel.hasDisabledShared(album)) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.warn_disable_shared_title))
                    .setMessage(resources.getString(R.string.warn_disable_shared_message))
                    .setNeutralButton(resources.getString(R.string.warn_disable_shared_cancel)) { _, _ ->
                        binding.switchSharedAlbum.isChecked = true
                    }.setPositiveButton(resources.getString(R.string.warn_disable_shared_accept)) { _, _ ->
                        save(album)
                    }.show()
            } else {
                save(album)
            }
        }
        binding.switchSharedAlbum.setOnCheckedChangeListener { _, isChecked ->
            binding.sharedSettings.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        binding.switchInvitationLink.setOnCheckedChangeListener { _, isChecked ->
            binding.invitationLinkLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        binding.invitationLinkLayout.setEndIconOnClickListener {
            Toast.makeText(context, "Copy", Toast.LENGTH_SHORT).show()
        }
        binding.membersImagesPermissionEditText.addTextChangedListener(ValidationTextWatcher(binding.membersImagesPermissionLayout))
        binding.membersChatPermissionEditText.addTextChangedListener(ValidationTextWatcher(binding.membersChatPermissionLayout))
        binding.guestsImagesPermissionEditText.addTextChangedListener(ValidationTextWatcher(binding.guestsImagesPermissionLayout))
        binding.guestsChatPermissionEditText.addTextChangedListener(ValidationTextWatcher(binding.guestsChatPermissionLayout))
    }

    private fun save(album: Album) {
        enableEdition(false)
        binding.editFAB.show()
        binding.saveFAB.hide()
        viewModel.saveAlbumInfo(album)
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
        binding.switchInvitationLink.visibility = editView
        binding.switchSharedAlbum.isEnabled = enable
        binding.guestsChatPermissionLayout.isEnabled = enable
        binding.guestsImagesPermissionLayout.isEnabled = enable
        binding.membersChatPermissionLayout.isEnabled = enable
        binding.membersImagesPermissionLayout.isEnabled = enable
    }

    private fun checkData(): Album? {
        val albumData = viewModel.getAlbumInfo()
        albumData.shared = binding.switchSharedAlbum.isChecked
        if (binding.switchSharedAlbum.isChecked) {
            albumData.membersImagesPermission =
                validateTextField(binding.membersImagesPermissionLayout) ?: return null
            albumData.membersChatPermission =
                validateTextField(binding.membersChatPermissionLayout) ?: return null
            albumData.guestsImagesPermission =
                validateTextField(binding.guestsImagesPermissionLayout) ?: return null
            albumData.guestsChatPermission =
                validateTextField(binding.guestsChatPermissionLayout) ?: return null
            albumData.invitationLinkEnabled = binding.switchInvitationLink.isChecked
            if (!albumData.invitationLinkEnabled) albumData.invitationLink = null
        } else {
            albumData.membersImagesPermission = null
            albumData.membersChatPermission = null
            albumData.guestsImagesPermission = null
            albumData.guestsChatPermission = null
            albumData.invitationLinkEnabled = false
            albumData.invitationLink = null
        }
        return albumData
    }

    private fun validateTextField(etLayout: TextInputLayout): String? {
        val str = etLayout.editText?.text?.toString()?.trim()
        if (str.isNullOrBlank()) {
            etLayout.error = resources.getString(R.string.err_empty_field)
            return null
        }
        return str
    }

    private fun updateUI(album: Album) {
        binding.switchSharedAlbum.isChecked = album.shared
        if (album.shared) {
            binding.membersImagesPermissionEditText.setText(album.membersImagesPermission)
            binding.membersChatPermissionEditText.setText(album.membersChatPermission)
            binding.guestsImagesPermissionEditText.setText(album.guestsImagesPermission)
            binding.guestsChatPermissionEditText.setText(album.guestsChatPermission)
        } else {
            binding.membersImagesPermissionEditText.text = null
            binding.membersChatPermissionEditText.text = null
            binding.guestsImagesPermissionEditText.text = null
            binding.guestsChatPermissionEditText.text = null
        }
        if (album.shared && album.invitationLinkEnabled) {
            binding.invitationLinkEditText.setText(album.invitationLink)
            binding.switchInvitationLink.isChecked = true
        } else {
            binding.invitationLinkEditText.text = null
            binding.switchInvitationLink.isChecked = false
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