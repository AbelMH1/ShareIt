package uniovi.eii.shareit.view.album.information

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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumInformationBinding
import uniovi.eii.shareit.model.Participant.Role
import uniovi.eii.shareit.view.adapter.AlbumInformationViewPagerAdapter
import uniovi.eii.shareit.viewModel.AlbumInformationViewModel
import uniovi.eii.shareit.viewModel.AlbumViewModel

class AlbumInformationFragment : Fragment() {

    companion object {
        fun newInstance() = AlbumInformationFragment()
    }

    private val args: AlbumInformationFragmentArgs by navArgs()
    private var _binding: FragmentAlbumInformationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlbumInformationViewModel by activityViewModels()
    private val albumViewModel: AlbumViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        albumViewModel.updateCurrentAlbum(args.albumID, args.albumName, args.albumCoverImage)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumInformationBinding.inflate(inflater, container, false)


        albumViewModel.currentUserRole.observe(viewLifecycleOwner) {
            if (it == Role.NONE) { // TODO: && album.visibility != public
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.warn_eliminated_from_album_title))
                    .setMessage(resources.getString(R.string.warn_eliminated_from_album_message))
                    .setCancelable(false)
                    .setNeutralButton(resources.getString(R.string.btn_cancel)) { _, _ ->
                        findNavController().navigate(AlbumInformationFragmentDirections.actionNavAlbumInformationToNavHome())
                    }
                    .setPositiveButton(resources.getString(R.string.btn_accept)) { _, _ ->
                        albumViewModel.deleteUserAlbum(args.albumID)
                        findNavController().navigate(AlbumInformationFragmentDirections.actionNavAlbumInformationToNavHome())
                    }.show()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureToolBar()
        configureTabs()
    }

    override fun onStart() {
        super.onStart()
        albumViewModel.registerAlbumDataListener(args.albumID, viewModel.getUpdateAlbumFunc())
        viewModel.registerAlbumParticipantsListener(args.albumID)
        albumViewModel.registerUserRoleListener(args.albumID)
    }

    override fun onStop() {
        super.onStop()
        albumViewModel.unregisterAlbumDataListener()
        viewModel.unregisterAlbumParticipantsListener()
        albumViewModel.unregisterUserRoleListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
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

    private fun configureTabs() {
        binding.pager.adapter = AlbumInformationViewPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            when (position) {
                0 -> tab.text = resources.getString(R.string.tab_general)
                1 -> tab.text = resources.getString(R.string.tab_shared)
                2 -> tab.text = resources.getString(R.string.tab_participants)
            }
        }.attach()
    }

}