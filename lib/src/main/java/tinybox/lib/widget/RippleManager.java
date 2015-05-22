package tinybox.lib.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by TinyBox on 2015/5/21.
 */
public final class RippleManager implements View.OnClickListener, Runnable {

    private View.OnClickListener mClickListener;
    private View mView;

    public RippleManager(){}

    public void onCreate(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {

    }

    public void setOnClickListener(View.OnClickListener listener) { mClickListener = listener; }

    public boolean onTouchEvent(MotionEvent event) {
        Drawable background = mView.getBackground();
        return background instanceof
    }
}
