package uniovi.eii.shareit.model

import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoField

data class Image(
    var author : String = "",
    var albumName: String = "",
    var imagePath : String = "",
    var creationDate: LocalDateTime = LocalDateTime.now(),
    var likes : List<String> = emptyList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        LocalDateTime.ofEpochSecond(parcel.readLong(), 0, ZoneOffset.UTC),
        parcel.createStringArrayList().orEmpty()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(author)
        parcel.writeString(albumName)
        parcel.writeString(imagePath)
        parcel.writeLong(creationDate.getLong(ChronoField.INSTANT_SECONDS))
        parcel.writeStringList(likes)
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
