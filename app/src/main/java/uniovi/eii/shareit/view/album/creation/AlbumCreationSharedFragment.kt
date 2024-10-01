package uniovi.eii.shareit.view.album.creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumCreationSharedBinding
import uniovi.eii.shareit.view.MainActivity
import uniovi.eii.shareit.view.adapter.ParticipantsListAdapter
import uniovi.eii.shareit.view.album.placeholder.PlaceholderContent
import uniovi.eii.shareit.viewModel.AlbumCreationViewModel

class AlbumCreationSharedFragment : Fragment() {

    private var _binding: FragmentAlbumCreationSharedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlbumCreationViewModel by navGraphViewModels(R.id.navigation_album_creation)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumCreationSharedBinding.inflate(inflater, container, false)

        binding.membersImagesPermissionEditText.setText(getString(R.string.images_permission_see), false)
        binding.membersChatPermissionEditText.setText(getString(R.string.chat_permission_hiden), false)
        binding.guestsImagesPermissionEditText.setText(getString(R.string.images_permission_see), false)
        binding.guestsChatPermissionEditText.setText(getString(R.string.chat_permission_hiden), false)

        binding.recyclerParticipants.layoutManager = LinearLayoutManager(context)
        binding.recyclerParticipants.adapter =
            ParticipantsListAdapter(PlaceholderContent.getParticipantsList(20))

        binding.createBtn.setOnClickListener {
            if (validateData()) {
                findNavController().navigate(
                    AlbumCreationSharedFragmentDirections.actionNavAlbumCreationSharedToNavHome()
                )
                viewModel.createAlbum()
            }
        }

        binding.membersImagesPermissionEditText.addTextChangedListener(
            MainActivity.ErrorCleaningTextWatcher(binding.membersImagesPermissionLayout)
        )
        binding.membersChatPermissionEditText.addTextChangedListener(
            MainActivity.ErrorCleaningTextWatcher(binding.membersChatPermissionLayout)
        )
        binding.guestsImagesPermissionEditText.addTextChangedListener(
            MainActivity.ErrorCleaningTextWatcher(binding.guestsImagesPermissionLayout)
        )
        binding.guestsChatPermissionEditText.addTextChangedListener(
            MainActivity.ErrorCleaningTextWatcher(binding.guestsChatPermissionLayout)
        )

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
                toolbar.subtitle = resources.getString(R.string.menu_album_creation_shared)
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

    private fun validateData(): Boolean {
        enableForm(false)
        val dataValidationResult = viewModel.validateSharedData(
            binding.membersImagesPermissionEditText.text?.toString() ?: "",
            binding.membersChatPermissionEditText.text?.toString() ?: "",
            binding.guestsImagesPermissionEditText.text?.toString() ?: "",
            binding.guestsChatPermissionEditText.text?.toString() ?: "",
            (binding.recyclerParticipants.adapter as ParticipantsListAdapter).getParticipants()
        )
        if (!dataValidationResult.isDataValid) {
            updateErrors(
                dataValidationResult.memberImagesError,
                dataValidationResult.memberChatError,
                dataValidationResult.guestImagesError,
                dataValidationResult.guestChatError
            )
        }
        enableForm(true)
        return dataValidationResult.isDataValid
    }

    private fun enableForm(isEnabled: Boolean) {
        binding.membersImagesPermissionLayout.isEnabled = isEnabled
        binding.membersChatPermissionLayout.isEnabled = isEnabled
        binding.guestsImagesPermissionLayout.isEnabled = isEnabled
        binding.guestsChatPermissionLayout.isEnabled = isEnabled
        binding.addParticipantBtn.isEnabled = isEnabled
        binding.addParticipantLayout.isEnabled = isEnabled
    }

    private fun updateErrors(
        memberImagesError: Int?,
        memberChatError: Int?,
        guestImagesError: Int?,
        guestChatError: Int?
    ) {
        if (memberImagesError != null) {
            binding.membersImagesPermissionLayout.error = resources.getString(memberImagesError)
            binding.membersImagesPermissionEditText.requestFocus()
        }
        if (memberChatError != null) {
            binding.membersChatPermissionLayout.error = resources.getString(memberChatError)
            binding.membersChatPermissionEditText.requestFocus()
        }
        if (guestImagesError != null) {
            binding.guestsImagesPermissionLayout.error = resources.getString(guestImagesError)
            binding.guestsImagesPermissionEditText.requestFocus()
        }
        if (guestChatError != null) {
            binding.guestsChatPermissionLayout.error = resources.getString(guestChatError)
            binding.guestsChatPermissionEditText.requestFocus()
        }
    }

}