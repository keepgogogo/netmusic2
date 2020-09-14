package com.xiaoxin.netmusic2.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.xiaoxin.netmusic2.R;
import com.xiaoxin.netmusic2.viewpager2.SongListEditFragmentViewPagerAdapter;

public class SongListEditFragment extends Fragment {

    private ViewPager2 viewPager2;
    private SongListEditFragmentViewPagerAdapter adapter;
    private final String[] tabLabels={"歌单列表","歌单详情"};

    @Override
    public void onViewCreated(@NonNull View view, @NonNull Bundle savedInstanceState)
    {
        adapter=new SongListEditFragmentViewPagerAdapter(this);
        viewPager2=(ViewPager2)view.findViewById(R.id.ViewPagerInSongListEditFragment);
        viewPager2.setAdapter(adapter);
        TabLayout tabLayout=view.findViewById(R.id.TabLayoutInSongListEditFragment);
        new TabLayoutMediator(tabLayout,viewPager2,
                (new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText(tabLabels[position]);
                    }
                })).attach();

    }









    public SongListEditFragment() {
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
        return inflater.inflate(R.layout.fragment_song_list_edit, container, false);
    }
}
