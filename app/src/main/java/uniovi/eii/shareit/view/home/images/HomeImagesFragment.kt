package uniovi.eii.shareit.view.home.images

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import uniovi.eii.shareit.databinding.FragmentHomeImagesBinding
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.view.adapter.SectionImageListAdapter
import uniovi.eii.shareit.view.adapter.SectionListAdapter
import uniovi.eii.shareit.viewModel.HomeViewModel

class HomeImagesFragment : Fragment() {

    companion object {
        fun newInstance() = HomeImagesFragment()
    }

    private var _binding: FragmentHomeImagesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var sectionListAdapter: SectionListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeImagesBinding.inflate(inflater, container, false)

        sectionListAdapter =
            SectionListAdapter(listener = object : SectionImageListAdapter.OnItemClickListener {
                override fun onItemClick(item: Image, position: Int) {
                    clickOnImageItem(item, position)
                }
            })

        binding.allImagesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.allImagesRecyclerView.adapter = sectionListAdapter

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
//        findNavController().navigate(R.id.nav_album_image)
    }

}