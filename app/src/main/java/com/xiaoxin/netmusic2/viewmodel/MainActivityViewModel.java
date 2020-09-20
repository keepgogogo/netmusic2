package com.xiaoxin.netmusic2.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.viewpager2.widget.ViewPager2;

import com.xiaoxin.netmusic2.MediaService;
import com.xiaoxin.netmusic2.database.SongListEntity;
import com.xiaoxin.netmusic2.viewpager2.SongListEditFragmentViewPagerAdapter;

public class MainActivityViewModel extends ViewModel {

    private SongListEntity songListEntity;
    private SongListEditFragmentViewPagerAdapter songListEditFragmentViewPagerAdapter;
    private ViewPager2 viewPager2;
    private MediaService.MyBinder myBinder;

    public ViewPager2 getViewPager2() {
        return viewPager2;
    }

    public void setViewPager2(ViewPager2 viewPager2) {
        this.viewPager2 = viewPager2;
    }



    public MediaService.MyBinder getMyBinder() {
        return myBinder;
    }

    public void setMyBinder(MediaService.MyBinder myBinder) {
        this.myBinder = myBinder;
    }

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
