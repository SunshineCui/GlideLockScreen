package com.gmtech.glidelockscreen;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;



/**
 * Description：下滑锁屏 滑动组件
 */
public class GlideLockPagerLayout extends LinearLayout implements ValueAnimator.AnimatorUpdateListener {
    private static final String TAG = GlideLockPagerLayout.class.getSimpleName();

    //    private Scroller mScroller;
    private GestureDetector mGestureDetector;

    private MoveCallback callback;

    public static int SLIDE_DISPLAY = -1;//关闭屏幕
    public static int SINGLE_POINT_SLIDE = 1;//单指滑动 或者 不满足双指滑动
    public static int DOUBLE_POINT_SLIDE = 2;//双指滑动
    public static int SLIDE_ABLE_DISPLAY = 3;//双指滑动 满足关闭屏幕条件

    //锁屏竖直距离阀值
    private int LOCK_DISTANCE_THRESHOLD = 800 / 3;
    // 手指按下时y轴坐标
    private int offsetY = 0;
    // 当前视图是否隐藏
    private boolean isHidden = true;  //true 锁屏隐藏；false 锁屏显示

    // 视图高度
    private int mViewHeight = 0;

    //拦截参数
    private float mLastMotionX;
    private float mLastMotionY;

    private View barView;//上部隐藏布局
    private View contentView;//主体布局

    private int barHeight;
    private int pointerCount;
    private boolean isBlack;

    public GlideLockPagerLayout(Context context) {
        this(context, null);
    }

    public GlideLockPagerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GlideLockPagerLayout(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 初始化手势检测器
        mGestureDetector = new GestureDetector(context, new GestureListenerImpl());
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        contentView = getChildAt(0);
        barView = getChildAt(getChildCount() - 1);
        measureChild(contentView, widthMeasureSpec, heightMeasureSpec);
        // 获取视图高度
        mViewHeight = contentView.getMeasuredHeight();
        LOCK_DISTANCE_THRESHOLD = mViewHeight/3;

        measureChild(barView, widthMeasureSpec, heightMeasureSpec);

        barHeight = barView.getMeasuredHeight();

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), barHeight + mViewHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        barView.layout(l, -barHeight + offsetY, r, 0 + offsetY);
        contentView.layout(l, 0, r, mViewHeight);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            // 除了ACTION_UP，其他手势交给GestureDetector
            case MotionEvent.ACTION_UP: {
                // 获取视图容器滚动的y轴距离
                float curY = event.getY();

                // 获取移动的y轴距离  往上是负数
                float deltaY = curY - mLastMotionY;
                Log.d(TAG, "onTouchEvent: deltaY" +deltaY);
                // 未超过制定距离，则隐藏
                if (deltaY < LOCK_DISTANCE_THRESHOLD) {
                    isHidden = true;
                    callback.onPointerCount(1);
                } else { // 超过指定距离，则显示
                    isHidden = false;
                    if (deltaY >= LOCK_DISTANCE_THRESHOLD && pointerCount ==2){
                        callback.onPointerCount(SLIDE_DISPLAY);
                    }else {
                        callback.onPointerCount(SINGLE_POINT_SLIDE);
                    }
                }
                if (isHidden) {
                    moveChild(0,true);
                } else {
                    moveChild(mViewHeight,true);
                }
            }
            break;
            case MotionEvent.ACTION_MOVE:
                // 获取当前滑动的y轴坐标
                float curY = event.getY();
                // 获取移动的y轴距离  往下是正数
                float deltaY = curY - mLastMotionY;
                // 视图在原来位置时向下滚动
                if (deltaY > 0) {
                    return mGestureDetector.onTouchEvent(event);
                } else {
                    return true;
                }
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d(TAG, "onTouchEvent: ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG, "onTouchEvent: ACTION_POINTER_UP");
                break;
            default:
                //其余情况交给GestureDetector手势处理
                return mGestureDetector.onTouchEvent(event);
        }

