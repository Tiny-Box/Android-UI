package tinybox.lib.drawable;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import tinybox.lib.R;
import tinybox.lib.util.ColorUtil;
import tinybox.lib.util.ThemeUtil;
import tinybox.lib.util.ViewUtil;

/**
 * Created on 2015/5/22.
 */
public class RippleDrawable extends Drawable implements Animatable, View.OnTouchListener {

    // isRunning flag
    private boolean mRunning = false;

    // Mask
    private Paint mShadePaint;
    private Paint mFillPaint;
    private Mask mMask;
    private Matrix mMatrix;
    // RadiaGradient: Create a shader that draws a radical gradient
    private RadialGradient mInShader;
    private RadialGradient mOutShader;
    private int mAlpha = 255;

    // background
    private Drawable mBackgroundDrawable;
    private RectF mBackgroundBounds;
    private Path mBackground;
    private int mBackgroundAnimDuration;
    private int mBackgroundColor;
    private float mBackgroundAlphaPercent;

    // Ripple
    private PointF mRipplePoint;
    private float mRippleRadius;
    private int mRippleType;
    private int mMaxRippleRadius;
    private int mRippleAnimDuration;
    private int mRippleColor;
    private int mDelayClickType;
    private float mRippleAlphaPercent;




    // Interpolator: the rate change of an animation.
    // which allows the basic animation effects(alpha, scale, translate, rotate)to be accelerated, decelerated, repeated.
    private Interpolator mInInterpolator;
    private Interpolator mOutInterpolator;

    private long mStartTime;

    // controls flag
    private int mState = STATE_OUT;

    // this is the delay flag
    public static final int DELAY_CLICK_NONE = 0;
    public static final int DELAY_CLICK_UNTIL_RELEASE = 1;
    public static final int DELAY_CLICK_AFTER_RELEASE = 2;

    private static final int TYPE_TOUCH_MATCH_VIEW = -1;
    private static final int TYPE_TOUCH = 0;
    private static final int TYPE_WAVE = 1;

    // motion flag
    private static final int STATE_OUT = 0;
    private static final int STATE_PRESS = 1;
    private static final int STATE_HOVER = 2;
    private static final int STATE_RELEASE_ON_HOLD = 3;
    private static final int STATE_RELEASE = 4;

    // which control the rate of gradient stop and radius
    private static final float[] GRADIENT_STOPS = new float[]{0f, 0.99f, 1f};
    private static final float GRADIENT_RADIUS = 16;



    private RippleDrawable(Drawable backgroundDrawable, int backgroundAnimDuration, int backgroundColor, int rippleType, int delayClickType, int maxRippleRadius, int rippleAnimDuration, int rippleColor, Interpolator inInterpolator, Interpolator outInterpolator, int type, int topLeftCornerRadius, int topRightCornerRadius, int bottomRightCornerRadius, int bottomLeftCornerRadius, int left, int top, int right, int bottom) {
        // set Drawable, Duration and Color
        setBackgroundDrawable(backgroundDrawable);
        mBackgroundAnimDuration = backgroundAnimDuration;
        mBackgroundColor = backgroundColor;

        mRippleType = rippleType;
        setDelayClickType(delayClickType);
        mMaxRippleRadius = maxRippleRadius;
        mRippleAnimDuration = rippleAnimDuration;
        mRippleColor = rippleColor;

        // when radius is error
        if (mRippleType == TYPE_TOUCH && mMaxRippleRadius <= 0)
            mRippleType = TYPE_TOUCH_MATCH_VIEW;

        // set interpolator of in and out
        mInInterpolator = inInterpolator;
        mOutInterpolator = outInterpolator;

        // set mask
        setMask(type, topLeftCornerRadius, topRightCornerRadius, bottomRightCornerRadius, bottomLeftCornerRadius, left, top, right, bottom);

        // ANTI_ALIAS_FLAG is the flag enabling antialiasing of bit mask
        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // Paint.Style.FILL: this style will be filled ignoring all stroke related settings in the paints
        mFillPaint.setStyle(Paint.Style.FILL);

        // the same as above
        mShadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadePaint.setStyle(Paint.Style.FILL);

        // set background and bounds
        mBackground = new Path();
        mBackgroundBounds = new RectF();

        // set ripple point
        mRipplePoint = new PointF();

        // set matrix
        mMatrix = new Matrix();

        //mInShader = new RadialGradient()
        // RadialGradient(x, y, radius, colors, positions, Shader.TileMode.CLAMP)
        // x, y: the coordinate of the center of the radius.
        // radius: the radius of the gradient
        // colors: the colors to be distributed between the center and edge of the circle
        // positions: the relative position of each corresponding color in the colors array. If this is NULL, the the colors are distributed evenly between the center and edge of the circle
        // Shader.TileMode.CLAMP: replicate the edge color if the shader draws outside of its original bounds, which can create wave effects
        mInShader = new RadialGradient(0, 0, GRADIENT_RADIUS, new int[]{mRippleColor, mRippleColor, 0}, GRADIENT_STOPS, Shader.TileMode.CLAMP);
        // mOutShader is the RadialGradient which has wave effects.
        if (mRippleType == TYPE_WAVE)
            mOutShader = new RadialGradient(0, 0, GRADIENT_RADIUS, new int[]{0, ColorUtil.getColor(mRippleColor, 0f), mRippleColor}, GRADIENT_STOPS, Shader.TileMode.CLAMP);

    }

