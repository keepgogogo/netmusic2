package com.xiaoxin.netmusic2.database;

import android.graphics.Bitmap;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "SongOfList")
public class SongEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;


    //歌曲文件路径
    @ColumnInfo
    private String path;

    //歌曲名
    @ColumnInfo
    private String name;

    //歌曲所属专辑
    @ColumnInfo
    private String album;

    //歌曲艺术家
    @ColumnInfo
    private String artist;

    //歌曲大小
    @ColumnInfo
    private long size;

    //歌曲时长
    @ColumnInfo
    private int duration;


    //歌单名
    @ColumnInfo
    private String songList;

    @ColumnInfo
    private byte[] albumPicture;

    @Ignore
    private boolean isCheckBoxChecked;

    public boolean isCheckBoxChecked() {
        return isCheckBoxChecked;
    }

    public void setCheckBoxChecked(boolean checkBoxChecked) {
        isCheckBoxChecked = checkBoxChecked;
    }

    public void setSong(Song song) {
        path=song.getPath();
        album=song.getAlbum();
        duration=song.getDuration();
        size=song.getSize();
        name=song.getName();
    }

    public String getSongList() {
        return songList;
    }

    public void setSongList(String songList) {
        this.songList = songList;
    }

    public byte[] getAlbumPicture() {
        return albumPicture;
    }

    public void setAlbumPicture(byte[] albumPicture) {
        this.albumPicture = albumPicture;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
