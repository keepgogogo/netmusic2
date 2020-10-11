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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.xiaoxin.netmusic2.broadcastreceiver.MediaNotificationReceiver;
import com.xiaoxin.netmusic2.database.SongDataBase;
import com.xiaoxin.netmusic2.database.SongDataBaseDao;
import com.xiaoxin.netmusic2.database.SongEntity;
import com.xiaoxin.netmusic2.listener.MediaPlayerBinder;
import com.xiaoxin.netmusic2.ui.SongListEditFragment;
import com.xiaoxin.netmusic2.ui.SongPlayingFragment;
import com.xiaoxin.netmusic2.viewmodel.MainActivityViewModel;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int FRAGMENT_OF_SONG_LIST = 0;
    public static final int FRAGMENT_OF_PLAYING = 1;

    private CircleImageView circleImageViewSeekBar;
    private SeekBar seekBar;
    private ImageView lastSongImageView;
    private ImageView nextSongImageView;
    private ImageView playImageView;
    private TextView durationTextView;

    private BottomNavigationView navigationView;
    private FragmentManager fragmentManager;
    private List<Fragment> fragmentContainer;
    private MainActivityViewModel mainActivityViewModel;
    private MediaPlayerBinder mediaPlayerBinder;
    private MediaManager mediaManager;
    private MediaManager.MediaEasyController mediaEasyController;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor preferenceEditor;

    private SongDataBase songDataBase;
    private SongDataBaseDao songDataBaseDao;

    private boolean isSeekBarChange;
    private boolean isLastSongExist;
    private SongEntity lastSongOfLastTime;

    private Disposable disposableOfSeekBar;

    private NotificationMaker notificationMaker;
    private MediaNotificationReceiver mediaNotificationReceiver;

    private Bitmap playImageBitmap;
    private Bitmap pauseImageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        initPauseAndPlayImageBitmap();
        setUIClickListener();
        initSharePreference();
        initSongDataBase();

        getStorageAccess();


        new Thread(new Runnable() {
            @Override
            public void run() {
                initFragmentManager();
                getLastPlayedSong();
                startUpdateSeekBar();
                initNavigation();
                initMediaManager();
                setOnPlayingSongChangeListener();
                initMainActivityViewModel();
                initSeekBarChangeListener();
                initNotification();
                initMediaNotificationReceiver();
            }
        }).start();

    }

    public void initMediaNotificationReceiver()
    {
        mediaNotificationReceiver=new MediaNotificationReceiver();
        mediaNotificationReceiver.setMediaEasyController(mediaEasyController);
        mediaNotificationReceiver.setNotificationMaker(notificationMaker);
    }

    public void initNotification() {
        notificationMaker = new NotificationMaker();
        notificationMaker.setPlayAndPauseImageBitmap(playImageBitmap,pauseImageBitmap);
        notificationMaker.setContext(this);
        notificationMaker.setMainActivityViewModel(mainActivityViewModel);
        notificationMaker.prepare();
    }

    public void initUI() {
        nextSongImageView = findViewById(R.id.NextSongInMainActivity);
        lastSongImageView = findViewById(R.id.LastSongInMainActivity);
        playImageView = findViewById(R.id.PauseInMainActivity);
        seekBar = findViewById(R.id.SeekBar);
        circleImageViewSeekBar = findViewById(R.id.CircleImageForAlbumInSeekBar);
        durationTextView = findViewById(R.id.TextViewForDurationSeekBar);
    }

    public void getLastPlayedSong() {
        final int idOfLastPlaySong = sharedPreferences.getInt("LastPlaySong", -1);
        if (idOfLastPlaySong != -1) {
            Observable.create(new ObservableOnSubscribe<List<SongEntity>>() {
                @Override
                public void subscribe(ObservableEmitter<List<SongEntity>> emitter) throws Exception {
                    SongEntity temp = songDataBaseDao.getById(idOfLastPlaySong);
                    emitter.onNext(songDataBaseDao.getBySongList(temp.getSongList()));
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(new Consumer<List<SongEntity>>() {
                        @Override
                        public void accept(List<SongEntity> entities) throws Exception {
                            SongEntity lastPlaySong = entities
                                    .get(getIndexOfSongEntityById(entities, idOfLastPlaySong));
                            byte[] pictureBytes = lastPlaySong.getAlbumPicture();
                            circleImageViewSeekBar.setImageBitmap(BitmapFactory
                                    .decodeByteArray(pictureBytes, 0, pictureBytes.length - 1));
                            lastSongOfLastTime = lastPlaySong;
                            isLastSongExist = true;
                        }
                    }).subscribe();
        }
    }

    public int getIndexOfSongEntityById(List<SongEntity> entities, int id) {
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    public void initPauseAndPlayImageBitmap() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                pauseImageBitmap = BitmapFactory.decodeResource(MainActivity.this.getResources()
                        , R.mipmap.ic_play_bar_btn_pause);
                playImageBitmap = BitmapFactory.decodeResource(MainActivity.this.getResources()
                        , R.mipmap.ic_play_bar_btn_play);
            }
        }).start();
    }

    public void setUIClickListener() {
        lastSongImageView.setOnClickListener(MainActivity.this);
        nextSongImageView.setOnClickListener(MainActivity.this);
        playImageView.setOnClickListener(MainActivity.this);
    }

    public void initSharePreference() {
        sharedPreferences = getSharedPreferences("MainActivity", MODE_PRIVATE);
        preferenceEditor = sharedPreferences.edit();
        preferenceEditor.apply();
    }

    public void startUpdateSeekBar() {
        disposableOfSeekBar = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                while (true) {
                    Thread.sleep(500);
                    if (isSeekBarChange || !mediaEasyController.havePlayerEverPlayed()) {
                        emitter.onNext(-1);
                    } else {
                        emitter.onNext(mediaEasyController.getMediaPlayerCurrentDuration());
                    }
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        if (integer != -1) {
                            int progress = (int) (((integer) / mediaEasyController.getUnderPlayingSongDuration()) * 100);
                            seekBar.setProgress(progress);
                            durationTextView.setText(timeParse(integer));
                        }
                    }
                }).subscribe();
    }

    public void resetSeekBar() {
        isSeekBarChange = true;
        seekBar.setProgress(0);
        isSeekBarChange = false;
    }

    public void initSeekBarChangeListener() {
        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mediaEasyController.pauseOrStart();
                isSeekBarChange = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaEasyController.mediaPlayJumpTo(seekBar.getProgress());
                isSeekBarChange = false;
                mediaEasyController.pauseOrStart();
            }
        };
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    public String timeParse(int currentDuration) {
        currentDuration = currentDuration / 1000;
        String m = "";
        m = m + currentDuration / 60;
        m = m + ":";
        m = m + currentDuration % 60;
        return m;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.NextSongInMainActivity:
                //todo
                mediaEasyController.nextSong();
//                resetSeekBar();
                break;
            case R.id.LastSongInMainActivity:
                mediaEasyController.lastSong();
//                resetSeekBar();
                //todo
                break;
            case R.id.PauseInMainActivity:
                if (isLastSongExist) {
                    mediaEasyController.playImmediatelyWhenStart(lastSongOfLastTime);
                    isLastSongExist = false;
                } else {
                    mediaEasyController.pauseOrStart();
                }

//                if (mediaEasyController.isMediaPlayerPause()) {
//                    //todo
//                    //暂停了怎么做
//                    playImageView.setImageBitmap(playImageBitmap);
//                    isSeekBarChange = true;
//                } else {
//                    //todo
//                    //还没开始怎么做
//                    playImageView.setImageBitmap(pauseImageBitmap);
//                    isSeekBarChange = false;
//                }
                mediaNotificationReceiver.onReceive(this,new Intent("com.xiaoxin.broadcast.pause"));
                break;
            default:
                break;
        }
    }

    private void initSongDataBase() {
        songDataBase = SongDataBase.getDatabase(this);
        songDataBaseDao = songDataBase.SongDataBaseDao();
    }

    public void setOnPlayingSongChangeListener() {
        mediaPlayerBinder = new MediaPlayerBinder() {

            @Override
            public void onChange(SongEntity oldSong, SongEntity newSong) {
                mainActivityViewModel.notifyPlayingSongChange(oldSong, newSong);
                byte[] pictureBytes = newSong.getAlbumPicture();
                circleImageViewSeekBar.setImageBitmap(BitmapFactory
                        .decodeByteArray(pictureBytes, 0, pictureBytes.length - 1));
                preferenceEditor.putInt("LastPlaySong", newSong.getId());
                preferenceEditor.apply();
                resetSeekBar();
            }

            @Override
            public void setIsLastPlaySongExist() {
                isLastSongExist = false;
            }

            @Override
            public void sendNotification(SongEntity entity) {
                makeNotification(entity);
            }

            @Override
            public void playStatusChanged(boolean isPlaying) {
                if (isPlaying)
                {
                    playImageView.setImageBitmap(pauseImageBitmap);
                    isSeekBarChange = false;
                }else {
                    playImageView.setImageBitmap(playImageBitmap);
                    isSeekBarChange = true;
                }
            }
        };
        mediaManager.setMediaPlayerBinder(mediaPlayerBinder);
    }

    public void makeNotification(SongEntity entity) {
        notificationMaker.setRemoteViewAlbumPicture(entity.getAlbumPicture());
        notificationMaker.setRemoteViewText(entity.getName(), entity.getArtist());
        notificationMaker.sendNotification();
    }

    public void initMediaManager() {
        mediaManager = new MediaManager();
        mediaManager.setSongDataBaseDao(songDataBaseDao);
        mediaEasyController = mediaManager.getMediaEasyController();
    }

    private void initMainActivityViewModel() {
        mainActivityViewModel = new MainActivityViewModel();
        mainActivityViewModel.setMediaManager(mediaManager);
        mainActivityViewModel.setSeekBarInTheBottomOfScreen(seekBar);
        mainActivityViewModel.setImageViewForAlbumPictureIntheBottomOfScreen(circleImageViewSeekBar);
    }

    public void initFragmentManager() {
        fragmentManager = getSupportFragmentManager();
        fragmentContainer = new ArrayList<>();
        fragmentContainer.add(new SongListEditFragment());
        fragmentContainer.add(new SongPlayingFragment());
        setFragment(fragmentContainer.get(FRAGMENT_OF_SONG_LIST));
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


    public void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_in_main_activity, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    //set the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_of_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.EditSongList:
                //todo
                break;
            case R.id.AddFromLocalSongs:
                Intent intent = new Intent(this, LocalSongsAddActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    public void getStorageAccess() {
        if (ContextCompat.checkSelfPermission(this, Manifest.
                permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.
                permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }
    }


    public MainActivityViewModel getMainActivityViewModel() {
        return mainActivityViewModel;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposableOfSeekBar.dispose();
        mediaEasyController.endPlay();
    }
}