    public void setBackgroundDrawable(Drawable backgroundDrawable) {
        mBackgroundDrawable = backgroundDrawable;
        if (mBackgroundDrawable != null) {
            // setBounds: Set the width and height, eg: setBounds(top, left, width, height)
            // getBounds: Return the drawable's bounds Rect.
            mBackgroundDrawable.setBounds(getBounds());
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
    }

    public int getDelayClickType() { return mDelayClickType; }

    public void setDelayClickType(int type) {
        mDelayClickType = type;
    }

    public void setMask(int type, int topLeftCornerRadius, int topRightCornerRadius, int bottomRightCornerRadius, int bottomLeftCornerRadius, int left, int top, int right, int bottom){
        mMask = new Mask(type, topLeftCornerRadius, topRightCornerRadius, bottomRightCornerRadius, bottomLeftCornerRadius, left, top, right, bottom);
    }

    // this method will create the RippleEffect
    private boolean setRippleEffect(float x, float y, float radius){
        if(mRipplePoint.x != x || mRipplePoint.y != y || mRippleRadius != radius){
            mRipplePoint.set(x, y);
            mRippleRadius = radius;
            radius = mRippleRadius / GRADIENT_RADIUS;
            mMatrix.reset();
            // Postconcats the matrix with the specified translation. M`=T(x, y)*M
            mMatrix.postTranslate(x, y);
            // Postconcats the matrix with the specified scale. M`=S(sx, sy, x, y)*M
            mMatrix.postScale(radius, radius, x, y);
            // set the shader's local matrix
            mInShader.setLocalMatrix(mMatrix);
            if(mOutShader != null)
                mOutShader.setLocalMatrix(mMatrix);

            return true;
        }

        return false;
    }

    // thie method will set the Ripple state
    private void setRippleState(int state){
        if(mState != state){
            mState = state;

            if(mState != STATE_OUT){
                if(mState != STATE_HOVER)
                    start();
                else
                    stop();
            }
            else
                stop();
        }
    }

    private int getMaxRippleRaidus(float x, float y) {
        float x1 = x < mBackgroundBounds.centerX() ? mBackgroundBounds.right : mBackgroundBounds.left;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (mState == STATE_OUT || mState == STATE_RELEASE) {
                    if (mRippleType == TYPE_WAVE || mRippleType == TYPE_TOUCH_MATCH_VIEW)
                        mMaxRippleRadius = getMaxRippleRaidus(event.getX(), event.getY());

                    setRippleEffect(event.getX(), event.getY(), 0);
                    setRippleState(STATE_PRESS);
                }
                else if (mRippleType == TYPE_TOUCH) {
                    if (setRippleEffect(event.getX(), event.getY(), mRippleRadius))
                        invalidateSelf();
                }
                break;
        }
        return true;
    }

    public void cancel() {
        setRippleState(STATE_OUT);
    }

    private void resetAnimation() {
        mStartTime = SystemClock.uptimeMillis();
    }

