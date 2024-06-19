package uniovi.eii.shareit.ui.album.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.appbar.MaterialToolbar
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumImageBinding
import uniovi.eii.shareit.model.Image
import java.util.Date

class ImageFragment : Fragment() {

    companion object {
        fun newInstance() = ImageFragment()
    }

    private var _binding: FragmentAlbumImageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ImageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumImageBinding.inflate(inflater, container, false)

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
        val items: MutableList<Image> = ArrayList()
        for (i in 1..10) {
            val time: Long = 86400000
            items.add(Image("Author $i", creationDate = Date(time*i)))
        }
        binding.pager.adapter = ImageViewPagerAdapter(this, items)
    }

}