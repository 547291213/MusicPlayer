package com.example.xukefeng.musicplayer.PackageClass;

import java.io.Serializable;

/**
 * Created by initializing on 2018/4/25.
   音乐信息封装类
 */

public class MusicInfo implements Serializable {
    private  int _id = - 1;       //音乐标识码
    private  int duration = -1 ;   //音乐时常
    private String artist = null ;    //音乐作者
    private String musicName= null ; //音乐名字
    private String album = null ;   //音乐文件专辑
    private String title = null ;  //音乐文件标题
    private int size  ;   //音乐文件的大小  返回byte大小
    private String data ;  //获取文件的完整路径
    private String album_id ; //实际存储为音乐专辑团片



    public String getAlbum_id()
    {
        return album_id ;
    }
    public void setAlbum_id(String album_id)
    {
        this.album_id = album_id ;
    }
    public void setData(String data)
    {
        this.data = data ;
    }
    public String getData()
    {
        return data ;
    }
    public void setSize(int size)
    {
        this.size = size ;
    }
    public int getSize()
    {
        return size ;
    }
    public void setTitle(String title)
    {
        this.title = title ;
    }
    public String getTitle()
    {
        return title ;
    }
    public String getAlbum()
    {
        return album ;
    }
    public void setAlbum(String album)
    {
        this.album = album ;
    }
    public int get_id()
    {
        return _id ;
    }
    public int getDuration()
    {
        return duration ;
    }
    public String getArtist()
    {
        return artist ;
    }
    public String getMusicName()
    {
        return musicName ;
    }
    public void set_id(int _id)
    {
        this._id = _id ;
    }
    public void setDuration(int duration)
    {
        this.duration = duration ;
    }
    public void setArtist(String artist)
    {
        this.artist = artist ;
    }
    public void setMusicName(String musicName)
    {
        this.musicName = musicName ;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
