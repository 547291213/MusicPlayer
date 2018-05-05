package com.example.xukefeng.musicplayer.PackageClass;

import java.io.Serializable;

/**
 * Created by initializing on 2018/4/26.
 * 歌词信息封装类
 */

public class LrcContent implements Serializable{
    private String lrcStr ; //歌词内容
    private int lrcTime ; //歌词时间

    public int getLrcTime() {
        return lrcTime;
    }

    public String getLrcStr() {
        return lrcStr;
    }

    public void setLrcStr(String lrcStr) {
        this.lrcStr = lrcStr;
    }

    public void setLrcTime(int lrcTime) {
        this.lrcTime = lrcTime;
    }
}
