package com.example.xukefeng.musicplayer.MusicService;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;


import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xukefeng.musicplayer.LrcDealClass.AudioControl;
import com.example.xukefeng.musicplayer.MainActivity;
import com.example.xukefeng.musicplayer.MusicPlay;
import com.example.xukefeng.musicplayer.PackageClass.MusicInfo;
import com.example.xukefeng.musicplayer.R;

import java.util.List;

import static android.app.PendingIntent.getActivity;
import static android.support.v4.app.NotificationCompat.PRIORITY_MIN;

/**
 * Created by initializing on 2018/5/2.
 * Service 应该实现的所有功能
 * 1 获取MuscInfoList列表，用户点击音乐的索引
 * 2 处理歌词内容，时间，
 * 3 前台服务实现
 * 4 音乐设置存储
 */

@RequiresApi(api = Build.VERSION_CODES.O)
public class MusicPlayerService extends Service {

    public static List<MusicInfo> musicInfoList ; //音乐列表
    public static int position = -1 ;  //音乐索引
    public static MediaPlayer mediaPlayer = null; //音乐播放器
    private final static int NOTIFICATION_ID = 1 ; //标识码
    private final static int NOTIFICATION_NEXT = 2 ; //下一首的请求码
    private final static int NOTIFICATION_PRE = 3 ; //上一首的请求码
    private final static int NOTIFICATION_PLAY = 4 ; //播放和暂停的请求码
    private final static int NOTIFICATION_TOUCH = 5 ;  //点击
    private final static int NOTIFICATION_DELETE = 6 ; //删除。没用
    public MusicBind musicBind = new MusicBind();
    //当前播放时间
    private int currentTime ;
    //音乐时常
    private int duration ;
    //音乐控制器
    private AudioControl audioControl ;
    /*
        判断用户点击的歌曲是否时当前正在播放的歌曲
    */
    public static Boolean MUSIC_STATE = false ;
    //自定义通知实现
    private RemoteViews remoteViews ;
    //通知构造器
    private NotificationCompat.Builder mBuilder ;
    //通知
    private Notification notification2 ;
    private MusicPlayReceiver musicPlayReceiver ;


    public class MusicBind extends Binder{
        /*
        获取Service
         */
        public MusicPlayerService getService()
        {
            return MusicPlayerService.this ;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();

//        Notification notification =new Notification.Builder(MusicPlayerService.this)
//                .setSmallIcon(R.mipmap.title)
//                .setContentTitle("New Message")
//                .setContentText("You've received new messages.")
//                .setAutoCancel(true)
//                .build();
//        NotificationManager nm;
//        nm  = (NotificationManager)getSystemService(NOTIFICATION_SERVICE) ;
//        nm.notify(NOTIFICATION_ID , notification);

        remoteViews =  new RemoteViews(getPackageName() , R.layout.service_remoteview_layout);
        // 设置点击通知结果
        Intent intent = new Intent(this, MusicPlay.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_TOUCH, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent delIntent = new Intent(this, MusicPlayerService.class);
        PendingIntent delPendingIntent = PendingIntent.getService(this, NOTIFICATION_DELETE, delIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        /*
        下一首播放
         */
        Intent intentNext = new Intent();
        intentNext.setAction("com.xkfeng.MUSCI_SWITCH") ;
        intentNext.putExtra("NEXT" , "NEXT") ;
        intentNext.putExtra("PRE" , "") ;
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_NEXT, intentNext, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.remoteViewNextBtn, nextPendingIntent);

        /*
        上一首
         */
        Intent intentPre = new Intent();
        intentPre.setAction("com.xkfeng.MUSCI_SWITCH") ;
        intentPre.putExtra("NEXT" , "") ;
        intentPre.putExtra("PRE" , "PRE") ;
        PendingIntent prePendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_PRE, intentPre, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.remoteViewPreBtn, prePendingIntent);

        /*
        播放或者暂停按钮
         */
        Intent intentPlay = new Intent("com.xkfeng.MUSCIPLAY_BROADCAST");
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_PLAY, intentPlay, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.remoteViewPlayBtn, playPendingIntent);


        mBuilder=new NotificationCompat.Builder(MusicPlayerService.this);
        //小图标  必须有
        mBuilder.setSmallIcon(R.mipmap.title);
        //标题    必须哟
        mBuilder.setContentTitle("MusicPlay");
        //内容    必须有
        mBuilder.setContentText("享受美妙的音乐吧.");
        //点击通知不会消失
        mBuilder.setAutoCancel(false) ;
       //点击通知效果
        mBuilder.setContentIntent(contentPendingIntent) ;
        //删除通知效果
        mBuilder.setDeleteIntent(delPendingIntent);
        notification2=mBuilder.build();

