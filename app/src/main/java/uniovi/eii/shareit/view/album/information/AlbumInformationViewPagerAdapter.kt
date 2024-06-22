package uniovi.eii.shareit.view.album.information

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import uniovi.eii.shareit.view.album.information.display.AlbumInformationGeneralFragment
import uniovi.eii.shareit.view.album.information.display.AlbumInformationParticipantsFragment
import uniovi.eii.shareit.view.album.information.display.AlbumInformationSharedFragment

private const val NUM_TABS = 3

class AlbumInformationViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = NUM_TABS

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int).
        when (position) {
            0 -> return AlbumInformationGeneralFragment.newInstance()
            1 -> return AlbumInformationSharedFragment.newInstance()
            2 -> return AlbumInformationParticipantsFragment.newInstance()
        }
        throw IllegalStateException("Incorrect number of tabs in the ViewPager")
    }
}