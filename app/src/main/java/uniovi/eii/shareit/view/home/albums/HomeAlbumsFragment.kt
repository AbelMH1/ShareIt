package uniovi.eii.shareit.view.home.albums

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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavGraph
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.transition.Hold
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentHomeAlbumsBinding
import uniovi.eii.shareit.model.UserAlbum
import uniovi.eii.shareit.utils.IdToTagMap
import uniovi.eii.shareit.view.adapter.AlbumListAdapter
import uniovi.eii.shareit.view.home.HomeFragmentDirections
import uniovi.eii.shareit.viewModel.AlbumsDisplayViewModel

class HomeAlbumsFragment : Fragment() {

    companion object {
        fun newInstance() = HomeAlbumsFragment()
    }

    private var _binding: FragmentHomeAlbumsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlbumsDisplayViewModel by activityViewModels()
    private lateinit var albumListAdapter: AlbumListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeAlbumsBinding.inflate(inflater, container, false)

        albumListAdapter =
            AlbumListAdapter(cardListener = object : AlbumListAdapter.OnItemClickListener {
                override fun onItemClick(item: UserAlbum, position: Int) {
                    clickOnCardViewItem(item, position)
                }
            }, infoBtnListener = object : AlbumListAdapter.OnItemClickListener {
                override fun onItemClick(item: UserAlbum, position: Int) {
                    clickOnInfoButtonItem(item, position)
                }
            })

        binding.albumRecyclerView.layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.albums_per_row))
        binding.albumRecyclerView.adapter = albumListAdapter

        binding.fab.setOnClickListener {
            parentFragment?.exitTransition = Hold()
            val extras = FragmentNavigatorExtras(binding.fab to resources.getString(R.string.fab_add_album_desc))
            findNavController().navigate(HomeFragmentDirections
                .actionNavHomeToNavAlbumCreation(), extras)
        }

        viewModel.displayAlbumList.observe(viewLifecycleOwner) {
            albumListAdapter.update(it)
            binding.noAlbumsTextView.isVisible = it.isEmpty()
        }

        viewModel.showFilterTags.observe(viewLifecycleOwner) { showFilter ->
            binding.chipsScrollView.isVisible = showFilter
        }
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val selectedTags = checkedIds.mapNotNull { IdToTagMap[it] }
            Log.d("ChipGroup", "Selected Tags: $selectedTags")
            viewModel.applyFilter(selectedTags)
        }

        viewModel.registerUserAlbumsListener()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureToolBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.unregisterUserAlbumsListener()
        _binding = null
    }

    private fun configureToolBar() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.album_order_filter, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_order_custom, R.id.action_order_creation_date, R.id.action_order_name, R.id.action_order_last_update -> {
                        menuItem.isChecked = !menuItem.isChecked
                        if(viewModel.applyOrder(order = menuItem.itemId))
                            Toast.makeText(context, resources.getString(R.string.toast_info_albums_order, menuItem.title), Toast.LENGTH_SHORT).show()
                        true
                    }

                    R.id.action_order_ascending, R.id.action_order_descending -> {
                        menuItem.isChecked = !menuItem.isChecked
                        if(viewModel.applyOrder(direction = menuItem.itemId))
                            Toast.makeText(context, resources.getString(R.string.toast_info_order_direction, menuItem.title), Toast.LENGTH_SHORT).show()
                        true
                    }

                    R.id.action_filter_tags -> {
                        menuItem.isChecked = !menuItem.isChecked
                        viewModel.toggleShowFilterTags(menuItem.isChecked)
                        true
                    }

                    else -> false
                }
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                menu.findItem(R.id.action_order)?.subMenu?.findItem(viewModel.currentOrder.value!!)?.isChecked = true
                menu.findItem(R.id.action_order)?.subMenu?.findItem(viewModel.currentOrderDirection.value!!)?.isChecked = true
                menu.findItem(R.id.action_filter)?.subMenu?.findItem(R.id.action_filter_tags)?.isChecked = binding.chipsScrollView.isVisible
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    fun clickOnCardViewItem(album: UserAlbum, position: Int) {
        Log.i("Click adapter", "Item Clicked at index $position: $album")
        val graph = findNavController().graph.findNode(R.id.navigation_album)
        if (graph is NavGraph) {
            graph.setStartDestination(R.id.nav_album)
        }
        findNavController().navigate(HomeFragmentDirections
            .actionNavHomeToNavigationAlbum(album.albumId, album.name, album.coverImage))
    }

    fun clickOnInfoButtonItem(album: UserAlbum, position: Int) {
        Log.i("Click adapter", "Item Info Clicked at index $position: $album")
        val graph = findNavController().graph.findNode(R.id.navigation_album)
        if (graph is NavGraph) {
            graph.setStartDestination(R.id.nav_album_information)
        }
        findNavController().navigate(HomeFragmentDirections
            .actionNavHomeToNavigationAlbum(album.albumId, album.name, album.coverImage))
    }

}