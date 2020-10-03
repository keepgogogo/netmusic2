package com.xiaoxin.netmusic2.viewpager2;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.xiaoxin.netmusic2.ui.SongAlbumFragment;
import com.xiaoxin.netmusic2.ui.SongLyricsFragment;

public class SongPlayingFragmentViewPagerAdapter extends FragmentStateAdapter {
    public static final int SONG_ALBUM_FRAGMENT=0;
    public static final int SONG_LYRICS_FRAGMENT=1;

    public SongPlayingFragmentViewPagerAdapter(Fragment fragment) {
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
                fragment=new SongAlbumFragment();
                break;
            case 1:
                fragment=new SongLyricsFragment();
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
