package com.xiaoxin.netmusic2.recycler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoxin.netmusic2.R;
import com.xiaoxin.netmusic2.database.SongEntity;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder>
        implements View.OnClickListener{

    private final String TAG="SONG ADAPTER";

    private Context context;
    private List<SongEntity> dataList;
    private byte[] playImageByte;
    private byte[] pauseImageByte;
    private SongRecyclerViewModel viewModel;
    private int lastPlayPosition=-1;

    public SongRecyclerViewModel getViewModel() {
        return viewModel;
    }

    public void setViewModel(SongRecyclerViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public SongAdapter()
    {
        super();
    }

    public void setContext(Context context){
        this.context=context;
        playImageByte =getPlayImageBytes();
        pauseImageByte=getPauseImageBytes();
    }

    public byte[] getPlayImageBytes(){
        Bitmap tempBitmap=BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_play_bar_btn_play);
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


    public void setDataList(List<SongEntity> mData) {
        this.dataList = mData;
    }

    public List<SongEntity> getDataList(){
        return dataList;
    }

    @Override
    public int getItemCount(){
        if (dataList==null){
            return 0;
        }
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
        String nameOfSong=dataList.get(position).getName();
        String singerOfSong=dataList.get(position).getArtist();
        holder.textViewForNameOfSong.setText(nameOfSong==null?"":nameOfSong);
        holder.textViewForNameOfSinger.setText(singerOfSong==null?"":singerOfSong);
        holder.imageView.setImageBitmap(getPlayImageBitmap(position));
        holder.imageView.setTag(position);
        holder.checkBox.setTag(position);
        holder.checkBox.setChecked(dataList.get(position).isCheckBoxChecked());

        setAlbumCover(holder,position);
    }

    public Bitmap getPlayImageBitmap(int position){
        byte[] temp=dataList.get(position).getPlayImagePicture();
        return BitmapFactory.decodeByteArray(temp,0,temp.length-1);
    }

    public Bitmap getAlbumBitmap(SongEntity songEntity){
        byte[] temp=songEntity.getAlbumPicture();
        return BitmapFactory.decodeByteArray(temp,0,temp.length-1);
    }

    public void setAlbumCover(SongViewHolder holder,int position)
    {
        if(dataList.get(position).getAlbumPicture()!=null)
        {
            byte[] bytes=dataList.get(position).getAlbumPicture();
            Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length-1);
            holder.imageViewForAlbumCover.setImageBitmap(bitmap);
        }
    }

    /**
     * 点击事件监听
     */

    private SongRecyclerClickListener clickListener;

    public void setClickListener(SongRecyclerClickListener clickListener){this.clickListener=clickListener;}

    public interface SongRecyclerClickListener
    {
        void onClick(View view,ViewNameSongRecyclerEnum viewName,SongEntity entity);
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
                    if(Arrays.equals(dataList.get(position).getPlayImagePicture(), playImageByte))
                    {
                        dataList.get(position).setPlayImagePicture(pauseImageByte);
                        clickListener.onClick(view,ViewNameSongRecyclerEnum.IMAGE_BUTTON_PLAY,dataList.get(position));
                    }else {
                        dataList.get(position).setPlayImagePicture(playImageByte);
                        clickListener.onClick(view,ViewNameSongRecyclerEnum.IMAGE_BUTTON_STOP,dataList.get(position));
                    }
                    notifyItemChanged(position);

                    //如果上个被播放的歌曲没有点暂停直接选了另一首来播放
                    lastPlayedItemReset(position);

                    break;
                case R.id.CheckBoxInRecyclerWidget:
                    if (dataList.get(position).isCheckBoxChecked())
                    {
                        dataList.get(position).setCheckBoxChecked(false);
                        notifyItemChanged(position);
                        clickListener.onClick(view,ViewNameSongRecyclerEnum.CHECK_BOX_SET_FALSE,dataList.get(position));
                        Log.d(TAG, "onClick: checkBox set false");
                    } else{
                        dataList.get(position).setCheckBoxChecked(true);
                        notifyItemChanged(position);
                        clickListener.onClick(view,ViewNameSongRecyclerEnum.CHECK_BOX_SET_TRUE,dataList.get(position));
                        Log.d(TAG, "onClick: checkBox set true");
                    }
                    break;
                default:
                    break;
            }
        }
        //viewModel.getCurrentData().setValue(dataList);
    }

    public void lastPlayedItemReset(int position){
        if (lastPlayPosition!=-1&&lastPlayPosition!=position){
            dataList.get(lastPlayPosition).setPlayImagePicture(playImageByte);
            notifyItemChanged(lastPlayPosition);
        }
        lastPlayPosition=position;
    }

    public enum ViewNameSongRecyclerEnum
    {
        CHECK_BOX_SET_FALSE,
        CHECK_BOX_SET_TRUE,
        IMAGE_BUTTON_PLAY,
        IMAGE_BUTTON_STOP
    }



}
