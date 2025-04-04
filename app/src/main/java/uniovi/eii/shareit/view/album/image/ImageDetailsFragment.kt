package uniovi.eii.shareit.view.album.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentImageDetailsBinding
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.Participant.Role
import uniovi.eii.shareit.viewModel.AlbumViewModel
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel.Companion.ALBUM_VIEW
import java.util.Locale

class ImageDetailsFragment : Fragment() {

    companion object {
        const val IMAGE = "image"
        const val USINGVIEWMODEL = "usingViewModel"
        @JvmStatic fun newInstance(image: Image, usingViewModel: String) =
            ImageDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(IMAGE, image)
                    putString(USINGVIEWMODEL, usingViewModel)
                }
            }
    }

    private var image = Image()
    private lateinit var usingViewModel: String

    private var _binding: FragmentImageDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var albumViewModel: AlbumViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            image = it.getParcelable(IMAGE) ?: Image()
            usingViewModel = it.getString(USINGVIEWMODEL)!!
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageDetailsBinding.inflate(inflater, container, false)

        if (usingViewModel == ALBUM_VIEW) {
            albumViewModel = ViewModelProvider(
                findNavController().getViewModelStoreOwner(R.id.navigation_album)
            )[AlbumViewModel::class.java]

            albumViewModel.album.observe(viewLifecycleOwner) {
                binding.likeBtn.isEnabled = albumViewModel.hasImagesVotePermission()
            }
            albumViewModel.currentUserRole.observe(viewLifecycleOwner) {
                if (it != Role.NONE) {
                    binding.likeBtn.isEnabled = albumViewModel.hasImagesVotePermission()
                }
            }

            binding.likeBtn.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) Toast.makeText(context, "Liked Image", Toast.LENGTH_SHORT).show()
                else Toast.makeText(context, "Removed like", Toast.LENGTH_SHORT).show()
            }
        } else {
            binding.likeBtn.isEnabled = false
        }

        binding.userName.text = image.authorName
        binding.dateUpload.text = image.creationDate.toString()
        binding.numLikes.text = String.format(Locale.getDefault(), image.likes.size.toString())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}