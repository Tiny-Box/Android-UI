package tinybox.lib.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.w3c.dom.Attr;

import tinybox.lib.drawable.RippleDrawable;

/**
 * Created by TinyBox on 2015/5/21.
 */
public class Button extends android.widget.Button {

    private RippleManager mRippleManager = new RippleManager();

    public Button(Context context) {
        super(context);

        // to do init
        init(context, null, 0, 0);
    }

    public Button(Context context, AttributeSet attrs) {
        super(context, attrs);

        // to do init
        init(context, attrs, 0, 0);
    }

    public Button(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // to do init
        init(context, attrs, defStyleAttr, 0);
    }

    public Button(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        // to do init
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        applyStyle(context, attrs, defStyleAttr, defStyleRes);
    }

    public void applyStyle(int resId) {
        applyStyle(getContext(), null, 0, resId);
    }

    public void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mRippleManager.onCreate(this, context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setBackgroundDrawable(Drawable drawable) {
        Drawable background = getBackground();

        if (background instanceof RippleDrawable && !(drawable instanceof RippleDrawable))
            ((RippleDrawable)background).setBackgroundDrawable(drawable);
        else
            super.setBackgroundDrawable(drawable);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        if (l == mRippleManager)
            super.setOnClickListener(l);
        else {
            mRippleManager.setOnClickListener(l);
            setOnClickListener(mRippleManager);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        return mRippleManager.onTouchEvent(event) || result;
    }
}
