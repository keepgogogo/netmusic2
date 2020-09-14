package com.xiaoxin.netmusic2.recycler;

import android.text.style.AlignmentSpan;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.xiaoxin.netmusic2.database.SongListEntity;

import java.util.List;

public class SongListRecyclerViewModel extends ViewModel {
    private MutableLiveData<List<SongListEntity>> currentData;
    public MutableLiveData<List<SongListEntity>> getCurrentData(){
        if(currentData==null)
        {
            currentData=new MutableLiveData<List<SongListEntity>>();
        }
        return currentData;
    }
}
