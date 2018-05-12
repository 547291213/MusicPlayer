package com.example.xukefeng.musicplayer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xukefeng.musicplayer.Animation.TextAnimation;
import com.example.xukefeng.musicplayer.LrcDealClass.AudioControl;
import com.example.xukefeng.musicplayer.LrcDealClass.LrcProcess;
import com.example.xukefeng.musicplayer.LrcDealClass.LrcView;
import com.example.xukefeng.musicplayer.MusicService.MusicPlayerService;
import com.example.xukefeng.musicplayer.PackageClass.LrcContent;
import com.example.xukefeng.musicplayer.PackageClass.MusicInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by initializing on 2018/4/25.
 */

public class MusicPlay extends Activity implements GestureDetector.OnGestureListener  {

    //歌词处理对象
    private LrcProcess mLrcProcess ;
    //存放歌词的列表集合
    private List<LrcContent> lrcList = new ArrayList<>() ;
    //歌词索引值，当前行
    private int index = 0 ;
    //音乐信息封装对象
    private List<MusicInfo> musicInfosList ;
    //当前音乐位置
    private int position ;
    //Bundle来获取启动Activity传递的参数
    private Bundle bundle ;
    //取得歌词View对象
    private LrcView LrcViewId ;
    //播放音乐对象
    private MediaPlayer mediaPlayer = null ;
    //当前播放时间
    public static  int currentTime ;
    //音乐时常
    private int duration ;
    //Handle对象
    private MyHandle handler ;
    private MyHandle2 handle2 ;

    //声明布局文件
    private TextView MusicName ;
    private TextView MusicArtist ;
    private ImageView MusicImage ;

    //音乐控制器
    private AudioControl audioControl ;

    //音乐控制服务
    private MusicPlayerService playerService ;
    //服务链接标志
    private ServiceConnection serviceConnection ;
    //绑定的Service
    private Intent intent ;
    //广播接收
    private MusicReceiver musicReceiver ;
    private MusicSwitcherReceiver switcherReceiver ;

    private GestureDetector detector ;

    private int temp_i = 0 ;

    //用来判断播放控制器是否被点击
    private static boolean AUDIO_STATE = false ;


    @Override
    public void takeKeyEvents(boolean get) {
        super.takeKeyEvents(get);
    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_detail_layout);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        bundle = new Bundle() ;
        bundle = getIntent().getExtras() ;
        try{
            musicInfosList = (List<MusicInfo>) bundle.getSerializable("musicinfo") ;
            position = bundle.getInt("position") ;
           /*
           用输出来检测是否成功获取参数
           */
            System.out.println("position is " + position + "" +
                    "\n MUSICINFOLIST DATA IS  " + musicInfosList.get(position).getData()) ;

        }catch (Exception e)
        {
            System.out.println("获取数据失败");
            e.printStackTrace();
        }

