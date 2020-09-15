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
    private SongListRecyclerViewModel viewModel;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private MainActivity mainActivity;
    private List<SongListEntity> allSongList;
    private MainActivityViewModel mainActivityViewModel;

    @Override
    public void onViewCreated(@NonNull View view,Bundle savedInstanceState)
    {
        mainActivity=(MainActivity)getActivity();
        assert mainActivity != null;
        mainActivityViewModel=mainActivity.getMainActivityViewModel();
        progressBar=(ProgressBar)view.findViewById(R.id.ProgressBarInAllSongListFragment);
        progressBar.setVisibility(View.VISIBLE);
        //初始化recyclerView
        initRecyclerView(view);
        //获取歌单列表
        loadSongListEntities();

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
        }).doOnNext(new Consumer<List<SongListEntity>>() {
            @Override
            public void accept(List<SongListEntity> songListEntities){
                allSongList=songListEntities;
                viewModel.getCurrentData().setValue(allSongList);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
        viewModel=new ViewModelProvider(this).get(SongListRecyclerViewModel.class);
        adapter=new SongListAdapter();

        final Observer<List<SongListEntity>> ListOfSongsObserver= new Observer<List<SongListEntity>>() {
            @Override
            public void onChanged(List<SongListEntity> songLists) {
                adapter.setDataList(songLists);
                recyclerView.setAdapter(adapter);
            }
        };

        viewModel.getCurrentData().observe(mainActivity,ListOfSongsObserver);
        adapter.setClickListener(new SongListAdapter.SongListRecyclerClickListener() {
            @Override
            public void onClick(View view, SongListAdapter.SongListRecyclerEnum viewName, int position) {
                switch (viewName)
                {
                    case PLAY_ALL_OF_THE_SONG_LIST:
                        //todo
                        break;
                    case OPEN_SONG_LIST:
                        mainActivityViewModel.setSongListEntity(allSongList.get(position));
                        mainActivityViewModel.getSongListEditFragmentViewPagerAdapter()
                                        .createFragment(SongListEditFragmentViewPagerAdapter
                                                .SONG_OF_SONG_LIST_FRAGMENT);
                        break;
                    default:
                        break;
                }
            }
        });
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

