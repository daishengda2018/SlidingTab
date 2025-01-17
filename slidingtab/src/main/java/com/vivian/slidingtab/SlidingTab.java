package com.vivian.slidingtab;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * *          _       _
 * *   __   _(_)_   _(_) __ _ _ __
 * *   \ \ / / \ \ / / |/ _` | '_ \
 * *    \ V /| |\ V /| | (_| | | | |
 * *     \_/ |_| \_/ |_|\__,_|_| |_|
 * <p>
 * Created by vivian on 2019-12-05.
 */
public class SlidingTab extends View {
    public static final float DEFAULT_TEXT_SIZE = 16f;
    public static final int DEFAULT_RADIUS = 200;
    public static final int DEFAULT_TAB_HEIGHT = 100;
    public static final int DEFAULT_STROKE_WIDTH = 2;

    private Paint mPaint;
    private Paint mTextPaint;
    private RectF mRect;
    private RectF mTabRect;
    /**
     * dp
     */
    private float mTextSize = DEFAULT_TEXT_SIZE;
    private int mRadius = DEFAULT_RADIUS;
    private int mTabHeight = DEFAULT_TAB_HEIGHT;
    private int width;
    private int mHeight;
    private int mTabWidth;
    private float mDistance;
    private float mBaseline;
    /**
     * px
     */
    private float mStrokeWidth = DEFAULT_STROKE_WIDTH;
    /**
     * default tab's count is 2
     */
    private int mTabCount = 2;
    private int mMainColor = Color.BLUE;
    private int mMainColorRes;
    private int mBgColor = Color.WHITE;
    private int mCurrentPosition = 0;
    private int mStartColor;
    private int mEndColor;
    private int mCurrentLeft = 0;
    private float mCurrentOffset = 0;
    private int mScrollPosition = 0;
    private List<String> mTitles = new ArrayList<>();
    private LinearGradient mLinearGradient;

    public SlidingTab(Context context) {
        super(context);
        initView(context);
    }

    public SlidingTab(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        dealAttrs(context, attrs);
        initView(context);
    }

    public SlidingTab(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        dealAttrs(context, attrs);
        initView(context);
    }

    public SlidingTab(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        dealAttrs(context, attrs);
        initView(context);
    }

    public void dealAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingTab);
        mTextSize = ta.getDimensionPixelSize(R.styleable.SlidingTab_textSize, (int) DEFAULT_TEXT_SIZE);
        mRadius = ta.getDimensionPixelSize(R.styleable.SlidingTab_radius, DEFAULT_RADIUS);
        mMainColor = ta.getColor(R.styleable.SlidingTab_mainColor, Color.BLUE);
        mMainColorRes = ta.getResourceId(R.styleable.SlidingTab_mainColorRes, 0);
        if (mMainColorRes != 0) {
            mMainColor = ContextCompat.getColor(context, mMainColorRes);
        }
        mStartColor = ta.getColor(R.styleable.SlidingTab_startColor, 0);
        mEndColor = ta.getColor(R.styleable.SlidingTab_endColor, 0);
        mTabHeight = ta.getDimensionPixelSize(R.styleable.SlidingTab_tabHeight, DEFAULT_TAB_HEIGHT);
        mStrokeWidth = ta.getDimensionPixelSize(R.styleable.SlidingTab_strokeWidth, DEFAULT_TAB_HEIGHT);
        ta.recycle();
    }

    public void initView(Context context) {
        setBackgroundColor(Color.WHITE);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(false);
        mPaint.setColor(mMainColor);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));

        mTextPaint = new Paint();
        mTextPaint.setColor(mMainColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        //计算baseline
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mDistance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        mTextPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));

        mRect = new RectF();
        mTabRect = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = resolveSize(150, widthMeasureSpec);
        int height = resolveSize(DEFAULT_TAB_HEIGHT, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        mHeight = h;

        mTabHeight = (int) (mHeight - 2 * mStrokeWidth);

        if (mTitles.size() == 0) {
            mTabWidth = 100;
        } else {
            mTabCount = mTitles.size();
            mTabWidth = (int) (width / mTabCount - mStrokeWidth * 2);
        }

        mRect.set(mStrokeWidth, mStrokeWidth, width - mStrokeWidth, mHeight - mStrokeWidth);

        //渐变色
        if (mStartColor != 0 && mEndColor != 0) {
            mLinearGradient = new LinearGradient(0, 0, width, mHeight, mStartColor, mEndColor, Shader.TileMode.CLAMP);
            mPaint.setShader(mLinearGradient);
            mTextPaint.setShader(mLinearGradient);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        int layerId = canvas.saveLayer(mRect, null);
        canvas.drawColor(mBgColor, PorterDuff.Mode.CLEAR);

        //画tab
        mPaint.setStrokeWidth(mStrokeWidth + 1);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (mScrollPosition == 0) {
            mCurrentLeft = mScrollPosition * mTabWidth + (int) (mCurrentOffset * mTabWidth);
        }
        mCurrentLeft = mScrollPosition * mTabWidth + (int) (mCurrentOffset * mTabWidth);
        mTabRect.set(mCurrentLeft + mStrokeWidth, mStrokeWidth, mCurrentLeft + mTabWidth + 2 * mStrokeWidth, mTabHeight + mStrokeWidth);
        canvas.drawRoundRect(mTabRect, mRadius, mRadius, mPaint);
        canvas.save();

        //画文字
        if (mTitles.size() > 0) {
            for (int i = 0; i < mTitles.size(); i++) {
                if (i == mCurrentPosition) {
                    //画当前文字
                    mTextPaint.setColor(mMainColor);
                    mBaseline = mTabRect.centerY() + mDistance;
                    canvas.drawText(mTitles.get(i), i * mTabWidth + (mTabWidth) / 2, mBaseline, mTextPaint);
                } else {
                    mTextPaint.setColor(mMainColor);
                    mBaseline = mTabRect.centerY() + mDistance;
                    canvas.drawText(mTitles.get(i), i * mTabWidth + (mTabWidth) / 2, mBaseline, mTextPaint);
                }
            }
        }

        canvas.restoreToCount(layerId);
        //底部
        mPaint.setXfermode(null);
        mPaint.setColor(mMainColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(mRect, mRadius, mRadius, mPaint);
    }

    /**
     * 更新位置
     *
     * @param newX
     */
    public void slidingTabPosition(int newX) {
        mTabRect.set(0, newX, width / mTabCount + newX, mTabHeight);
    }

    public void setCurrentPosition(int currentPosition) {
        mCurrentPosition = currentPosition;
        invalidate();
    }

    public void setScrollFromCurrentPosition(int scrollPosition, float offset) {
        mCurrentOffset = offset;
        mScrollPosition = scrollPosition;
        invalidate();
    }

    public void setTitles(List<String> titles) {
        mTitles = titles;
        invalidate();
    }

    public void setTitles(String... titles) {
        Collections.addAll(mTitles, titles);
    }

    public void bindViewPager(ViewPager viewPager) {
        if (viewPager == null) {
            return;
        }
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                setScrollFromCurrentPosition(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                setCurrentPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
