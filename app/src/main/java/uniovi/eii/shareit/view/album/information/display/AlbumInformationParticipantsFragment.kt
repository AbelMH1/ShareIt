package uniovi.eii.shareit.view.album.information.display

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumInformationParticipantsBinding
import uniovi.eii.shareit.model.Participant
import uniovi.eii.shareit.view.MainActivity.ErrorCleaningTextWatcher
import uniovi.eii.shareit.view.adapter.ParticipantsListAdapter
import uniovi.eii.shareit.viewModel.AlbumInformationViewModel
import uniovi.eii.shareit.viewModel.AlbumViewModel
import java.util.Locale

class AlbumInformationParticipantsFragment : Fragment() {

    companion object {
        fun newInstance() = AlbumInformationParticipantsFragment()
    }

    private var _binding: FragmentAlbumInformationParticipantsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlbumInformationViewModel by navGraphViewModels(R.id.navigation_album)
    private val albumViewModel: AlbumViewModel by navGraphViewModels(R.id.navigation_album)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumInformationParticipantsBinding.inflate(inflater, container, false)

        binding.recyclerParticipants.layoutManager = LinearLayoutManager(context)
        binding.recyclerParticipants.adapter =
            ParticipantsListAdapter(albumViewModel.getAlbumInfo().creatorId,
                albumViewModel.isCurrentUserOwner(),
                resources.getStringArray(R.array.ParticipantRole).toList())

        binding.addParticipantBtn.setOnClickListener {
            addNewParticipant()
        }

        viewModel.participants.observe(viewLifecycleOwner) {
            updateUI(it)
        }

        viewModel.addParticipantAttempt.observe(viewLifecycleOwner) {
            if (it.isDataValid) {
                binding.addParticipantEditText.text = null
            } else {
                updateErrors(it.emailError, it.firestoreError)
            }
            enableForm(true)
        }

        albumViewModel.album.observe(viewLifecycleOwner) {
            val isUserOwner = albumViewModel.isCurrentUserOwner()
            val isAlbumPrivate = albumViewModel.isAlbumPrivate()
            binding.ownerAddingLayout.isVisible = isUserOwner && !isAlbumPrivate
            binding.labelEnableSharedToAddParticipantInfo.isVisible = isUserOwner && isAlbumPrivate
        }

        binding.addParticipantEditText.addTextChangedListener(ErrorCleaningTextWatcher(binding.addParticipantLayout))

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.addParticipantLayout.error = null
        binding.addParticipantEditText.text = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val recyclerViewAdapter = (binding.recyclerParticipants.adapter as ParticipantsListAdapter)
        val participant = recyclerViewAdapter.getLastSelectedItem()
        when (item.itemId) {
            R.id.action_promote_to_member -> promoteParticipant(participant)
            R.id.action_demote_to_guest -> demoteParticipant(participant)
            R.id.action_eliminate_participant -> eliminateParticipant(participant)
            else -> return super.onContextItemSelected(item)
        }
        return true
    }

    private fun updateUI(participants: List<Participant>) {
        (binding.recyclerParticipants.adapter as ParticipantsListAdapter).update(participants)
    }

    private fun enableForm(enabled: Boolean) {
        binding.addParticipantLayout.isEnabled = enabled
        binding.addParticipantBtn.isEnabled = enabled
        binding.addParticipantBtn.text =
            if (enabled) resources.getString(R.string.btn_add) else null
        binding.progressBar.isVisible = !enabled
    }

    private fun updateErrors(
        emailError: Int?, firestoreError: String?
    ) {
        if (emailError != null) {
            binding.addParticipantLayout.error = resources.getString(emailError)
            binding.addParticipantEditText.requestFocus()
        }
        if (firestoreError != null) {
            Toast.makeText(context, resources.getString(R.string.toast_error_adding_participant, firestoreError), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun addNewParticipant() {
        enableForm(false)
        viewModel.addNewParticipant(
            binding.addParticipantEditText.text?.toString()?.lowercase(Locale.getDefault()) ?: ""
        )
    }

    private fun eliminateParticipant(participant: Participant) {
        viewModel.eliminateParticipant(participant)
        Toast.makeText(context, resources.getString(R.string.toast_participant_removed, participant.name), Toast.LENGTH_SHORT)
            .show()
    }

    private fun promoteParticipant(participant: Participant) {
        viewModel.promoteParticipant(participant)
        Toast.makeText(context, resources.getString(R.string.toast_participant_promoted, participant.name), Toast.LENGTH_SHORT)
            .show()
    }

    private fun demoteParticipant(participant: Participant) {
        viewModel.demoteParticipant(participant)
        Toast.makeText(context, resources.getString(R.string.toast_participant_demoted, participant.name), Toast.LENGTH_SHORT)
            .show()
    }
}