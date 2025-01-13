package uniovi.eii.shareit.view.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.LineRecyclerViewParticipantBinding
import uniovi.eii.shareit.model.Participant

class ParticipantsListAdapter(
    private val creatorId: String = "",
    private val userRole: String = "",
    private var participantsList: List<Participant> = emptyList(),
    private var selectedItemPosition: Int = -1
) : RecyclerView.Adapter<ParticipantsListAdapter.ParticipantViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun update(participantsList: List<Participant>) {
        this.participantsList = participantsList
        notifyDataSetChanged()
    }

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
        holder.assignValuesToComponents(participant, position, creatorId, userRole)
    }

    fun getLastSelectedItemPosition(): Int = selectedItemPosition

    fun getParticipants(): List<Participant> = participantsList

    inner class ParticipantViewHolder(binding: LineRecyclerViewParticipantBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {
        private val name = binding.participantName
        private val mail = binding.participantMail
        private val role = binding.participantRole
        private val image = binding.participantImage

        fun assignValuesToComponents(
            participant: Participant,
            position: Int,
            creatorId: String,
            userRole: String
        ) {
            name.text = participant.name
            mail.text = participant.email
            role.text = participant.role
//            image.setImageURI(participant.imagePath.toUri())
            if (userRole == Participant.OWNER && participant.participantId != creatorId) {
                itemView.setOnCreateContextMenuListener(this)
                itemView.setOnClickListener {
                    selectedItemPosition = position
                    it.showContextMenu()
                }
                itemView.setOnLongClickListener {
                    selectedItemPosition = position
                    false
                }
            }
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu?.setHeaderTitle(name.text)
            val inflater = (v?.context as Activity).menuInflater
            inflater.inflate(R.menu.album_participant_options, menu)
            when (role.text) {
                Participant.GUEST -> menu?.removeItem(R.id.action_demote_to_guest)
                Participant.MEMBER -> menu?.removeItem(R.id.action_promote_to_member)
            }
        }
    }
}