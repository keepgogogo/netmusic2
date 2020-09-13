package com.xiaoxin.netmusic2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class FileLoader {
    public static final Uri REQUEST_AUDIO_EXTERNAL_CONTENT_URI=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    public static final String AUDIO_DEFAULT_SORT_ORDER=MediaStore.Audio.Media.DEFAULT_SORT_ORDER;


    public String sortOrder;
    public Uri requestUri;
    private Cursor cursor;
    private Context context;

    public FileLoader setRequestUri(Uri uri){
        requestUri=uri;
        return this;
    }

    public FileLoader setSortOrder(String sortOrder){
        this.sortOrder=sortOrder;
        return this;
    }

    public FileLoader setContext(Context context){
        this.context=context;
        return this;
    }

    public boolean startQuery()
    {
        if(requestUri!=null&&sortOrder!=null)
        {
            cursor=context.getContentResolver().query(requestUri,
                    null,null,null,sortOrder);
            return true;
        }
        return false;
    }

    public void getStorageAccess(Activity activity)
    {
        if(ContextCompat.checkSelfPermission(context, Manifest.
                permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        if(ContextCompat.checkSelfPermission(context, android.Manifest.
                permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},2);
        }
    }

    public Cursor getCursor(){return cursor;}

}
