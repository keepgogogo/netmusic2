package com.xiaoxin.netmusic2.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "SongList")
public class SongListEntity {
    @ColumnInfo
    private String songList;

    @ColumnInfo
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSongList() {
        return songList;
    }

    public void setSongList(String songList) {
        this.songList = songList;
    }
}
