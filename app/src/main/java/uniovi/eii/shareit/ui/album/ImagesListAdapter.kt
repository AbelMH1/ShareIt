package uniovi.eii.shareit.ui.album

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import uniovi.eii.shareit.databinding.FragmentAlbumSmallImageBinding
import uniovi.eii.shareit.ui.album.placeholder.PlaceholderContent.PlaceholderItem

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class ImagesListAdapter(
    private val imagesList: List<PlaceholderItem>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ImagesListAdapter.ImagesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        return ImagesViewHolder(
            FragmentAlbumSmallImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        val image = imagesList[position]
        Log.i("Lista", "Visualiza elemento: $image")
        holder.assignValuesToComponents(image, listener)
    }

    override fun getItemCount(): Int = imagesList.size

    interface OnItemClickListener {
        fun onItemClick(item : PlaceholderItem)
    }

    inner class ImagesViewHolder(binding: FragmentAlbumSmallImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val idView: TextView = binding.itemNumber
        private val contentView: ImageView = binding.image

        override fun toString(): String {
            return super.toString() + " '" + contentView.contentDescription + "'"
        }

        fun assignValuesToComponents(image: PlaceholderItem, listener : OnItemClickListener){
            idView.text = image.id
            contentView.setImageResource(image.content)
            itemView.setOnClickListener {
                listener.onItemClick(image)
            }
        }
    }

}