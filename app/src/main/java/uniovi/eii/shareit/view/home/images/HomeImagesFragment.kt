package uniovi.eii.shareit.view.home.images

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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentHomeImagesBinding
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.Section
import uniovi.eii.shareit.view.adapter.ImageListAdapter
import uniovi.eii.shareit.view.adapter.SectionListAdapter
import uniovi.eii.shareit.view.home.HomeFragmentDirections
import uniovi.eii.shareit.viewModel.AlbumsDisplayViewModel
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel.Companion.GENERAL_VIEW
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel.Companion.ImagesDisplayViewModelFactory

class HomeImagesFragment : Fragment() {

    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int = 5) = HomeImagesFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_COLUMN_COUNT, columnCount)
            }
        }
    }

    private var _binding: FragmentHomeImagesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ImagesDisplayViewModel
    private lateinit var sectionListAdapter: SectionListAdapter
    private lateinit var imageListAdapter: ImageListAdapter
    private var columnCount = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeImagesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(
            requireActivity(),
            ImagesDisplayViewModelFactory()
        )[GENERAL_VIEW, ImagesDisplayViewModel::class.java]

        val albumsDisplayViewModel: AlbumsDisplayViewModel by activityViewModels()
        if (albumsDisplayViewModel.getAlbumListIds().isNotEmpty()) {
            viewModel.registerUserImagesListener(
                albumsDisplayViewModel.getAlbumListIds()
            )
        }

        columnCount = resources.getInteger(R.integer.images_per_row)+1 // Para que se vean más imágenes que dentro del álbum
        sectionListAdapter =
            SectionListAdapter(listener = object : ImageListAdapter.OnItemClickListener {
                override fun onItemClick(item: Image, position: Int) {
                    clickOnImageItem(item, position)
                }
            }, columns = columnCount)
        imageListAdapter =
            ImageListAdapter(listener = object : ImageListAdapter.OnItemClickListener {
                override fun onItemClick(item: Image, position: Int) {
                    clickOnImageItem(item, position)
                }
            })

        // Set the adapter
        binding.allImagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sectionListAdapter
        }

        viewModel.displayImageList.observe(viewLifecycleOwner) {
            if(!viewModel.shouldDisplaySections())
                setUpImageRecyclerView(it)
        }
        viewModel.displaySectionList.observe(viewLifecycleOwner) {
            if(viewModel.shouldDisplaySections())
                setUpSectionRecyclerView(it)
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
                menuInflater.inflate(R.menu.image_order_filter, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_order_album, R.id.action_order_date, R.id.action_order_likes -> {
                        menuItem.isChecked = !menuItem.isChecked
                        Toast.makeText(context, resources.getString(R.string.toast_info_images_order, menuItem.title), Toast.LENGTH_SHORT).show()
                        viewModel.applyOrder(order = menuItem.itemId)
                        true
                    }

                    R.id.action_order_ascending, R.id.action_order_descending -> {
                        menuItem.isChecked = !menuItem.isChecked
                        Toast.makeText(context, resources.getString(R.string.toast_info_order_direction, menuItem.title), Toast.LENGTH_SHORT).show()
                        viewModel.applyOrder(direction = menuItem.itemId)
                        true
                    }

                    R.id.action_filter_all, R.id.action_filter_mine -> {
                        menuItem.isChecked = !menuItem.isChecked
                        Toast.makeText(context, resources.getString(R.string.toast_info_images_filter, menuItem.title), Toast.LENGTH_SHORT).show()
                        viewModel.applyFilter(menuItem.itemId)
                        true
                    }
                    else -> false
                }
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                menu.findItem(R.id.action_order)?.subMenu?.findItem(viewModel.currentOrder.value!!)?.isChecked = true
                menu.findItem(R.id.action_order)?.subMenu?.findItem(viewModel.currentOrderDirection.value!!)?.isChecked = true
                menu.findItem(R.id.action_filter)?.subMenu?.findItem(viewModel.currentFilter.value!!)?.isChecked = true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun clickOnImageItem(image: Image, position: Int) {
        Log.i("Click adapter", "Item Clicked at index $position: $image")
        viewModel.currentImage = position
        findNavController().navigate(
            HomeFragmentDirections.actionNavHomeToNavAlbumImage())
    }

    private fun setUpSectionRecyclerView(sections: List<Section>) {
        binding.allImagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sectionListAdapter
        }
        sectionListAdapter.update(sections)
        binding.noImagesTextView.isVisible = sections.isEmpty()
    }

    private fun setUpImageRecyclerView(images: List<Image>) {
        binding.allImagesRecyclerView.apply {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = imageListAdapter
        }
        imageListAdapter.update(images)
        binding.noImagesTextView.isVisible = images.isEmpty()
    }

}