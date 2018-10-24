package com.whut.ein3614.customviewtest;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class HorizontalScrollView extends ViewGroup {
    private static final String TAG = "HorizontalScrollView";
    private int mChildIndex;
    private int mChildWidth;
    private int mChildrenSize;
    private MarginLayoutParams mChildLp;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    //记录上次滑动坐标(onInterceptTouchEvent)
    private int mLastXIntercept = 0;
    private int mLastYIntercept = 0;
    //记录上次滑动的坐标
    private int mLastX = 0;
    private int mLastY = 0;

    public HorizontalScrollView(Context context) {
        super(context);
        init();
    }

    public HorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (mScroller == null) {
            mScroller = new Scroller(getContext());
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                intercepted = false;
                if (!mScroller.isFinished()) {//如果上次滑动没有完成
                    mScroller.abortAnimation();//优化滑动体验
                    intercepted = true;//全交由父容器处理
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastXIntercept;
                int deltaY = y - mLastYIntercept;
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    intercepted = true;//如果是水平滑动，则父容器拦截
                } else {
                    intercepted = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;
            default:
                break;
        }
        Log.d(TAG, "intercepted: " + intercepted);
        mLastX = x;
        mLastY = y;
        mLastXIntercept = x;
        mLastYIntercept = y;
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mVelocityTracker.addMovement(event);
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                scrollBy(-deltaX, 0);//只水平滑动，注意右滑mScrollX为负，即传参为负内容向右滑动
                break;
            case MotionEvent.ACTION_UP://松手后的滑动动画处理
                int scrollX = getScrollX();//注意这里拿到的mScrollX（scrollX），内容向右偏移的时候，mScrollX值为负
                mVelocityTracker.computeCurrentVelocity(1000);
                float xVelocity = mVelocityTracker.getXVelocity();
                if (Math.abs(xVelocity) >= 50) {//快滑
                    mChildIndex = xVelocity > 0 ? mChildIndex - 1 : mChildIndex + 1;
                } else {//慢滑
                    mChildIndex = (scrollX + mChildWidth/2)/mChildWidth;
                }
                mChildIndex = Math.max(0,Math.min(mChildIndex,mChildrenSize-1));//不超边界
                int dx = mChildIndex*(mChildWidth + mChildLp.leftMargin + mChildLp.rightMargin) - scrollX ;
                smoothScrollBy(dx,0);//注意这里的滑动：传参为负时，内容向右滑动
                mVelocityTracker.clear();
                break;
            default:
                break;
        }
        mLastX = x;
        mLastY = y;
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int childCount = getChildCount();
        int measureWidth = 0;
        int measureHeight = 0;
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();
        ViewGroup.LayoutParams lp = getLayoutParams();
        final View childView = getChildAt(0);
        MarginLayoutParams clp = (MarginLayoutParams) childView.getLayoutParams();
        mChildLp = clp;

        measureChildren(MeasureSpec.makeMeasureSpec(widthSpecSize - clp.leftMargin - clp.rightMargin, widthSpecMode),
                MeasureSpec.makeMeasureSpec(heightSpecSize - clp.topMargin - clp.bottomMargin, heightSpecMode));

        if (childCount == 0) {
            if (lp.width >= 0) {
                measureWidth = lp.width;
            } else if (lp.width == LayoutParams.MATCH_PARENT) {
                measureWidth = widthSpecSize;
            } else if (lp.width == LayoutParams.WRAP_CONTENT) {
                measureWidth = 0;
            }
            if (lp.height >= 0) {
                measureHeight = lp.height;
            } else if (lp.height == LayoutParams.MATCH_PARENT) {
                measureHeight = heightSpecSize;
            } else if (lp.height == LayoutParams.WRAP_CONTENT) {
                measureHeight = 0;
            }
            setMeasuredDimension(measureWidth, measureHeight);
        } else if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            measureWidth = (childView.getMeasuredWidth() + clp.leftMargin + clp.rightMargin) * childCount + paddingLeft + paddingRight;
            measureHeight = childView.getMeasuredHeight() + clp.topMargin + clp.bottomMargin + paddingTop + paddingBottom;
            setMeasuredDimension(measureWidth, measureHeight);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            measureWidth = (childView.getMeasuredWidth() + clp.leftMargin + clp.rightMargin) * childCount + paddingLeft + paddingRight;
            setMeasuredDimension(measureWidth, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            measureHeight = childView.getMeasuredHeight() + clp.topMargin + clp.bottomMargin + paddingTop + paddingBottom;
            setMeasuredDimension(widthSpecSize, measureHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;
        final int childCount = getChildCount();
        mChildrenSize = childCount;

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        childLeft += paddingLeft;
        for (int i = 0; i < childCount; i++) {
            final View childView = getChildAt(i);
            if (childView.getVisibility() != View.GONE) {
                MarginLayoutParams clp = (MarginLayoutParams) childView.getLayoutParams();
                childLeft += clp.leftMargin;
                final int childWidth = childView.getMeasuredWidth();
                mChildWidth = childWidth;
                final int childHeight = childView.getMeasuredHeight();
                childView.layout(childLeft, paddingTop + clp.topMargin, childLeft + childWidth, paddingTop + clp.topMargin + childHeight);
                childLeft += childWidth + clp.rightMargin;
            }
        }
    }

    // 继承自margin，支持子视图android:layout_margin属性
    public static class LayoutParams extends MarginLayoutParams {


        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }


        public LayoutParams(int width, int height) {
            super(width, height);
        }


        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }


        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    private void smoothScrollBy(int dx,int dy){
        mScroller.startScroll(getScrollX(),0,dx,0,500);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mVelocityTracker.recycle();
        super.onDetachedFromWindow();
    }
}
