package uniovi.eii.shareit.view.album.information.display

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumInformationParticipantsBinding
import uniovi.eii.shareit.model.Participant
import uniovi.eii.shareit.view.adapter.ParticipantsListAdapter
import uniovi.eii.shareit.viewModel.AlbumInformationViewModel

class AlbumInformationParticipantsFragment : Fragment() {

    companion object {
        fun newInstance() = AlbumInformationParticipantsFragment()
    }

    private var _binding: FragmentAlbumInformationParticipantsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlbumInformationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumInformationParticipantsBinding.inflate(inflater, container, false)

        val isUserOwner = viewModel.isCurrentUserOwner()
        binding.recyclerParticipants.layoutManager = LinearLayoutManager(context)
        binding.recyclerParticipants.adapter =
            ParticipantsListAdapter(viewModel.getAlbumInfo().creatorId, isUserOwner)

        binding.ownerAddingLayout.isVisible = isUserOwner

        viewModel.participants.observe(viewLifecycleOwner) {
            updateUI(it)
        }
        return binding.root
    }

    private fun updateUI(participants: List<Participant>) {
        (binding.recyclerParticipants.adapter as ParticipantsListAdapter).update(participants)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val position = (binding.recyclerParticipants.adapter as ParticipantsListAdapter).getLastSelectedItemPosition()
        when (item.itemId) {
            R.id.action_promote_to_member -> {
                Toast.makeText(context, "Member promoted ${position+1}", Toast.LENGTH_SHORT).show()
            }

            R.id.action_demote_to_guest -> {
                Toast.makeText(context, "Member demoted ${position+1}", Toast.LENGTH_SHORT).show()
            }

            R.id.action_eliminate_participant -> {
                Toast.makeText(context, "Member eliminated ${position+1}", Toast.LENGTH_SHORT).show()
            }

            else -> return super.onContextItemSelected(item)
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}