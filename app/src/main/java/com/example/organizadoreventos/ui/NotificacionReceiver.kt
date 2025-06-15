package com.example.organizadoreventos

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificacionReceiver : BroadcastReceiver() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val descripcion = intent.getStringExtra("descripcion") ?: "Sin descripción"
        val idEvento = intent.getIntExtra("idEvento", 0)

        Log.d("RecordatorioReceiver", "Recibido el intent para el evento ID: $idEvento")

        val builder = NotificationCompat.Builder(context, "canal_recordatorios")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recordatorio de evento")
            .setContentText(descripcion)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(idEvento, builder.build())

        Log.d("RecordatorioReceiver", "Notificación mostrada para evento ID: $idEvento")
    }
}
