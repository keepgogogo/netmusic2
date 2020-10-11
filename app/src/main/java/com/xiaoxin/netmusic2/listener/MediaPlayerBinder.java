package com.xiaoxin.netmusic2.listener;

import com.xiaoxin.netmusic2.database.SongEntity;

public interface MediaPlayerBinder extends MediaPlayerListener {
    void setIsLastPlaySongExist();
    void sendNotification(SongEntity entity);
    void playStatusChanged(boolean isPlaying);
}
