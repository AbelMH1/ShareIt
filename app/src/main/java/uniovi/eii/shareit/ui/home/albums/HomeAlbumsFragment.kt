package uniovi.eii.shareit.ui.home.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentHomeAlbumsBinding

class HomeAlbumsFragment : Fragment() {

    companion object {
        fun newInstance() = HomeAlbumsFragment()
    }

    private var _binding: FragmentHomeAlbumsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeAlbumsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[HomeAlbumsViewModel::class.java]
        _binding = FragmentHomeAlbumsBinding.inflate(inflater, container, false)

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.nav_album_creation)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}