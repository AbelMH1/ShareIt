package uniovi.eii.shareit.ui.album.image

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import uniovi.eii.shareit.model.Image

class ImageViewPagerAdapter(
    fragment: Fragment,
    private val imagesList: MutableList<Image>
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return imagesList.size
    }

    override fun createFragment(position: Int): Fragment {
        return ImageDetailsFragment.newInstance(imagesList[position])
    }

    fun add(index: Int, image: Image) {
        imagesList.add(index, image)
        notifyItemChanged(index)
    }

    fun refreshFragment(index: Int, image: Image) {
        imagesList[index] = image
        notifyItemChanged(index)
    }

    fun remove(index: Int) {
        imagesList.removeAt(index)
        notifyItemChanged(index)
    }

    override fun getItemId(position: Int): Long {
        return imagesList[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return imagesList.find { it.hashCode().toLong() == itemId } != null
    }
}