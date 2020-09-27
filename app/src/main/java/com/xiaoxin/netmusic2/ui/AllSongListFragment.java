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
import com.xiaoxin.netmusic2.MediaService;
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
    private MediaService.MyBinder serviceBinder;

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
        serviceBinder=mainActivityViewModel.getMyBinder();
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
            public void onClick(View view, SongListAdapter.SongListRecyclerEnum viewName, int position) {
                switch (viewName)
                {
                    case PLAY_ALL_OF_THE_SONG_LIST:
                        mainActivityViewModel.setSongListEntity(allSongList.get(position));
                        mainActivityViewModel.getSongListEditFragmentViewPagerAdapter()
                                .createFragment(SongListEditFragmentViewPagerAdapter
                                        .SONG_OF_SONG_LIST_FRAGMENT);
                        serviceBinder.startPlayListByRank(allSongList.get(position));
                        break;
                    case OPEN_SONG_LIST:
                        //todo
                        //viewpager自动切换
                        mainActivityViewModel.setSongListEntity(allSongList.get(position));
                        mainActivityViewModel.getSongListEditFragmentViewPagerAdapter()
                                        .createFragment(SongListEditFragmentViewPagerAdapter
                                                .SONG_OF_SONG_LIST_FRAGMENT);
                        mainActivityViewModel.getViewPager2().setCurrentItem(1,true);
                        break;
                    default:
                        break;
                }
            }
        });
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

