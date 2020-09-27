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
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaExtractor;
import android.media.MediaPlayer;
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
import android.widget.TextView;

import com.xiaoxin.netmusic2.database.Song;
import com.xiaoxin.netmusic2.database.SongDataBase;
import com.xiaoxin.netmusic2.database.SongDataBaseDao;
import com.xiaoxin.netmusic2.database.SongEntity;
import com.xiaoxin.netmusic2.database.SongListDataBase;
import com.xiaoxin.netmusic2.database.SongListDataBaseDao;
import com.xiaoxin.netmusic2.database.SongListEntity;
import com.xiaoxin.netmusic2.recycler.SongAdapter;
import com.xiaoxin.netmusic2.recycler.SongRecyclerViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.content.ContentValues.TAG;

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
    private SongRecyclerViewModel songRecyclerViewModel;
    private RecyclerView.LayoutManager layoutManager;
    private List<SongEntity> songsOfLocal;
    private List<SongEntity> localSongsInDataBase;
    private List<SongEntity> songsOfNewSongList;
    private KeyListener editTextKeyListener;
    
    private SongListDataBase songListDataBase;
    private SongListDataBaseDao songListDataBaseDao;
    
    private SongDataBase songDataBase;
    private SongDataBaseDao songDataBaseDao;

    private AlertDialog alertDialog;
    private Bitmap defaultAlbumBitmap;
    private ByteArrayOutputStream defaultAlbumOutPutStream;
    private byte[] defaultAlbumBytes;
    private byte[] playImageBytes;

    private MediaPlayerOperator mediaPlayerOperator;
    private SongEntity underPlayingSong;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_songs_add);
        songsOfNewSongList=new ArrayList<>();
        initUI();
        setClickListenOfThis();
        makeEditTextCannotBeEdited();
        initBothDataBaseAndDao();
        loadLocalSongsReservedInDataBase();
        //通过rxjava2调用MediaStore读取本地歌曲
        loadSongsFromMediaStore();
        //初始化recyclerView
        initRecyclerView();
    }

    private void initBothDataBaseAndDao() {
        songListDataBase=SongListDataBase.getDatabase(this);
        songListDataBaseDao=songListDataBase.SongListDataBaseDao();
        songDataBase=SongDataBase.getDatabase(this);
        songDataBaseDao=songDataBase.SongDataBaseDao();
    }

    private void makeEditTextCannotBeEdited() {
        editTextKeyListener=editText.getKeyListener();
        editText.setHint("请等待检索完成");
        editText.setKeyListener(null);
    }

    public void setClickListenOfThis(){
        buttonForConfirmEditText.setOnClickListener(this);
        buttonForChooseAll.setOnClickListener(this);
        buttonForConfirmSongList.setOnClickListener(this);
        buttonForCancelAll.setOnClickListener(this);
    }

    public void loadLocalSongsReservedInDataBase(){
        Observable.create(new ObservableOnSubscribe<List<SongEntity>>() {
            @Override
            public void subscribe(ObservableEmitter<List<SongEntity>> emitter) throws Exception {
                emitter.onNext(songDataBaseDao.getBySongList("LocalSongs"));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<SongEntity>>() {
                    @Override
                    public void accept(List<SongEntity> entities) throws Exception {
                        if (entities.size()!=0){
                            localSongsInDataBase=entities;
                            adapter.setDataList(entities);
                            songRecyclerViewModel.getCurrentData().setValue(entities);
                            progressBar.setVisibility(View.GONE);
                            makeEditTextEditable();
                        }
                    }
                })
                .subscribe();
    }

    /**
     * 该方法使用rxjava2读取手机中目前已存在的歌曲信息，并更新adapter中的数据以更新列表
     */
    @SuppressLint("CheckResult")
    public void loadSongsFromMediaStore()
    {

        fileLoader=new FileLoader();
        fileLoader.setContext(this)
                .setRequestUri(FileLoader.REQUEST_AUDIO_EXTERNAL_CONTENT_URI)
                .setSortOrder(FileLoader.AUDIO_DEFAULT_SORT_ORDER)
                .getStorageAccess(LocalSongsAddActivity.this);

        Observable.create(new ObservableOnSubscribe<List<SongEntity>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<SongEntity>> emitter)
            {
                defaultAlbumBytes=getDefaultAlbumBytes();
                playImageBytes=getPlayImageBytes();
                fileLoader.startQuery();
                List<SongEntity> songEntities=loadSongIntoList(fileLoader.getCursor());
                emitter.onNext(getNewAddSongs(songEntities));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<SongEntity>>() {
                    @Override
                    public void accept(List<SongEntity> songEntities) throws Exception {
                        if (localSongsInDataBase==null){
                            localSongsInDataBase=songEntities;
                            adapter.setDataList(localSongsInDataBase);
                            saveNewAddLocalSongs(songEntities);
                        }else {
                            localSongsInDataBase.addAll(songEntities);
                            adapter.getDataList().addAll(songEntities);
                            updateAllLocalSongs(songEntities);
                        }

//                        songRecyclerViewModel.getCurrentData().setValue(localSongsInDataBase);
                        progressBar.setVisibility(View.GONE);
                        makeEditTextEditable();

                    }
                }).subscribe();
    }

    public void updateAllLocalSongs(final List<SongEntity> entities){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(SongEntity entity: entities){
                    entity.setSongList("LocalSongs");
                }
//                songDataBaseDao.deleteBySongList("LocalSongs");
//                songDataBaseDao.insert(localSongsInDataBase);
                songDataBaseDao.update(entities.toArray(new SongEntity[0]));
                Log.d("LocalSongAddActivity", "run:update database ");
            }
        }).start();
    }

    public void saveNewAddLocalSongs(final List<SongEntity> entities){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (SongEntity entity : entities){
                    if(entity!=null){
                        entity.setSongList("LocalSongs");
                    }
                }
                SongEntity[] arraysOfSongEntity=new SongEntity[entities.size()];
                for(int i=0;i<arraysOfSongEntity.length;i++)
                {
                    arraysOfSongEntity[i]=entities.get(i);
                }
                songDataBaseDao.insert(arraysOfSongEntity);
                Log.d(TAG, "run: insert local songs to database");
            }
        }).start();
    }

    public List<SongEntity> getNewAddSongs(List<SongEntity> entityList){
        if (localSongsInDataBase==null)return entityList;
        for(int i=0;i<entityList.size();i++){
            SongEntity tempEntity=entityList.get(i);
            if(!localSongsInDataBase.contains(tempEntity)){
                entityList.remove(tempEntity);
            }
        }
        return entityList;
    }

    public byte[] getDefaultAlbumBytes(){
        defaultAlbumBitmap=BitmapFactory.decodeResource(LocalSongsAddActivity.this.getResources(),
                R.mipmap.default_cover);
        defaultAlbumOutPutStream=new ByteArrayOutputStream();
        defaultAlbumBitmap.compress(Bitmap.CompressFormat.PNG,100,defaultAlbumOutPutStream);
        return  defaultAlbumOutPutStream.toByteArray();
    }

    public byte[] getPlayImageBytes(){
        Bitmap tempBitmap=BitmapFactory.decodeResource(LocalSongsAddActivity.this.getResources(),
                R.mipmap.ic_play_bar_btn_play);
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        tempBitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        return outputStream.toByteArray();
    }

    public void makeEditTextEditable(){
        editTextListenStart();
        editText.setKeyListener(editTextKeyListener);
        editText.setHint("请输入歌曲名称");
    }

    public List<SongEntity> loadSongIntoList(Cursor cursor){
        List<SongEntity> entities=new ArrayList<>();
//        int i=1;
        while(cursor.moveToNext())
        {
            Song tempSong=new Song();
            tempSong.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
            if(!(new File(tempSong.getPath()).exists()))
            {
                continue;
            }

            entities.add(getCreatedSongEntity(tempSong,cursor));

//            Log.d("RxJava读取", "subscribe:完成了"+i+"首歌操作 ");
//            i++;
        }
        return entities;
    }

    public SongEntity getCreatedSongEntity(Song tempSong,Cursor cursor){
        SongEntity entity=new SongEntity();
        entity.setSong(getBasicInformationOfSong(tempSong,cursor));
        entity.setAlbumPicture(getAlbumBytes(entity.getAlbumID()));
        entity.setPlayImagePicture(playImageBytes);
        return entity;
    }

    public Song getBasicInformationOfSong(Song tempSong,Cursor cursor){
        tempSong.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
        tempSong.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
        tempSong.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
        tempSong.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
//        tempSong.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
        tempSong.setAlbumId(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)));
        return tempSong;
    }

    /**
     * 该方法使用rxjava2将新设置的歌单存入数据库
     */
    @SuppressLint("CheckResult")
    public void saveNewSongList(final String name)
    {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter)
            {
                insertSongListToDataBase(name);
                insertSongToDataBase(name);
                alertDialog.dismiss();
                emitter.onNext(0);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        makeAllSongItemUnChosen();
                    }
                })
                .subscribe();
    }

    public void insertSongListToDataBase(final String name){
        SongListEntity entity=new SongListEntity();
        entity.setCount(songsOfNewSongList.size());
        entity.setSongList(name);
        songListDataBaseDao.insert(entity);
//        makeAllSongItemUnChosen();
    }

    public void makeAllSongItemUnChosen(){
       for(int i=0;i<localSongsInDataBase.size();i++){
           localSongsInDataBase.get(i).setCheckBoxChecked(false);
       }
        adapter.setDataList(localSongsInDataBase);
        songRecyclerViewModel.getCurrentData().setValue(localSongsInDataBase);
    }

    public void insertSongToDataBase(final String name){
        SongEntity[] songsToBeAdd=new SongEntity[songsOfNewSongList.size()];
        for(int i=0;i<songsToBeAdd.length;i++){
            SongEntity temp=new SongEntity();
            CopySongEntity(temp,songsOfNewSongList.get(i));
            temp.setSongList(name);
            songsToBeAdd[i]=temp;
        }
        try {
            songDataBaseDao.insert(songsToBeAdd);
        }catch (SQLiteConstraintException e){
            Log.d(TAG, "insertSongToDataBase: SQLiteConstraintException");
        }
    }

    public void CopySongEntity(SongEntity target,SongEntity source){
        target.setPlayImagePicture(source.getPlayImagePicture());
        target.setAlbumPicture(source.getAlbumPicture());
        target.setAlbum(source.getAlbum());
        target.setCheckBoxChecked(source.isCheckBoxChecked());
        target.setAlbumID(source.getAlbumID());
        target.setArtist(source.getArtist());
        target.setName(source.getName());
        target.setPath(source.getPath());
        target.setDuration(source.getDuration());
        target.setPlaying(source.isPlaying());
        target.setSize(source.getSize());
    }



    public byte[] getAlbumBytes(long albumID){
        Bitmap tempBitmap=loadCoverFromMediaStore(albumID);
        if(tempBitmap!=null){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return baos.toByteArray();
        }else {
            return defaultAlbumBytes;
        }
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
                    songRecyclerViewModel.getCurrentData().setValue(localSongsInDataBase);
                    adapter.setDataList(localSongsInDataBase);
                }
                else
                {
                    Observable.create(new ObservableOnSubscribe<List<SongEntity>>() {
                        @Override
                        public void subscribe(ObservableEmitter<List<SongEntity>> emitter) throws Exception {
                            List<SongEntity> temp=new ArrayList<>();
                            for(SongEntity songEntity : localSongsInDataBase)
                            {
                                if((songEntity.getName()!=null)&&songEntity.getName().contains(m))
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
                    }).subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext(new Consumer<List<SongEntity>>() {
                                @Override
                                public void accept(List<SongEntity> songs) throws Exception {
                                    songRecyclerViewModel.getCurrentData().setValue(songs);
                                    adapter.setDataList(songs);
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
        recyclerView=(RecyclerView)findViewById(R.id.RecyclerViewLocalSongs);
        layoutManager=new LinearLayoutManager(LocalSongsAddActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        songRecyclerViewModel =new ViewModelProvider(this).get(SongRecyclerViewModel.class);
        adapter=new SongAdapter();
        adapter.setContext(this);
        recyclerView.setAdapter(adapter);
        mediaPlayerOperator= new MediaPlayerOperator();

        final Observer<List<SongEntity>> ListOfSongsObserver= new Observer<List<SongEntity>>() {
            @Override
            public void onChanged(List<SongEntity> songs) {
                adapter.setDataList(songs);
                recyclerView.setAdapter(adapter);
            }
        };

        songRecyclerViewModel.getCurrentData().observe(LocalSongsAddActivity.this,ListOfSongsObserver);
        adapter.setClickListener(new SongAdapter.SongRecyclerClickListener() {
            @Override
            public void onClick(View view, SongAdapter.ViewNameSongRecyclerEnum viewName, SongEntity entity) {
                switch (viewName)
                {
                    case CHECK_BOX_SET_FALSE:
                        //todo
                        songsOfNewSongList.remove(entity);
                        break;

                    case CHECK_BOX_SET_TRUE:
                        //TODO
                        songsOfNewSongList.add(entity);
                        break;

                    case IMAGE_BUTTON_PLAY:
                        if(underPlayingSong!=null){
                            if(entity.equals(underPlayingSong)){
                                mediaPlayerOperator.continueMediaPlayer();
                            }else {
                                mediaPlayerOperator.resetMediaPlayer();
                                underPlayingSong=entity;
                                try {
                                    mediaPlayerOperator.setMusicPath(entity.getPath());
                                    mediaPlayerOperator.MediaPlayerPrepare();
                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                            }
                        }else {
                            mediaPlayerOperator.resetMediaPlayer();
                            underPlayingSong=entity;
                            try {
                                mediaPlayerOperator.setMusicPath(entity.getPath());
                                mediaPlayerOperator.MediaPlayerPrepare();
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                        break;

                    case IMAGE_BUTTON_STOP:
                        mediaPlayerOperator.pauseMediaPlayer();
                        break;

                    default:
                        break;
                }
            }
        });
        adapter.setViewModel(songRecyclerViewModel);
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
        progressBar.setVisibility(View.VISIBLE);
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
                if (songsOfNewSongList.size()!=0){
                    startNewSongListSave();
                }else{
                    showAddSongsNoticeDialog();
                }
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

    public void startNewSongListSave() {
        final EditText dialogEditText=new EditText(this);
        new AlertDialog.Builder(this).setTitle("请输入新歌单的名字")
                .setView(dialogEditText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String m=dialogEditText.getText().toString();
                        final ProgressBar mProgressBar=new ProgressBar(LocalSongsAddActivity.this);
                        alertDialog=new AlertDialog
                                .Builder(LocalSongsAddActivity.this).setTitle("请稍微等待")
                                .setView(mProgressBar)
                                .setCancelable(false)
                                .show();
                        saveNewSongList(m);
                    }
                }).setNegativeButton("取消",null).show();
    }

    public void showAddSongsNoticeDialog(){
        TextView textViewForNotice=new TextView(this);
        textViewForNotice.setText("当前尚未选择任何歌曲");
        new AlertDialog.Builder(this).setTitle("提示")
                .setView(textViewForNotice)
                .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).show();
    }

    private static class MediaPlayerOperator{
        private MediaPlayer mediaPlayer;

        public void MediaPlayerPrepare() throws IOException {
            mediaPlayer.prepare();
        }

        public MediaPlayerOperator(){
            super();
            mediaPlayer=new MediaPlayer();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
        }

        public void resetMediaPlayer(){
            mediaPlayer.reset();
        }

        public void pauseMediaPlayer(){
            mediaPlayer.pause();
        }

        public void continueMediaPlayer(){
            mediaPlayer.start();
        }

        public void setMusicPath(String path) throws IOException {
            mediaPlayer.setDataSource(path);
        }

        public MediaPlayer getMediaPlayer() {
            return mediaPlayer;
        }

        public void setMediaPlayer(MediaPlayer mediaPlayer) {
            this.mediaPlayer = mediaPlayer;
        }
    }

}