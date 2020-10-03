package com.xiaoxin.netmusic2.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.xiaoxin.netmusic2.MainActivity;
import com.xiaoxin.netmusic2.R;
import com.xiaoxin.netmusic2.database.SongEntity;
import com.xiaoxin.netmusic2.viewmodel.MainActivityViewModel;

import me.wcy.lrcview.LrcView;

public class SongLyricsFragment extends Fragment {

    private MainActivity mainActivity;
    private MainActivityViewModel mainActivityViewModel;

    private TextView textViewForSongName;
    private TextView textViewForArtist;
    private LrcView lrcView;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        initMainActivityAndViewModel();
        initUI(view);
        initLrcView();
    }

    public void initMainActivityAndViewModel() {
        mainActivity=(MainActivity)getActivity();
        if(mainActivity!=null){
            mainActivityViewModel=mainActivity.getMainActivityViewModel();
        }
    }

    public void initUI(View view){
        textViewForArtist=view.findViewById(R.id.TextViewForArtistInSongLyricsFragment);
        textViewForSongName=view.findViewById(R.id.TextViewForSongNameInSongLyricsFragment);
        lrcView=view.findViewById(R.id.LrcViewInSongLyricsFragment);
    }

    public void initLrcView(){
        SongEntity underPlayingSong=mainActivityViewModel
                .getMediaEasyController().getUnderPlayingSongEntity();




    }


    public SongLyricsFragment() {
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
        return inflater.inflate(R.layout.fragment_song_lyrics, container, false);
    }
}
