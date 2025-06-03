package uniovi.eii.shareit.view.album.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumImageBinding
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.model.Participant.Role
import uniovi.eii.shareit.view.adapter.ImageViewPagerAdapter
import uniovi.eii.shareit.viewModel.AlbumViewModel
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel.Companion.ALBUM_VIEW
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel.Companion.ImagesDisplayViewModelFactory

class ImageFragment : Fragment() {

    private val args: ImageFragmentArgs by navArgs()
    private var _binding: FragmentAlbumImageBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ImagesDisplayViewModel
    private lateinit var albumViewModel: AlbumViewModel
    private lateinit var imagePagerAdapter: ImageViewPagerAdapter
    private var usingViewModel = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usingViewModel = args.USEVIEWMODEL
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumImageBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(
            requireActivity(),
            ImagesDisplayViewModelFactory()
        )[usingViewModel, ImagesDisplayViewModel::class.java]

        if (usingViewModel == ALBUM_VIEW) {
            albumViewModel = ViewModelProvider(
                findNavController().getViewModelStoreOwner(R.id.navigation_album)
            )[AlbumViewModel::class.java]

            albumViewModel.album.observe(viewLifecycleOwner) { album ->
                if (album.visibility != Album.Visibility.PUBLIC && !albumViewModel.isCurrentUserParticipant()) {
                    showAccessDeniedDialog()
                }
            }
            albumViewModel.currentUserRole.observe(viewLifecycleOwner) {
                if (it == Role.NONE && !albumViewModel.isAlbumPublic()) {
                    showAccessDeniedDialog()
                }
            }
        }

        imagePagerAdapter = ImageViewPagerAdapter(this, usingViewModel)

        viewModel.displayImageList.observe(viewLifecycleOwner) {
            imagePagerAdapter.update(it.toMutableList())
            binding.pager.setCurrentItem(viewModel.currentImage, false)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureToolBar()
        configureViewPager()
    }

    override fun onDestroyView() {
        super.onDestroyView()
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

    private fun configureViewPager() {
        binding.pager.adapter = imagePagerAdapter
        binding.pager.setCurrentItem(viewModel.currentImage, false)

        binding.pager.registerOnPageChangeCallback(
            object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    viewModel.currentImage = position
                    viewModel.checkUpdatedImageLikes(position)
                }
            }
        )
    }

    private fun showAccessDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.warn_eliminated_from_album_title))
            .setMessage(resources.getString(R.string.warn_eliminated_from_album_message))
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.bt_exit)) { _, _ ->
                albumViewModel.deleteUserAlbum(albumViewModel.getAlbumInfo().albumId)
                findNavController().navigate(R.id.action_exit_album_to_nav_home)
            }.show()
    }
}