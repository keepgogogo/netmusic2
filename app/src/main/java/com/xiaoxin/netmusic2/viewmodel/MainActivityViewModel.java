package com.xiaoxin.netmusic2.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.viewpager2.widget.ViewPager2;

import com.xiaoxin.netmusic2.MediaManager;
import com.xiaoxin.netmusic2.database.SongEntity;
import com.xiaoxin.netmusic2.database.SongListEntity;
import com.xiaoxin.netmusic2.listener.PlayingSongChangeListener;
import com.xiaoxin.netmusic2.viewpager2.SongListEditFragmentViewPagerAdapter;

public class MainActivityViewModel extends ViewModel {

    private SongListEntity underPlayingSongList;
    private SongEntity underPlayingSongEntity;
    private SongListEditFragmentViewPagerAdapter songListEditFragmentViewPagerAdapter;
    private ViewPager2 viewPager2;
    private MediaManager mediaManager;
    private MediaManager.MediaEasyController mediaEasyController;
    private PlayingSongChangeListener songOfSongListFragmentSongChangeListener;

    public MediaManager getMediaManager() {
        return mediaManager;
    }

    public void setMediaManager(MediaManager mediaManager) {
        this.mediaManager = mediaManager;
        mediaEasyController=mediaManager.getMediaEasyController();
    }


    public MediaManager.MediaEasyController getMediaEasyController(){
        return mediaEasyController;
    }

    public ViewPager2 getViewPager2() {
        return viewPager2;
    }

    public void setViewPager2(ViewPager2 viewPager2) {
        this.viewPager2 = viewPager2;
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
        underPlayingSongList =new SongListEntity();
    }

    public SongListEntity getUnderPlayingSongList() {
        return underPlayingSongList;
    }

    public void setUnderPlayingSongList(SongListEntity underPlayingSongList) {
        this.underPlayingSongList = underPlayingSongList;
    }

    public SongEntity getUnderPlayingSongEntity() {
        return underPlayingSongEntity;
    }

    public void setUnderPlayingSongEntity(SongEntity underPlayingSongEntity) {
        this.underPlayingSongEntity = underPlayingSongEntity;
    }

    public void setSongOfSongListFragmentSongChangeListener(PlayingSongChangeListener songChangeListener){
        songOfSongListFragmentSongChangeListener=songChangeListener;
    }

    public void notifyPlayingSongChange(SongEntity oldSong,SongEntity newSong){
        songOfSongListFragmentSongChangeListener.onChange(oldSong, newSong);
    }
}
