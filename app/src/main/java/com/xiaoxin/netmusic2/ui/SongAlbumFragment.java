package com.xiaoxin.netmusic2.ui;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.xiaoxin.netmusic2.MainActivity;
import com.xiaoxin.netmusic2.MediaManager;
import com.xiaoxin.netmusic2.R;
import com.xiaoxin.netmusic2.database.SongEntity;
import com.xiaoxin.netmusic2.listener.MediaPlayerListener;
import com.xiaoxin.netmusic2.viewmodel.MainActivityViewModel;

import de.hdodenhof.circleimageview.CircleImageView;

public class SongAlbumFragment extends Fragment {

    private TextView textViewForSongName;
    private TextView textViewForArtist;
    private CircleImageView circleImageViewForAlbum;
    private ObjectAnimator albumAnimator;
    private Thread threadForAlbumRotation;
    private float currentRotation;
    private boolean isAlbumAnimatorPause;

    private MainActivity mainActivity;
    private MainActivityViewModel mainActivityViewModel;

    private MediaManager mediaManager;
    private MediaManager.MediaEasyController mediaEasyController;
    private MediaPlayerListener mediaPlayerListener;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        initMainActivityAndViewModel();
        initSongChangeListener();
        initMediaManager();
        initUI(view);
        populateUIComponents();
        initAlbumAnimator();
        startImageViewRotation();

//        initAlbumRotationThread();
    }





    public void initMainActivityAndViewModel(){
        mainActivity=(MainActivity)getActivity();
        assert mainActivity != null;
        mainActivityViewModel=mainActivity.getMainActivityViewModel();
    }

    public void initSongChangeListener(){
        mediaPlayerListener =new MediaPlayerListener() {
            @Override
            public void onChange(SongEntity oldSong, SongEntity newSong) {
                if(newSong!=null){
                    populateUIComponents(newSong);
                    initAlbumAnimator();
                    startImageViewRotation();
                }
            }
        };
        mainActivityViewModel.setSongAlbumFragmentSongChangeListener(mediaPlayerListener);
    }

    public Bitmap getBitmapFromBytes(byte[] bytes){
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length-1);
    }

    public void initMediaManager(){
        mediaManager=mainActivityViewModel.getMediaManager();
        mediaEasyController=mainActivityViewModel.getMediaEasyController();
    }

    public void initUI(View view){
        circleImageViewForAlbum=view.findViewById(R.id.AlbumPictureInSongAlbumFragment);
        textViewForSongName=view.findViewById(R.id.TextViewForSongNameInSongAlbumFragment);
        textViewForArtist=view.findViewById(R.id.TextViewForArtistInSongAlbumFragment);
    }

    public void populateUIComponents(){
        SongEntity playingSong=mediaEasyController.getUnderPlayingSongEntity();
        if(playingSong!=null){
            textViewForSongName.setText(playingSong.getName());
            textViewForArtist.setText(playingSong.getArtist());
            circleImageViewForAlbum.setImageBitmap(getBitmapFromBytes(playingSong.getAlbumPicture()));
        }
    }

    public void populateUIComponents(SongEntity entity){
        if(entity!=null){
            textViewForSongName.setText(entity.getName());
            textViewForArtist.setText(entity.getArtist());
            circleImageViewForAlbum.setImageBitmap(getBitmapFromBytes(entity.getAlbumPicture()));
        }
    }

//    public void initAlbumRotationThread(){
//        currentRotation=0;
//        threadForAlbumRotation=new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(true){
//                    if(currentRotation == 359.9f){
//                        currentRotation =0f ;
//                    }
//                    currentRotation = currentRotation + 0.1f;
//
//
//                    try {
//                        Thread.sleep(5);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//    }
//

    public void initAlbumAnimator(){
        albumAnimator=ObjectAnimator.ofFloat(circleImageViewForAlbum,"rotation",0f,360f);
        albumAnimator.setDuration(15000);
        albumAnimator.setRepeatCount(Animation.INFINITE);
        albumAnimator.setRepeatMode(ObjectAnimator.RESTART);
        albumAnimator.setInterpolator(new LinearInterpolator());
    }


    public void startImageViewRotation(){
        if(isAlbumAnimatorPause){
            albumAnimator.resume();
        }else {
            albumAnimator.start();
        }
        isAlbumAnimatorPause=false;
    }

    public void pauseImageViewRotation(){
        albumAnimator.pause();
        isAlbumAnimatorPause=true;
    }

    public SongAlbumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song_album, container, false);
    }
}
