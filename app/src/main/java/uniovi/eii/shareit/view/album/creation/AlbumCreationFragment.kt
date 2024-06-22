package uniovi.eii.shareit.view.album.creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumCreationBinding
import uniovi.eii.shareit.viewModel.AlbumCreationViewModel

class AlbumCreationFragment : Fragment() {

    private var _binding: FragmentAlbumCreationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: AlbumCreationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumCreationBinding.inflate(inflater, container, false)

        viewModel.text.observe(viewLifecycleOwner) {
            binding.nameEditText.setText(it)
        }

        binding.dateToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.toggleNone -> {
                        binding.dateStartLayout.visibility = View.GONE
                        binding.dateEndLayout.visibility = View.GONE
                    }

                    R.id.toggleSingle -> {
                        binding.dateStartLayout.visibility = View.VISIBLE
                        binding.dateEndLayout.visibility = View.GONE
                    }

                    R.id.toggleRange -> {
                        binding.dateStartLayout.visibility = View.VISIBLE
                        binding.dateEndLayout.visibility = View.VISIBLE
                    }
                }
            }
        }
        binding.toggleNone.performClick()

        binding.dateStartLayout.setEndIconOnClickListener {
            // Show date picker
        }

        binding.dateEndLayout.setEndIconOnClickListener {
            // Show date picker
        }

        binding.switchLocationSelection.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    binding.mapView.visibility = View.VISIBLE
                }

                false -> {
                    binding.mapView.visibility = View.GONE
                }
            }
        }

        binding.switchSharedAlbum.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    binding.continueBtn.visibility = View.VISIBLE
                    binding.createBtn.visibility = View.GONE
                }

                false -> {
                    binding.continueBtn.visibility = View.GONE
                    binding.createBtn.visibility = View.VISIBLE
                }
            }
        }

        binding.continueBtn.setOnClickListener {
            findNavController().navigate(R.id.nav_album_creation_shared)
        }

        binding.createBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureToolBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val toolbar = requireActivity().findViewById(R.id.toolbar) as MaterialToolbar
        toolbar.subtitle = null
        _binding = null
    }

    private fun configureToolBar() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                val toolbar = requireActivity().findViewById(R.id.toolbar) as MaterialToolbar
                toolbar.isTitleCentered = false
                toolbar.subtitle = resources.getString(R.string.menu_album_creation_general)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                menu.removeItem(R.id.action_account)
            }
        }, viewLifecycleOwner)
    }

}