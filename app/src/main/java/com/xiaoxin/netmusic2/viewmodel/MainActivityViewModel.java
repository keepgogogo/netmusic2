package com.xiaoxin.netmusic2.viewmodel;

import androidx.lifecycle.ViewModel;

import com.xiaoxin.netmusic2.database.SongListEntity;
import com.xiaoxin.netmusic2.viewpager2.SongListEditFragmentViewPagerAdapter;

public class MainActivityViewModel extends ViewModel {

    private SongListEntity songListEntity;
    private SongListEditFragmentViewPagerAdapter songListEditFragmentViewPagerAdapter;

    public SongListEditFragmentViewPagerAdapter getSongListEditFragmentViewPagerAdapter() {
        return songListEditFragmentViewPagerAdapter;
    }

    public void setSongListEditFragmentViewPagerAdapter
            (SongListEditFragmentViewPagerAdapter songListEditFragmentViewPagerAdapter) {
        this.songListEditFragmentViewPagerAdapter = songListEditFragmentViewPagerAdapter;
    }

    public MainActivityViewModel(){
        super();
        songListEntity=new SongListEntity();
    }

    public SongListEntity getSongListEntity() {
        return songListEntity;
    }

    public void setSongListEntity(SongListEntity songListEntity) {
        this.songListEntity = songListEntity;
    }
}
