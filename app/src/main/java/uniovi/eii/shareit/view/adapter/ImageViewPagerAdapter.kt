package uniovi.eii.shareit.view.adapter

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.view.album.image.ImageDetailsFragment

class ImageViewPagerAdapter(
    fragment: Fragment,
    private var imagesList: MutableList<Image> = mutableListOf()
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return imagesList.size
    }

    override fun createFragment(position: Int): Fragment {
        return ImageDetailsFragment.newInstance(imagesList[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(imagesList: MutableList<Image>) {
        this.imagesList = imagesList
        notifyDataSetChanged()
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