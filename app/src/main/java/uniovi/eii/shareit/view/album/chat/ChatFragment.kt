package uniovi.eii.shareit.view.album.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentChatBinding
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
    private val albumViewModel: AlbumViewModel by activityViewModels()
    private val chatViewModel: AlbumChatViewModel by activityViewModels()
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

        albumViewModel.currentAlbum.observe(viewLifecycleOwner) {
            val toolbar = requireActivity().findViewById(R.id.toolbar) as MaterialToolbar
            toolbar.title = it.name
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
                val toolbar = requireActivity().findViewById(R.id.toolbar) as MaterialToolbar
                toolbar.isTitleCentered = true
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

}