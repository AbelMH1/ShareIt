package uniovi.eii.shareit.utils

 import android.Manifest.permission.CAMERA
 import android.Manifest.permission.POST_NOTIFICATIONS
 import android.Manifest.permission.READ_EXTERNAL_STORAGE
 import android.Manifest.permission.READ_MEDIA_IMAGES
 import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
 import android.content.Context
 import android.content.pm.PackageManager.PERMISSION_GRANTED
 import android.os.Build.VERSION
 import android.os.Build.VERSION_CODES
 import androidx.core.content.ContextCompat

fun Context.hasGalleryPermission(): Boolean {
    return when {
        // Full access on Android 13 (API level 33) or higher
        VERSION.SDK_INT >= VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES) == PERMISSION_GRANTED -> true
        // Partial access on Android 14 (API level 34) or higher
        VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE &&
                ContextCompat.checkSelfPermission(this, READ_MEDIA_VISUAL_USER_SELECTED) == PERMISSION_GRANTED -> true
        // Full access up to Android 12 (API level 32)
        ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED -> true
        else -> false
    }
}

fun getRequiredGalleryPermissions(): Array<String> {
    return when {
        VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE ->
            arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VISUAL_USER_SELECTED)
        VERSION.SDK_INT >= VERSION_CODES.TIRAMISU ->
            arrayOf(READ_MEDIA_IMAGES)
        else ->
            arrayOf(READ_EXTERNAL_STORAGE)
    }
}

fun Context.hasCameraPermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, CAMERA) == PERMISSION_GRANTED
}

fun Context.hasNotificationPermission(): Boolean {
    return if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PERMISSION_GRANTED
    } else {
        true // Notification permission is not required for versions below Android 13
    }
}