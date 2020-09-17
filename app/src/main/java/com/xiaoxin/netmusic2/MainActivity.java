package com.xiaoxin.netmusic2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getStorageAccess();

        fragmentManager=getSupportFragmentManager();
        fragmentContainer=new ArrayList<>();

        Fragment songListEditFragment=new SongListEditFragment();
        Fragment songPlayingFragment=new SongPlayingFragment();

        fragmentContainer.add(songListEditFragment);
        fragmentContainer.add(songPlayingFragment);
        initNavigation();
        setFragment(fragmentContainer.get(FRAGMENT_OF_SONG_LIST));
        mainActivityViewModel=new MainActivityViewModel();


        mediaService=new MediaService();
        myBinder=mediaService.getMyBinder();
        mainActivityViewModel.setMyBinder(myBinder);
        mainActivityViewModel.setMediaService(mediaService);


    }


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