package uniovi.eii.shareit.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.toDate(): Date? {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    sdf.isLenient = false
    return try {
        sdf.parse(this.trim())
    } catch (e: ParseException) {
        null
    }
}
