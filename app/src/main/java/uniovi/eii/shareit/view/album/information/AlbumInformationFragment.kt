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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
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
    private val viewModel: AlbumInformationViewModel by navGraphViewModels(R.id.navigation_album)
    private val albumViewModel: AlbumViewModel by navGraphViewModels(R.id.navigation_album)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumInformationBinding.inflate(inflater, container, false)

        albumViewModel.registerUserRoleListener(args.albumID)
        albumViewModel.registerAlbumDataListener(args.albumID)
        viewModel.registerAlbumParticipantsListener(args.albumID)

        albumViewModel.album.observe(viewLifecycleOwner) { album ->
            viewModel.updateAlbumData(album)
        }
        albumViewModel.currentUserRole.observe(viewLifecycleOwner) {
            if (it == Role.NONE) { // TODO: && album.visibility != public
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
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureToolBar()
        configureTabs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.unregisterAlbumParticipantsListener()
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