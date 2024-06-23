package uniovi.eii.shareit.view.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import uniovi.eii.shareit.databinding.LineRecyclerViewImageSectionBinding
import uniovi.eii.shareit.model.Section

class SectionListAdapter(
    private var sectionList: List<Section> = emptyList(),
    private val listener: SectionImageListAdapter.OnItemClickListener
) : RecyclerView.Adapter<SectionListAdapter.SectionViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun update(sectionList: List<Section>) {
        this.sectionList = sectionList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        return SectionViewHolder(
            LineRecyclerViewImageSectionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val accPos = sectionList.subList(0, position).sumOf { s: Section -> s.imageList.size }
        val section = sectionList[position]
        Log.i("Lista", "Visualiza elemento: $section")
        holder.assignValuesToComponents(section, listener, position, accPos)
    }

    override fun getItemCount(): Int = sectionList.size

    inner class SectionViewHolder(private val binding: LineRecyclerViewImageSectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun assignValuesToComponents(section: Section, listener: SectionImageListAdapter.OnItemClickListener, position: Int, accPos: Int) {
            binding.sectionTitle.text = section.sectionTitle
            val adapter = SectionImageListAdapter(section.imageList, listener, accPos)
            binding.imagesRecyclerView.layoutManager = GridLayoutManager(itemView.context, 5)
            binding.imagesRecyclerView.adapter = adapter
        }
    }

}