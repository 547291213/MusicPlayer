package com.example.xukefeng.musicplayer.LrcDealClass;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by initializing on 2018/5/2.
 */

public class XCRoundImageView extends android.support.v7.widget.AppCompatImageView{
    private Paint mPaintBitmap = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap mRawBitmap;
    private BitmapShader mShader;
    private Matrix mMatrix = new Matrix();

    public XCRoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //获取原生Bitmap位图
        Bitmap rawBitmap = getBitmap(getDrawable());
        if (rawBitmap != null){
            //获取图片宽和高
            int viewWidth = getWidth();
            int viewHeight = getHeight();
            //由于要变换为圆形，在变换过程中取边长相对小的为基准
            int viewMinSize = Math.min(viewWidth, viewHeight);
            float dstWidth = viewMinSize;
            float dstHeight = viewMinSize;
            //如果是第一次绘制
            if (mShader == null || !rawBitmap.equals(mRawBitmap)){
                mRawBitmap = rawBitmap;
                /*
                BitmapShader是Shader的子类，可以通过Paint.setShader（Shader shader）进行设置、
                这里我们只关注BitmapShader，构造方法：
                mBitmapShader = new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP);
                参数1：bitmap
                参数2，参数3：TileMode；
                TileMode的取值有三种：
                CLAMP 拉伸
                REPEAT 重复
                MIRROR 镜像
                如果大家给电脑屏幕设置屏保的时候，如果图片太小，可以选择重复、拉伸、镜像；
                重复：就是横向、纵向不断重复这个bitmap
                镜像：横向不断翻转重复，纵向不断翻转重复；
                拉伸：这个和电脑屏保的模式应该有些不同，这个拉伸的是图片最后的那一个像素；横向的最后一个横行像素，不断的重复，纵项的那一列像素，不断的重复；
                */
                mShader = new BitmapShader(mRawBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            }
            if (mShader != null){
                /*
                 void setLocalMatrix(Matrix localM);
                 设置shader的本地矩阵,如果localM为空将重置shader的本地矩阵。
                 */
                mMatrix.setScale(dstWidth / rawBitmap.getWidth(), dstHeight / rawBitmap.getHeight());

                mShader.setLocalMatrix(mMatrix);
            }
            mPaintBitmap.setShader(mShader);
            float radius = viewMinSize / 2.0f;
            canvas.drawCircle(radius, radius, radius, mPaintBitmap);
        } else {
            super.onDraw(canvas);
        }
    }

    private Bitmap getBitmap(Drawable drawable){
        if (drawable instanceof BitmapDrawable){
            return ((BitmapDrawable)drawable).getBitmap();
        } else if (drawable instanceof ColorDrawable){
            Rect rect = drawable.getBounds();
            int width = rect.right - rect.left;
            int height = rect.bottom - rect.top;
            int color = ((ColorDrawable)drawable).getColor();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawARGB(Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color));
            return bitmap;
        } else {
            return null;
        }
    }
}