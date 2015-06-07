package tinybox.lib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import tinybox.lib.R;
import tinybox.lib.drawable.RippleDrawable;
import tinybox.lib.util.ViewUtil;

/**
 * Created by TinyBox on 2015/5/21.
 */
public final class RippleManager implements View.OnClickListener, Runnable {

    private View.OnClickListener mClickListener;
    private View mView;

    public RippleManager(){}

    public void onCreate(View view, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (view.isInEditMode()) {
            return;
        }

        mView = view;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RippleView, defStyleAttr, defStyleRes);
        int rippleStyle = a.getResourceId(R.styleable.RippleView_rd_style, 0);
        RippleDrawable drawable = null;

        if (rippleStyle != 0) {
            drawable = new RippleDrawable.Builder(context, rippleStyle).backgroundDrawalbe(mView.getBackground()).build();
        }
        else {
            boolean rippleEnable = a.getBoolean(R.styleable.RippleView_rd_enable, false);
            if (rippleEnable)
                drawable = new RippleDrawable.Builder(context, attrs, defStyleAttr, defStyleRes).backgroundDrawalbe(mView.getBackground()).build();
        }

        a.recycle();

        if (drawable != null)
            ViewUtil.setBackgroud(mView, drawable);

    }

    public void setOnClickListener(View.OnClickListener listener) { mClickListener = listener; }

    public boolean onTouchEvent(MotionEvent event) {
        Drawable background = mView.getBackground();
        return background instanceof RippleDrawable && ((RippleDrawable) background).onTouch(mView, event);
    }

    @Override
    public void onClick(View view) {
        Drawable background = mView.getBackground();
        long delay = 0;

        if (background instanceof RippleDrawable)
            delay = ((RippleDrawable)background).getDelayClickTime();
//        else if (background instanceof ToolbarRippleDrawable)
//            delay = ((ToolbarRippleDrawable)background).getDelayClickTime();

        if (delay > 0 && mView.getHandler() != null)
            mView.getHandler().postDelayed(this, delay);
        else
            run();
    }

    @Override
    public void run() {
        if (mClickListener != null)
            mClickListener.onClick(mView);
    }

    // cancel the ripple effect of this and all of it's children
    public static void cancelRipple(View view) {
        Drawable background = view.getBackground();

        if (background instanceof RippleDrawable)
            ((RippleDrawable)background).cancel();
//        else if (background instanceof ToolbarRippleDrawable)
//            ((ToolbarRippleDrawable)background).cancel();

        if (view instanceof ViewGroup){
            ViewGroup vg = (ViewGroup)view;
            for (int i = 0, count = vg.getChildCount(); i < count; i++)
                RippleManager.cancelRipple(vg.getChildAt(i));
        }

    }

}
