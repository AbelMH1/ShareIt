package uniovi.eii.shareit.view.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialContainerTransform
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentHomeBinding
import uniovi.eii.shareit.view.adapter.HomeViewPagerAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition  = MaterialContainerTransform().apply {
//            isDrawDebugEnabled = true
            scrimColor = Color.TRANSPARENT
            containerColor = requireContext().getColor(R.color.md_theme_surface)
            startContainerColor = requireContext().getColor(R.color.md_theme_secondary)
            endContainerColor = requireContext().getColor(R.color.md_theme_surface)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureToolBar()
        configureTabs()
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onResume() {
        super.onResume()
        exitTransition = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun configureToolBar() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                val toolbar: MaterialToolbar = requireActivity().findViewById(R.id.toolbar)
                toolbar.isTitleCentered = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner)
    }

    private fun configureTabs() {
        binding.pager.adapter = HomeViewPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = resources.getString(R.string.tab_albums)
                    tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_menu_gallery, null)
                }

                1 -> {
                    tab.text = resources.getString(R.string.tab_images)
                    tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_image_24, null)
                }
            }
        }.attach()
    }
}