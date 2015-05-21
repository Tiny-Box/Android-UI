package tinybox.lib.util;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

/**
 * Created by TinyBox on 2015/5/21.
 */
public class ViewUtil {

    public static void setBackgroud(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            view.setBackground(drawable);
        else
            view.setBackgroundDrawable(drawable);
    }
}
