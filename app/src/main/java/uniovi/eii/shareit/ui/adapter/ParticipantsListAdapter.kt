package uniovi.eii.shareit.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uniovi.eii.shareit.databinding.LineRecyclerViewParticipantBinding
import uniovi.eii.shareit.ui.album.placeholder.PlaceholderContent.PlaceholderItem

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class ParticipantsListAdapter(
    private val participantsList: List<PlaceholderItem> = emptyList(),
) : RecyclerView.Adapter<ParticipantsListAdapter.ParticipantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        return ParticipantViewHolder(
            LineRecyclerViewParticipantBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int = participantsList.size

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val participant = participantsList[position]
        Log.i("Lista", "Visualiza elemento: $participant")
        holder.assignValuesToComponents(participant)
    }

    inner class ParticipantViewHolder(binding: LineRecyclerViewParticipantBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val participantText = binding.participantName

        fun assignValuesToComponents(participant: PlaceholderItem) {
            participantText.text = "Participant ${participant.id}"
        }
    }
}