package com.xiaoxin.netmusic2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.session.MediaSession;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.xiaoxin.netmusic2.broadcastreceiver.MediaNotificationReceiver;
import com.xiaoxin.netmusic2.database.SongEntity;
import com.xiaoxin.netmusic2.listener.MediaPlayerListener;
import com.xiaoxin.netmusic2.viewmodel.MainActivityViewModel;

public class NotificationMaker {
    public static final int ID_OF_NOTIFICATION = 1;

    private MediaPlayerListener mediaPlayerListener;
    private MainActivityViewModel mainActivityViewModel;

    private Notification notification;
    private NotificationManager notificationManager;
    private NotificationChannel notificationChannel;
    private RemoteViews remoteViews;
    private Context context;

    private Intent intentToMainActivity;
    private Intent nextSongIntent;
    private Intent lastSongIntent;
    private Intent pauseSongIntent;
    private Intent closeNotificationIntent;
    private PendingIntent pendingIntentToMainActivity;
    private PendingIntent pendingNextSongIntent;
    private PendingIntent pendingLastSongIntent;
    private PendingIntent pendingPauseSongIntent;
    private PendingIntent pendingCloseNotificationIntent;

    private Bitmap playImageBitmap;
    private Bitmap pauseImageBitmap;

    public void setContext(Context context) {
        this.context = context;
    }

    public void setMainActivityViewModel(MainActivityViewModel mainActivityViewModel)
    {
        this.mainActivityViewModel=mainActivityViewModel;
        mediaPlayerListener=new MediaPlayerListener() {
            @Override
            public void onChange(SongEntity oldSong, SongEntity newSong) {
                updateRemoteViews(newSong);
            }
        };
        mainActivityViewModel.setNotificationMakerSongChangeListener(mediaPlayerListener);
    }

    public void initRemoteViews() {
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification);
        remoteViews.setOnClickPendingIntent(R.id.LastSongInNotification,pendingLastSongIntent);
        remoteViews.setOnClickPendingIntent(R.id.CloseTheNotification,pendingCloseNotificationIntent);
        remoteViews.setOnClickPendingIntent(R.id.PauseInNotification,pendingPauseSongIntent);
        remoteViews.setOnClickPendingIntent(R.id.NextSongInNotification,pendingNextSongIntent);
        remoteViews.setOnClickPendingIntent(R.id.notification,pendingIntentToMainActivity);
        setRemoteViewsPlayImage();
    }

    public void initIntent() {
//        intentToMainActivity = new Intent(context, MainActivity.class);
        intentToMainActivity=new Intent(MediaNotificationReceiver.NotificationReceiver);
        intentToMainActivity.putExtra(MediaNotificationReceiver.NotificationReceiver
                ,"com.xiaoxin.broadcast.main");

        nextSongIntent=new Intent(MediaNotificationReceiver.NotificationReceiver);
        nextSongIntent.putExtra(MediaNotificationReceiver.NotificationReceiver
                ,"com.xiaoxin.broadcast.next");

        lastSongIntent=new Intent(MediaNotificationReceiver.NotificationReceiver);
        lastSongIntent.putExtra(MediaNotificationReceiver.NotificationReceiver
                ,"com.xiaoxin.broadcast.last");

        pauseSongIntent=new Intent(MediaNotificationReceiver.NotificationReceiver);
        pauseSongIntent.putExtra(MediaNotificationReceiver.NotificationReceiver
                ,"com.xiaoxin.broadcast.pause");

        closeNotificationIntent=new Intent(MediaNotificationReceiver.NotificationReceiver);
        closeNotificationIntent.putExtra(MediaNotificationReceiver.NotificationReceiver
                ,"com.xiaoxin.broadcast.close");
//
//        nextSongIntent=new Intent("com.xiaoxin.broadcast.next");
//        lastSongIntent=new Intent("com.xiaoxin.broadcast.last");
//        pauseSongIntent=new Intent("com.xiaoxin.broadcast.pause");
//        closeNotificationIntent=new Intent("com.xiaoxin.broadcast.close");

    }

    public void initPendingIntent()
    {
        pendingIntentToMainActivity = PendingIntent.getBroadcast(context,0,
                intentToMainActivity,PendingIntent.FLAG_UPDATE_CURRENT);

        pendingCloseNotificationIntent=PendingIntent.getBroadcast(context,1,
                closeNotificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        pendingLastSongIntent=PendingIntent.getBroadcast(context,2,
                lastSongIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        pendingNextSongIntent=PendingIntent.getBroadcast(context,3,
                nextSongIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        pendingPauseSongIntent=PendingIntent.getBroadcast(context,4,
                pauseSongIntent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void prepare() {

        initIntent();
        initPendingIntent();
        initRemoteViews();

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("normal", "通知界面"
                    , NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notification = new NotificationCompat.Builder(context, "normal")
                .setStyle(new NotificationCompat.BigPictureStyle())
                .setCustomBigContentView(remoteViews)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntentToMainActivity)
                .setContentIntent(pendingPauseSongIntent)
                .setContentIntent(pendingLastSongIntent)
                .setContentIntent(pendingNextSongIntent)
                .setContentIntent(pendingCloseNotificationIntent)
                .setAutoCancel(false)
                .setOngoing(true)
                .build();

//        notification=new NotificationCompat.Builder(context,"normal")
//                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                .setSmallIcon(R.drawable.ic_launcher)
//                .addAction(R.drawable.ic_play_bar_btn_next,"Next",pendingNextSongIntent)
//                .addAction(R.drawable.ic_play_bar_btn_last,"Last",pendingLastSongIntent)
//                .addAction(R.drawable.ic_play_bar_btn_pause,"pause",pendingPauseSongIntent)
//
//                .setContentTitle("Wonderful music")
//                .setContentText("My Awesome Band")
//                .setCustomContentView(remoteViews)
//                .build();


    }

    public void sendNotification() {
        notificationManager.notify(ID_OF_NOTIFICATION, notification);
    }

    public void setRemoteViewAlbumPicture(byte[] bytes) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length - 1);
        remoteViews.setImageViewBitmap(R.id.AlbumPictureInNotification, bitmap);
    }

    public void setRemoteViewsPlayImage()
    {
        remoteViews.setImageViewBitmap(R.id.PauseInNotification,playImageBitmap);
    }

    public void setRemoteViewsPauseImage()
    {
        remoteViews.setImageViewBitmap(R.id.PauseInNotification,pauseImageBitmap);
    }

    public void setRemoteViewText(String songName,String artist) {
        remoteViews.setTextViewText(R.id.SingerInNotification, artist);
        remoteViews.setTextViewText(R.id.nameOfSongInNotification, songName);
    }

    public void setPlayAndPauseImageBitmap(Bitmap playImageBitmap,Bitmap pauseImageBitmap)
    {
        this.playImageBitmap=playImageBitmap;
        this.pauseImageBitmap=pauseImageBitmap;
    }

    public void updateRemoteViews(SongEntity entity)
    {
        setRemoteViewText(entity.getName(),entity.getArtist());
        setRemoteViewAlbumPicture(entity.getAlbumPicture());
        setRemoteViewsPauseImage();
    }

    public void cancelNotification(){
        notificationManager.cancel(ID_OF_NOTIFICATION);
    }

}
