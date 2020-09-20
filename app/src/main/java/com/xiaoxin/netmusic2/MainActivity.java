package com.xiaoxin.netmusic2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RemoteViews;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.xiaoxin.netmusic2.database.SongDataBase;
import com.xiaoxin.netmusic2.database.SongDataBaseDao;
import com.xiaoxin.netmusic2.ui.SongListEditFragment;
import com.xiaoxin.netmusic2.ui.SongPlayingFragment;
import com.xiaoxin.netmusic2.viewmodel.MainActivityViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int FRAGMENT_OF_SONG_LIST=0;
    public static final int FRAGMENT_OF_PLAYING=1;

    private BottomNavigationView navigationView;
    private FragmentManager fragmentManager;
    private List<Fragment> fragmentContainer;
    private MainActivityViewModel mainActivityViewModel;

    private MediaService mediaService;
    private MediaService.MyBinder myBinder;
    private SongDataBase songDataBase;
    private SongDataBaseDao songDataBaseDao;
    private NotificationManager notificationManager;
    private RemoteViews remoteViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songDataBase=SongDataBase.getDatabase(this);
        songDataBaseDao=songDataBase.SongDataBaseDao();

        getStorageAccess();

        fragmentManager=getSupportFragmentManager();
        fragmentContainer=new ArrayList<>();
        fragmentContainer.add(new SongListEditFragment());
        fragmentContainer.add(new SongPlayingFragment());
        setFragment(fragmentContainer.get(FRAGMENT_OF_SONG_LIST));

        //设置BottomNavigationView
        initNavigation();

        mainActivityViewModel=new MainActivityViewModel();

        //设置播放服务
        setMediaService();

        notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        //设置notification
//        setNotification();



    }

    /**
     * 设置notification
     */
//    public void setNotification(){
//        notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//        NotificationCompat.Builder builder= new NotificationCompat.Builder(this,"NetMusic");
//
//        Intent intent = new Intent(this, MainActivity.class);
//        // 点击跳转到主界面
//        PendingIntent intent_go = PendingIntent.getActivity(this, 5, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.notification, intent_go);
//
//        // 4个参数context, requestCode, intent, flags
//        PendingIntent intent_close = PendingIntent.getActivity(this, 0, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.CloseTheNotification, intent_close);
//
//        // 设置上一曲
//        Intent prv = new Intent();
//        prv.setAction(SyncStateContract.Constants.ACTION_PRV);
//        PendingIntent intent_prev = PendingIntent.getBroadcast(this, 1, prv,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.LastSongInNotification, intent_prev);
//    }

    /**
     * 设置播放服务
     */
    public void setMediaService(){
        Intent startMediaService=new Intent(MainActivity.this, MediaService.class);
        startService(startMediaService);


        Intent bindIntent=new Intent(MainActivity.this,MediaService.class);
        bindService(bindIntent,connection,BIND_AUTO_CREATE);

    }

    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myBinder=(MediaService.MyBinder)iBinder;

            myBinder.setDataBaseDao(songDataBaseDao);
            mainActivityViewModel.setMyBinder(myBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };


    //对导航栏进行初始化，设置点击事件
    public void initNavigation() {
        navigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationBar);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.SongList:
                        setFragment(fragmentContainer.get(FRAGMENT_OF_SONG_LIST));
                        break;
                    case R.id.underPlaying:
                        setFragment(fragmentContainer.get(FRAGMENT_OF_PLAYING));
                        break;

                    default:
                        break;
                }
                return true;
            }
        });

//        if(navigationView.)
    }



    public void setFragment(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_in_main_activity,fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    //set the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_of_main_activity,menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(MainActivity.this, MediaService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.EditSongList:
                //todo
                break;
            case R.id.AddFromLocalSongs:
                Intent intent=new Intent(this,LocalSongsAddActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    public void getStorageAccess()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.
                permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        if(ContextCompat.checkSelfPermission(this, android.Manifest.
                permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},2);
        }
    }


    public MainActivityViewModel getMainActivityViewModel() {
        return mainActivityViewModel;
    }
}