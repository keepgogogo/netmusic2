package com.xiaoxin.netmusic2.database;

import android.graphics.Bitmap;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "SongOfList")
public class SongEntity {

    public SongEntity(Song mSong)
    {
        super();
        song=mSong;
        artist=song.getArtist();
    }

    //歌单的歌曲
    @ColumnInfo
    private Song song;

    @ColumnInfo
    private String artist;

    //歌单名
    @ColumnInfo
    private String songList;

    @ColumnInfo
    private Bitmap albumPicture;

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public String getSongList() {
        return songList;
    }

    public void setSongList(String songList) {
        this.songList = songList;
    }

    public Bitmap getAlbumPicture() {
        return albumPicture;
    }

    public void setAlbumPicture(Bitmap albumPicture) {
        this.albumPicture = albumPicture;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
