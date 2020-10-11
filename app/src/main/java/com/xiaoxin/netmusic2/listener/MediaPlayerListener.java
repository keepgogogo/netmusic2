package com.xiaoxin.netmusic2.listener;

import com.xiaoxin.netmusic2.database.SongEntity;

public interface MediaPlayerListener {
    void onChange(SongEntity oldSong,SongEntity newSong);
}
