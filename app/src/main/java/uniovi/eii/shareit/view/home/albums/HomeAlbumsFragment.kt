package uniovi.eii.shareit.view.home.albums

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentHomeAlbumsBinding
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.view.adapter.AlbumListAdapter
import uniovi.eii.shareit.view.album.AlbumFragment
import uniovi.eii.shareit.view.home.HomeViewModel

class HomeAlbumsFragment : Fragment() {

    companion object {
        fun newInstance() = HomeAlbumsFragment()
    }

    private var _binding: FragmentHomeAlbumsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var albumListAdapter: AlbumListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeAlbumsBinding.inflate(inflater, container, false)

        albumListAdapter =
            AlbumListAdapter(cardListener = object : AlbumListAdapter.OnItemClickListener {
                override fun onItemClick(item: Album, position: Int) {
                    clickOnCardViewItem(item, position)
                }
            }, infoBtnListener = object : AlbumListAdapter.OnItemClickListener {
                override fun onItemClick(item: Album, position: Int) {
                    clickOnInfoButtonItem(item, position)
                }
            })

        binding.albumRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.albumRecyclerView.adapter = albumListAdapter

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.nav_album_creation)
        }

        viewModel.albumList.observe(viewLifecycleOwner) {
            albumListAdapter.update(it)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun clickOnCardViewItem(album: Album, position: Int) {
        Log.i("Click adapter", "Item Clicked at index $position: $album")
        Toast.makeText(context, "Item Clicked ${album.name}", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.nav_album, Bundle().apply {
            putInt(AlbumFragment.ARG_COLUMN_COUNT, 2)
            // TODO: Pasar id del album a abrir
        })
    }

    fun clickOnInfoButtonItem(album: Album, position: Int) {
        Log.i("Click adapter", "Item Clicked at index $position: $album")
        Toast.makeText(context, "Info Clicked ${album.name}", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.nav_album_information)
        // TODO: Pasar id del album a abrir
    }

}