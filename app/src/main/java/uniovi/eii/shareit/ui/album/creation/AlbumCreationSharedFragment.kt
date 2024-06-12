package uniovi.eii.shareit.ui.album.creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentAlbumCreationSharedBinding

class AlbumCreationSharedFragment : Fragment() {

    private var _binding: FragmentAlbumCreationSharedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlbumCreationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumCreationSharedBinding.inflate(inflater, container, false)

        binding.switchInvitationLink.setOnCheckedChangeListener { _, isChecked ->
            when(isChecked) {
                true -> {
                    binding.invitationLinkLayout.visibility = View.VISIBLE
                }
                false -> {
                    binding.invitationLinkLayout.visibility = View.GONE
                }
            }
        }

        binding.invitationLinkLayout.setEndIconOnClickListener {
            Toast.makeText(context, "Copy", Toast.LENGTH_SHORT).show()
        }

        binding.createBtn.setOnClickListener {
            findNavController().navigateUp()
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
                toolbar.subtitle = resources.getString(R.string.menu_album_creation_shared)
                menu.clear()
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner)
    }

}