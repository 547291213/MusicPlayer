package com.example.xukefeng.musicplayer.Animation;

import android.graphics.Camera;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

/**
 * Created by initializing on 2018/4/26.
 */

public class TextAnimation extends Animation {

    private float currentX ;  //指定X
    private float currentY ;  //指定Y
    //定义持续时间
    private int duration ;
    //设定Camera
    private Camera camera = new Camera() ;

    public TextAnimation(float x , float y ,int duration)
    {
        currentX = x ;
        currentY = y ;
        this.duration = duration ;
        System.out.println("THE X IS " + currentX + "\nTHE Y IS " + currentY) ;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);

        //设置持续时间
        setDuration(duration);
        //设置动画结束后保留
        setFillAfter(true);
        //设置变换速度
        setInterpolator(new AccelerateDecelerateInterpolator());
        //setInterpolator(new AccelerateInterpolator());
        //setInterpolator(new LinearInterpolator());
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        /*
        保存
         */
        camera.save();
        //根据interpolatedTime来控制X Y Z上的偏移
        camera.translate(10.0f - 10.0f * interpolatedTime ,
                30.0f  - 30.0f * interpolatedTime ,
                80.0f - 80.0f * interpolatedTime);
        //根据interpolatedTime在X轴做角度变换
        camera.rotateX(360 * interpolatedTime);
        //根据interpolatedTime在Y轴做角度变换
        camera.rotateY(360 * interpolatedTime);
        //获取Transformation参数封装的matrix对象
        camera.getMatrix(t.getMatrix());
        t.getMatrix().preTranslate(-currentX / 4 , -currentY / 4) ;
        t.getMatrix().postTranslate(currentX , currentY) ;
        /*
        如果存在保存的状态，就恢复
         */
        camera.restore();
    }

}
