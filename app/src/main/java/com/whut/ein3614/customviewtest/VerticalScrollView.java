package com.whut.ein3614.customviewtest;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VerticalScrollView extends LinearLayout {

    private int mTouchSlop;
    private TextView mHeader;
    // header的高度  单位：px
    private int mOriginalHeaderHeight;
    //显示出来的高度
    private int mHeaderHeight;
    private int mStatus = STATUS_EXPANDED;
    public static final int STATUS_EXPANDED = 1;
    public static final int STATUS_COLLAPSED = 2;

    // 分别记录上次滑动的坐标
    private int mLastX = 0;
    private int mLastY = 0;

    // 分别记录上次滑动的坐标(onInterceptTouchEvent)
    private int mLastXIntercept = 0;
    private int mLastYIntercept = 0;

    private OnGiveUpTouchEventListener mGiveUpTouchEventListener;

    public VerticalScrollView(Context context) {
        super(context);
    }

    public VerticalScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalScrollView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus ) {
            mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            mHeader = findViewById(R.id.tv_title);
            mOriginalHeaderHeight = mHeader.getMeasuredHeight();
            mHeaderHeight = mOriginalHeaderHeight;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mLastXIntercept = x;
                mLastYIntercept = y;
                mLastX = x;
                mLastY = y;
                intercepted = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int deltaX = x - mLastXIntercept;
                int deltaY = y - mLastYIntercept;
                if (y <= getHeaderHeight()) {//当事件落在Header上面时，父容器不拦截该事件
                    intercepted = false;
                } else if (Math.abs(deltaY) <= Math.abs(deltaX)) {//视为水平滑动时，不拦截该事件
                    intercepted = false;
                } else if (mStatus == STATUS_EXPANDED && deltaY <= -mTouchSlop) {//Header是展开状态时并且向上滑动，拦截该事件
                    intercepted = true;
                } else if (mGiveUpTouchEventListener != null) {//当ListView滑动到顶部了并且向下滑动时，父容器拦截该事件
                    if (mGiveUpTouchEventListener.giveUpTouchEvent(ev) && deltaY >= mTouchSlop) {
                        intercepted = true;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                intercepted = false;
                mLastXIntercept = mLastYIntercept = 0;
                break;
            }
            default:
                break;
        }
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                mHeaderHeight += deltaY;
                setHeaderHeight(mHeaderHeight);
                break;
            }
            case MotionEvent.ACTION_UP: {
                // 这里做了下判断，当松开手的时候，会自动向两边滑动，具体向哪边滑，要看当前所处的位置
                int destHeight = 0;
                if (mHeaderHeight <= mOriginalHeaderHeight * 0.5) {
                    destHeight = 0;
                    mStatus = STATUS_COLLAPSED;
                } else {
                    destHeight = mOriginalHeaderHeight;
                    mStatus = STATUS_EXPANDED;
                }
                // 慢慢滑向终点
                this.smoothSetHeaderHeight(mHeaderHeight, destHeight, 500);
                break;
            }
            default:
                break;
        }
        mLastX = x;
        mLastY = y;
        return true;
    }

    public interface OnGiveUpTouchEventListener {
        boolean giveUpTouchEvent(MotionEvent event);
    }
    public void setOnGiveUpTouchEventListener(OnGiveUpTouchEventListener l) {
        mGiveUpTouchEventListener = l;
    }
    public int getHeaderHeight() {
        return mHeaderHeight;
    }
    public void setHeaderHeight(int height) {
        if (height <= 0) {//显示在屏幕上的header高度范围
            height = 0;
        } else if (height > mOriginalHeaderHeight) {
            height = mOriginalHeaderHeight;
        }

        if (height == 0) {
            mStatus = STATUS_COLLAPSED;
        } else {
            mStatus = STATUS_EXPANDED;
        }

        if (mHeader != null && mHeader.getLayoutParams() != null) {
            mHeader.getLayoutParams().height = height;
            mHeader.requestLayout();
            mHeaderHeight = height;
        }
    }
    public void setOriginalHeaderHeight(int originalHeaderHeight) {
        mOriginalHeaderHeight = originalHeaderHeight;
    }

    public void smoothSetHeaderHeight(final int from, final int to, long duration) {
        smoothSetHeaderHeight(from, to, duration, false);
    }

    public void smoothSetHeaderHeight(final int from, final int to, long duration, final boolean modifyOriginalHeaderHeight) {
        final int frameCount = (int) (duration / 1000f * 30) + 1;
        final float partation = (to - from) / (float) frameCount;
        new Thread("Thread#smoothSetHeaderHeight") {

            @Override
            public void run() {
                for (int i = 0; i < frameCount; i++) {
                    final int height;
                    if (i == frameCount - 1) {
                        height = to;
                    } else {
                        height = (int) (from + partation * i);
                    }
                    post(new Runnable() {
                        public void run() {
                            setHeaderHeight(height);
                        }
                    });
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (modifyOriginalHeaderHeight) {
                    setOriginalHeaderHeight(to);
                }
            }

        }.start();
    }

}
