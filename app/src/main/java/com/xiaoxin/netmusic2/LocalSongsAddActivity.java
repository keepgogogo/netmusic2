package com.xiaoxin.netmusic2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.xiaoxin.netmusic2.database.Song;
import com.xiaoxin.netmusic2.database.SongDataBase;
import com.xiaoxin.netmusic2.database.SongDataBaseDao;
import com.xiaoxin.netmusic2.database.SongEntity;
import com.xiaoxin.netmusic2.database.SongListDataBase;
import com.xiaoxin.netmusic2.database.SongListDataBaseDao;
import com.xiaoxin.netmusic2.database.SongListEntity;
import com.xiaoxin.netmusic2.recycler.SongAdapter;
import com.xiaoxin.netmusic2.recycler.SongRecyclerViewModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public class LocalSongsAddActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editText;
    private ProgressBar progressBar;
    private Button buttonForConfirmEditText;
    private Button buttonForChooseAll;
    private Button buttonForCancelAll;
    private Button buttonForConfirmSongList;

    private FileLoader fileLoader;
    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private SongRecyclerViewModel viewModel;
    private RecyclerView.LayoutManager layoutManager;
    private List<SongEntity> songsOfLocal;
    private List<SongEntity> songsOfNewSongList;
    private KeyListener keyListener;
    
    private SongListDataBase songListDataBase;
    private SongListDataBaseDao songListDataBaseDao;
    
    private SongDataBase songDataBase;
    private SongDataBaseDao songDataBaseDao;

    private AlertDialog alertDialog;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_songs_add);

        //初始化UI组件
        initUI();

        buttonForConfirmEditText.setOnClickListener(this);
        buttonForChooseAll.setOnClickListener(this);
        buttonForConfirmSongList.setOnClickListener(this);
        buttonForCancelAll.setOnClickListener(this);
        progressBar.setVisibility(View.VISIBLE);

        keyListener=editText.getKeyListener();
        editText.setHint("请等待检索完成");
        editText.setKeyListener(null);

        songsOfNewSongList=new ArrayList<>();
        songListDataBase=SongListDataBase.getDatabase(this);
        songListDataBaseDao=songListDataBase.SongListDataBaseDao();
        
        songDataBase=SongDataBase.getDatabase(this);
        songDataBaseDao=songDataBase.SongDataBaseDao();
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
                List<SongEntity> songEntities=new ArrayList<>();
                fileLoader.startQuery();
                Cursor cursor=fileLoader.getCursor();
                while(cursor.moveToNext())
                {
                    Song tempSong=new Song();
                    tempSong.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                    if(!(new File(tempSong.getPath()).exists()))
                    {
                        continue;
                    }
                    tempSong.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
                    tempSong.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
                    tempSong.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
                    tempSong.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                    tempSong.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
                    tempSong.setAlbumId(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)));
                    SongEntity entity=new SongEntity(tempSong);
                    entity.setAlbumPicture(loadCoverFromMediaStore(tempSong.getAlbumId()));
                    songEntities.add(entity);
                }
                emitter.onNext(songEntities);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<SongEntity>>() {
                    @Override
                    public void accept(List<SongEntity> songEntities) throws Exception {
                        viewModel.getCurrentData().setValue(songEntities);
                        songsOfLocal=songEntities;
                        progressBar.setVisibility(View.GONE);

                        //启动对editText中已输入内容变化的监听
                        editTextListenStart();
                        editText.setKeyListener(keyListener);
                        editText.setHint("请输入歌曲名称");
//                        progressBar.setMaxWidth(0);
                    }
                }).subscribe();
    }

    /**
     * 该方法使用rxjava2将新设置的歌单存入数据库
     */
    @SuppressLint("CheckResult")
    public void saveNewSongList(final String name)
    {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter)throws Exception
            {
                SongListEntity entity=new SongListEntity();
                entity.setCount(songsOfNewSongList.size());
                entity.setSongList(name);
                songListDataBaseDao.insert(entity);
                for(SongEntity songEntity : songsOfNewSongList)
                {
                    songEntity.setSongList(name);
                }
                songDataBaseDao.insert(songsOfNewSongList);
                alertDialog.dismiss();
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    /**
     * 从媒体库加载封面<br>
     * 本地音乐
     */
    private Bitmap loadCoverFromMediaStore(long albumId) {
        ContentResolver resolver = this.getContentResolver();
        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(artworkUri,albumId);
        InputStream is;
        try {
            is = resolver.openInputStream(uri);
        } catch (FileNotFoundException ignored) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeStream(is, null, options);
    }

    /**
     * 该方法中启动对editText中内容变化的监听
     */
    public void editTextListenStart()
    {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("CheckResult")
            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count)
            {
                final String m=s.toString();
                if(m.equals(""))
                {
                    viewModel.getCurrentData().setValue(songsOfLocal);
                }
                else
                {
                    Observable.create(new ObservableOnSubscribe<List<SongEntity>>() {
                        @Override
                        public void subscribe(ObservableEmitter<List<SongEntity>> emitter) throws Exception {
                            List<SongEntity> temp=new ArrayList<>();
                            for(SongEntity songEntity : songsOfLocal)
                            {
                                if((songEntity.getSong().getName()!=null)&&songEntity.getSong().getName().contains(m))
                                {
                                    temp.add(songEntity);
                                }
                            }
                            if (temp.size()==0)
                            {
                                emitter.onNext(new ArrayList<SongEntity>());
                            }
                            else
                            {
                                emitter.onNext(temp);
                            }
                        }
                    }).map(new Function<List<SongEntity>, List<SongEntity>>() {
                        @Override
                        public List<SongEntity> apply(@NonNull List<SongEntity> songs)
                        {
                            return songs;
                        }

                    }).subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext(new Consumer<List<SongEntity>>() {
                                @Override
                                public void accept(List<SongEntity> songs) throws Exception {
                                    viewModel.getCurrentData().setValue(songs);
                                }
                            }).subscribe();
                }
            }



            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    /**
     * 初始化recyclerView
     */
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
                        songsOfNewSongList.remove(songsOfLocal.get(position));
                        break;
                    case CHECK_BOX_SET_TRUE:
                        //TODO
                        songsOfNewSongList.add(songsOfLocal.get(position));
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

    /**
     * 初始化所有UI组件
     */
    public void initUI(){
        editText=(EditText)findViewById(R.id.EditTextInLocalSongActivity);
        progressBar=(ProgressBar)findViewById(R.id.ProgressBarInLocalSongs);
        buttonForConfirmEditText=(Button)findViewById(R.id.ButtonForSearchInLocalSongActivity);
        buttonForCancelAll=(Button)findViewById(R.id.ButtonForCancelAllInLocalSongActivity);
        buttonForConfirmSongList=(Button)findViewById(R.id.ButtonForConfirmSongListInLocalSongActivity);
        buttonForChooseAll=(Button)findViewById(R.id.ButtonForChooseAllInLocalSongActivity);
    }

    /**
     * 活动内点击事件
     */
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.ButtonForCancelAllInLocalSongActivity:

                break;
            case R.id.ButtonForChooseAllInLocalSongActivity:

                break;
            case R.id.ButtonForConfirmSongListInLocalSongActivity:
                final EditText dialogEditText=new EditText(this);
                new AlertDialog.Builder(this).setTitle("请输入新歌单的名字")
                        .setView(dialogEditText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String m=dialogEditText.getText().toString();
                                saveNewSongList(m);
                            }
                        }).setNegativeButton("取消",null).show();
                final ProgressBar mProgressBar=new ProgressBar(this);
                alertDialog=new AlertDialog.Builder(this).setTitle("请稍微等待")
                        .setView(mProgressBar)
                        .setCancelable(false)
                        .show();
                break;
            case R.id.ButtonForSearchInLocalSongActivity:
                View viewFocus = this.getCurrentFocus();
                if (viewFocus != null) {
                    InputMethodManager imManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert imManager != null;
                    imManager.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
                }
                break;
            default:
                break;
        }
    }

}