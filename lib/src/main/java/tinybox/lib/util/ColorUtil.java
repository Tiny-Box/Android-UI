package tinybox.lib.util;

import android.graphics.Color;

/**
 * Created on 2015/5/23.
 */
public class ColorUtil {
    public static int getColor(int baseColor, float alphaPercent){
        // Color.alpha(): Return the alpha componet of a color int, the same is color >> 24
        // Math.round: Return the closest integer of the argument.
        int alpha = Math.round(Color.alpha(baseColor) * alphaPercent);

        return (baseColor & 0x00FFFFFF) | (alpha << 24);
    }
}
