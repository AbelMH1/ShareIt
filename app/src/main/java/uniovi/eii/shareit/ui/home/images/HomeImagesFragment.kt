package uniovi.eii.shareit.ui.home.images

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import uniovi.eii.shareit.databinding.FragmentHomeImagesBinding

class HomeImagesFragment : Fragment() {

    companion object {
        fun newInstance() = HomeImagesFragment()
    }

    private var _binding: FragmentHomeImagesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeImagesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(HomeImagesViewModel::class.java)
        _binding = FragmentHomeImagesBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}