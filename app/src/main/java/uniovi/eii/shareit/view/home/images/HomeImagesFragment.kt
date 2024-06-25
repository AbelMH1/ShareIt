package uniovi.eii.shareit.view.home.images

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentHomeImagesBinding
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.view.adapter.SectionImageListAdapter
import uniovi.eii.shareit.view.adapter.SectionListAdapter
import uniovi.eii.shareit.view.album.image.ImageFragment
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

        sectionListAdapter =
            SectionListAdapter(listener = object : SectionImageListAdapter.OnItemClickListener {
                override fun onItemClick(item: Image, position: Int) {
                    clickOnImageItem(item, position)
                }
            })

        // Set the adapter
        binding.allImagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sectionListAdapter
        }

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own images action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        viewModel.sectionList.observe(viewLifecycleOwner) {
            sectionListAdapter.update(it)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun clickOnImageItem(image: Image, position: Int) {
        Toast.makeText(context, "Item Clicked $position", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.nav_album_image, Bundle().apply {
            putString(ImageFragment.USE_VIEWMODEL, GENERAL_VIEW)
            putInt(ImageFragment.SELECTED_IMAGE, position)
        })
    }

}