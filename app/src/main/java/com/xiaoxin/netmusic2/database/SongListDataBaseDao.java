package com.xiaoxin.netmusic2.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SongListDataBaseDao {

    @Insert
    void insert(SongListEntity entity);

    @Query("SELECT * FROM SongList")
    List<SongListEntity> loadAll();

    @Delete
    void delete(SongListEntity entity);
}
