package com.xiaoxin.netmusic2.viewpager2;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.xiaoxin.netmusic2.ui.AllSongListFragment;
import com.xiaoxin.netmusic2.ui.SongOfSongListFragment;

public class SongListEditFragmentViewPagerAdapter extends FragmentStateAdapter {

    public static final int ALL_SONG_LIST_FRAGMENT=0;
    public static final int SONG_OF_SONG_LIST_FRAGMENT=1;

    private AllSongListFragment allSongListFragment;
    private SongOfSongListFragment songOfSongListFragment;

    public SongListEditFragmentViewPagerAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position)
    {
        Fragment fragment=null;
        switch (position)
        {
            case 0:
                fragment=new AllSongListFragment();;
                break;
            case 1:
                fragment=new SongOfSongListFragment();
                break;
            default:
                break;
        }
        assert fragment!=null;
        return fragment;
    }

    @Override
    public int getItemCount(){return 2;}

}
