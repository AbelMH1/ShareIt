package uniovi.eii.shareit.view.album

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumSmallImageBinding
import uniovi.eii.shareit.model.Image

class ImagesListAdapter(
    private var imagesList: List<Image> = emptyList(),
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ImagesListAdapter.ImagesViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun update(imagesList: List<Image>) {
        this.imagesList = imagesList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        return ImagesViewHolder(
            FragmentAlbumSmallImageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        val image = imagesList[position]
        Log.i("Lista", "Visualiza elemento: $image")
        holder.assignValuesToComponents(image, listener, position)
    }

    override fun getItemCount(): Int = imagesList.size

    interface OnItemClickListener {
        fun onItemClick(item: Image, position: Int)
    }

    inner class ImagesViewHolder(binding: FragmentAlbumSmallImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val idView: TextView = binding.itemNumber
        private val contentView: ImageView = binding.image

        override fun toString(): String {
            return super.toString() + " '" + contentView.contentDescription + "'"
        }

        fun assignValuesToComponents(image: Image, listener: OnItemClickListener, position: Int) {
            idView.text = image.author
            contentView.setImageResource(R.drawable.ic_menu_camera)
            itemView.setOnClickListener {
                listener.onItemClick(image, position)
            }
        }
    }

}