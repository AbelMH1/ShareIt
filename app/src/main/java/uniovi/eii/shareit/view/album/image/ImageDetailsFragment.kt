package uniovi.eii.shareit.view.album.image

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.FragmentImageDetailsBinding
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.Participant.Role
import uniovi.eii.shareit.utils.getMinutesStableValue
import uniovi.eii.shareit.utils.loadCircularImageIntoView
import uniovi.eii.shareit.utils.loadImageIntoView
import uniovi.eii.shareit.utils.toFormattedImageDetailsString
import uniovi.eii.shareit.viewModel.AlbumViewModel
import uniovi.eii.shareit.viewModel.ImageDetailsViewModel
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel.Companion.ALBUM_VIEW
import uniovi.eii.shareit.viewModel.ImagesDisplayViewModel.Companion.ImagesDisplayViewModelFactory
import java.util.Date
import java.util.Locale

class ImageDetailsFragment : Fragment() {

    companion object {
        const val IMAGE = "image"
        const val POSITION = "position"
        const val USINGVIEWMODEL = "usingViewModel"
        @JvmStatic
        fun newInstance(image: Image, position: Int, usingViewModel: String) =
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
    private val viewModel: ImageDetailsViewModel by viewModels()
    private lateinit var imagesViewModel: ImagesDisplayViewModel
    private lateinit var albumViewModel: AlbumViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            image = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(IMAGE, Image::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelable(IMAGE)
            } ?: Image()
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

        viewModel.isCompletedImageDeletion.observe(viewLifecycleOwner) { success ->
            if (success) {
                // Volver al fragmento anterior
                Toast.makeText(
                    context,
                    resources.getString(R.string.info_image_deleted),
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            } else {
                Snackbar.make(
                    binding.root,
                    resources.getString(R.string.error_deleting_image),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.registerLikedListener(image)
        viewModel.isLikedByCurrentUser.observe(viewLifecycleOwner) {
            binding.likeBtn.isChecked = it
        }

        binding.btMoreOptions.setOnClickListener {
            showMoreOptionsMenu()
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
                } else {
                    viewModel.unlikeImage(image)
                    imagesViewModel.incrementImageLikes(image.imageId, -1)
                }
            }
        } else {
            binding.likeBtn.setOnClickListener {
                binding.likeBtn.isChecked = !binding.likeBtn.isChecked
                Snackbar.make(
                    binding.root,
                    resources.getString(R.string.warn_like_only_enabled_in_albums),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        return binding.root
    }

    private fun updateImageDetails(image: Image) {
        binding.userName.text = image.authorName
        binding.dateUpload.text = image.creationDate.toFormattedImageDetailsString()
        requireContext().loadImageIntoView(image.imagePath.toUri(), binding.image, centerCrop = false)
        requireContext().loadCircularImageIntoView(image.authorImage, Date().getMinutesStableValue(), binding.userImage)
    }

    private fun updateImageDetails(numLikes: Int) {
        binding.numLikes.text = String.format(Locale.getDefault(), numLikes.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.unregisterLikedListener()
        _binding = null
    }

    private fun showMoreOptionsMenu() {
        val popupMenu = PopupMenu(requireContext(), binding.btMoreOptions)
        popupMenu.menuInflater.inflate(R.menu.image_details_more_options, popupMenu.menu)

        popupMenu.setForceShowIcon(true)

        // Verificar permisos y establecer visibilidad del ítem de eliminación
        val deleteItem = popupMenu.menu.findItem(R.id.action_delete_image)
        deleteItem.icon?.setTint(resources.getColor(R.color.md_theme_error_mediumContrast, null))
        deleteItem.isVisible = usingViewModel == ALBUM_VIEW && albumViewModel.canDeleteImage(image)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete_image -> {
                    showDeleteConfirmationDialog()
                }
                else -> return@setOnMenuItemClickListener false
            }
            true
        }

        popupMenu.show()
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.warn_delete_image_title))
            .setMessage(resources.getString(R.string.warn_delete_image_message))
            .setPositiveButton(R.string.btn_accept) { _, _ ->
                viewModel.deleteImage(image)
            }
            .setNeutralButton(R.string.btn_cancel, null)
            .show()
    }
}