package com.vb.touchv;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Toast;

/**
 * Created by Vieboo on 2016/5/31.
 */
public class TouchView extends View {

    private Paint paint;
    private Bitmap bitmap;
    private Rect bitRect, destRect;
    private int width = 0, height = 0;

    public TouchView(Context context) {
        super(context);
        init();
    }

    public TouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        bitmap = ((BitmapDrawable) getResources().getDrawable(R.mipmap.captain_america)).getBitmap();
        bitRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        destRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBitmap(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(0 == width || 0 == height) {
            width = w;
            height = h;
            destRect.left = width - bitmap.getWidth();
            destRect.right = width;
            destRect.top = height - bitmap.getHeight();
            destRect.bottom = height;
        }
    }

    private void drawBitmap(Canvas canvas) {
        canvas.drawBitmap(bitmap, bitRect, destRect, paint);
    }


    private boolean isClick = false;
    private int touchX, touchY;
    private int deltaX, deltaY; //点击点到图片左边和顶部的距离

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(!destRect.contains(x, y)) {
                    //没有在图片上点击，不处理触摸消息
                    return super.onTouchEvent(event);
                }
                deltaX = x - destRect.left;
                deltaY = y - destRect.top;
                touchX = x;
                touchY = y;
                isClick = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if(isClick && (Math.abs(touchX - x) > 2 || Math.abs(touchY - y) > 2)) {
                    isClick = false;
                }
                move(x - deltaX, y - deltaY);
                break;
            case MotionEvent.ACTION_UP:
                if(Math.abs(touchX - x) > 2 || Math.abs(touchY - y) > 2) {
                    isClick = false;
                }
                if(isClick) {
                    Toast.makeText(getContext(), "点击", Toast.LENGTH_SHORT).show();
                    isClick = false;
                }
                up(x - deltaX, y - deltaY);
                break;
        }
        return true;
    }


    private void move(int x, int y) {
        if(x <= width - bitmap.getWidth() && x >= 0) {
            destRect.left = x;
            destRect.right = destRect.left + bitmap.getWidth();
        }
        if(y <= height - bitmap.getHeight() && y >= 0) {
            destRect.top = y;
            destRect.bottom = destRect.top + bitmap.getHeight();
        }
        this.invalidate();
    }

    private void up(int x, int y) {
        if(x > width - bitmap.getWidth()) x = width - bitmap.getWidth();
        if(x < 0) x=0;
        if(y > height - bitmap.getHeight()) y = height - bitmap.getHeight();
        if(y < 0) y = 0;
        int min_x = Math.min(x, width - x);
        int min_y = Math.min(y, height - y);
        //横边距小于纵边距
        if(Math.min(min_x, min_y) == min_x) {
            //y轴
            if(y > height - bitmap.getHeight()) {
                destRect.top = height - bitmap.getHeight();
                destRect.bottom = height;
            } else if(y < 0) {
                destRect.top = 0;
                destRect.bottom = bitmap.getHeight();
            } else {
                destRect.top = y;
                destRect.bottom = destRect.top + bitmap.getHeight();
            }
            //x轴
            if(min_x == x) {
                startTranslate(x, destRect.top, 0, destRect.top, 150);
//                destRect.left = 0;
//                destRect.right =  bitmap.getWidth();
            }else {
                startTranslate(x, destRect.top, width - bitmap.getWidth(), destRect.top, 150);
//                destRect.left = width - bitmap.getWidth();
//                destRect.right = width;
            }
        }
        //纵边距小于横边距
        else {
            //x轴
            if(x > width - bitmap.getWidth()) {
                destRect.left = width - bitmap.getWidth();
                destRect.right = width;
            }else if(x < 0) {
                destRect.left = 0;
                destRect.right = bitmap.getWidth();
            }else {
                destRect.left = x;
                destRect.right = destRect.left + bitmap.getWidth();
            }
            //y轴
            if(min_y == y) {
                startTranslate(destRect.left, y, destRect.left, 0, 150);
//                destRect.top = 0;
//                destRect.bottom = bitmap.getHeight();
            }else {
                startTranslate(destRect.left, y, destRect.left, height - bitmap.getHeight(), 150);
//                destRect.top = height - bitmap.getHeight();
//                destRect.bottom = height;
            }
        }

//        this.invalidate();
    }

    /**
     * 移动位图
     *
     * @param startLeft 起始左边距
     * @param startTop 起始距上边距离
     * @param toLeft 到达左边距
     * @param toTop 到达上边距
     * @param duration 时长
     */
    int mStartLeft, mStartTop, mToLeft, mToTop;
    public void startTranslate(int startLeft, int startTop, int toLeft, int toTop, long duration) {
        mStartLeft = startLeft;
        mStartTop = startTop;

        mToLeft = toLeft;
        mToTop = toTop;

        // 使用ValueAnimator创建一个过程
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                // 不断重新计算上下左右位置
                float fraction = (Float) animator.getAnimatedValue();
                int currentLeft = (int) ((mToLeft - mStartLeft) * fraction + mStartLeft);
                int currentTop = (int) ((mToTop - mStartTop) * fraction + mStartTop);
                if (destRect == null) {
                    destRect = new Rect(currentLeft, currentTop, currentLeft + bitmap.getWidth(),
                            currentTop + bitmap.getHeight());
                }
                destRect.left = currentLeft;
                destRect.right = currentLeft + bitmap.getWidth();
                destRect.top = currentTop;
                destRect.bottom = currentTop + bitmap.getHeight();
                // 重绘
                postInvalidate();
            }
        });
        valueAnimator.start();
    }
}
