package com.xiaoxin.netmusic2.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

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
import com.xiaoxin.netmusic2.database.SongListDataBase;
import com.xiaoxin.netmusic2.database.SongListDataBaseDao;
import com.xiaoxin.netmusic2.database.SongListEntity;
import com.xiaoxin.netmusic2.recycler.SongListAdapter;
import com.xiaoxin.netmusic2.recycler.SongListRecyclerViewModel;
import com.xiaoxin.netmusic2.viewmodel.MainActivityViewModel;
import com.xiaoxin.netmusic2.viewpager2.SongListEditFragmentViewPagerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AllSongListFragment extends Fragment {

    private SongListDataBase songListDataBase;
    private SongListDataBaseDao songListDataBaseDao;

    private SongDataBase songDataBase;
    private SongDataBaseDao songDataBaseDao;

    private SongListAdapter adapter;
    private SongListRecyclerViewModel songListRecyclerViewModel;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private MainActivity mainActivity;
    private List<SongListEntity> allSongList;
    private MainActivityViewModel mainActivityViewModel;
    private MediaManager.MediaEasyController mediaEasyController;

    @Override
    public void onViewCreated(@NonNull View view,Bundle savedInstanceState)
    {
        initUI(view);

        initDataEvents();
        //初始化recyclerView
        initRecyclerView(view);
        //获取歌单列表
        loadSongListEntities();

    }

    public void initDataEvents(){
        mainActivity=(MainActivity)getActivity();
        assert mainActivity != null;
        mainActivityViewModel=mainActivity.getMainActivityViewModel();
        allSongList=new ArrayList<>();
        mediaEasyController=mainActivityViewModel.getMediaEasyController();
    }

    public void initUI(View view){
        progressBar=(ProgressBar)view.findViewById(R.id.ProgressBarInAllSongListFragment);
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * 获取歌单列表
     */
    public void loadSongListEntities(){
        songListDataBase=SongListDataBase.getDatabase(mainActivity);
        songListDataBaseDao=songListDataBase.SongListDataBaseDao();
        Observable.create(new ObservableOnSubscribe<List<SongListEntity>>() {
            @Override
            public void subscribe(ObservableEmitter<List<SongListEntity>> emitter){
                List<SongListEntity> songListEntities=songListDataBaseDao.loadAll();
                emitter.onNext(songListEntities);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<SongListEntity>>() {
                    @Override
                    public void accept(List<SongListEntity> songListEntities){
                        allSongList=songListEntities;
                        if(allSongList!=null)
                        {
                            songListRecyclerViewModel.getCurrentData().setValue(allSongList);
                        } else {
                            songListRecyclerViewModel.getCurrentData().setValue(null);
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .subscribe();
    }


    /**
     * 初始化recyclerView
     */
    public void initRecyclerView(@NonNull View view)
    {
        recyclerView=(RecyclerView)view.findViewById(R.id.RecyclerViewInAllSongListFragment);
        layoutManager=new LinearLayoutManager(mainActivity);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        songListRecyclerViewModel =new ViewModelProvider(this).get(SongListRecyclerViewModel.class);
        adapter=new SongListAdapter();
        adapter.setContext(mainActivity);
        recyclerView.setAdapter(adapter);

        final Observer<List<SongListEntity>> ListOfSongsObserver= new Observer<List<SongListEntity>>() {
            @Override
            public void onChanged(List<SongListEntity> songLists) {
                adapter.setDataList(songLists);
                recyclerView.setAdapter(adapter);
            }
        };

        songListRecyclerViewModel.getCurrentData().observe(getViewLifecycleOwner(),ListOfSongsObserver);
        adapter.setClickListener(new SongListAdapter.SongListRecyclerClickListener() {
            @Override
            public void onClick(View view, SongListAdapter.SongListRecyclerEnum viewName, int position){
                switch (viewName)
                {
                    case PLAY_ALL_OF_THE_SONG_LIST:
                        if (mediaEasyController.getUnderPlayingSongEntity()==null){
                            startSongListPlay(position);
                        }
                        else if (allSongList.get(position).getSongList()
                                .equals(mediaEasyController.getUnderPlayingSongEntity().getSongList())){
                            try {
                                mediaEasyController.startPlay();
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }else {
                            startSongListPlay(position);
                        }
                        mainActivityViewModel.setUnderPlayingSongList(allSongList.get(position));
                        setSongListItemPlayImage();
                        viewPagerChangeToSongFragment(position);
                        break;
                    case PAUSE_ALL_OF_THE_SONG_LIST:
                        mediaEasyController.pausePlay();
                        break;
                    case OPEN_SONG_LIST:
                        //todo
                        //viewpager自动切换
                        viewPagerChangeToSongFragment(position);
                        startSongListPlay(position);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void setSongListItemPlayImage(){
        SongListEntity temp=mainActivityViewModel.getUnderPlayingSongList();
        int position=getIndexOfSongEntityFromList(allSongList,temp);
        temp.setPlayImagePicture(adapter.getPauseImageBytes());
        if(position!=-1){
            allSongList.remove(position);
            allSongList.add(position,temp);
            adapter.setDataList(allSongList);
            songListRecyclerViewModel.getCurrentData().setValue(allSongList);
        }
    }

    public int getIndexOfSongEntityFromList(List<SongListEntity> entityList, SongListEntity entity){
        String name=entity.getSongList();
        for(int i=0;i<entityList.size();i++){
            if (entityList.get(i).getSongList()
                    .equals(name)){
                return i;
            }
        }
        return -1;
    }

    public void startSongListPlay(int position){
        mainActivityViewModel.setUnderPlayingSongList(allSongList.get(position));
        mainActivityViewModel.getSongListEditFragmentViewPagerAdapter()
                .createFragment(SongListEditFragmentViewPagerAdapter
                        .SONG_OF_SONG_LIST_FRAGMENT);
        mediaEasyController.startPlayListByRank(allSongList.get(position));
    }

    public void viewPagerChangeToSongFragment(int position){
        mainActivityViewModel.setUnderPlayingSongList(allSongList.get(position));
        mainActivityViewModel.getSongListEditFragmentViewPagerAdapter()
                .createFragment(SongListEditFragmentViewPagerAdapter
                        .SONG_OF_SONG_LIST_FRAGMENT);
        mainActivityViewModel.getSongListEditFragmentViewPager2().setCurrentItem(1,true);
    }

    @Override
    public void onResume(){
        super.onResume();
        Observable.create(new ObservableOnSubscribe<List<SongListEntity>>() {
            @Override
            public void subscribe(ObservableEmitter<List<SongListEntity>> emitter) throws Exception {
                songListDataBase=SongListDataBase.getDatabase(getActivity());
                songListDataBaseDao=songListDataBase.SongListDataBaseDao();
                emitter.onNext(songListDataBaseDao.loadAll());
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<SongListEntity>>() {
                    @Override
                    public void accept(List<SongListEntity> songListEntities) throws Exception {
                        if(!allSongList.equals(songListEntities)){
                            adapter.setDataList(songListEntities);
                            allSongList=songListEntities;
                            songListRecyclerViewModel.getCurrentData().setValue(songListEntities);
                            setSongListItemPlayImage();
                        }
                    }
                }).subscribe();
    }








    public AllSongListFragment() {
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
        return inflater.inflate(R.layout.fragment_all_song_list, container, false);
    }
}

