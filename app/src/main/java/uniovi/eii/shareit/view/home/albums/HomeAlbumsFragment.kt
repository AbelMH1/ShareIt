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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentHomeAlbumsBinding
import uniovi.eii.shareit.model.UserAlbum
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

        binding.albumRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.albumRecyclerView.adapter = albumListAdapter

        binding.fab.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections
                .actionNavHomeToNavAlbumCreation())
        }

        viewModel.displayAlbumList.observe(viewLifecycleOwner) {
            albumListAdapter.update(it)
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
                menuInflater.inflate(R.menu.album_order, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_order_custom, R.id.action_order_creation_date, R.id.action_order_name, R.id.action_order_last_update -> {
                        menuItem.isChecked = !menuItem.isChecked
                        Toast.makeText(context, "Ordering albums by ${menuItem.title}", Toast.LENGTH_SHORT).show()
                        viewModel.applyOrder(order = menuItem.itemId)
                        true
                    }

                    R.id.action_order_ascending, R.id.action_order_descending -> {
                        menuItem.isChecked = !menuItem.isChecked
                        Toast.makeText(context, "Changed order direction: ${menuItem.title}", Toast.LENGTH_SHORT).show()
                        viewModel.applyOrder(direction = menuItem.itemId)
                        true
                    }

                    else -> false
                }
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                menu.findItem(R.id.action_order)?.subMenu?.findItem(viewModel.currentOrder.value!!)?.isChecked = true
                menu.findItem(R.id.action_order)?.subMenu?.findItem(viewModel.currentOrderDirection.value!!)?.isChecked = true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    fun clickOnCardViewItem(album: UserAlbum, position: Int) {
        Log.i("Click adapter", "Item Clicked at index $position: $album")
        Toast.makeText(context, "Item Clicked ${album.name}", Toast.LENGTH_SHORT).show()
        // TODO: Pasar id del album a abrir
        findNavController().navigate(HomeFragmentDirections
            .actionNavHomeToNavAlbum(5))
    }

    fun clickOnInfoButtonItem(album: UserAlbum, position: Int) {
        Log.i("Click adapter", "Item Clicked at index $position: $album")
        Toast.makeText(context, "Info Clicked ${album.name}", Toast.LENGTH_SHORT).show()
        // TODO: Pasar id del album a abrir
        findNavController().navigate(HomeFragmentDirections
            .actionNavHomeToNavAlbumInformation())
    }

}