package uniovi.eii.shareit.view.album

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumBinding
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.Participant.Role
import uniovi.eii.shareit.model.Section
import uniovi.eii.shareit.view.adapter.ImageListAdapter
import uniovi.eii.shareit.view.adapter.SectionListAdapter
import uniovi.eii.shareit.viewModel.AlbumViewModel
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel.Companion.ALBUM_VIEW
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel.Companion.ImagesDisplayViewModelFactory

/**
 * A fragment representing a list of Items(Images).
 */
class AlbumFragment : Fragment() {

    private val args: AlbumFragmentArgs by navArgs()
    private var _binding: FragmentAlbumBinding? = null
    private val binding get() = _binding!!
    private val albumViewModel: AlbumViewModel by navGraphViewModels(R.id.navigation_album)
    private lateinit var imagesViewModel: ImagesDisplayViewModel
    private lateinit var sectionListAdapter: SectionListAdapter
    private lateinit var imageListAdapter: ImageListAdapter
    private var columnCount = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        columnCount = args.COLUMNCOUNT
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumBinding.inflate(inflater, container, false)
        imagesViewModel = ViewModelProvider(
            requireActivity(),
            ImagesDisplayViewModelFactory()
        )[ALBUM_VIEW, ImagesDisplayViewModel::class.java]

        albumViewModel.registerUserRoleListener(args.albumID)
        albumViewModel.registerAlbumDataListener(args.albumID)
        imagesViewModel.registerAlbumImagesListener(args.albumID)

        albumViewModel.album.observe(viewLifecycleOwner) {
            if (it.visibility != Album.Visibility.PUBLIC && !albumViewModel.isCurrentUserParticipant()) {
                showAccessDeniedDialog()
            }
            val toolbar: MaterialToolbar = requireActivity().findViewById(R.id.toolbar)
            toolbar.title = it.name
            checkPermissions()
        }
        albumViewModel.currentUserRole.observe(viewLifecycleOwner) {
            if (it == Role.NONE && !albumViewModel.isAlbumPublic()) {
                showAccessDeniedDialog()
            } else {
                checkPermissions()
            }
        }

        val onClickListener = object : ImageListAdapter.OnItemClickListener {
            override fun onItemClick(item: Image, position: Int) {
                clickOnItem(item, position)
            }
        }
        sectionListAdapter = SectionListAdapter(listener = onClickListener, columns = columnCount)
        imageListAdapter = ImageListAdapter(listener = onClickListener)

        imagesViewModel.displayImageList.observe(viewLifecycleOwner) {
            if (!imagesViewModel.shouldDisplaySections())
                setUpImageRecyclerView(it)
        }
        imagesViewModel.displaySectionList.observe(viewLifecycleOwner) {
            if (imagesViewModel.shouldDisplaySections())
                setUpSectionRecyclerView(it)
        }

        // Set the adapter
        binding.imagesRecyclerView.apply {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = imageListAdapter
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(AlbumFragmentDirections.actionNavAlbumToAddImageFragment())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureToolBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun configureToolBar() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                val toolbar: MaterialToolbar = requireActivity().findViewById(R.id.toolbar)
                toolbar.isTitleCentered = false
                menuInflater.inflate(R.menu.album, menu)
                menuInflater.inflate(R.menu.image_order_filter, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_chat -> {
                        findNavController().navigate(AlbumFragmentDirections
                            .actionNavAlbumToNavAlbumChat(args.albumID, args.albumName))
                        true
                    }

                    R.id.action_info -> {
                        findNavController().navigate(AlbumFragmentDirections
                            .actionNavAlbumToNavAlbumInformation(args.albumID, args.albumName, args.albumCoverImage))
                        true
                    }

                    R.id.action_order_date, R.id.action_order_likes -> {
                        menuItem.isChecked = !menuItem.isChecked
                        Toast.makeText(context, "Ordering images by ${menuItem.title}", Toast.LENGTH_SHORT).show()
                        imagesViewModel.applyOrder(order = menuItem.itemId)
                        true
                    }

                    R.id.action_order_ascending, R.id.action_order_descending -> {
                        menuItem.isChecked = !menuItem.isChecked
                        Toast.makeText(context, "Changed order direction: ${menuItem.title}", Toast.LENGTH_SHORT).show()
                        imagesViewModel.applyOrder(direction = menuItem.itemId)
                        true
                    }

                    R.id.action_filter_all, R.id.action_filter_mine -> {
                        menuItem.isChecked = !menuItem.isChecked
                        Toast.makeText(context, "Filtering images: ${menuItem.title}", Toast.LENGTH_SHORT).show()
                        imagesViewModel.applyFilter(menuItem.itemId)
                        true
                    }

                    else -> false
                }
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                menu.removeItem(R.id.action_account)
                menu.findItem(R.id.action_order)?.subMenu?.removeItem(R.id.action_order_album)
                menu.findItem(R.id.action_order)?.subMenu?.findItem(imagesViewModel.currentOrder.value!!)?.isChecked = true
                menu.findItem(R.id.action_order)?.subMenu?.findItem(imagesViewModel.currentOrderDirection.value!!)?.isChecked = true
                menu.findItem(R.id.action_filter)?.subMenu?.findItem(imagesViewModel.currentFilter.value!!)?.isChecked = true
                if (!albumViewModel.hasChatSeePermission()) {
                    menu.removeItem(R.id.action_chat)
                }
            }
        }, viewLifecycleOwner)
    }

    fun clickOnItem(image: Image, position: Int) {
        Log.i("Click adapter", "Item Clicked at index $position: $image")
        imagesViewModel.currentImage = position
        findNavController().navigate(AlbumFragmentDirections
            .actionNavAlbumToNavAlbumImage())
    }

    private fun setUpSectionRecyclerView(sections: List<Section>) {
        binding.imagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sectionListAdapter
        }
        sectionListAdapter.update(sections)
    }

    private fun setUpImageRecyclerView(images: List<Image>) {
        binding.imagesRecyclerView.apply {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = imageListAdapter
        }
        imageListAdapter.update(images)
    }

    private fun checkPermissions() {
        activity?.invalidateMenu()
        if (albumViewModel.hasImagesAddPermission()) {
            binding.fab.show()
        } else {
            binding.fab.hide()
        }
    }

    private fun showAccessDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.warn_eliminated_from_album_title))
            .setMessage(resources.getString(R.string.warn_eliminated_from_album_message))
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.bt_exit)) { _, _ ->
                albumViewModel.deleteUserAlbum(args.albumID)
                findNavController().navigate(R.id.action_exit_album_to_nav_home)
            }.show()
    }
}