package uniovi.eii.shareit.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
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

fun Date.toFormattedString(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(this)
}

fun Date.toFormattedImageDetailsString(): String {
    val sdf = SimpleDateFormat("MMM d, yyyy; HH:mm", Locale.getDefault())
    return sdf.format(this)
}

fun Date.toFormattedChatDateString(): String {
    val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    return sdf.format(this)
}
fun Date.toFormattedChatHourString(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(this)
}

fun Date.areSameDay(other: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = this@areSameDay }
    val cal2 = Calendar.getInstance().apply { time = other }

    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun Date.getMinutesStableValue(minuteRange: Int = 10): Long {
    val intervalMillis = minuteRange * 60 * 1000 // minuteRange minutes in milliseconds
    return this.time / intervalMillis
}