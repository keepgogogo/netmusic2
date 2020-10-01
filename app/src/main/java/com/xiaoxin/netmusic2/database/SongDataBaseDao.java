package com.xiaoxin.netmusic2.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SongDataBaseDao {

    @Insert
    void insert(SongEntity ... songEntities);

//    @Insert
//    void insert(List<SongEntity> entities);


    @Query("SELECT * FROM SongOfList")
    List<SongEntity> getAll();

    @Query("SELECT * FROM SongOfList WHERE songList =:songList")
    List<SongEntity> getBySongList(String songList);

    @Query("SELECT * FROM SongOfList WHERE artist =:artist")
    List<SongEntity> getByArtist(String artist);

    @Query("DELETE FROM SongOfList WHERE songList=:songList ")
    void deleteBySongList(String songList);

    @Query("SELECT * FROM SongOfList WHERE id=:id")
    SongEntity getById(int id);

//    @Query("SELECT * FROM SongOfList WHERE name =:nameOfSong")
//    List<Song> getByName(String nameOfSong);

    @Delete
    void delete(SongEntity ... songEntities);

    @Update
    void update(SongEntity ... songEntities);
//
//    @Update
//    void update(List<SongEntity> songEntities);
}