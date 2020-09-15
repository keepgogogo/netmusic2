package com.xiaoxin.netmusic2.ui;

import android.annotation.SuppressLint;
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
import com.xiaoxin.netmusic2.database.SongEntity;
import com.xiaoxin.netmusic2.recycler.SongAdapter;
import com.xiaoxin.netmusic2.recycler.SongRecyclerViewModel;
import com.xiaoxin.netmusic2.viewmodel.MainActivityViewModel;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SongOfSongListFragment extends Fragment {

    private SongAdapter adapter;
    private SongRecyclerViewModel viewModel;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private MainActivity mainActivity;
    private MainActivityViewModel mainActivityViewModel;

    private SongDataBase songDataBase;
    private SongDataBaseDao songDataBaseDao;

    private List<SongEntity> songEntities;

    @Override
    public void onViewCreated(@NonNull View view,Bundle savedInstanceState)
    {
        mainActivity=(MainActivity)getActivity();
        assert mainActivity != null;
        mainActivityViewModel=mainActivity.getMainActivityViewModel();

        progressBar=(ProgressBar)view.findViewById(R.id.ProgressBarInSongOfSongListFragment);
        progressBar.setVisibility(View.VISIBLE);

        //获取歌曲列表
        loadSongEntities(mainActivityViewModel.getSongListEntity().getSongList());

        //初始化recyclerView
        initRecyclerView(view);


    }

    //初始化recyclerView
    public void initRecyclerView(@NonNull View view)
    {
        recyclerView=(RecyclerView)view.findViewById(R.id.RecyclerInSongOfSongListFragment);
        layoutManager=new LinearLayoutManager(mainActivity);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        viewModel=new ViewModelProvider(this).get(SongRecyclerViewModel.class);
        adapter=new SongAdapter();

        final Observer<List<SongEntity>> ListOfSongsObserver= new Observer<List<SongEntity>>() {
            @Override
            public void onChanged(List<SongEntity> songs) {
                adapter.setDataList(songs);
                recyclerView.setAdapter(adapter);
            }
        };

        viewModel.getCurrentData().observe(mainActivity,ListOfSongsObserver);
        adapter.setClickListener(new SongAdapter.SongRecyclerClickListener() {
            @Override
            public void onClick(View view, SongAdapter.ViewNameSongRecyclerEnum viewName, int position) {
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
                        break;
                    case IMAGE_BUTTON_STOP:
                        //TODO
                        break;
                    default:
                        break;
                }
            }
        });
    }

    //获取歌曲列表
    @SuppressLint("CheckResult")
    public void loadSongEntities(final String nameOfSongList)
    {
        songDataBase=SongDataBase.getDatabase(mainActivity);
        songDataBaseDao=songDataBase.SongDataBaseDao();


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
                        viewModel.getCurrentData().setValue(songs);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }).subscribe();
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
