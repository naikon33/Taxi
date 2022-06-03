package com.example.diplom

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.diplom.Model.InfoModel
import com.google.android.gms.common.internal.service.Common
import java.lang.StringBuilder

object Authentification {
    fun buildWelcomeMessage(): String {
        return StringBuilder("Добро пожаловать, ")
            .append(currentUser!!.firstName)
            .append(" ")
            .append(currentUser!!.lastName)
            .toString()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun showNotification(
        context: Context,
        id: Int,
        title: String?,
        body: String?,
        intent: Intent?
    ) {
        var pendingIntent: PendingIntent? = null
        if (intent != null){
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val NOTIFICATION_CHANNEL_ID = "SpeedLight_diplom"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_0_1){
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "SpeedLight",
                NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "SpeedLight"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0,100,500,1000)
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val builder = NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setSmallIcon(R.drawable.ic_baseline_local_taxi_24)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources,R.drawable.ic_baseline_local_taxi_24))
        if (pendingIntent != null){
            builder.setContentIntent(pendingIntent)
        }
        val notification = builder.build()
        notificationManager.notify(id,notification)
    }
    val NOTI_BODY: String = "body"
    val NOTI_TILE: String = "title"
    val TOKEN_RFERENCE: String = ""
    val Driver_location:String="DriverLocations"
    var currentUser: InfoModel?=null
    val Driver_Info="DriverInfo"
}