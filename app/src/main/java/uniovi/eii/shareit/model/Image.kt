package uniovi.eii.shareit.model

import android.os.Parcel
import android.os.Parcelable
import java.util.Date

data class Image(
    var imageId: String = "",
    var authorName : String = "",
    var authorId: String = "",
    var albumId: String = "",
    var imagePath : String = "",
    var creationDate: Date = Date(),
    var likes : Int = 0,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        Date(parcel.readLong()),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(imageId)
        parcel.writeString(authorName)
        parcel.writeString(authorId)
        parcel.writeString(albumId)
        parcel.writeString(imagePath)
        parcel.writeLong(creationDate.time)
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