        //启动前台服务
        startForeground(NOTIFICATION_ID, notification2);

        System.out.println("前台服务启动成功")  ;

        /*
        注册广播
         */
        musicPlayReceiver = new MusicPlayReceiver();
        IntentFilter filter = new IntentFilter("com.xkfeng.MUSCIPLAY_BROADCAST") ;
        registerReceiver(musicPlayReceiver , filter) ;


    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    /**
     * This is the method that can be called to update the Notification
     */
    private void updateNotification(){
        remoteViews.setTextViewText(R.id.remoteViewTitle,musicInfoList.get(position).getTitle());
        remoteViews.setTextViewText(R.id.remoteViewAuthor,musicInfoList.get(position).getArtist());

        try{
            int imagePath =  Integer.parseInt(musicInfoList.get(position).getAlbum_id())  ;
            remoteViews.setImageViewResource(R.id.remoteViewImageId , imagePath);
        }catch (Exception e)
        {
            Bitmap bt = BitmapFactory.decodeFile(musicInfoList.get(position).getAlbum_id()) ;
            remoteViews.setImageViewBitmap(R.id.remoteViewImageId , bt);

        }
        //自定义View
        mBuilder.setContent(remoteViews) ;
        //更新
        notification2=mBuilder.build();
        //更新前台服务
        startForeground(NOTIFICATION_ID, notification2);
    }

    /*
    销毁数据
     */
        @Override
        public void onDestroy() {
            super.onDestroy();
            stopForeground(true);
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            //注销广播
            unregisterReceiver(musicPlayReceiver);
        }
    /*
    设置歌曲索引
     */
    public void setIndex(int indexx)
    {
        /*
        判断用户点击的歌曲是否时当前正在播放的歌曲
         */
        if (indexx != position)
        {
            MUSIC_STATE = false ;
        }
        else {
            MUSIC_STATE = true ;
        }
        position = indexx ;
    }

    /*
    设置音乐列表
     */
    public void setMusicInfoList(List<MusicInfo> musicInfoListt)
    {
        musicInfoList = musicInfoListt ;
    }
    /*
    如果点击的歌曲和当前歌曲不一致则停止之前的歌曲，并且播放新的歌曲
    如果点击的歌曲和当前歌曲一直，则不做处理重新播放处理，但需要做同步处理
     */
    public  void  setMediaPlayer(MediaPlayer mediaPlayerr)
    {
        /*
        初始为空状态
         */
        if (mediaPlayer == null)
        {
            mediaPlayer = mediaPlayerr ;
            mediaPlayer.start();
        }
        else if (mediaPlayer != null && !MUSIC_STATE)
        {
            mediaPlayer.stop();
            mediaPlayer = mediaPlayerr ;
            mediaPlayer.start();
            //  Toast.makeText(this , "音乐开始播放" , Toast.LENGTH_SHORT).show();
        }
        //如果点击的歌曲和当前歌曲一致.同步处理
        /*
        歌词同步和音乐控制进度条同步
         */
        else{

            Intent intent1 = new Intent() ;
            intent1.setAction("com.xkfeng.MUSCI_BROADCAST") ;
            intent1.putExtra("currentTime" , mediaPlayer.getCurrentPosition()) ;
            mediaPlayer.stop();
            mediaPlayer = mediaPlayerr ;
            sendBroadcast(intent1);
        }
        /*
        更新前台服务
         */
        updateNotification() ;

    }


    private class MusicPlayReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            //对音乐状态进行判断：如果处于播放状态，则停止播放，并且修改按钮图片，设置按钮isPasuse为true

            if (intent.getStringExtra("AudioControl") == null)
            {
                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.pause();
                    remoteViews.setImageViewResource(R.id.remoteViewPlayBtn , R.mipmap.music_pause);
                    AudioControl.isPause = true ;
                }else {
                /*
                停滞状态下，除了需要修改按钮图片外。还要判断该歌曲是否有播放进度，有则需要跳转到播放处
                 */
                    if (currentTime == 0) {
                        mediaPlayer.start();
                    } else {
                        mediaPlayer.start();
                        mediaPlayer.seekTo(currentTime);
                    }
                    AudioControl.isPause = false;
                    remoteViews.setImageViewResource(R.id.remoteViewPlayBtn, R.mipmap.music_play);

                }
            }
            else{
                if (AudioControl.isPause == false)
                {
                    remoteViews.setImageViewResource(R.id.remoteViewPlayBtn, R.mipmap.music_play);
                }else{
                    remoteViews.setImageViewResource(R.id.remoteViewPlayBtn , R.mipmap.music_pause);
                }
            }
            //更新前台服务
            updateNotification();
        }
    }


}
