package uniovi.eii.shareit.ui.album.information.display

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import uniovi.eii.shareit.databinding.FragmentAlbumInformationGeneralBinding
import uniovi.eii.shareit.ui.album.information.AlbumInformationViewModel

class AlbumInformationGeneralFragment : Fragment() {

    companion object {
        fun newInstance() = AlbumInformationGeneralFragment()
    }

    private var _binding: FragmentAlbumInformationGeneralBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlbumInformationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumInformationGeneralBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}