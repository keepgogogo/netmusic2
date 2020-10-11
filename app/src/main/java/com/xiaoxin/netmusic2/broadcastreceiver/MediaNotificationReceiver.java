package com.xiaoxin.netmusic2.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.xiaoxin.netmusic2.MediaManager;
import com.xiaoxin.netmusic2.NotificationMaker;

import java.util.Objects;

public class MediaNotificationReceiver extends BroadcastReceiver {

    public static String NotificationReceiver="com.xiaoxin.broadcastreceiver";

    private MediaManager.MediaEasyController mediaEasyController;
    private NotificationMaker notificationMaker;

    public void setNotificationMaker(NotificationMaker notificationMaker)
    {
        this.notificationMaker=notificationMaker;
    }

    public void setMediaEasyController(MediaManager.MediaEasyController mediaEasyController)
    {
        this.mediaEasyController=mediaEasyController;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (Objects.requireNonNull(intent.getAction()))
        {
            case "com.xiaoxin.broadcast.next" :
                mediaEasyController.nextSong();
                break;
            case "com.xiaoxin.broadcast.last" :
                mediaEasyController.lastSong();
                break;
            case "com.xiaoxin.broadcast.pause" :
                mediaEasyController.pauseOrStart();
                if (mediaEasyController.isMediaPlayerPause())
                {
                    notificationMaker.setRemoteViewsPlayImage();
                }else {
                    notificationMaker.setRemoteViewsPauseImage();
                }
                break;
            case "com.xiaoxin.broadcast.close" :
                mediaEasyController.pauseOrStart();
                notificationMaker.cancelNotification();
                break;
            default:
                break;

        }
    }
}
