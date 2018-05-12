package com.example.xukefeng.musicplayer.LrcDealClass;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.midi.MidiOutputPort;
import android.os.Build;
import android.os.Bundle;
import android.sax.RootElement;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.xukefeng.musicplayer.MusicSetting;
import com.example.xukefeng.musicplayer.R;

import java.io.Serializable;

/**
 * Created by initializing on 2018/4/25.
 */

/*
addRule 表示动态设置属性。LayoutParams.addRule()方法
有两钟重载模式
addRule(int verb)
比如：addRule（RelativeLayout.CENTER_VERTICAL）表示在ReativeLayout中相应节点布局为  垂直居中
但是所设置的节点不能和其它节点关联。
addRule(int verb , int anchor)
比如：addRule(RelativeLayout.ALGN_LEFT,R.id.data)表示该节点布局在data节点的左边
其中第一个参数属性值为：
RelativeLayout.ABOVE
RelativeLayout.BELOW
RelativeLayout.ALIGN_LEFT
RelativeLayout.LEFT_OF
RelativeLayout.RIGHT_OF



 */
public class AudioControl extends RelativeLayout implements View.OnClickListener , SeekBar.OnSeekBarChangeListener{

    //声明Context对象，因为自定View及其子类需要指明所属Context
    private Context mContext ;
    private ImageView imagePlay ;   //播放和暂停按钮
    private ImageView imageNext ;   //下一首歌
    private ImageView imagePre ;     //上一首歌
    private ImageView imageSet ;     //音乐设置
    private TextView currentText ;   //当前时间View
    private TextView totleText ;   //歌曲总时间View
    private SeekBar seekBar ;     //歌曲播放进度
    private static final int DIP_80 = 80 ;
    private static final int DIP_10 = 10 ;
    // addRule(int verb , int id) 需要指定id
    private int layoutViewId = 0x7F24FF00 ;

    /*
    设定音乐播放相关属性
     */
    private MediaPlayer mediaPlayer ;  //获取MediaPlay对象
    private int currentTime = 0 ;     //歌曲当前时间
    private int durationTime = 0 ;   ///歌曲 总时间
    private int bufferTime =0 ;     //歌曲缓存。没有实现。因为是读取本地音乐所以暂不需要
    public  static boolean isPause = false ;  //歌曲状态判断
    public static boolean SEEK_BAR_STATE = true ; //默认不是滑动状态
    /*
    构造函数
     */
    public AudioControl(Context context)
    {
        this(context , null) ;
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public AudioControl(Context context , AttributeSet set)
    {
        super(context , set);
        //获取Context对象
        mContext = context ;
        //初始化
        init() ;

    }
    /*
    用方法来设置布局参数
     */
    private RelativeLayout.LayoutParams getParams()
    {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(DIP_80 , DIP_80)  ;
        params.setMargins(10,0,0,0);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        return params ;
    }
    /*
    完成初始化
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void init()
    {
        RelativeLayout.LayoutParams params = getParams() ;
        imagePre = new ImageView(mContext) ;
        imagePre.setLayoutParams(params);
        imagePre.setId(layoutViewId + 1);
        imagePre.setOnClickListener(this);
        imagePre.setImageResource(R.mipmap.music_pre);

        RelativeLayout.LayoutParams params4 = getParams() ;
        imagePlay = new ImageView(mContext) ;
        params4.addRule(RelativeLayout.RIGHT_OF , imagePre.getId());
        imagePlay.setLayoutParams(params4);
        imagePlay.setId(layoutViewId);
        imagePlay.setOnClickListener(this);

        RelativeLayout.LayoutParams params5 = getParams() ;
        imageNext = new ImageView(mContext) ;
        params5.addRule(RelativeLayout.RIGHT_OF , imagePlay.getId());
        imageNext.setId(layoutViewId + 2);
        imageNext.setLayoutParams(params5);
        imageNext.setOnClickListener(this);
        imageNext.setImageResource(R.mipmap.music_next);

        RelativeLayout.LayoutParams params6 = getParams() ;
        imageSet = new ImageView(mContext) ;
        params6.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        imageSet.setId(layoutViewId + 3);
        imageSet.setOnClickListener(this);
        imageSet.setLayoutParams(params6);
        imageSet.setImageResource(R.mipmap.set);


        currentText = newTextView(mContext , layoutViewId + 4) ;
        RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)currentText.getLayoutParams() ;
        params1.setMargins(40 , 0 , 30 , 0);
        params1.addRule(RelativeLayout.RIGHT_OF , imageNext.getId());
        currentText.setLayoutParams(params1);


        totleText = newTextView(mContext , layoutViewId + 5) ;
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) totleText.getLayoutParams() ;
        params2.setMargins(DIP_10,0 , DIP_10 , 0);
        params2.addRule(RelativeLayout.LEFT_OF , imageSet.getId());
        totleText.setLayoutParams(params2);

        seekBar = new SeekBar(mContext) ;
        RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT ,
                LayoutParams.WRAP_CONTENT) ;
        params3.addRule(RelativeLayout.CENTER_IN_PARENT);
        params3.addRule(RelativeLayout.RIGHT_OF , currentText.getId());
        params3.addRule(RelativeLayout.LEFT_OF , totleText.getId());
        seekBar.setLayoutParams(params3);
        seekBar.setMax(100);
        seekBar.setFocusable(true);
        seekBar.setId(layoutViewId + 6);
        seekBar.setMinimumHeight(100);
        seekBar.setThumbOffset(0);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    seekBar.setFocusable(true);
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE)
                {
                    seekBar.requestFocus() ;
                }
                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    seekBar.setFocusable(false);
                }
                return false;
            }
        }) ;

    }

    /*
    自定义TextView类
     */
    private TextView newTextView(Context context , int id )
    {
        TextView textView = new TextView(context) ;
        textView.setId(id);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP , 14);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT ,
                LayoutParams.MATCH_PARENT) ;
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        textView.setLayoutParams(params);
        return textView ;
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //除去所有的View
        removeAllViews();
        //根据播放状态来设置播放按钮图片
        if (currentTime == 0 || isPause)
        {
            imagePlay.setImageResource(R.mipmap.music_pause);
        }
        else
        {
            imagePlay.setImageResource(R.mipmap.music_play);
        }
        /*
        设置歌曲当前播放时间和总时间
         */
        try {
            currentTime = mediaPlayer.getCurrentPosition() ;
            durationTime = mediaPlayer.getDuration() ;
        }catch (Exception e)
        {
            currentTime = 0 ;
            durationTime = 0 ;
        }
        /*
        对歌曲时间进行处理。由毫秒格式转换为HH:ss 形式。timeToStr方法来完成转换
         */
        currentText.setText(timeToStr(currentTime));
        totleText.setText(timeToStr(durationTime));