    @Override
    public void start() {
        if (isRunning())
            return;

        resetAnimation();

        scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
        invalidateSelf();
    }

    @Override
    public void stop() {
        if (!isRunning())
            return;

        
        mRunning = false;

    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    @Override
    public void scheduleSelf(Runnable what, long when) {
        mRunning = true;
        super.scheduleSelf(what, when);
    }

    // Represents a command that can be executed, one kind of handle
    private final Runnable mUpdater = new Runnable() {
        @Override
        public void run() {
            switch (mRippleType) {
                case TYPE_TOUCH:
                case TYPE_TOUCH_MATCH_VIEW:
                    updateTouch();
                    break;
                case TYPE_WAVE:
                    updateWave();
                    break;
            }
        }
    };

    private void updateTouch() {
        if (mState != STATE_RELEASE) {
            // SystemClock.uptimeMillis(): Returns milliseconds since boot, not counting time spent in deep sleep.
            float backgroundProgress = Math.min(1f, (float)(SystemClock.uptimeMillis() - mStartTime) / mBackgroundAnimDuration);
            // interpolator is a speed control.
            mBackgroundAlphaPercent = mInInterpolator.getInterpolation(backgroundProgress) * Color.alpha(mBackgroundColor) * 255;

            // it's same as above
            float touchProgress = Math.min(1f, (float)(SystemClock.uptimeMillis() - mStartTime) / mRippleAnimDuration);
            mRippleAlphaPercent = mInInterpolator.getInterpolation(touchProgress);

            // then set the effect
            setRippleEffect(mRipplePoint.x, mRipplePoint.y, mMaxRippleRadius * mInInterpolator.getInterpolation(touchProgress));

            // above is the judge about whether we press the button
            // If we click on the button, the mStartTime will change, so Math.min() will return 1f
            // then the follow code will run
            if (backgroundProgress == 1f && touchProgress == 1f){
                mStartTime = SystemClock.uptimeMillis();
                setRippleState(mState == STATE_PRESS ? STATE_HOVER : STATE_RELEASE);
            }
        }
        else {
            float backgroundProgress = Math.min(1f, (float)(SystemClock.uptimeMillis() - mStartTime) / mBackgroundAnimDuration);
            mBackgroundAlphaPercent = (1f - mOutInterpolator.getInterpolation(backgroundProgress)) * Color.alpha(mBackgroundColor) / 255f;

            float touchProgress = Math.min(1f, (float)(SystemClock.uptimeMillis() - mStartTime) / mRippleAnimDuration);
            mRippleAlphaPercent = 1f - mOutInterpolator.getInterpolation(touchProgress);

            // set the ripple
            // not understand clear
            setRippleEffect(mRipplePoint.x, mRipplePoint.y, mMaxRippleRadius * (1f + 0.5f * mOutInterpolator.getInterpolation(touchProgress)));

            if(backgroundProgress == 1f && touchProgress == 1f)
                setRippleState(STATE_OUT);
        }

        if(isRunning()){
            // scheduleSelf: use the current Drawable.Callback implementation to have this Drawable.
            // scheduleSelf(what, when)
            scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
        }

        // use the current Drawable.Callback implementation to redrawn.
        invalidateSelf();
    }

    private void updateWave() {
        float progress = Math.min(1f, (float)(SystemClock.uptimeMillis() - mStartTime)/ mRippleAnimDuration);

        if (mState != STATE_RELEASE) {
            setRippleEffect(mRipplePoint.x, mRipplePoint.y, mMaxRippleRadius * mInInterpolator.getInterpolation(progress));

            if (progress == 1f) {
                mStartTime = SystemClock.uptimeMillis();
                if (mState == STATE_PRESS)
                    setRippleState(STATE_HOVER);
                else {
                    setRippleEffect(mRipplePoint.x, mRipplePoint.y, 0);
                    setRippleState(STATE_RELEASE);
                }
            }
        }
        else {
            setRippleEffect(mRipplePoint.x, mRipplePoint.y, mMaxRippleRadius * mInInterpolator.getInterpolation(progress));

            if (progress == 1f) {
                // state is release, and progress is long time. so we can judge the user have pressed the button.
                setRippleState(STATE_OUT);
            }
        }

        if (isRunning()) {
            scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
        }
        invalidateSelf();
    }

    public static class Mask {

        public static final int TYPE_RECTANGLE = 0;
        public static final int TYPE_OVAL = 1;

        final int type;

        final float[] cornerRadius = new float[8];

        final int left;
        final int right;
        final int top;
        final int bottom;

        public Mask(int type, int topLeftCornerRadius, int topRightCornerRadius, int bottomRightCornerRadius, int bottomLeftCornerRadius, int left, int top, int right, int bottom) {
            this.type = type;

            cornerRadius[0] = topLeftCornerRadius;
            cornerRadius[1] = topLeftCornerRadius;

            cornerRadius[2] = topRightCornerRadius;
            cornerRadius[3] = topRightCornerRadius;

            cornerRadius[4] = bottomRightCornerRadius;
            cornerRadius[5] = bottomRightCornerRadius;

            cornerRadius[6] = bottomLeftCornerRadius;
            cornerRadius[7] = bottomLeftCornerRadius;

            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }
    }

    public static class Builder {
        private Drawable mBackgroundDrawable;
        private int mBackgroundAnimDuration = 200;
        private int mBackgroundColor;

        private int mRippleType;
        private int mMaxRippleRadius;
        private int mRippleAnimDuration = 400;
        private int mRippleColor;
        private int mDelayClickType;

        private Interpolator mInInterpolator;
        private Interpolator mOutInterpolator;

        private int mMaskType;
        private int mMaskTopLeftCornerRadius;
        private int mMaskTopRightCornerRadius;
        private int mMaskBottomLeftCornerRadius;
        private int mMaskBottomRightCornerRadius;
        private int mMaskLeft;
        private int mMaskTop;
        private int mMaskRight;
        private int mMaskBottom;


        public Builder(){}

        public Builder (Context context, int defStyleRes) {
            this(context, null, 0, defStyleRes);
        }

        public Builder (Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RippleDrawable, defStyleAttr, defStyleRes);
            int type, resId;

            // rd_backgroundColor is in the attrs.xml
            backgroundColor(a.getColor(R.styleable.RippleDrawable_rd_backgroundColor, 0));

            // getInteger is a funny method: getInteger(int index, int defValue)
            //      index: index of attribute at index
            //      defValue: Value to return if the attribute is not defined or not a resource.
            // This method use to retrieve the integer value for the attribute at index. this design, ennnn, is funny. :)

            // R.integer.config_mediumAnimTime: The duration (in milliseconds) of a medium-length animation.
            // This is a static value
            backgroundAnimDuration(a.getInteger(R.styleable.RippleDrawable_rd_backgroundAnimDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));
            rippleType(a.getInteger(R.styleable.RippleDrawable_rd_rippleType, RippleDrawable.TYPE_TOUCH));
            delayClickType(a.getInteger(R.styleable.RippleDrawable_rd_delayClick, RippleDrawable.DELAY_CLICK_NONE));

            type = ThemeUtil.getType(a, R.styleable.RippleDrawable_rd_maxRippleRadius);
            if (type >= TypedValue.TYPE_FIRST_INT && type <= TypedValue.TYPE_LAST_INT)
                maxRippleRadius(a.getInteger(R.styleable.RippleDrawable_rd_maxRippleRadius, -1));
            else
                maxRippleRadius(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_maxRippleRadius, ThemeUtil.dpToPx(context, 48)));

            // don't know
            rippleColor(a.getColor(R.styleable.RippleDrawable_rd_rippleColor, ThemeUtil.colorControlHighlight(context, 0)));
            rippleAnimDuration(a.getInteger(R.styleable.RippleDrawable_rd_rippleAnimDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));

            // getResourceId is similar with getInteger
            if((resId = a.getResourceId(R.styleable.RippleDrawable_rd_inInterpolator, 0)) != 0)
                inInterpolator(AnimationUtils.loadInterpolator(context, resId));
            if((resId = a.getResourceId(R.styleable.RippleDrawable_rd_outInterpolator, 0)) != 0)
                outInterpolator(AnimationUtils.loadInterpolator(context, resId));

            // TYPE_RECTANGLE is a flag
            maskType(a.getInteger(R.styleable.RippleDrawable_rd_maskType, Mask.TYPE_RECTANGLE));
            // set radius
            // although I don't know why he must set top and bottom respectively
            cornerRadius(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_cornerRadius, 0));
            topLeftCornerRadius(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_topLeftCornerRadius, mMaskTopLeftCornerRadius));
            topRightCornerRadius(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_topRightCornerRadius, mMaskTopRightCornerRadius));
            bottomRightCornerRadius(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_bottomRightCornerRadius, mMaskBottomRightCornerRadius));
            bottomLeftCornerRadius(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_bottomLeftCornerRadius, mMaskBottomLeftCornerRadius));

            // It's similar with radius
            padding(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_padding, 0));
            left(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_leftPadding, mMaskLeft));
            right(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_rightPadding, mMaskRight));
            top(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_topPadding, mMaskTop));
            bottom(a.getDimensionPixelSize(R.styleable.RippleDrawable_rd_bottomPadding, mMaskBottom));

            a.recycle();


        }

        // build basic
        public RippleDrawable build(){
            if(mInInterpolator == null)
                mInInterpolator = new AccelerateInterpolator();

            if(mOutInterpolator == null)
                mOutInterpolator = new DecelerateInterpolator();

            return new RippleDrawable(mBackgroundDrawable, mBackgroundAnimDuration, mBackgroundColor, mRippleType, mDelayClickType, mMaxRippleRadius, mRippleAnimDuration, mRippleColor, mInInterpolator, mOutInterpolator, mMaskType, mMaskTopLeftCornerRadius, mMaskTopRightCornerRadius, mMaskBottomRightCornerRadius, mMaskBottomLeftCornerRadius, mMaskLeft, mMaskTop, mMaskRight, mMaskBottom);
        }

        public Builder backgroundColor (int color) {
            mBackgroundColor = color;
            return this;
        }

        public Builder backgroundAnimDuration (int duration) {
            mBackgroundAnimDuration = duration;
            return this;
        }

        public Builder rippleType (int type) {
            mRippleType = type;
            return this;
        }

        public Builder delayClickType (int type) {
            mDelayClickType = type;
            return this;
        }

        public Builder maxRippleRadius (int radius) {
            mMaxRippleRadius = radius;
            return this;
        }

        public Builder rippleColor (int color) {
            mRippleColor = color;
            return this;
        }

        public Builder rippleAnimDuration (int duration) {
            mRippleAnimDuration = duration;
            return this;
        }

        public Builder inInterpolator (Interpolator interpolator) {
            mInInterpolator = interpolator;
            return this;
        }

        public Builder outInterpolator (Interpolator interpolator) {
            mOutInterpolator = interpolator;
            return this;
        }

        public Builder maskType (int type) {
            mMaskType = type;
            return this;
        }

        public Builder cornerRadius (int radius) {
            mMaskBottomLeftCornerRadius = radius;
            mMaskBottomRightCornerRadius = radius;
            mMaskTopLeftCornerRadius = radius;
            mMaskTopRightCornerRadius = radius;
            return this;
        }

        public Builder topLeftCornerRadius(int radius){
            mMaskTopLeftCornerRadius = radius;
            return this;
        }

        public Builder topRightCornerRadius(int radius){
            mMaskTopRightCornerRadius = radius;
            return this;
        }

        public Builder bottomLeftCornerRadius(int radius){
            mMaskBottomLeftCornerRadius = radius;
            return this;
        }

        public Builder bottomRightCornerRadius(int radius){
            mMaskBottomRightCornerRadius = radius;
            return this;
        }

        public Builder padding(int padding){
            mMaskLeft = padding;
            mMaskTop = padding;
            mMaskRight = padding;
            mMaskBottom = padding;
            return this;
        }

        public Builder left(int padding){
            mMaskLeft = padding;
            return this;
        }

        public Builder top(int padding){
            mMaskTop = padding;
            return this;
        }

        public Builder right(int padding){
            mMaskRight = padding;
            return this;
        }

        public Builder bottom(int padding){
            mMaskBottom = padding;
            return this;
        }
    }
}
