package uniovi.eii.shareit.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uniovi.eii.shareit.databinding.LineRecyclerViewParticipantBinding
import uniovi.eii.shareit.model.Participant

class ParticipantsListAdapter(
    private val participantsList: List<Participant> = emptyList(),
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
        private val name = binding.participantName
        private val mail = binding.participantMail
        private val role = binding.participantRole
        private val image = binding.participantImage

        fun assignValuesToComponents(participant: Participant) {
            name.text = participant.name
            mail.text = participant.email
            role.text = participant.role
//            image.setImageURI(participant.imagePath.toUri())
        }
    }
}