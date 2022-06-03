package com.example.diplom.Services

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.diplom.Authentification
import com.example.diplom.UserUtils
import com.google.android.gms.common.internal.service.Common
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class Message:FirebaseMessagingService(){
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (FirebaseAuth.getInstance().currentUser!!.uid != null) {
            UserUtils.updateToken(this, token)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        if (data != null){
            Authentification.showNotification(this, Random.nextInt(),
                data[Authentification.NOTI_TILE],
                data[Authentification.NOTI_BODY],
                null)
        }
    }
}