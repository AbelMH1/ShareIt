package uniovi.eii.shareit.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.VIEW_MODEL_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.Section
import uniovi.eii.shareit.view.album.placeholder.PlaceholderContent
import java.time.format.DateTimeFormatter


class ImagesDisplayViewModel(private val instanceKey: String) : ViewModel() {

    companion object {
        const val ALBUM_VIEW = "album"
        const val GENERAL_VIEW = "general"

        @Suppress("UNCHECKED_CAST") // Añadir el resto de parámetros necesarios en el constructor de la clase: ImagesDisplayViewModelFactory(private val extraParams: String)
        class ImagesDisplayViewModelFactory : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val instanceKey = checkNotNull(extras[VIEW_MODEL_KEY])
                if (modelClass.isAssignableFrom(ImagesDisplayViewModel::class.java)) {
                    return ImagesDisplayViewModel(instanceKey) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private val _imageList = MutableLiveData<List<Image>>(emptyList())
    val imageList: LiveData<List<Image>> = _imageList

    private val _sectionList = MutableLiveData<List<Section>>(emptyList())
    val sectionList: LiveData<List<Section>> = _sectionList

    init {
        Log.d("ImageViewModel", "View: $instanceKey")
        if (instanceKey == ALBUM_VIEW) {
            updateImageList(PlaceholderContent.getImagesList(15))
        } else {
            updateImageList(PlaceholderContent.getImagesList(20))
        }
    }

    fun getKey(): String {
        return instanceKey
    }

    fun updateImageList(newImages: List<Image>) {
        val newImagesValues = newImages.sortedByDescending { image -> image.creationDate }
        _imageList.value = newImagesValues
        val newSectionList: List<Section> =
            newImagesValues.groupBy { image: Image -> image.creationDate }.map { entry ->
                Section(
                    entry.key.format(DateTimeFormatter.ofPattern("d MMM uuuu")), entry.value
                )
            }
        updateSectionList(newSectionList)
    }

    fun updateSectionList(newSections: List<Section>) {
        _sectionList.value = newSections
    }
}

