package tinybox.lib.drawable;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Interpolator;

/**
 * Created on 2015/5/22.
 */
public class RippleDrawable extends Drawable implements Animatable, View.OnTouchListener {

    // Mask
    private Paint mShadePaint;
    private Paint mFillPaint;
    private Mask mMask;
    private Matrix mMatrix;
    // RadiaGradient: Create a shader that draws a radical gradient
    private RadialGradient mInShader;
    private RadialGradient mOutShader;

    // background
    private Drawable mBackgroundDrawable;
    private RectF mBackgroundBounds;
    private Path mBackground;
    private int mBackgroundAnimDuration;
    private int mBackgroundColor;
    private float mBackgroundAlphaPercent;

    // Ripple
    private PointF mRipplePoint;
    private int mRippleTyple;
    private int mMaxRippleRadius;
    private int mRippleAnimDuration;
    private int mRippleColor;
    private int mDelayClickType;




    // Interpolator: the rate change of an animation.
    // which allows the basic animation effects(alpha, scale, translate, rotate)to be accelerated, decelerated, repeated.
    private Interpolator mInInterpolator;
    private Interpolator mOutInterpolator;

    private static final int TYPE_TOUCH_MATCH_VIEW = -1;
    private static final int TYPE_TOUCH = 0;
    private static final int TYPE_WAVE = 1;



    private RippleDrawable(Drawable backgroundDrawable, int backgroundAnimDuration, int backgroundColor, int rippleType, int delayClickType, int maxRippleRadius, int rippleAnimDuration, int rippleColor, Interpolator inInterpolator, Interpolator outInterpolator, int type, int topLeftCornerRadius, int topRightCornerRadius, int bottomRightCornerRadius, int bottomLeftCornerRadius, int left, int top, int right, int bottom) {
        // set Drawable, Duration and Color
        setBackgroundDrawable(backgroundDrawable);
        mBackgroundAnimDuration = backgroundAnimDuration;
        mBackgroundColor = backgroundColor;

        mRippleTyple = rippleType;
        setDelayClickType(delayClickType);
        mMaxRippleRadius = maxRippleRadius;
        mRippleAnimDuration = rippleAnimDuration;
        mRippleColor = rippleColor;

        // when radius is error
        if (mRippleTyple == TYPE_TOUCH && mMaxRippleRadius <= 0)
            mRippleTyple = TYPE_TOUCH_MATCH_VIEW;

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

    }

    public void setBackgroundDrawable(Drawable backgroundDrawable) {
        mBackgroundDrawable = backgroundDrawable;
        if (mBackgroundDrawable != null) {
            // setBounds: Set the width and height, eg: setBounds(top, left, width, height)
            // getBounds: Return the drawable's bounds Rect.
            mBackgroundDrawable.setBounds(getBounds());
        }
    }

    public int getDelayClickType() { return mDelayClickType; }

    public void setDelayClickType(int type) {
        mDelayClickType = type;
    }

    public void setMask(int type, int topLeftCornerRadius, int topRightCornerRadius, int bottomRightCornerRadius, int bottomLeftCornerRadius, int left, int top, int right, int bottom){
        mMask = new Mask(type, topLeftCornerRadius, topRightCornerRadius, bottomRightCornerRadius, bottomLeftCornerRadius, left, top, right, bottom);
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
}
