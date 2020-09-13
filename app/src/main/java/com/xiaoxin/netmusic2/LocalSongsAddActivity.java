package com.xiaoxin.netmusic2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.xiaoxin.netmusic2.database.Song;
import com.xiaoxin.netmusic2.database.SongEntity;
import com.xiaoxin.netmusic2.recycler.SongAdapter;
import com.xiaoxin.netmusic2.recycler.SongRecyclerViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class LocalSongsAddActivity extends AppCompatActivity {

    private EditText editText;
    private ProgressBar progressBar;
    private Button buttonForConfirmEditText;

    private FileLoader fileLoader;
    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private SongRecyclerViewModel viewModel;
    private RecyclerView.LayoutManager layoutManager;
    private List<SongEntity> songsOfLocal;
    private KeyListener keyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_songs_add);

        editText=(EditText)findViewById(R.id.EditTextInLocalSongActivity);
        progressBar=(ProgressBar)findViewById(R.id.ProgressBarInLocalSongs);
        buttonForConfirmEditText=(Button)findViewById(R.id.ButtonForSearchInLocalSongActivity);

        progressBar.setVisibility(View.VISIBLE);
        keyListener=editText.getKeyListener();
        editText.setHint("请等待检索完成");
        editText.setKeyListener(null);

        //通过rxjava2调用MediaStore读取本地歌曲
        loadSongs();
        //初始化recyclerView
        initRecyclerView();
    }

    /**
     * 该方法使用rxjava2读取手机中目前已存在的歌曲信息，并更新adapter中的数据以更新列表
     */
    @SuppressLint("CheckResult")
    public void loadSongs()
    {
        fileLoader=new FileLoader();
        fileLoader.setContext(this)
                .setRequestUri(FileLoader.REQUEST_AUDIO_EXTERNAL_CONTENT_URI)
                .setSortOrder(FileLoader.AUDIO_DEFAULT_SORT_ORDER)
                .getStorageAccess(LocalSongsAddActivity.this);

        Observable.create(new ObservableOnSubscribe<List<SongEntity>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<SongEntity>> emitter)throws Exception
            {
                List<Song> song=new ArrayList<>();
                fileLoader.startQuery();
                Cursor cursor=fileLoader.getCursor();
                while(cursor.moveToNext())
                {
                    Song temp=new Song();
                    temp.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                    if(!(new File(temp.getPath()).exists()))
                    {
                        continue;
                    }
                    temp.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
                    temp.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
                    temp.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
                    temp.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                    temp.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
                    temp.setAlbumId(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)));
                    song.add(temp);
                }
                emitter.onNext(song);
            }
        }).map(new Function<List<Song>, List<Song>>()
        {
            @Override
            public List<Song> apply(@NonNull List<Song> songs) throws Exception
            {
                Log.d(TAG, "apply: work");
                return songs;
            }

        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<Song>>() {
                    @Override
                    public void accept(List<Song> songs) throws Exception {
                        viewModel.getCurrentData().setValue(songs);
                        songsOfLocal=songs;
                        progressBar.setVisibility(View.GONE);
                        editText.setKeyListener(keyListener);
                        editText.setHint("请输入歌曲名称");
//                        progressBar.setMaxWidth(0);
                    }
                }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.d(TAG, "accept: lost");
            }
        }).subscribe();
    }


    //初始化recyclerView
    public void initRecyclerView()
    {
        recyclerView=(RecyclerView)findViewById(R.id.RecyclerInSongOfSongListFragment);
        layoutManager=new LinearLayoutManager(LocalSongsAddActivity.this);
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

        viewModel.getCurrentData().observe(LocalSongsAddActivity.this,ListOfSongsObserver);
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

}