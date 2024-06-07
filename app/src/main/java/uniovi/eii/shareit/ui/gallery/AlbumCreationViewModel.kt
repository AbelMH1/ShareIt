package uniovi.eii.shareit.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AlbumCreationViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Album Creation Fragment"
    }
    val text: LiveData<String> = _text
}