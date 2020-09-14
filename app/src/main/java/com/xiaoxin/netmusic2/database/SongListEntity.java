package com.xiaoxin.netmusic2.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "SongList")
public class SongListEntity {
    @ColumnInfo
    private String songList;

    public String getSongList() {
        return songList;
    }

    public void setSongList(String songList) {
        this.songList = songList;
    }
}
