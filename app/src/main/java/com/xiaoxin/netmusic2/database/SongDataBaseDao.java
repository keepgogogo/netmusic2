package com.xiaoxin.netmusic2.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

public interface SongDataBaseDao {

    @Insert
    void insert(SongEntity ... songEntities);

    @Query("SELECT * FROM SongOfList")
    List<SongEntity> getAll();

    @Query("SELECT * FROM SongOfList WHERE songList =:songList")
    List<SongEntity> getBySongList(String songList);

    @Query("SELECT * FROM SongOfList WHERE artist =:artist")
    List<SongEntity> getByArtist(String artist);

//    @Query("SELECT * FROM SongOfList WHERE name =:nameOfSong")
//    List<Song> getByName(String nameOfSong);

    @Delete
    void delete(SongEntity ... songEntities);

    @Update
    void update(SongEntity ... songEntities);
}