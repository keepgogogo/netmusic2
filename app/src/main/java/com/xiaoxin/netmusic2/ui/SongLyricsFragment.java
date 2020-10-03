package com.xiaoxin.netmusic2.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.xiaoxin.netmusic2.MainActivity;
import com.xiaoxin.netmusic2.R;
import com.xiaoxin.netmusic2.viewmodel.MainActivityViewModel;

public class SongLyricsFragment extends Fragment {

    private MainActivity mainActivity;
    private MainActivityViewModel mainActivityViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @NonNull Bundle savedInstanceState)
    {
        initMainActivityAndViewModel();

    }

    public void initMainActivityAndViewModel() {
        mainActivity=(MainActivity)getActivity();
        if(mainActivity!=null){
            mainActivityViewModel=mainActivity.getMainActivityViewModel();
        }
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
