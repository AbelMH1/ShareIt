package uniovi.eii.shareit.view.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import uniovi.eii.shareit.view.home.albums.HomeAlbumsFragment
import uniovi.eii.shareit.view.home.images.HomeImagesFragment

private const val NUM_TABS = 2
class HomeViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = NUM_TABS

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int).
        when (position) {
            0 -> return HomeAlbumsFragment.newInstance()
            1 -> return HomeImagesFragment.newInstance()
        }
        throw IllegalStateException("Incorrect number of tabs in the ViewPager")
    }
}