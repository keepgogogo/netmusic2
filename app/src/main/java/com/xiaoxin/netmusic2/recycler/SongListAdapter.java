package com.xiaoxin.netmusic2.recycler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongListViewHolder>
        implements View.OnClickListener {
    private Context context;
    private List<SongListEntity> dataList;
    private byte[] playImageByte;
    private byte[] pauseImageByte;
    private int lastPlayPosition=-1;

    public void setContext(Context context) {
        this.context = context;
        playImageByte =getPlayImageBytes();
        pauseImageByte=getPauseImageBytes();
    }

    public byte[] getPlayImageBytes(){
        Bitmap tempBitmap= BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_play_bar_btn_play);
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        tempBitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        return outputStream.toByteArray();
    }

    public byte[] getPauseImageBytes(){
        Bitmap tempBitmap=BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_play_bar_btn_pause);
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        tempBitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        return outputStream.toByteArray();
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
        holder.imageViewAndPlayAll.setImageBitmap(getPlayImageBitmap(position));
        holder.cardView.setTag(position);
    }

    public Bitmap getPlayImageBitmap(int position){
        byte[] temp=dataList.get(position).getPlayImagePicture();
        return BitmapFactory.decodeByteArray(temp,0,temp.length-1);
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
                    if(Arrays.equals(dataList.get(position).getPlayImagePicture(), playImageByte))
                    {
                        dataList.get(position).setPlayImagePicture(pauseImageByte);
                        clickListener.onClick(view, SongListRecyclerEnum.PLAY_ALL_OF_THE_SONG_LIST,position);
                    }else {
                        dataList.get(position).setPlayImagePicture(playImageByte);
                        clickListener.onClick(view, SongListRecyclerEnum.PAUSE_ALL_OF_THE_SONG_LIST,position);
                    }
                    notifyItemChanged(position);

                    //如果上个被播放的歌曲没有点暂停直接选了另一首来播放
                    lastPlayedItemReset(position);

//                    clickListener.onClick(view,SongListRecyclerEnum.PLAY_ALL_OF_THE_SONG_LIST,position);
//                    break;
                default:
                    break;
            }
        }
    }

    public void lastPlayedItemReset(int position){
        if (lastPlayPosition!=-1&&lastPlayPosition!=position){
            dataList.get(lastPlayPosition).setPlayImagePicture(playImageByte);
            notifyItemChanged(lastPlayPosition);
        }
        lastPlayPosition=position;
    }

    public enum SongListRecyclerEnum{
        OPEN_SONG_LIST,
        PLAY_ALL_OF_THE_SONG_LIST,
        PAUSE_ALL_OF_THE_SONG_LIST
    }
}
