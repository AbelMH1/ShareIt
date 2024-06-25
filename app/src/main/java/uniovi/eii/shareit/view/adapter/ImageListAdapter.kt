package uniovi.eii.shareit.view.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.LineRecyclerViewImageBinding
import uniovi.eii.shareit.model.Image

class ImageListAdapter(
    private var imagesList: List<Image> = emptyList(),
    private val listener: OnItemClickListener,
    private val accPos: Int = 0
) : RecyclerView.Adapter<ImageListAdapter.ImageViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun update(imagesList: List<Image>) {
        this.imagesList = imagesList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            LineRecyclerViewImageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = imagesList[position]
        Log.i("Lista", "Visualiza elemento: $image")
        holder.assignValuesToComponents(image, listener, position, accPos)
    }

    override fun getItemCount(): Int = imagesList.size

    interface OnItemClickListener {
        fun onItemClick(item: Image, position: Int)
    }

    inner class ImageViewHolder(private val binding: LineRecyclerViewImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun assignValuesToComponents(image: Image, listener: OnItemClickListener, position: Int, accumulatedPosition: Int) {
            binding.image.setImageResource(R.drawable.ic_menu_gallery) // TODO: Añadir imagen
            itemView.setOnClickListener {
                listener.onItemClick(image, accumulatedPosition+position)
            }
        }
    }

}