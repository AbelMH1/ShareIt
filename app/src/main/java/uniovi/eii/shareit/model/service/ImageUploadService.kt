package uniovi.eii.shareit.model.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniovi.eii.shareit.R
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.repository.FirestoreImageService

class ImageUploadService : Service() {
    companion object {
        private const val TAG = "ImageUploadService"
        private const val NOTIFICATION_CHANNEL_ID = "image_upload_channel"
        const val ACTION_UPLOAD_IMAGE = "uniovi.eii.shareit.ACTION_UPLOAD_IMAGE"
        const val EXTRA_IMAGE = "uniovi.eii.shareit.EXTRA_IMAGE"

        /**
         * Inicia el servicio de carga de imágenes.
         *
         * @param context El contexto a utilizar para iniciar el servicio.
         * @param image La imagen a cargar, debe ser un objeto [Image].
         */
        fun startService(context: Context, image: Image) {
            // Crear intent con los datos necesarios
            val intent = Intent(context, ImageUploadService::class.java).apply {
                action = ACTION_UPLOAD_IMAGE
                putExtra(EXTRA_IMAGE, image)
            }
            // Iniciar el servicio
            ContextCompat.startForegroundService(context, intent)
        }

    }

    inner class LocalBinder : Binder() {
        fun getService(): ImageUploadService = this@ImageUploadService
    }

    private lateinit var notificationManager: NotificationManager

    private val _isUploadCompleted = MutableLiveData<Boolean>()
    val isUploadCompleted: LiveData<Boolean> = _isUploadCompleted

    private val binder = LocalBinder()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var image: Image? = null

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind")
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        if (intent != null) {
            when (intent.action) {
                ACTION_UPLOAD_IMAGE -> {
                    image = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(EXTRA_IMAGE, Image::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(EXTRA_IMAGE)
                    }

                    if (image != null) {
                        startAsForegroundService()
                        uploadImage()
                    } else {
                        Log.e(TAG, "Missing required data for upload")
                        stopSelf()
                    }
                }

                else -> {
                    Log.e(TAG, "Unknown action: ${intent.action}")
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        coroutineScope.coroutineContext.cancelChildren()
    }

    override fun onTimeout(startId: Int) {
        super.onTimeout(startId)
        Log.d(TAG, "onTimeout")
        stopSelf()
    }

    /**
     * Promociona el servicio a un servicio en primer plano, mostrando una notificación al usuario.
     *
     * Esta función debe ser llamada dentro de los 10 segundos posteriores al inicio del servicio o el sistema lanzará una excepción.
     */
    private fun startAsForegroundService() {
        // Crear el canal de notificación
        createServiceNotificationChannel()

        // Crear la notificación
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(resources.getString(R.string.notification_title_image_upload))
            .setContentText(resources.getString(R.string.notification_desc_image_upload, image?.albumName ?: ""))
            .setSmallIcon(R.drawable.ic_stat_name)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()

        // Iniciar como servicio en primer plano
        ServiceCompat.startForeground(
            this,
            100,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
            } else {
                0
            }
        )
    }

    private fun createServiceNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            resources.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun uploadImage() {
        coroutineScope.launch {
            try {
                if (image == null) {
                    _isUploadCompleted.postValue(false)
                    stopSelf()
                    return@launch
                }

                val result = FirestoreImageService.uploadImage(image!!, image!!.imagePath.toUri())
                Log.e(TAG, "Image upload result: $result")
                _isUploadCompleted.postValue(result)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ImageUploadService,
                        if (result) R.string.info_success_uploading_image else R.string.error_uploading_image,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading image", e)
                _isUploadCompleted.postValue(false)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ImageUploadService,
                        R.string.error_uploading_image,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE) // Elimina la notificación del primer plano
                stopSelf() // Detiene el servicio cuando todos los clientes se han desvinculado
            }
        }
    }
}