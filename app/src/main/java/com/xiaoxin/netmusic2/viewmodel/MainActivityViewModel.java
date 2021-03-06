package com.xiaoxin.netmusic2.viewmodel;

import android.widget.SeekBar;

import androidx.lifecycle.ViewModel;
import androidx.viewpager2.widget.ViewPager2;

import com.xiaoxin.netmusic2.MediaManager;
import com.xiaoxin.netmusic2.database.SongEntity;
import com.xiaoxin.netmusic2.database.SongListEntity;
import com.xiaoxin.netmusic2.listener.MediaPlayerListener;
import com.xiaoxin.netmusic2.viewpager2.SongListEditFragmentViewPagerAdapter;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivityViewModel extends ViewModel {

    private SongListEntity underPlayingSongList;
    private SongEntity underPlayingSongEntity;
    private SongListEditFragmentViewPagerAdapter songListEditFragmentViewPagerAdapter;
    private ViewPager2 songListEditFragmentViewPager2;
    private MediaManager mediaManager;
    private MediaManager.MediaEasyController mediaEasyController;
    private MediaPlayerListener songOfSongListFragmentSongChangeListener;
    private MediaPlayerListener songAlbumFragmentSongChangeListener;
    private MediaPlayerListener notificationMakerSongChangeListener;
    private SeekBar seekBarInTheBottomOfScreen;
    private CircleImageView imageViewForAlbumPictureIntheBottomOfScreen;

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

    public ViewPager2 getSongListEditFragmentViewPager2() {
        return songListEditFragmentViewPager2;
    }

    public void setSongListEditFragmentViewPager2(ViewPager2 songListEditFragmentViewPager2) {
        this.songListEditFragmentViewPager2 = songListEditFragmentViewPager2;
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

    public void setSongOfSongListFragmentSongChangeListener(MediaPlayerListener songChangeListener){
        songOfSongListFragmentSongChangeListener=songChangeListener;
    }

    public void setSongAlbumFragmentSongChangeListener(MediaPlayerListener songChangeListener){
        songAlbumFragmentSongChangeListener=songChangeListener;
    }

    public void setNotificationMakerSongChangeListener(MediaPlayerListener notificationMakerSongChangeListener) {
        this.notificationMakerSongChangeListener = notificationMakerSongChangeListener;
    }

    public void notifyPlayingSongChange(SongEntity oldSong, SongEntity newSong){
        if(songOfSongListFragmentSongChangeListener!=null){
            songOfSongListFragmentSongChangeListener.onChange(oldSong, newSong);
        }

        if(songAlbumFragmentSongChangeListener!=null){
            songAlbumFragmentSongChangeListener.onChange(oldSong, newSong);
        }

        if(notificationMakerSongChangeListener!=null){
            notificationMakerSongChangeListener.onChange(oldSong, newSong);
        }
    }

    public SeekBar getSeekBarInTheBottomOfScreen() {
        return seekBarInTheBottomOfScreen;
    }

    public void setSeekBarInTheBottomOfScreen(SeekBar seekBarInTheBottomOfScreen) {
        this.seekBarInTheBottomOfScreen = seekBarInTheBottomOfScreen;
    }

    public CircleImageView getImageViewForAlbumPictureInTheBottomOfScreen() {
        return imageViewForAlbumPictureIntheBottomOfScreen;
    }

    public void setImageViewForAlbumPictureIntheBottomOfScreen(CircleImageView imageViewForAlbumPictureIntheBottomOfScreen) {
        this.imageViewForAlbumPictureIntheBottomOfScreen = imageViewForAlbumPictureIntheBottomOfScreen;
    }
}
