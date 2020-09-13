package com.xiaoxin.netmusic2.recycler;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.xiaoxin.netmusic2.database.SongEntity;


import java.util.List;

public class SongRecyclerViewModel extends ViewModel {
    private MutableLiveData<List<SongEntity>> currentData;
    public MutableLiveData<List<SongEntity>> getCurrentData()
    {
        if(currentData ==null)
        {
            currentData =new MutableLiveData<List<SongEntity>>();
        }
        return currentData;
    }
}
