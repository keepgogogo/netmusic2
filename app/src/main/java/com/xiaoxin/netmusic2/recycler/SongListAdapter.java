package com.xiaoxin.netmusic2.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoxin.netmusic2.R;
import com.xiaoxin.netmusic2.database.SongListEntity;

import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongListViewHolder>
        implements View.OnClickListener {
    private Context context;
    private List<SongListEntity> dataList;
    private SongListViewHolder viewHolder;

    public void setContext(Context context) {
        this.context = context;
    }

    public void setDataList(List<SongListEntity> dataList) {
        this.dataList = dataList;
    }

    public List<SongListEntity> getDataList() {
        return dataList;
    }

    @Override
    public int getItemCount(){
        if (dataList==null){
            return 0;
        }
        return dataList.size();
    }

    public class SongListViewHolder extends RecyclerView.ViewHolder{
        public TextView textViewForSongListName;
        public TextView textViewForSongListCount;
        public ImageView imageViewAndPlayAll;
        public CardView cardView;
        public SongListViewHolder(@NonNull View itemView)
        {
            super(itemView);
            cardView=(CardView)itemView.findViewById(R.id.CardViewInSongListRecycler);
            textViewForSongListName=(TextView)itemView.findViewById(R.id.TextViewSongListNameInSongListRecycler);
            textViewForSongListCount=(TextView)itemView.findViewById(R.id.TextViewForSongNumberInSongListRecycler);
            imageViewAndPlayAll=(ImageView)itemView.findViewById(R.id.ImageButtonForPlayInSongListRecycler);

            cardView.setOnClickListener(SongListAdapter.this);
            imageViewAndPlayAll.setOnClickListener(SongListAdapter.this);
            viewHolder=SongListViewHolder.this;
        }
    }

    @NonNull
    @Override
    public SongListViewHolder onCreateViewHolder(ViewGroup parent,int ViewType){
        View itemView=(View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_list_recycler_widget,parent,false);
        return new SongListAdapter.SongListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SongListViewHolder holder,int position){
        String songListCount="共有"+dataList.get(position).getCount()+"首歌";
        holder.textViewForSongListCount.setText(songListCount);
        holder.textViewForSongListName.setText(dataList.get(position).getSongList());
        holder.imageViewAndPlayAll.setTag(position);
        holder.cardView.setTag(position);
    }

    /**
     * 点击事件监听
     */

    private SongListRecyclerClickListener clickListener;

    public interface SongListRecyclerClickListener{
        void onClick(View view,SongListRecyclerEnum viewName,int position);
    }

    public void setClickListener(SongListRecyclerClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onClick(View view)
    {
        int position=(int)view.getTag();
        if(clickListener!=null){
            switch (view.getId())
            {
                case R.id.CardViewInSongListRecycler:
                    clickListener.onClick(view,SongListRecyclerEnum.OPEN_SONG_LIST,position);
                    break;
                case R.id.ImageButtonForPlayInSongListRecycler:
                    clickListener.onClick(view,SongListRecyclerEnum.PLAY_ALL_OF_THE_SONG_LIST,position);
                    break;
                default:
                    break;
            }
        }
    }

    public enum SongListRecyclerEnum{
        OPEN_SONG_LIST,
        PLAY_ALL_OF_THE_SONG_LIST
    }
}
