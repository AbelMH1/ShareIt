package uniovi.eii.shareit.view.album.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentChatBinding
import uniovi.eii.shareit.model.Participant.Role
import uniovi.eii.shareit.view.adapter.MessageListAdapter
import uniovi.eii.shareit.viewModel.AlbumChatViewModel
import uniovi.eii.shareit.viewModel.AlbumViewModel

class ChatFragment : Fragment() {

    companion object {
        fun newInstance() = ChatFragment()
    }

    private val args: ChatFragmentArgs by navArgs()
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val albumViewModel: AlbumViewModel by navGraphViewModels(R.id.navigation_album)
    private val chatViewModel: AlbumChatViewModel by viewModels()
    private val adapter = MessageListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        chatViewModel.registerChatMessagesListener(args.albumID)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        binding.messagesRecycler.layoutManager = layoutManager
        adapter.setCurrentUserId(chatViewModel.getCurrentUserId())
        binding.messagesRecycler.adapter = adapter

        chatViewModel.messageList.observe(viewLifecycleOwner) {
            adapter.update(it)
            binding.messagesRecycler.layoutManager?.scrollToPosition(it.size - 1)
        }

        albumViewModel.album.observe(viewLifecycleOwner) {
            val toolbar: MaterialToolbar = requireActivity().findViewById(R.id.toolbar)
            toolbar.title = it.name
            checkPermissions()
        }
        albumViewModel.currentUserRole.observe(viewLifecycleOwner) {
            if (it == Role.NONE) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.warn_eliminated_from_album_title))
                    .setMessage(resources.getString(R.string.warn_eliminated_from_album_message))
                    .setCancelable(false)
                    .setNeutralButton(resources.getString(R.string.btn_cancel)) { _, _ ->
                        findNavController().navigate(R.id.action_exit_album_to_nav_home)
                    }
                    .setPositiveButton(resources.getString(R.string.btn_accept)) { _, _ ->
                        albumViewModel.deleteUserAlbum(args.albumID)
                        findNavController().navigate(R.id.action_exit_album_to_nav_home)
                    }.show()
            } else {
                checkPermissions()
            }
        }

        binding.sendFAB.setOnClickListener {
            val message = binding.writeMessageEditText.text?.toString()?.trim() ?: ""
            if (message.isBlank()) return@setOnClickListener
            chatViewModel.sendMessage(message, args.albumID)
            binding.writeMessageEditText.text = null
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureToolBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chatViewModel.unregisterChatMessagesListener()
        _binding = null
    }

    private fun configureToolBar() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                val toolbar: MaterialToolbar = requireActivity().findViewById(R.id.toolbar)
                toolbar.isTitleCentered = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner)
    }

    private fun checkPermissions() {
        if (!albumViewModel.hasChatSeePermission()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.warn_chat_see_permission_revoked_title))
                .setMessage(resources.getString(R.string.warn_chat_see_permission_revoked_message))
                .setCancelable(false)
                .setPositiveButton(resources.getString(R.string.btn_accept)) { _, _ ->
                    findNavController().navigateUp()
                }.show()
        }
        val canUserComment = albumViewModel.hasChatCommentPermission()
        binding.permissionToCommentLayout.isVisible = canUserComment
        binding.notPermissionToCommentLayout.isVisible = !canUserComment
    }
}