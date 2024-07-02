package uniovi.eii.shareit.view.album.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.MaterialToolbar
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumImageBinding
import uniovi.eii.shareit.view.adapter.ImageViewPagerAdapter
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel.Companion.ImagesDisplayViewModelFactory

class ImageFragment : Fragment() {

    private val args: ImageFragmentArgs by navArgs()
    private var _binding: FragmentAlbumImageBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ImagesDisplayViewModel
    private lateinit var imagePagerAdapter: ImageViewPagerAdapter
    private var usingViewModel = ""
    private var selectedImage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            usingViewModel = it.getString(USE_VIEWMODEL)!!
            selectedImage = it.getInt(SELECTED_IMAGE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumImageBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(
            requireActivity(),
            ImagesDisplayViewModelFactory()
        )[usingViewModel, ImagesDisplayViewModel::class.java]

        imagePagerAdapter = ImageViewPagerAdapter(this)

        viewModel.displayImageList.observe(viewLifecycleOwner) {
            imagePagerAdapter.update(it.toMutableList())
            binding.pager.doOnPreDraw {
                binding.pager.currentItem = selectedImage
            }
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
                val toolbar = requireActivity().findViewById(R.id.toolbar) as MaterialToolbar
                toolbar.isTitleCentered = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                menu.removeItem(R.id.action_account)
            }
        }, viewLifecycleOwner)
    }

    private fun configureViewPager() {
        binding.pager.adapter = imagePagerAdapter
        binding.pager.doOnPreDraw {
            binding.pager.currentItem = selectedImage
        }
    }

}