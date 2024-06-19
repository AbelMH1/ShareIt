package uniovi.eii.shareit.ui.album.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import uniovi.eii.shareit.databinding.FragmentImageDetailsBinding
import uniovi.eii.shareit.model.Image

class ImageDetailsFragment : Fragment() {

    companion object {
        const val IMAGE = "image"
        @JvmStatic fun newInstance(image: Image) =
            ImageDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(IMAGE, image)
                }
            }
    }

    private var image = Image()

    private var _binding: FragmentImageDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            image = it.getParcelable(IMAGE) ?: Image()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageDetailsBinding.inflate(inflater, container, false)

        binding.likeBtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) Toast.makeText(context, "Liked Image", Toast.LENGTH_SHORT).show()
            else Toast.makeText(context, "Removed like", Toast.LENGTH_SHORT).show()
        }

        binding.userName.text = image.author
        binding.dateUpload.text = image.creationDate.toString()

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