package com.xiaoxin.netmusic2.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SongListEntity.class},version = 1,exportSchema = false)
public abstract class SongListDataBase extends RoomDatabase {
    public abstract SongListDataBaseDao SongListDataBaseDao();
    private static volatile SongListDataBase INSTANCE;//volatile保证从内存读写，各线程中获取的database实例相同

    public static SongListDataBase getDatabase(final Context context)
    {
        if(INSTANCE == null )
        {
            synchronized (SongListDataBase.class)
            {
                if(INSTANCE == null)
                {
                    INSTANCE= Room.databaseBuilder(context.getApplicationContext(),
                            SongListDataBase.class,
                            "SongListDataBase")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
