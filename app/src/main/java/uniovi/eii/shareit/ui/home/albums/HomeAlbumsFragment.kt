package uniovi.eii.shareit.ui.home.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
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
    ): View? {
        viewModel = ViewModelProvider(this).get(HomeAlbumsViewModel::class.java)
        _binding = FragmentHomeAlbumsBinding.inflate(inflater, container, false)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own album action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}