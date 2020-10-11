package com.xiaoxin.netmusic2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat

open class NotificationMakerKt {

    private val ID_OF_NOTIFICATION = 1

    private lateinit var notification: Notification
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var remoteViews: RemoteViews
    private lateinit var context: Context

    private lateinit var intentToMainActivity: Intent
    private lateinit var pendingIntentToMainActivity: PendingIntent

    fun setContext(context: Context) {
        this.context = context
    }

    fun initRemoteViews() {
        remoteViews = RemoteViews(context.packageName, R.layout.notification)
    }

    fun initIntent() {
        intentToMainActivity = Intent(context, MainActivity::class.java)
        pendingIntentToMainActivity = PendingIntent
                .getActivity(context, 0, intentToMainActivity, 0)
    }

    fun prepare() {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel("normal", "通知界面", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notification = NotificationCompat.Builder(context, "normal")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViews)
                .setCustomBigContentView(remoteViews)
                .setContentIntent(pendingIntentToMainActivity)
                .setAutoCancel(false)
                .build()

    }

    fun sendNotification() {
        notificationManager.notify(ID_OF_NOTIFICATION, notification);
    }

    fun setRemoteViewAlbumPicture(bytes: ByteArray) {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size - 1)
        remoteViews.setImageViewBitmap(R.id.AlbumPictureInNotification, bitmap)
    }

    fun setRemoteViewText(songName: String, artist: String) {
        remoteViews.setTextViewText(R.id.SingerInNotification, artist)
        remoteViews.setTextViewText(R.id.nameOfSongInNotification, songName)
    }


}