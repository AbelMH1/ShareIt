package uniovi.eii.shareit.view.album.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentImageDetailsBinding
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.Participant.Role
import uniovi.eii.shareit.utils.loadImageIntoView
import uniovi.eii.shareit.utils.toFormattedImageDetailsString
import uniovi.eii.shareit.viewModel.AlbumViewModel
import uniovi.eii.shareit.viewModel.ImageDetailsViewModel
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel.Companion.ALBUM_VIEW
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel.Companion.ImagesDisplayViewModelFactory
import java.util.Locale

class ImageDetailsFragment : Fragment() {

    companion object {
        const val IMAGE = "image"
        const val POSITION = "position"
        const val USINGVIEWMODEL = "usingViewModel"
        @JvmStatic fun newInstance(image: Image, position: Int, usingViewModel: String) =
            ImageDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(IMAGE, image)
                    putInt(POSITION, position)
                    putString(USINGVIEWMODEL, usingViewModel)
                }
            }
    }

    private var image = Image()
    private var position = 0
    private lateinit var usingViewModel: String

    private var _binding: FragmentImageDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel : ImageDetailsViewModel by viewModels()
    private lateinit var imagesViewModel: ImagesDisplayViewModel
    private lateinit var albumViewModel: AlbumViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            image = it.getParcelable(IMAGE) ?: Image()
            position = it.getInt(POSITION, 0)
            usingViewModel = it.getString(USINGVIEWMODEL)!!
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageDetailsBinding.inflate(inflater, container, false)
        imagesViewModel = ViewModelProvider(
            requireActivity(),
            ImagesDisplayViewModelFactory()
        )[usingViewModel, ImagesDisplayViewModel::class.java]

        imagesViewModel.displayImageList.observe(viewLifecycleOwner) {
            updateImageDetails(it[position])
        }

        imagesViewModel.updatedImageLikes.observe(viewLifecycleOwner) { updatedLikes ->
            if (updatedLikes.containsKey(image.imageId)) {
                updateImageDetails(updatedLikes[image.imageId]!!)
            }
        }

        viewModel.registerLikedListener(image)
        viewModel.isLikedByCurrentUser.observe(viewLifecycleOwner) {
            binding.likeBtn.isChecked = it
        }

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

            binding.likeBtn.setOnClickListener {
                if (binding.likeBtn.isChecked) {
                    viewModel.likeImage(image)
                    imagesViewModel.incrementImageLikes(image.imageId, 1)
                    Toast.makeText(context, "Liked Image", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.unlikeImage(image)
                    imagesViewModel.incrementImageLikes(image.imageId, -1)
                    Toast.makeText(context, "Removed like", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            binding.likeBtn.isEnabled = false
        }

        return binding.root
    }

    private fun updateImageDetails(image: Image) {
        binding.userName.text = image.authorName
        binding.dateUpload.text = image.creationDate.toFormattedImageDetailsString()
        requireContext().loadImageIntoView(image.imagePath.toUri(), binding.image)
    }

    private fun updateImageDetails(numLikes: Int) {
        binding.numLikes.text = String.format(Locale.getDefault(), numLikes.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.unregisterLikedListener()
        _binding = null
    }

}