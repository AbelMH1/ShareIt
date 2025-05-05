package uniovi.eii.shareit.model

import android.os.Parcel
import android.os.Parcelable
import java.util.Date

data class Image(
    var imageId: String = "",
    var authorId: String = "",
    var authorName : String = "",
    var albumId: String = "",
    var albumName: String = "",
    var imagePath : String = "",
    var creationDate: Date = Date(),
    var authorImage: String = "",
    var likes : Int = 0,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        Date(parcel.readLong()),
        parcel.readString() ?: "",
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(imageId)
        parcel.writeString(authorId)
        parcel.writeString(authorName)
        parcel.writeString(albumId)
        parcel.writeString(albumName)
        parcel.writeString(imagePath)
        parcel.writeLong(creationDate.time)
        parcel.writeString(authorImage)
        parcel.writeInt(likes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Image> {
        override fun createFromParcel(parcel: Parcel): Image {
            return Image(parcel)
        }

        override fun newArray(size: Int): Array<Image?> {
            return arrayOfNulls(size)
        }
    }
}