        return super.onTouchEvent(event);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = ev.getY();
                mLastMotionX = ev.getX();
                mGestureDetector.onTouchEvent(ev);
            case MotionEvent.ACTION_MOVE:
                final float x = ev.getX();
                final int xDiff = (int) Math.abs(x - mLastMotionX);

                final float y = ev.getY();
                final int yDiff = (int) Math.abs(y - mLastMotionY);
                //y轴大于x轴距离 拦截
                if (mLastMotionY < (mViewHeight / 2) && xDiff < yDiff) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d(TAG, "onInterceptTouchEvent: ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG, "onInterceptTouchEvent: ACTION_POINTER_UP");
                break;
            default:
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }


    public void setMoveListen(MoveCallback moveCallback) {
        this.callback = moveCallback;
    }

    public interface MoveCallback {
        //触控抬起
        void onTouchUp(boolean isHidden);
        //触控点
        void onPointerCount(int pointerCount);
    }

    class GestureListenerImpl implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
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
            Log.d(TAG, "onScroll: offsetY" + offsetY);
            int pointerCount = e2.getPointerCount();

            if (GlideLockPagerLayout.this.pointerCount != 2) {
                GlideLockPagerLayout.this.pointerCount = pointerCount;
            }else {
                TextView textview = (TextView) ((ViewGroup) barView).getChildAt(0);
                textview.setVisibility(VISIBLE);
                if (offsetY >= LOCK_DISTANCE_THRESHOLD){
                    textview.setText("松开双指息屏");
                }else {
                    textview.setText("双指下滑息屏");
                }
            }

            if (offsetY >= LOCK_DISTANCE_THRESHOLD && GlideLockPagerLayout.this.pointerCount == 2){
                callback.onPointerCount(SLIDE_ABLE_DISPLAY);
            }else {
                callback.onPointerCount(GlideLockPagerLayout.this.pointerCount);
            }

            moveChild(offsetY-distanceY,false);
            return false;
        }

        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    public void moveChild(float v,boolean isAnimator) {
        float lastOffsetY = offsetY;

        if (barView == null) return;
        if (isAnimator){
            float a = v - lastOffsetY;
            ValueAnimator animator = ValueAnimator.ofFloat(a<0?lastOffsetY:(float) (lastOffsetY+0.5),v);
            long duration = (long) Math.min(Math.abs(a) * 2,500);
            animator.setDuration(duration);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.addUpdateListener(this);
            animator.start();
        }else {
            offsetY = (int) (v + 0.5);
            //最小0
            offsetY = Math.max(0,offsetY);
            //最大mViewHeight
            offsetY = Math.min(offsetY,mViewHeight);
            barView.requestLayout();
            invalidate();
        }

    }


    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float value = (float) animation.getAnimatedValue();
        offsetY = (int) (value+0.5);

        if (value >= mViewHeight || value <= 0){
            if (value >= mViewHeight && pointerCount==2){
                //息屏
                // TODO: 2020/11/12
            }
            TextView textview = (TextView) ((ViewGroup) barView).getChildAt(0);
            textview.setText("双指下滑息屏");
            textview.setVisibility(GONE);
            isBlack = false;
            //动画结束
            callback.onTouchUp(isHidden);
            pointerCount = 0;
        }
        barView.requestLayout();
        invalidate();
        Log.d(TAG, "onAnimationUpdate: "+value);
    }


//    private void changeBackground(){
//        if (barView!=null && !isBlack){
//            isBlack = true;
//            final View textview = ((ViewGroup) barView).getChildAt(0);
//            ValueAnimator animator = ValueAnimator.ofFloat(0,1);
//            animator.setDuration(500);
//            animator.setInterpolator(new AccelerateInterpolator());
//            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    float animatedValue = (float) animation.getAnimatedValue();
//                    if (animatedValue==1){
//                        textview.setVisibility(VISIBLE);
//                    }
//                }
//            });
//            animator.start();
//        }
//    }
}
