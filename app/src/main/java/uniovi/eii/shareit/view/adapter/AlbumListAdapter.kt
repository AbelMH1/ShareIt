package uniovi.eii.shareit.view.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uniovi.eii.shareit.databinding.LineRecyclerViewAlbumBinding
import uniovi.eii.shareit.model.Album

class AlbumListAdapter(
    private var albumList: List<Album> = emptyList(),
    private val cardListener: OnItemClickListener,
    private val infoBtnListener: OnItemClickListener
) : RecyclerView.Adapter<AlbumListAdapter.AlbumViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun update(albumList: List<Album>) {
        this.albumList = albumList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        return AlbumViewHolder(
            LineRecyclerViewAlbumBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albumList[position]
        Log.i("AlbumListAdapter", "Visualiza elemento: $album")
        holder.assignValuesToComponents(album, position, cardListener, infoBtnListener)
    }

    override fun getItemCount(): Int = albumList.size

    interface OnItemClickListener {
        fun onItemClick(item: Album, position: Int)
    }

    inner class AlbumViewHolder(private val binding: LineRecyclerViewAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun assignValuesToComponents(album: Album, position: Int, cardListener: OnItemClickListener, infoBtnListener: OnItemClickListener) {
            binding.albumName.text = album.name
            binding.creatorName.text = album.creatorName
            binding.infoBtn.setOnClickListener {
                Log.d("AlbumListAdapter", "Click en información del álbum")
                infoBtnListener.onItemClick(album, position)
            }
            itemView.setOnClickListener {
                Log.d("AlbumListAdapter", "Click en la cardView")
                cardListener.onItemClick(album, position)
            }
        }
    }

}