package uniovi.eii.shareit.ui.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import uniovi.eii.shareit.ui.home.albums.HomeAlbumsFragment
import uniovi.eii.shareit.ui.home.images.HomeImagesFragment

private const val NUM_TABS = 2
class HomeViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = NUM_TABS

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int).
        when (position) {
            0 -> return HomeImagesFragment.newInstance()
            1 -> return HomeAlbumsFragment.newInstance()
        }
        throw IllegalStateException("Incorrect number of tabs in the ViewPager")
    }
}