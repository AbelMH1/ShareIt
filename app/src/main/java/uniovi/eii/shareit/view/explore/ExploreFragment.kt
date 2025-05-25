package uniovi.eii.shareit.view.explore

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavGraph
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentExploreBinding
import uniovi.eii.shareit.model.UserAlbum
import uniovi.eii.shareit.utils.IdToTagMap
import uniovi.eii.shareit.view.adapter.AlbumListAdapter
import uniovi.eii.shareit.viewModel.ExploreViewModel

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExploreViewModel by viewModels()
    private lateinit var albumListAdapter: AlbumListAdapter

    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)

        viewModel.loadInitialData()

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

        binding.albumRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.albumRecyclerView.adapter = albumListAdapter

        viewModel.displayAlbumList.observe(viewLifecycleOwner) { albums ->
            albumListAdapter.update(albums)
            isLoading = false
        }

        val onScrollChangeListener =
            View.OnScrollChangeListener { v, _, scrollY, _, _ ->
                // Altura total del RecyclerView - Altura de la vista visible - Altura de un elemento:
                // Carga más álbumes cuando solo queda un elemento para llegar al final del RecyclerView
                val isAtBottom =
                    scrollY >= binding.albumRecyclerView.measuredHeight - v.measuredHeight -
                            (binding.albumRecyclerView.getChildAt(0)?.measuredHeight ?: 0)
                if (isAtBottom && !isLoading) {
                    isLoading = true
                    Log.d("Scroll", "Loading more albums...")
                    viewModel.loadMoreAlbums()
                }
            }

        viewModel.isSearchMoreEnabled.observe(viewLifecycleOwner) { isEnabled ->
            if (isEnabled) {
                binding.progressBar.isVisible = true
                binding.exploreNestedScrollView.setOnScrollChangeListener(onScrollChangeListener)
            } else{
                Toast.makeText(context, resources.getString(R.string.toast_no_more_albums), Toast.LENGTH_SHORT).show()
                binding.exploreNestedScrollView.setOnScrollChangeListener(null as View.OnScrollChangeListener?)
                binding.progressBar.isVisible = false
            }
        }

        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val selectedTags = checkedIds.mapNotNull { IdToTagMap[it] }
            Log.d("ChipGroup", "Selected Tags: $selectedTags")
            viewModel.loadInitialSearchData(filterTags=selectedTags)
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

    fun clickOnCardViewItem(album: UserAlbum, position: Int) {
        Log.i("Click adapter", "Item Clicked at index $position: $album")
        val graph = findNavController().graph.findNode(R.id.navigation_album)
        if (graph is NavGraph) {
            graph.setStartDestination(R.id.nav_album)
        }
        findNavController().navigate(
            ExploreFragmentDirections
                .actionNavExploreToNavigationAlbum(album.albumId, album.name, album.coverImage))
    }

    fun clickOnInfoButtonItem(album: UserAlbum, position: Int) {
        Log.i("Click adapter", "Item Clicked at index $position: $album")
        val graph = findNavController().graph.findNode(R.id.navigation_album)
        if (graph is NavGraph) {
            graph.setStartDestination(R.id.nav_album_information)
        }
        findNavController().navigate(
            ExploreFragmentDirections
            .actionNavExploreToNavigationAlbum(album.albumId, album.name, album.coverImage))
    }

    private fun configureToolBar() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                val toolbar: MaterialToolbar = requireActivity().findViewById(R.id.toolbar)
                toolbar.isTitleCentered = true
                menuInflater.inflate(R.menu.explore, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                val searchItem = menu.findItem(R.id.action_search)
                searchItem.setOnActionExpandListener(object :
                    MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                        binding.chipsScrollView.isVisible = true
                        return true
                    }
                    override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                        viewModel.restoreExploreList()
                        binding.chipGroup.clearCheck()
                        binding.chipsScrollView.isVisible = false
                        return true
                    }
                })
                val searchView = searchItem.actionView as SearchView
                searchView.queryHint = getString(R.string.searchbar_hint)
                searchView.setOnQueryTextListener(object :
                    SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        viewModel.loadInitialSearchData(query)
                        return true
                    }
                    override fun onQueryTextChange(newText: String?): Boolean {
                        if (newText.isNullOrEmpty()) {
                            viewModel.loadInitialSearchData("")
                        }
                        return true
                    }
                })
            }
        }, viewLifecycleOwner)
    }
}