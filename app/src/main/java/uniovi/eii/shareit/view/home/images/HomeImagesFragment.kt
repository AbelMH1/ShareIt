package uniovi.eii.shareit.view.home.images

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import uniovi.eii.shareit.databinding.FragmentHomeImagesBinding
import uniovi.eii.shareit.viewModel.HomeViewModel

class HomeImagesFragment : Fragment() {

    companion object {
        fun newInstance() = HomeImagesFragment()
    }

    private var _binding: FragmentHomeImagesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeImagesBinding.inflate(inflater, container, false)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own images action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}