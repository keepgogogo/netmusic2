package com.xiaoxin.netmusic2.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.xiaoxin.netmusic2.MainActivity;
import com.xiaoxin.netmusic2.R;
import com.xiaoxin.netmusic2.viewmodel.MainActivityViewModel;
import com.xiaoxin.netmusic2.viewpager2.SongPlayingFragmentViewPagerAdapter;

public class SongPlayingFragment extends Fragment {

    private MainActivity mainActivity;
    private MainActivityViewModel mainActivityViewModel;
    private ViewPager2 viewPager2;
    private SongPlayingFragmentViewPagerAdapter viewPagerAdapter;
    final private String[] tabLabels={"歌曲海报","歌词"};

    @Override
    public void onViewCreated(@NonNull View view, @NonNull Bundle savedInstanceState)
    {
        initMainActivityAndViewModel();
        initViewPager(view);
    }

    public void initMainActivityAndViewModel(){
        mainActivity=(MainActivity)getActivity();
        assert mainActivity != null;
        mainActivityViewModel=mainActivity.getMainActivityViewModel();
    }

    public void initViewPager(View view){
        viewPagerAdapter=new SongPlayingFragmentViewPagerAdapter(SongPlayingFragment.this);
        viewPager2=(ViewPager2)view.findViewById(R.id.ViewPagerInSongPlayingFragment);
        viewPager2.setAdapter(viewPagerAdapter);
        TabLayout tabLayout=view.findViewById(R.id.TabLayoutInSongPlayingFragment);
        new TabLayoutMediator(tabLayout,viewPager2,
                (new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText(tabLabels[position]);
                    }
                })).attach();
        viewPagerAdapter
                .createFragment(SongPlayingFragmentViewPagerAdapter.SONG_ALBUM_FRAGMENT);
    }









    public SongPlayingFragment() {
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
        return inflater.inflate(R.layout.fragment_song_playing, container, false);
    }
}
