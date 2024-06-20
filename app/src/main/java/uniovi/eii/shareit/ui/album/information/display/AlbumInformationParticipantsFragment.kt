package uniovi.eii.shareit.ui.album.information.display

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import uniovi.eii.shareit.databinding.FragmentAlbumInformationParticipantsBinding
import uniovi.eii.shareit.ui.adapter.ParticipantsListAdapter
import uniovi.eii.shareit.ui.album.information.AlbumInformationViewModel
import uniovi.eii.shareit.ui.album.placeholder.PlaceholderContent

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

        binding.recyclerParticipants.layoutManager = LinearLayoutManager(context)
        binding.recyclerParticipants.adapter = ParticipantsListAdapter(PlaceholderContent.getParticipantsList(20))

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}