package com.xiaoxin.netmusic2.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

public interface SongListDataBaseDao {

    @Insert
    void insert(String m);

    @Query("SELECT * FROM SongList")
    List<String> loadAll();

    @Delete
    void delete(String m);
}
