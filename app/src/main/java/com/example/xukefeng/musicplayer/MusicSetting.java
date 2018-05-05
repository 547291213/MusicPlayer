package com.example.xukefeng.musicplayer;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.xukefeng.musicplayer.MusicService.MusicPlayerService;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by initializing on 2018/4/27.
 */

public class MusicSetting extends Activity {

    //定义播放音乐的MediaPlayer
    private MediaPlayer mediaPlayer = null ;
    //定义系统的示波器
    private Visualizer visualizer  ;
    //定义系统的均衡器
    private Equalizer equalizer ;
    //定义系统的重低音控制器
    private BassBoost bassBoost ;
    //重低音的值
    public static int progress = 0 ;
    //定义系统的预设音场控制器
    private PresetReverb presetReverb ;
    //定义布局文件
    private LinearLayout linearLayout ;
    private List<Short> reverbNames = new ArrayList<>() ;
    private List<String> reverbVals = new ArrayList<>() ;
    //权限请求码
    private final int REQUEST_CODE = 1 ;
    //服务链接标志
    private ServiceConnection serviceConnection ;
    //绑定服务Intent
    private Intent intent ;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        linearLayout = new LinearLayout(this) ;
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(linearLayout);

        //权限获取
        int check = checkSelfPermission(Manifest.permission.RECORD_AUDIO) ;
        if (check == PackageManager.PERMISSION_GRANTED)
        {
            //如果已经授权
            init();
        }
        //没有授权，则需要在授权之后再初始化
        else
        {
            ActivityCompat.requestPermissions(this , new String[] {Manifest.permission.RECORD_AUDIO} , REQUEST_CODE) ;
        }
    }


    /*
    重写权限处理方法
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
      //  super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            init();
            System.out.println("获取到音乐设置权限");
        }
        else{
            System.out.println("没有获取到音乐设置权限");
        }
    }

    /*
        初始化方法
         */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void init()
    {
        /*
        绑定服务
         */
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                System.out.println("MUSIC SETTING服务链接成功") ;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                System.out.println("MUSIC SETTING服务链接失败");
            }
        } ;
        //绑定服务
        intent = new Intent() ;
        intent.setClass(MusicSetting.this , MusicPlayerService.class) ;
        bindService(intent , serviceConnection , BIND_AUTO_CREATE) ;
        //获取静态全局MediaPlayer对象
        mediaPlayer = MusicPlayerService.mediaPlayer ;

        //初始化示波器
        setupVisualizer();
        //初始化均衡控制器
        setupEqualizer();
        //初始化重低音控制器
        setupBassBoost();
        //初始化预设音场控制器
        setupPresetReverb();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
        将设置保存到MusicPlayer上
         */
        //MusicPlayerService.mediaPlayer = mediaPlayer ;
        System.out.println("音乐设置已经保存") ;
        unbindService(serviceConnection);
    }

    /*
    设置示波器
     */
    private void setupVisualizer()
    {

        // 创建MyVisualizerView组件，用于显示波形图
        final MyVisualizerView mVisualizerView =
                new MyVisualizerView(this);
        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (240f * getResources().getDisplayMetrics().density)));
        // 将MyVisualizerView组件添加到layout容器中
        linearLayout.addView(mVisualizerView);
        // 以MediaPlayer的AudioSessionId创建Visualizer
        // 相当于设置Visualizer负责显示该MediaPlayer的音频数据
        if(visualizer != null){
            visualizer = null;
        }
        visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
        visualizer.setEnabled(false) ;
        //设置需要转换的音乐内容长度，专业的说这就是采样，该采样值一般为2的指数倍，如64,128,256,512,1024。

        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        // 为mVisualizer设置监听器
        /*
         * Visualizer.setDataCaptureListener(OnDataCaptureListener listener, int rate, boolean waveform, boolean fft
         *
         *      listener，表监听函数，匿名内部类实现该接口，该接口需要实现两个函数
                rate， 表示采样的周期，即隔多久采样一次，联系前文就是隔多久采样128个数据
                iswave，是波形信号
                isfft，是FFT信号，表示是获取波形信号还是频域信号

         */
        visualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener()
                {
                    //这个回调应该采集的是快速傅里叶变换有关的数据
                    @Override
                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] fft, int samplingRate)
                    {
                    }
                    //这个回调应该采集的是波形数据
                    @Override
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] waveform, int samplingRate)
                    {
                        // 用waveform波形数据更新mVisualizerView组件
                        mVisualizerView.updateVisualizer(waveform);
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
        visualizer.setEnabled(true);
    }

    /**
     * 初始化均衡控制器
     */
    private void setupEqualizer()
    {
        // 以MediaPlayer的AudioSessionId创建Equalizer
        // 相当于设置Equalizer负责控制该MediaPlayer
        equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
        // 启用均衡控制效果
        equalizer.setEnabled(true);
        TextView eqTitle = new TextView(this);
        eqTitle.setText("均衡器:");
        eqTitle.setTextSize(25);
        eqTitle.setPadding(5,15,5,10);

        linearLayout.addView(eqTitle);
        // 获取均衡控制器支持最小值和最大值
        final short minEQLevel = equalizer.getBandLevelRange()[0];//第一个下标为最低的限度范围
        short maxEQLevel = equalizer.getBandLevelRange()[1];  // 第二个下标为最高的限度范围
        // 获取均衡控制器支持的所有频率
        short brands = equalizer.getNumberOfBands();
        for (short i = 0; i < brands; i++)
        {
            final TextView eqTextView = new TextView(this);
            // 创建一个TextView，用于显示频率
            eqTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            eqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            // 设置该均衡控制器的频率
            eqTextView.setText((equalizer.getCenterFreq(i) / 1000)
                    +  " Hz");
            linearLayout.addView(eqTextView);
            // 创建一个水平排列组件的LinearLayout
            LinearLayout tmpLayout = new LinearLayout(this);
            tmpLayout.setOrientation(LinearLayout.HORIZONTAL);
            // 创建显示均衡控制器最小值的TextView
            TextView minDbTextView = new TextView(this);
            minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            // 显示均衡控制器的最小值
            minDbTextView.setText((minEQLevel / 100) +  " dB");
            // 创建显示均衡控制器最大值的TextView
            TextView maxDbTextView = new TextView(this);
            maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            // 显示均衡控制器的最大值
            maxDbTextView.setText((maxEQLevel / 100) +  " dB");
            LinearLayout.LayoutParams layoutParams = new
                    LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            // 定义SeekBar做为调整工具
            SeekBar bar = new SeekBar(this);
            bar.setLayoutParams(layoutParams);
            bar.setMax(maxEQLevel - minEQLevel);
            bar.setProgress(equalizer.getBandLevel(i));
            final short brand = i;
            // 为SeekBar的拖动事件设置事件监听器
            bar.setOnSeekBarChangeListener(new SeekBar
                    .OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar,
                                              int progress, boolean fromUser)
                {
                    // 设置该频率的均衡值
                    equalizer.setBandLevel(brand,
                            (short) (progress + minEQLevel));
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {
                }
            });
            // 使用水平排列组件的LinearLayout“盛装”3个组件
            tmpLayout.addView(minDbTextView);
            tmpLayout.addView(bar);
            tmpLayout.addView(maxDbTextView);
            // 将水平排列组件的LinearLayout添加到myLayout容器中
            linearLayout.addView(tmpLayout);
        }
    }

    /*
    初始化重低音控制器
     */
    private void setupBassBoost()
    {

        //根据MediaPlayer的AudioSessionId创建BassBoost
        bassBoost = new BassBoost(0 , mediaPlayer.getAudioSessionId()) ;
        //设置启动重低音的效果
        bassBoost.setEnabled(true) ;
        TextView bbTitle = new TextView(this) ;
        bbTitle.setText("重低音：");
        bbTitle.setPadding(5,10,5,10);
        bbTitle.setTextSize(25);
        linearLayout.addView(bbTitle);
        //使用SeekBar作为重低音的调整工具
        final SeekBar bar = new SeekBar(this) ;
        //设置重低音的范围为0 ---- 1000
        bar.setMax(1000);
        bar.setProgress(progress);
        System.out.println("THE PROGRESS IS " + progress ) ;

        //为SeekBar的拖动事件设置事件监听器
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //设置重低音的效果
                MusicSetting.progress = progress ;
                bassBoost.setStrength((short)MusicSetting.progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        linearLayout.addView(bar);
    }

    /*
    初始化预设音场控制器
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setupPresetReverb()
    {
        //根据MediaPlayer的AudioSessionId创建presetReverb
        presetReverb = new PresetReverb(0 , mediaPlayer.getAudioSessionId()) ;
        //设置启动预设音场控制
        presetReverb.setEnabled(true) ;

        TextView prTitle = new TextView(this) ;
        prTitle.setText("音场：");
        prTitle.setPadding(5,10,5,10);
        prTitle.setTextSize(25);
        linearLayout.addView(prTitle);
        //获取系统支持的所有中立音场
        for (short i = 0 ; i < equalizer.getNumberOfPresets() ; i++)
        {
            reverbNames.add(i) ;
            reverbVals.add(equalizer.getPresetName(i)) ;
        }

        //使用Spinner来显示和设置重力音场
        Spinner spinner = new Spinner(this) ;
        spinner.setAdapter(new ArrayAdapter<String>(this ,R.layout.sinner_item ,reverbVals));
        //为Spinner中的数据设置选中监听事件
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //设定音场
                presetReverb.setPreset(reverbNames.get(position));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        }) ;
        spinner.setScrollBarSize(20);
        //设置为当前音乐效果
        spinner.setSelection(presetReverb.getPreset());

        linearLayout.addView(spinner);

    }
    private static class MyVisualizerView extends View
    {
        // bytes数组保存了波形抽样点的值
        private byte[] bytes;
        private float[] points;
        private Paint paint = new Paint();
        private Rect rect = new Rect();
        private byte type = 0;
        public MyVisualizerView(Context context)
        {
            super(context);
            bytes = null;
            // 设置画笔的属性
            paint.setStrokeWidth(1f);
            paint.setAntiAlias(true);//抗锯齿
            paint.setColor(Color.YELLOW);//画笔颜色
            paint.setStyle(Paint.Style.FILL);
        }

        public void updateVisualizer(byte[] ftt)
        {
            bytes = ftt;
            // 通知该组件重绘自己。
            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent me)
        {
            // 当用户触碰该组件时，切换波形类型
            if(me.getAction() != MotionEvent.ACTION_DOWN)
            {
                return false;
            }
            type ++;
            if(type >= 3)
            {
                type = 0;
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);
            if (bytes == null)
            {
                return;
            }
            // 绘制白色背景
            canvas.drawColor(Color.WHITE);
            // 使用rect对象记录该组件的宽度和高度
            rect.set(0,0,getWidth(),getHeight());
            switch(type)
            {
                // -------绘制块状的波形图-------
                case 0:
                    for (int i = 0; i < bytes.length - 1; i++)
                    {
                        float left = getWidth() * i / (bytes.length - 1);
                        // 根据波形值计算该矩形的高度
                        float top = rect.height()-(byte)(bytes[i+1]+128)
                                * rect.height() / 128;
                        float right = left + 1;
                        float bottom = rect.height();
                        canvas.drawRect(left, top, right, bottom, paint);
                    }
                    break;
                // -------绘制柱状的波形图（每隔18个抽样点绘制一个矩形）-------
                case 1:
                    for (int i = 0; i < bytes.length - 1; i += 18)
                    {
                        float left = rect.width()*i/(bytes.length - 1);
                        // 根据波形值计算该矩形的高度
                        float top = rect.height()-(byte)(bytes[i+1]+128)
                                * rect.height() / 128;
                        float right = left + 6;
                        float bottom = rect.height();
                        canvas.drawRect(left, top, right, bottom, paint);
                    }
                    break;
                // -------绘制曲线波形图-------
                case 2:
                    // 如果point数组还未初始化
                    if (points == null || points.length < bytes.length * 4)
                    {
                        points = new float[bytes.length * 4];
                    }
                    for (int i = 0; i < bytes.length - 1; i++)
                    {
                        // 计算第i个点的x坐标
                        points[i * 4] = rect.width()*i/(bytes.length - 1);
                        // 根据bytes[i]的值（波形点的值）计算第i个点的y坐标
                        points[i * 4 + 1] = (rect.height() / 2)
                                + ((byte) (bytes[i] + 128)) * 128
                                / (rect.height() / 2);
                        // 计算第i+1个点的x坐标
                        points[i * 4 + 2] = rect.width() * (i + 1)
                                / (bytes.length - 1);
                        // 根据bytes[i+1]的值（波形点的值）计算第i+1个点的y坐标
                        points[i * 4 + 3] = (rect.height() / 2)
                                + ((byte) (bytes[i + 1] + 128)) * 128
                                / (rect.height() / 2);
                    }
                    // 绘制波形曲线
                    canvas.drawLines(points, paint);
                    break;
            }
        }
    }

}