        /*
        计算百分比占比
         */
        seekBar.setProgress((currentTime == 0)? 0 : currentTime * 100 / durationTime );
        addView(imagePre);
        addView(imagePlay);
        addView(imageNext);
        addView(currentText);
        addView(seekBar);
        addView(totleText);
        addView(imageSet);
        //System.out.println("ON AUDIO CONTROL ONLAYOUT");
    }

    /*
    将时间由毫秒转换为标准 分：秒 形式
     */
    private String timeToStr(int time)
    {
        String timeStr ;
        int second = time / 1000 ;
        int minute = second / 60 ;
        second = second - minute * 60 ;
        if (minute > 9)
        {
            timeStr = String.valueOf(minute) + ":" ;
        }else
        {
            timeStr = "0" + String .valueOf(minute) + ":" ;
        }
        if (second > 9)
        {
            timeStr += String.valueOf(second) ;
        }else {
            timeStr += "0" + String.valueOf(second) ;
        }

        return timeStr ;
    }
    /*
      获取主程序正在播放的MediaPlayer对象
     */
    public void setMediaPlayer(MediaPlayer mediaPlayer)
    {
        this.mediaPlayer = mediaPlayer ;
    }
   /*
    获取主程序正在播放的MediaPlayer对象的当前播放时间
    */
   public void setCurrentTime(int currentTime)
   {
       this.currentTime = currentTime ;
       /*
       重绘和重新Layout
       */
       invalidate();
       requestLayout();
   }

    /*
    重写点击事件
     */
    @Override
    public void onClick(View v) {

        /*
        对播放状态按钮的点击事件进行监听
         */
        if (v.getId() == imagePlay.getId())
        {
            /*
            如果处于播放状态，则停止播放，并且修改按钮图片，设置按钮isPasuse为true
             */
            if (mediaPlayer.isPlaying())
            {
                mediaPlayer.pause();
                imagePlay.setImageResource(R.mipmap.music_pause);
                isPause = true ;
            }else{
                /*
                停滞状态下，除了需要修改按钮图片外。还要判断该歌曲是否有播放进度，有则需要跳转到播放处
                 */
                if (currentTime == 0)
                {
                    mediaPlayer.start();
                }
                else
                {
                    mediaPlayer.start();
                    mediaPlayer.seekTo(currentTime);
                }
                isPause = false ;
                imagePlay.setImageResource(R.mipmap.music_play);
            }
            Intent intentPlay = new Intent("com.xkfeng.MUSCIPLAY_BROADCAST");
            intentPlay.putExtra("AudioControl" , "AudioControl") ;
            mContext.sendBroadcast(intentPlay);

        }
        /*
        如果点击了设置按钮
         */
        if (v.getId() == imageSet.getId())
        {
            Intent intent = new Intent() ;
            Bundle bundle = new Bundle() ;
            intent = intent.setClass(mContext, MusicSetting.class);
            mContext.startActivity(intent) ;
        }
        /*
        如果点击了上一首按钮
         */
        if (v.getId() == imagePre.getId())
        {
            /*
            发送广播交给MusicPlay来处理
             */
            Intent intent = new Intent() ;
            intent.setAction("com.xkfeng.MUSCI_SWITCH") ;
            intent.putExtra("PRE" , "PRE") ;
            intent.putExtra("NEXT" , "") ;
            mContext.sendBroadcast(intent);

        }
        /*
        如果点击了下一首按钮
         */
        if (v.getId() == imageNext.getId())
        {
            /*
            发送广播交给MusicPlay来处理
             */
            Intent intent = new Intent() ;
            intent.setAction("com.xkfeng.MUSCI_SWITCH") ;
            intent.putExtra("NEXT" , "NEXT") ;
            intent.putExtra("PRE" , "") ;
            mContext.sendBroadcast(intent);
        }

        /*
        重绘和重新Layout
         */
        invalidate();
        requestLayout();
    }

    /*
    重写SeekBar相关事件
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        /*
        对用户手动设定SeekBar进度值进行相应的跳转
         */
        if (fromUser && SEEK_BAR_STATE)
        {
            int time = seekBar.getProgress() * durationTime / 100 ;
            mediaPlayer.seekTo(time);
            /*
            重绘和重新Layout
            */
            invalidate();
            requestLayout();

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

        SEEK_BAR_STATE = false ;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        SEEK_BAR_STATE = true ;
//        this.requestFocus() ;
        /*
        拖动完成后实现跳转
         */
        onProgressChanged(seekBar , seekBar.getProgress() , true);
    }
}
