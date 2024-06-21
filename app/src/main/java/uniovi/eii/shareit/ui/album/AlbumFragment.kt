package uniovi.eii.shareit.ui.album

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import uniovi.eii.shareit.R
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.ui.album.image.ImageFragment

/**
 * A fragment representing a list of Items.
 */
class AlbumFragment : Fragment() {

    private var columnCount = 4
    private val viewModel: AlbumViewModel by activityViewModels()
    private lateinit var imageListAdapter: ImagesListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_album, container, false)
        imageListAdapter = ImagesListAdapter(listener =
            object : ImagesListAdapter.OnItemClickListener {
                override fun onItemClick(item: Image, position: Int) {
                    clickOnItem(item, position)
                }
            })
        viewModel.imageList.observe(viewLifecycleOwner) {
            imageListAdapter.update(it)
        }

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = imageListAdapter
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureToolBar()
    }

    private fun configureToolBar() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                val toolbar = requireActivity().findViewById(R.id.toolbar) as MaterialToolbar
                toolbar.isTitleCentered = false
                menuInflater.inflate(R.menu.album, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_chat -> {
                        Toast.makeText(context, "Opening chat...", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.nav_album_chat)
                        true
                    }
                    R.id.action_info -> {
                        findNavController().navigate(R.id.nav_album_information)
                        true
                    }
                    R.id.action_order -> {
                        Toast.makeText(context, "Order images...", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_filter -> {
                        Toast.makeText(context, "Filter images...", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                menu.removeItem(R.id.action_account)
            }
        }, viewLifecycleOwner)
    }

    fun clickOnItem(image: Image, position: Int) {
        Log.i("Click adapter", "Item Clicked at index $position: $image")
        Toast.makeText(context, "Item Clicked ${image.author}", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.nav_album_image, Bundle().apply { putInt(ImageFragment.SELECTED_IMAGE, position) })
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            AlbumFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}