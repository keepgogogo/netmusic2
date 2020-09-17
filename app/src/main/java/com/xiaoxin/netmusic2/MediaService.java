package com.xiaoxin.netmusic2;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import com.xiaoxin.netmusic2.database.SongEntity;

import java.io.IOException;
import java.util.List;

public class MediaService extends Service {

    private static final String TAG="MediaService";
    private List<SongEntity> songEntities;
    private boolean isPaused;

    private MyBinder myBinder=new MyBinder();
    private MediaPlayer mediaPlayer=new MediaPlayer();

    public void playSong(SongEntity songEntity) throws IOException {
        if(mediaPlayer.isPlaying()&&!isPaused){
            mediaPlayer.reset();
            mediaPlayer.setDataSource(songEntity.getPath());
            mediaPlayer.start();
        }else if(isPaused){
            mediaPlayer.start();
        }else {
            mediaPlayer.setDataSource(songEntity.getPath());
            mediaPlayer.start();
        }
        isPaused=false;
    }

    public void nextSong(SongEntity songEntity)throws IOException {
        mediaPlayer.reset();
        mediaPlayer.setDataSource(songEntity.getPath());
        mediaPlayer.start();
        isPaused=false;
    }

    public void lastSong(SongEntity songEntity)throws IOException{
        mediaPlayer.reset();
        mediaPlayer.setDataSource(songEntity.getPath());
        mediaPlayer.start();
        isPaused=false;
    }

    public void pauseMediaPlayer(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            isPaused=true;
        }
    }

    public void closeMediaPlayer(){
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }


    public List<SongEntity> getSongEntities() {
        return songEntities;
    }

    public void setSongEntities(List<SongEntity> songEntities) {
        this.songEntities = songEntities;
    }

    public MyBinder getMyBinder() {
        return myBinder;
    }

    public void setMyBinder(MyBinder myBinder) {
        this.myBinder = myBinder;
    }

    public MediaService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder{}
}
