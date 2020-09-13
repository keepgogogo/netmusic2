package com.xiaoxin.netmusic2.recycler;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoxin.netmusic2.R;
import com.xiaoxin.netmusic2.database.SongEntity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder>
        implements View.OnClickListener{

    private Context context;
    private List<SongEntity> dataList;
    private SongViewHolder viewHolder;
    private Bitmap playingBitmap;
    private Bitmap pauseBitmap;

    public SongAdapter()
    {
        super();
        playingBitmap= BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_play_bar_btn_play);
        pauseBitmap=BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_play_bar_btn_pause);
    }

    public void setContext(Context context){this.context=context;}

    public void setDataList(List<SongEntity> mData) {
        this.dataList = mData;
    }

    public List<SongEntity> getDataList(){
        return dataList;
    }

    @Override
    public int getItemCount(){
        return dataList.size();
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewForNameOfSong;
        public TextView textViewForNameOfSinger;
        public CheckBox checkBox;
        public ImageView imageView;
        public ImageView imageViewForAlbumCover;
        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewForNameOfSinger=(TextView)itemView.findViewById(R.id.TextViewSingerNameInRecyclerWidget);
            textViewForNameOfSong=(TextView)itemView.findViewById(R.id.TextViewSongNameInRecyclerWidget);
            checkBox=(CheckBox)itemView.findViewById(R.id.CheckBoxInRecyclerWidget);
            imageView=(ImageView)itemView.findViewById(R.id.ImageButtonForPlayInRecyclerWidget);
            imageViewForAlbumCover=(ImageView)itemView.findViewById(R.id.ImageViewForAlbumCoverInRecyclerWidget);


            checkBox.setOnClickListener(SongAdapter.this);
            imageView.setOnClickListener(SongAdapter.this);

            viewHolder=SongViewHolder.this;
        }
    }

    @NotNull
    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int ViewType)
    {
        View itemView=(View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_recycler_widget,parent,false);
        return new SongViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(SongViewHolder holder,int position)
    {
        String nameOfSong=dataList.get(position).getSong().getName();
        String singerOfSong=dataList.get(position).getArtist();
        holder.textViewForNameOfSong.setText(nameOfSong==null?"":nameOfSong);
        holder.textViewForNameOfSinger.setText(singerOfSong==null?"":singerOfSong);
        holder.imageView.setTag(position);
        holder.checkBox.setTag(position);
        holder.checkBox.setChecked(false);

        setAlbumCover(holder,position);
    }

    public void setAlbumCover(SongViewHolder holder,int position)
    {
        if(dataList.get(position).getAlbumPicture()!=null)
        {
            holder.imageViewForAlbumCover.setImageBitmap(dataList.get(position).getAlbumPicture());
        }
    }

    /**
     * 点击事件监听
     */

    private SongRecyclerClickListener clickListener;

    public void setClickListener(SongRecyclerClickListener clickListener){this.clickListener=clickListener;}

    public interface SongRecyclerClickListener
    {
        void onClick(View view,ViewNameSongRecyclerEnum viewName,int position);
    }

    @Override
    public void onClick(View view)
    {
        int position=(int)view.getTag();
        if(clickListener!=null)
        {
            switch (view.getId())
            {
                case R.id.ImageButtonForPlayInRecyclerWidget:
                    Bitmap bitmap=((BitmapDrawable)viewHolder.imageView.getDrawable()).getBitmap();
                    if(bitmap.equals(playingBitmap))
                    {
                        viewHolder.imageView.setImageBitmap(pauseBitmap);
                        clickListener.onClick(view,ViewNameSongRecyclerEnum.IMAGE_BUTTON_PLAY,position);
                    } else {
                        viewHolder.imageView.setImageBitmap(playingBitmap);
                        clickListener.onClick(view,ViewNameSongRecyclerEnum.IMAGE_BUTTON_STOP,position);
                    }
                    break;
                case R.id.CheckBoxInRecyclerWidget:
                    if (viewHolder.checkBox.isChecked())
                    {
                        viewHolder.checkBox.setChecked(false);
                        clickListener.onClick(view,ViewNameSongRecyclerEnum.CHECK_BOX_SET_FALSE,position);
                    } else{
                        viewHolder.checkBox.setChecked(true);
                        clickListener.onClick(view,ViewNameSongRecyclerEnum.CHECK_BOX_SET_TRUE,position);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public enum ViewNameSongRecyclerEnum
    {
        CHECK_BOX_SET_FALSE,
        CHECK_BOX_SET_TRUE,
        IMAGE_BUTTON_PLAY,
        IMAGE_BUTTON_STOP
    }



}
