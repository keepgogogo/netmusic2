package com.xiaoxin.netmusic2.database;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

public class Song {

    private int id;

    //歌曲文件路径
    private String path;

    //歌曲名
    private String name;

    //歌曲所属专辑
    private String album;

    //歌曲艺术家
    private String artist;

    //歌曲大小
    private long size;

    //歌曲时长
    private int duration;

    //专辑封面id
    private long albumId;

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public long getAlbumId() {
        return albumId;
    }


    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public long getSize() {
        return size;
    }

    public int getDuration() {
        return duration;
    }

    public int getId() {
        return id;
    }
}
