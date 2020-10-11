package com.xiaoxin.netmusic2.ui;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoxin.netmusic2.MainActivity;
import com.xiaoxin.netmusic2.MediaManager;
import com.xiaoxin.netmusic2.R;
import com.xiaoxin.netmusic2.database.SongDataBase;
import com.xiaoxin.netmusic2.database.SongDataBaseDao;
import com.xiaoxin.netmusic2.database.SongEntity;
import com.xiaoxin.netmusic2.database.SongListDataBase;
import com.xiaoxin.netmusic2.database.SongListDataBaseDao;
import com.xiaoxin.netmusic2.database.SongListEntity;
import com.xiaoxin.netmusic2.listener.MediaPlayerListener;
import com.xiaoxin.netmusic2.recycler.SongAdapter;
import com.xiaoxin.netmusic2.recycler.SongRecyclerViewModel;
import com.xiaoxin.netmusic2.viewmodel.MainActivityViewModel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SongOfSongListFragment extends Fragment {

    private SongAdapter adapter;
    private SongRecyclerViewModel songRecyclerViewModel;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private MainActivity mainActivity;
    private MainActivityViewModel mainActivityViewModel;
    private SeekBar seekBarInTheBottomOfScreen;
    private CircleImageView imageViewOfAlbumInTheBottomOfScreen;

    private SongDataBase songDataBase;
    private SongDataBaseDao songDataBaseDao;

    private List<SongEntity> songEntities;
    private MediaManager.MediaEasyController mediaEasyController;
    private MediaPlayerListener mediaPlayerListener;

    @Override
    public void onViewCreated(@NonNull View view,Bundle savedInstanceState)
    {


        initMainActivityObject();
        initMainActivityViewModel();
        initMediaEasyController();
        initProgressBar(view);
        //初始化recyclerView
        initRecyclerView(view);
        //获取歌曲列表
        loadSongEntities();
        initPlayingSongChangeListener();
        initImageOfAlbumInTheBottomOfScreen();
    }

    public void initProgressBar(@NonNull View view) {
        progressBar=(ProgressBar)view.findViewById(R.id.ProgressBarInSongOfSongListFragment);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void initMediaEasyController() {
        mediaEasyController=mainActivityViewModel.getMediaEasyController();
    }

    public void initMainActivityViewModel() {
        assert mainActivity != null;
        mainActivityViewModel = mainActivity.getMainActivityViewModel();
        seekBarInTheBottomOfScreen=mainActivityViewModel.getSeekBarInTheBottomOfScreen();
    }

    public void initMainActivityObject() {
        mainActivity = (MainActivity) getActivity();
    }

    //初始化recyclerView
    public void initRecyclerView(@NonNull View view)
    {
        recyclerView=(RecyclerView)view.findViewById(R.id.RecyclerInSongOfSongListFragment);
        layoutManager=new LinearLayoutManager(mainActivity);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        songRecyclerViewModel =new ViewModelProvider(this).get(SongRecyclerViewModel.class);
        adapter=new SongAdapter();
        adapter.setContext(mainActivity);

        final Observer<List<SongEntity>> ListOfSongsObserver= new Observer<List<SongEntity>>() {
            @Override
            public void onChanged(List<SongEntity> songs) {
                adapter.setDataList(songs);
                recyclerView.setAdapter(adapter);
            }
        };

        songRecyclerViewModel.getCurrentData().observe(mainActivity,ListOfSongsObserver);
        adapter.setClickListener(new SongAdapter.SongRecyclerClickListener() {
            @Override
            public void onClick(View view, SongAdapter.ViewNameSongRecyclerEnum viewName, SongEntity entity) {
                switch (viewName)
                {
                    case CHECK_BOX_SET_FALSE:
                        //todo
                        break;
                    case CHECK_BOX_SET_TRUE:
                        //TODO
                        break;
                    case IMAGE_BUTTON_PLAY:
                        //TODO
                        mainActivity.resetSeekBar();
                        mediaEasyController.playMidway(entity);
                        setAlbumPictureInTheBottomOfScreen(entity);

                        break;
                    case IMAGE_BUTTON_STOP:
                        mediaEasyController.pauseOrStart();

                        //TODO
                        break;
                    default:
                        break;
                }
            }
        });
    }

    //获取歌曲列表
    public void loadSongEntities()
    {
        final String nameOfSongList=mainActivityViewModel.getUnderPlayingSongList().getSongList();
        if (nameOfSongList==null || nameOfSongList.length()==0)return;
        initSongDataBase();
        initMainActivityViewModel();

        Observable.create(new ObservableOnSubscribe<List<SongEntity>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<SongEntity>> emitter)
            {
                List<SongEntity> songEntities;
                songEntities=songDataBaseDao.getBySongList(nameOfSongList);
                emitter.onNext(songEntities);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<SongEntity>>() {
                    @Override
                    public void accept(List<SongEntity> songs){
                        songEntities=songs;
                        songRecyclerViewModel.getCurrentData().setValue(songs);
                        progressBar.setVisibility(View.GONE);
                        loadUnderPlaySong();
                    }
                }).subscribe();
    }

    public void loadUnderPlaySong(){
        SongEntity underPlaySong=mediaEasyController.getUnderPlayingSongEntity();
        if(underPlaySong!=null){
            int indexOfUnderPlaySong=getIndexOfSongEntityFromList(adapter.getDataList(),underPlaySong);
            if (indexOfUnderPlaySong!=-1){
                underPlaySong.setPlaying(true);
                songEntities.remove(indexOfUnderPlaySong);
                songEntities.add(indexOfUnderPlaySong,underPlaySong);
                adapter.setDataList(songEntities);
                songRecyclerViewModel.getCurrentData().setValue(songEntities);
            }
            setAlbumPictureInTheBottomOfScreen(underPlaySong);
        }
    }

    public void setAlbumPictureInTheBottomOfScreen(SongEntity entity){
        byte[] temp=entity.getAlbumPicture();
        imageViewOfAlbumInTheBottomOfScreen.setImageBitmap(BitmapFactory
                .decodeByteArray(temp,0,temp.length-1));
    }

    public int getIndexOfSongEntityFromList(List<SongEntity> entityList, SongEntity entity){
        String name=entity.getName();
        for(int i=0;i<entityList.size();i++){
            if (entityList.get(i).getName()
                    .equals(name)){
                return i;
            }
        }
        return -1;
    }

    public void initImageOfAlbumInTheBottomOfScreen(){
        imageViewOfAlbumInTheBottomOfScreen=mainActivityViewModel.getImageViewForAlbumPictureInTheBottomOfScreen();
    }


    @Override
    public void onResume() {
        super.onResume();
        initSongDataBase();
        initMainActivityObject();
        initMainActivityViewModel();
        final String nameOfSongList=mainActivityViewModel.getUnderPlayingSongList().getSongList();


        Observable.create(new ObservableOnSubscribe<List<SongEntity>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<SongEntity>> emitter)
            {
                if (nameOfSongList!=null){
                    List<SongEntity> songEntities;
                    songEntities=songDataBaseDao.getBySongList(nameOfSongList);
                    emitter.onNext(songEntities);
                }else{
                    SongListDataBase songListDataBase=SongListDataBase.getDatabase(mainActivity);
                    SongListDataBaseDao songListDataBaseDao=songListDataBase.SongListDataBaseDao();
                    List<SongListEntity> entities=songListDataBaseDao.loadAll();
                    emitter.onNext(songDataBaseDao.getBySongList(entities.get(0).getSongList()));
                }

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<SongEntity>>() {
                    @Override
                    public void accept(List<SongEntity> songs){
                        songEntities=songs;
                        songRecyclerViewModel.getCurrentData().setValue(songs);
                        progressBar.setVisibility(View.GONE);
                        loadUnderPlaySong();
                    }
                }).subscribe();

    }

    public void initSongDataBase() {
        songDataBase = SongDataBase.getDatabase(mainActivity);
        songDataBaseDao = songDataBase.SongDataBaseDao();
    }

    public void initPlayingSongChangeListener(){
        mediaPlayerListener =new MediaPlayerListener() {

            @Override
            public void onChange(SongEntity oldSong, SongEntity newSong) {
                List<SongEntity> tempEntities=adapter.getDataList();
                int indexOfOldSong=getIndexOfSongEntityFromList(tempEntities,oldSong);
                int indexOfNewSong=getIndexOfSongEntityFromList(tempEntities,newSong);
                oldSong.setPlaying(false);
                newSong.setPlaying(true);
                tempEntities.remove(indexOfNewSong);
                tempEntities.add(indexOfNewSong,newSong);
                tempEntities.remove(indexOfOldSong);
                tempEntities.add(indexOfOldSong,oldSong);
                adapter.setDataList(tempEntities);
                songRecyclerViewModel.getCurrentData().setValue(tempEntities);
            }

        };
        mainActivityViewModel.setSongOfSongListFragmentSongChangeListener(mediaPlayerListener);
    }



    public SongOfSongListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song_of_song_list, container, false);
    }
}
