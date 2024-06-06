package uniovi.eii.shareit.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.pager.adapter = ViewPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = resources.getString(R.string.tab_images)
                    tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_menu_camera, null)
                }
                1 -> {
                    tab.text = resources.getString(R.string.tab_albums)
                    tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_menu_gallery, null)
                }
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}