package com.example.proyectoandroid.data.net

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.proyectoandroid.R

object NotificacionHelper {
    private const val canal_id = "solicitudes_channel"
    private var iniciar = false

    fun inicializar(context: Context) {
        if (iniciar) return
        val nombre = "Solicitudes"
        val desc = "Notificaciones de nuevas solicitudes de amistad"
        val canal = NotificationChannel(
            canal_id, nombre, NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = desc }
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(canal)
        iniciar = true
    }

    fun mostrarNotificacion(context: Context, nombre: String) {
        inicializar(context)
        val builder = NotificationCompat.Builder(context, canal_id)
            .setSmallIcon(R.drawable.logo)       // tu icono
            .setContentTitle("Nueva solicitud")
            .setContentText("$nombre te ha enviado una solicitud")
            .setAutoCancel(true)

        // Comprueba permiso POST_NOTIFICATIONS en Android 13+
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Aquí podrías solicitar el permiso en tu Activity
            return
        }

        NotificationManagerCompat.from(context).notify(
            nombre.hashCode(),  // id único por remitente
            builder.build()
        )
    }
}