        detector = new GestureDetector((GestureDetector.OnGestureListener) this) ;
        /*
        初始化
         */
        init() ;
        /*
        服务链接标志
         */
        serviceConnection = new ServiceConnection() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                   MusicPlayerService.MusicBind bind = (MusicPlayerService.MusicBind)service ;
                   /*
                   将当前歌曲同步到Service中去
                    */
                   playerService = bind.getService() ;
                   playerService.setIndex(position);
                   playerService.setMusicInfoList(musicInfosList);
                   playerService.setMediaPlayer(mediaPlayer);
                   System.out.println("MUSIC PLAY 服务链接成功") ;

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                   playerService = null ;
                   System.out.println("MUSIC PLAY服务链接失败");
            }
        } ;

        /*
        绑定服务
         */
        intent = new Intent() ;
        intent.setClass(MusicPlay.this , MusicPlayerService.class) ;
        bindService( intent , serviceConnection , Service.BIND_AUTO_CREATE) ;

        /*
        广播的注册
         */
        /*
        该广播用于处理用户在音乐列表界面点击了音乐的事件
         */
        musicReceiver = new MusicReceiver() ;
        IntentFilter filter = new IntentFilter("com.xkfeng.MUSCI_BROADCAST") ;
        registerReceiver(musicReceiver , filter) ;

        /*
        该广播用于处理用户在音乐播放界面点击了 下一首/上一首的事件
         */
        switcherReceiver = new MusicSwitcherReceiver() ;
        IntentFilter filter1 = new IntentFilter("com.xkfeng.MUSCI_SWITCH") ;
        registerReceiver(switcherReceiver , filter1) ;

        /*
        该Timer用于更新：歌词和音乐控制器(audioControl)的SeekBar和当前播放事件
         */
        handler = new MyHandle() ;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x111) ;
            }
        }, 100 ,1000);

        /*
        该Timer用于实现：音乐播放界面图片旋转动画
         */
        handle2 = new MyHandle2() ;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handle2.sendEmptyMessage(0x112) ;
            }
        }, 0 ,8000);
        audioControl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){//获得焦点
                    AUDIO_STATE = true ;
                }else{//失去焦点

                    AUDIO_STATE = false ;
                }
            }
        });
        audioControl.setFocusableInTouchMode(true);
    }

    /*
    初始化
    把需要重复使用的初始化放到init方法钟。其余只调用一次的方法放在create中
     */
    private void init()
    {
        //实例化歌词处理对象
        mLrcProcess = new LrcProcess() ;
        mLrcProcess.readLrc(musicInfosList.get(position).getData()) ;
        //实例化歌词显示界面对象
        lrcList = mLrcProcess.getLrcList() ;
        LrcViewId = (LrcView)findViewById(R.id.LrcViewId) ;
        LrcViewId.setmLrcList(mLrcProcess.getLrcList());
        /*
        打开音乐文件路径
         */
        mediaPlayer = MediaPlayer.create(this , Uri.parse(musicInfosList.get(position).getData())) ;

        /*
        绑定音乐播放完成监听事件
         */
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onCompletion(MediaPlayer mp) {
                //音乐列表size
                int size = musicInfosList.size() ;
                //指向下一首歌
                position ++ ;
                //溢出归0.默认为循环播放
                if (position == size )
                {
                    position = 0 ;
                }
                /*
                初始化
                歌词索引index
                当前时间currentTime
                再调用init（）方法来打开新的音乐文件，并且为新的音乐绑定该事件，做这些处理
                同步更新到Service中
                 */
                index = 0 ;
                currentTime = 0 ;
                init();
                playerService.setIndex(position);
                playerService.setMusicInfoList(musicInfosList);
                playerService.setMediaPlayer(mediaPlayer);
            }
        });
        //歌曲时长
        duration = musicInfosList.get(position).getDuration() ;

        /*
        获取并且设置音乐的标题和歌手。并添加动画来显示
         */
        MusicArtist = (TextView) findViewById(R.id.MusicArtist) ;
        MusicName = (TextView) findViewById(R.id.MusicName) ;
        MusicName.setText(musicInfosList.get(position).getTitle());
        MusicArtist.setText(musicInfosList.get(position).getArtist());
        //动画效果
        MusicArtist.setAnimation(new TextAnimation(0 , 0 , 2000));
        MusicName.setAnimation(new TextAnimation(0 , 0 , 2000));

        /*
        设置背景图片
         */
        MusicImage = (ImageView)findViewById(R.id.MusicImage) ;
        //从音乐信息封装类中获取图片专辑id，这里专辑ID实际存储为专辑图片路径
        /*
           可能存在两钟情况：
           1，本地MP3专辑钟自带图片，那么存储的就是图片路径，这时可以用BitmapFacttory.decodeFile来获图片
           2，本地MP3专辑钟没有图片，那么会使用系统的默认图片，R.drawable.timg。实际为一个长整形数据
         */
        try{
            int imagePath =  Integer.parseInt(musicInfosList.get(position).getAlbum_id())  ;
            MusicImage.setImageResource(imagePath);
        }catch (Exception e)
        {
            Bitmap bt = BitmapFactory.decodeFile(musicInfosList.get(position).getAlbum_id()) ;
            MusicImage.setImageBitmap(bt);

        }

        /*
        初始化音乐控制器 绑定当前mediaPlayer对象并且通知重绘，更新layout来调整seekBar的progress值
         */
        audioControl = (AudioControl) findViewById(R.id.AudioControlId) ;
        audioControl.setMediaPlayer(mediaPlayer);
        audioControl.invalidate();
        audioControl.requestLayout();

    }

    /*
    让用户通过滑动屏幕来切换歌曲
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*
        将触屏事件交给手势控制处理
         */
        //当音乐控制器获取焦点时不处理  其它情况则处理
        if (event.getAction() == MotionEvent.ACTION_MOVE && AUDIO_STATE )
        {
            audioControl.requestFocus() ;

            return true ;
        }
        return detector.onTouchEvent(event);
    }

    /*

         */
    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null)
        {
            System.out.println("ONRESUME AND MEDIAPLAYER IS NOT NULL");
        }
    }

    /*
    手势控制接口
     */
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        /*
        指定满足条件的最小的滑动速度和距离
        滑动速度为 40
        滑动距离为 300
         */
        //向左划 下一首
        if (null == e1 || e2 == null)
        {
            System.out.println("MOTIONEVENT OBJECT IS NULL");
            return false ;
        }
        if (e1.getX() - e2.getX() >= 300 && Math.abs(velocityX) >= 40)
        {
            /*
            发送广播交给MusicPlay来处理
             */
            Intent intent = new Intent() ;
            intent.setAction("com.xkfeng.MUSCI_SWITCH") ;
            intent.putExtra("NEXT" , "NEXT") ;
            intent.putExtra("PRE" , "") ;
            this.sendBroadcast(intent);
        }
        //向右划 上一首
        else if (e2.getX() - e1.getX() >= 300 && Math.abs(velocityX) >= 40)
        {
            /*
            发送广播交给MusicPlay来处理
             */
            Intent intent = new Intent() ;
            intent.setAction("com.xkfeng.MUSCI_SWITCH") ;
            intent.putExtra("NEXT" , "") ;
            intent.putExtra("PRE" , "PRE") ;
            this.sendBroadcast(intent);
        }
        /*
        手势检测滚动歌词
         */
        LrcViewId.onFling(e1, e2, velocityX, velocityY) ;
        return false;
    }

    public class MyHandle extends  Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x111)
            {
                temp_i = index ;
                /*
                lrcIndex()方法：根据当前时间解析出正在播放的歌词
                 */
                LrcViewId.setIndex(lrcIndex());
                LrcViewId.invalidate();
                audioControl.setCurrentTime(mediaPlayer.getCurrentPosition());
                /*
                temp_i 和 index用于判断：在歌词切换时为歌词LrcViewId设置动画
                 */
                if (temp_i != index)
                {
                    LrcViewId.setAnimation(AnimationUtils.loadAnimation(MusicPlay.this , R.anim.text_out));
                }
            }
        }
    }

    public class MyHandle2 extends  Handler
    {
        @Override
        public void handleMessage(Message msg) {
            if ((msg.what == 0x112))
            {
                //设置图片旋转
                MusicImage.setAnimation(AnimationUtils.loadAnimation(MusicPlay.this , R.anim.image_rotate));

            }
        }
    }
    public int lrcIndex() {
        if(mediaPlayer.isPlaying()) {
            currentTime = mediaPlayer.getCurrentPosition();
            duration = mediaPlayer.getDuration();
//            System.out.println("currentTIme is " + currentTime + "\n" +
//                    "duration is " + duration);
        }
        else
        {
          //  System.out.println("MEDIAPLAYER IS NOT PLAYING") ;
        }
        if(currentTime < duration) {
            for (int i = 0; i < lrcList.size(); i++) {
                if (i < lrcList.size() - 1) {
//                    System.out.println("currentTime is " + currentTime +
//                    "\nTHE LRC TIME IS " + lrcList.get(i).getLrcTime());
                    if (currentTime < lrcList.get(i).getLrcTime() && i == 0) {
                        index = i;
                    }
                    if (currentTime > lrcList.get(i).getLrcTime()
                            && currentTime < lrcList.get(i + 1).getLrcTime()) {
                        index = i;

                    }
                }
                if (i == lrcList.size() - 1
                        && currentTime > lrcList.get(i).getLrcTime()) {
                    index = i;
                }
            }
        }
        return index;
    }

    /*
    广播接收器的作用：
    处理用户点击了之前点击的歌曲，用于同步进度
     */
    public  class MusicReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {

                System.out.println(" MUSIC 广播接收成功") ;
                int tempTime = intent.getIntExtra("currentTime" , 0) ;
                System.out.println("THE TEMPTIME IS " + tempTime) ;
                mediaPlayer.start();
                mediaPlayer.seekTo(tempTime);
                LrcViewId.setIndex(lrcIndex());
                LrcViewId.invalidate();
                audioControl.setCurrentTime(mediaPlayer.getCurrentPosition());

        }
    }
    /*
    广播接收器的作用
    用于处理用户点击 下一首和上一首按钮。 实现歌曲跳转。
     */
    public class MusicSwitcherReceiver extends BroadcastReceiver
    {
        @TargetApi(Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("MUSICSWICTH 广播接收成功");
            if (!intent.getStringExtra("NEXT").isEmpty() && intent.getStringExtra("NEXT").equals("NEXT"))
            {
                /*
                直接指向歌词末尾 但是可能出现音乐本身是暂停状态，所以不会切换。这里需要将歌曲设置为播放状态
                 */
                mediaPlayer.seekTo(mediaPlayer.getDuration());
                if (AudioControl.isPause )  //如果处于暂停状态
                {
                    //设置歌曲处于播放状态
                    AudioControl.isPause = false ;
                    mediaPlayer.start();
                    //发送广播让前台服务的播放按钮图片也随之改变
                    Intent intentPlay = new Intent("com.xkfeng.MUSCIPLAY_BROADCAST");
                    intentPlay.putExtra("AudioControl" , "AudioControl") ;
                    sendBroadcast(intentPlay);
                }

            }
            else if (intent.getStringExtra("PRE").equals("PRE"))
            {
               position-- ;
               if (position < 0)
               {
                   position = musicInfosList.size() - 1 ;
               }
                index = 0 ;
                currentTime = 0 ;
                init();

                //设置歌曲处于播放状态
                AudioControl.isPause = false ;
                playerService.setIndex(position);
                playerService.setMusicInfoList(musicInfosList);
                playerService.setMediaPlayer(mediaPlayer);
                //发送广播让前台服务的播放按钮图片也随之改变
                Intent intentPlay = new Intent("com.xkfeng.MUSCIPLAY_BROADCAST");
                intentPlay.putExtra("AudioControl" , "AudioControl") ;
                sendBroadcast(intentPlay);
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //服务解绑
        unbindService(serviceConnection);
        //解除注册
        unregisterReceiver(musicReceiver);
        unregisterReceiver(switcherReceiver);
    }
}
