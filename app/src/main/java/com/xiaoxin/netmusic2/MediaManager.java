package com.xiaoxin.netmusic2;


import android.media.MediaPlayer;

import com.xiaoxin.netmusic2.database.SongDataBaseDao;
import com.xiaoxin.netmusic2.database.SongEntity;
import com.xiaoxin.netmusic2.database.SongListEntity;
import com.xiaoxin.netmusic2.listener.MediaPlayerBinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class MediaManager {

    public static final int PLAY_AS_RANK_OF_LIST = 0;
    public static final int PLAY_BY_RANDOM = 1;

    private MediaPlayer mediaPlayer;
    private MediaEasyController mediaEasyController;
    private List<SongEntity> songEntities;
    private SongListEntity songListEntity;
    private SongDataBaseDao songDataBaseDao;
    private SongEntity underPlayingSongEntity;
    private boolean isPaused;
    private boolean havePlayerEverPlay;
    private MediaPlayerBinder mediaPlayerBinder;
    private float underPlayingSongDuration;

    public MediaManager() {
        super();
        isPaused = false;
        havePlayerEverPlay=false;
        mediaEasyController = new MediaEasyController();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mediaPlayer) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mediaPlayer.start();
                        mediaPlayerBinder.setIsLastPlaySongExist();
                        havePlayerEverPlay=true;
                        underPlayingSongDuration = mediaPlayer.getDuration();

                        mediaPlayerBinder.sendNotification(getUnderPlayingSongEntity());
                    }
                }).start();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                try {
                    SongEntity nextSong;
                    if (songEntities != null) {
                        if (positionOfPlayingMusic == songEntities.size() - 1) {
                            positionOfPlayingMusic = 0;
                            nextSong = songEntities.get(arrangementOfSong.get(positionOfPlayingMusic));
                        } else {
                            nextSong = songEntities.get(arrangementOfSong.get(++positionOfPlayingMusic));
                        }
                        mediaPlayerBinder.onChange(underPlayingSongEntity, nextSong);
                        playSong(nextSong);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private int positionOfPlayingMusic;
    private ArrayList<Integer> arrangementOfSong;

    public void playSong(final SongEntity songEntity) throws IOException {

        if (mediaPlayer.isPlaying() && !isPaused) {
            try {
                SongEntity temp=underPlayingSongEntity;
                underPlayingSongEntity = songEntity;
                mediaPlayerBinder.onChange(temp,songEntity);
                mediaPlayer.reset();
                mediaPlayer.setDataSource(songEntity.getPath());

                mediaPlayer.prepare();//todo
            } catch (IOException e) {
                e.printStackTrace();
            }

        }



        else if (isPaused) {
            if (mediaPlayer.getDuration()==songEntity.getDuration())
            {
                mediaPlayer.start();
            }else {
                try {
                    SongEntity temp=underPlayingSongEntity;
                    underPlayingSongEntity = songEntity;
                    mediaPlayerBinder.onChange(temp,songEntity);
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(songEntity.getPath());
                    mediaPlayer.prepare();//todo
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        else {
            try {
//                mediaPlayerListener.onChange(underPlayingSongEntity,songEntity);
                underPlayingSongEntity=songEntity;
                mediaPlayer.reset();
                mediaPlayer.setDataSource(songEntity.getPath());
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        isPaused = false;
    }


    public void nextSong(SongEntity songEntity) throws IOException {
        playSong(songEntity);
        isPaused = false;
    }

    public void lastSong(SongEntity songEntity) throws IOException {
        playSong(songEntity);
        isPaused = false;
    }

    public void pauseMediaPlayer() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
        }
    }

    public void closeMediaPlayer() {
        if (mediaPlayer != null) {
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

    public SongListEntity getSongListEntity() {
        return songListEntity;
    }

    public void setSongListEntity(final SongListEntity listEntity, final int playMode) {
        songListEntity = listEntity;
        Observable.create(new ObservableOnSubscribe<List<SongEntity>>() {
            @Override
            public void subscribe(ObservableEmitter<List<SongEntity>> emitter) throws Exception {
                List<SongEntity> songEntities = songDataBaseDao.getBySongList(listEntity.getSongList());

                arrangementOfSong = new ArrayList<Integer>();
                int sizeOfSongEntities = songEntities.size();
                switch (playMode) {

                    case PLAY_AS_RANK_OF_LIST:
                        for (int i = 0; i < sizeOfSongEntities; i++) {
                            arrangementOfSong.add(i);
                        }
                        break;
                    case PLAY_BY_RANDOM:
                        while (arrangementOfSong.size() != sizeOfSongEntities) {
                            int x = new Random().nextInt(sizeOfSongEntities);
                            if (!arrangementOfSong.contains(x)) {
                                arrangementOfSong.add(x);
                            }
                        }
                        break;
                    default:
                        break;

                }
                emitter.onNext(songEntities);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<SongEntity>>() {
                    @Override
                    public void accept(List<SongEntity> entities) throws Exception {
                        songEntities = entities;
                        playSong(songEntities.get(arrangementOfSong.get(0)));
                    }
                }).subscribe();
    }

    public void setSongDataBaseDao(SongDataBaseDao songDataBaseDao) {
        this.songDataBaseDao = songDataBaseDao;
    }

    public boolean isPaused() {
        return isPaused;
    }


    public int getPositionOfPlayingMusic() {
        return positionOfPlayingMusic;
    }

    public void setPositionOfPlayingMusic(int positionOfPlayingMusic) {
        this.positionOfPlayingMusic = positionOfPlayingMusic;
    }
    public class MediaEasyController {

        public void startPlayListByRank(SongListEntity songListEntity) {
            setSongListEntity(songListEntity, PLAY_AS_RANK_OF_LIST);
            positionOfPlayingMusic = 0;
        }

        public void startPlayListByRandom(SongListEntity songListEntity) {
            setSongListEntity(songListEntity, PLAY_BY_RANDOM);
            positionOfPlayingMusic = 0;
        }

        public boolean isMediaPlayerPause(){
            return isPaused();
        }

        public void pauseOrStart() {
            if (!isPaused()) {
                pauseMediaPlayer();
            } else if (isPaused()) {
                mediaPlayer.start();
                isPaused=false;
            }
            mediaPlayerBinder.playStatusChanged(isPaused);
        }

        public float getUnderPlayingSongDuration() {
            return underPlayingSongDuration;
        }

        public int getMediaPlayerCurrentDuration() {
            return mediaPlayer.getCurrentPosition();
        }

        public void pausePlay() {
            pauseMediaPlayer();
        }

        public void startPlay() throws IOException {
            if (isPaused) {
                playSong(new SongEntity());
            } else {
                playSong(songEntities.get(arrangementOfSong.get(positionOfPlayingMusic)));
            }
        }

        public void nextSong() {
            try {
                if (positionOfPlayingMusic==arrangementOfSong.size()-1){
                    positionOfPlayingMusic=0;
                }
                playSong(songEntities.get(arrangementOfSong.get(++positionOfPlayingMusic)));
            } catch (IOException o) {
                o.printStackTrace();
            }

        }

        public void lastSong() {
            try {
                if (positionOfPlayingMusic==0){
                    positionOfPlayingMusic=arrangementOfSong.size()-1;
                }
                playSong(songEntities.get(arrangementOfSong.get(--positionOfPlayingMusic)));
            } catch (IOException o) {
                o.printStackTrace();
            }

        }

        public void setDataBaseDao(SongDataBaseDao songDataBaseDao) {
            setSongDataBaseDao(songDataBaseDao);
        }

        public void endPlay() {
            closeMediaPlayer();
        }

        public void playMidway(SongEntity songEntity) {
            try {
                playSong(songEntity);
                setPositionOfPlayingMusic(getCorrectPositionOfSong(songEntity));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void playImmediatelyWhenStart(SongEntity songEntity){
            try {
                playSong(songEntity);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        public int getCorrectPositionOfSong(SongEntity entity){
            for(int i=0;i<arrangementOfSong.size();i++){
                if (songEntities.get(arrangementOfSong.get(i)).getName()
                        .equals(entity.getName())){
                    return i;
                }
            }
            return 0;
        }

        public SongEntity getUnderPlayingSongEntity() {
            return MediaManager.this.getUnderPlayingSongEntity();
        }

        public void mediaPlayJumpTo(int progress) {
            float time = progress * 1.0f;
            time=time/100;
            time=time*(getUnderPlayingSongEntity().getDuration());
            mediaPlayer.seekTo((int)time);
        }

        public boolean havePlayerEverPlayed(){
            return havePlayerEverPlay;
        }


    }

    public MediaEasyController getMediaEasyController() {
        return mediaEasyController;
    }

    public SongEntity getUnderPlayingSongEntity() {
        return underPlayingSongEntity;
    }

    public void setMediaPlayerBinder(MediaPlayerBinder mediaPlayerBinder) {
        this.mediaPlayerBinder = mediaPlayerBinder;
    }

}
