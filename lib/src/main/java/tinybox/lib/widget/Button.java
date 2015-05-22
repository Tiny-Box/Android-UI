package tinybox.lib.widget;

import android.content.Context;
import android.util.AttributeSet;

import org.w3c.dom.Attr;

/**
 * Created by TinyBox on 2015/5/21.
 */
public class Button extends android.widget.Button {

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

    }
}
