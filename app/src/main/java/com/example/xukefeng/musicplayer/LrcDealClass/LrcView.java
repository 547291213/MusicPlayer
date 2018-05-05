package com.example.xukefeng.musicplayer.LrcDealClass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.xukefeng.musicplayer.MusicPlay;
import com.example.xukefeng.musicplayer.PackageClass.LrcContent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by initializing on 2018/4/26.
 */

public class LrcView extends android.support.v7.widget.AppCompatTextView implements GestureDetector.OnGestureListener{

    private float width ;   //指定宽度
    private float height ;  //指定高度
    private Paint currentPaint ; //当前画笔对象
    private Paint notCurrentPaint ; //非当前画笔对象
    private static final float  textHeight = 85 ; //文本高度
    private static final float textSize = 60 ; //非当前行文本大小
    private static final float currentTextSize = 80 ; //当前行文本大小
    //装载所有的歌词内容
    private List<LrcContent> mLrcList = new ArrayList<>() ;
    private int index = 0 ; //list集合下标
    private GestureDetector detector ; //手势控制
    /**
     * 正常歌词模式
     */
    public final static int DISPLAY_MODE_NORMAL = 0;
    /**
     * 拖动歌词模式
     */
    public final static int DISPLAY_MODE_SEEK = 1;
    /*

     */
     private int mDisplayMode = 0 ;
    /**
     * 最小移动的距离，当拖动歌词时如果小于该距离不做处理
     */
    private int mMinSeekFiredOffset = 70;

    public LrcView(Context context)
    {
       this(context , null) ;
    }

    public LrcView(Context context , AttributeSet set)
    {
        super(context , set);
        //完成内容的初始化
        init();

    }

    //初始化内容
    private void init()
    {
        //可对焦点
        setFocusable(true);
        //高亮部分处理
        currentPaint = new Paint() ;
        currentPaint.setAntiAlias(true); //抗锯齿
        currentPaint.setTextAlign(Paint.Align.CENTER); //文本对齐方法为水平居中

        //非高亮部分
        notCurrentPaint = new Paint() ;
        notCurrentPaint.setAntiAlias(true);
        notCurrentPaint.setTextAlign(Paint.Align.CENTER);

        detector = new GestureDetector(this) ;
    }
    /*
    *从外部导入歌词内容
     */
    public void setmLrcList(List<LrcContent> mLrcList) {
        this.mLrcList = mLrcList;
    }
    /*
    *设置歌词索引
     */
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //根据View大小的变动动态调整  宽和高
        this.width = w ;
        this.height = h ;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null)
        {
            System.out.println("CANVAS IS NULL");
            return ;
        }
        currentPaint.setColor(Color.argb(210, 251, 248, 29));
        //notCurrentPaint.setColor(Color.argb(140, 255, 255, 255));
        notCurrentPaint.setColor(Color.GRAY);

        currentPaint.setTextSize(currentTextSize);
        currentPaint.setTypeface(Typeface.SERIF) ;

        notCurrentPaint.setTextSize(textSize);
        notCurrentPaint.setTypeface(Typeface.DEFAULT) ;

        try {
            setText("");
            //根据index画出当前歌词，高亮显示
            canvas.drawText(mLrcList.get(index).getLrcStr() , width / 2 ,  height / 2 , currentPaint);

//            if (mDisplayMode == DISPLAY_MODE_SEEK) {
//                // 上下拖动歌词的时候 画出拖动要高亮的那句歌词的时间 和 高亮的那句歌词下面的一条直线
//                // 画出高亮的那句歌词下面的一条直线
//                currentPaint.setColor(Color.GRAY);
//                //该直线的x坐标从0到屏幕宽度  y坐标为高亮歌词和下一行歌词中间
//                canvas.drawLine(10, height / 2, width - 10, height / 2 , currentPaint);
//
//                // 画出高亮的那句歌词的时间
//                currentPaint.setColor(Color.GRAY);
//                currentPaint.setTextSize(20);
//                currentPaint.setTextAlign(Paint.Align.LEFT);
//                canvas.drawText(String.valueOf(MusicPlay.currentTime), 0, height / 2, currentPaint);
//            }

            float tempY =  height / 2 ;
            //画出已经播放过的歌词
            for (int i = index - 1 ; i >=  0 ; i--)
            {
                tempY -= textHeight ;
                if (tempY <= 0)  break ;
                canvas.drawText(mLrcList.get(i).getLrcStr() , width / 2 , tempY , notCurrentPaint);
            }
            tempY = height / 2 ;
            //画出接下来将要播放的歌词
            for (int i = index + 1 ; i < mLrcList.size() ; i++)
            {
                tempY += textHeight ;
                if (tempY > height)  break;
                canvas.drawText(mLrcList.get(i).getLrcStr() , width / 2 , tempY , notCurrentPaint);
            }
        }catch (Exception e)
        {
           setText("暂无歌词");
           setTextAlignment(TEXT_ALIGNMENT_CENTER);

//           System.out.println("暂时没有歌词，请处理");
        }

    }


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
        int temp_index = 0;
        //向下划
        if (e1.getY() - e2.getY() >= 300 && Math.abs(velocityY) >= 40)
        {
//               temp_index = (int) (( e1.getY() - e2.getY()) / 85);
//               if (temp_index > 0)
//               {
//                   index += temp_index ;
//                   postInvalidate();
//               }
        }
        //向上
        else if (e2.getY() - e1.getY() >= 300 && Math.abs(velocityY) >= 40)
        {
//            temp_index = (int) (( e2.getY() - e1.getY()) / 85);
//            if (temp_index > 0)
//            {
//                index -= temp_index ;
//                if (index < 0) index = 0 ;
//                postInvalidate();
//            }
        }
        //System.out.println("LRCVIEW ON FLING");
        return false;
    }
}
