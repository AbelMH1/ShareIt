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
import uniovi.eii.shareit.model.Role

class ParticipantsListAdapter(
    private val creatorId: String = "",
    private val isUserOwner: Boolean = false,
    private var roleList: List<String> = emptyList(),
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
        holder.assignValuesToComponents(participant, position, creatorId, isUserOwner, roleList)
    }

    fun getLastSelectedItem(): Participant {
        return participantsList[selectedItemPosition]
    }

    inner class ParticipantViewHolder(binding: LineRecyclerViewParticipantBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {
        private val name = binding.participantName
        private val mail = binding.participantMail
        private val role = binding.participantRole
        private val image = binding.participantImage
        private lateinit var participantRole: Role

        fun assignValuesToComponents(
            participant: Participant,
            position: Int,
            creatorId: String,
            isUserOwner: Boolean,
            roleList: List<String>
        ) {
            name.text = participant.name
            mail.text = participant.email
            participantRole = participant.role
            role.text = roleList[participantRole.ordinal]
//            image.setImageURI(participant.imagePath.toUri())
            if (isUserOwner && participant.participantId != creatorId) {
                itemView.setOnCreateContextMenuListener(this)
                itemView.setOnClickListener {
                    selectedItemPosition = position
                    it.showContextMenu()
                }
                itemView.setOnLongClickListener {
                    selectedItemPosition = position
                    false
                }
            } else {
                itemView.setOnClickListener(null)
                itemView.setOnLongClickListener(null)
                itemView.isLongClickable = false
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
            when (participantRole) {
                Role.GUEST -> menu?.removeItem(R.id.action_demote_to_guest)
                Role.MEMBER -> menu?.removeItem(R.id.action_promote_to_member)
                else -> {}
            }
        }
    }
}