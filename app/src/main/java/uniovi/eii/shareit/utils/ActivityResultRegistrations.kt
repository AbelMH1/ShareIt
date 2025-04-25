package uniovi.eii.shareit.utils

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

fun Fragment.registerPermissionsRequest(
    onPermissionsGranted: () -> Unit,
    onPermissionsDeniedMessage: Int
): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        Log.d("Permissions", "Results: $results")
        if (results.isNotEmpty() && results.values.all { it }) {
            onPermissionsGranted()
        } else {
            Toast.makeText(requireContext(), resources.getString(onPermissionsDeniedMessage), Toast.LENGTH_SHORT).show()
        }
    }
}

fun Fragment.registerPermissionRequest(
    onPermissionsGranted: () -> Unit,
    onPermissionsDeniedMessage: Int
): ActivityResultLauncher<String> {
    return registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        Log.d("Permissions", "Results: $isGranted")
        if (isGranted) {
            onPermissionsGranted()
        } else {
            Toast.makeText(requireContext(), resources.getString(onPermissionsDeniedMessage), Toast.LENGTH_SHORT).show()
        }
    }
}

fun Fragment.registerCameraPicker(
    onSuccess: () -> Unit,
): ActivityResultLauncher<Uri> {
    return registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            onSuccess()
        } else {
            Log.d("Camera", "No media selected")
        }
    }
}

fun Fragment.registerGalleryPicker(
    onSuccess: (uri: Uri) -> Unit
): ActivityResultLauncher<PickVisualMediaRequest> {
    return registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            onSuccess(uri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }
}