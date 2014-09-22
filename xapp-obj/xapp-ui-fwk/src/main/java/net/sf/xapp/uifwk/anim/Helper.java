/*
 *
 * Date: 2011-feb-24
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk.anim;

import java.awt.*;

public class Helper
{
    public static Color mixColor(Color a, Color b, double f)
    {
        return new Color(mixColor(a.getRGB(),b.getRGB(),f));    
    }

    public static int mixColor(int a, int b, double f)
    {
        int tr = (int) linearInterpolate((a >> 16) & 0xff, (b >> 16) & 0xff, f);
        int tg = (int) linearInterpolate((a >> 8) & 0xff, (b >> 8) & 0xff, f);
        int tb = (int) linearInterpolate((a) & 0xff, (b) & 0xff, f);
        return (tr << 16) | (tg << 8) | (tb);
    }

    public static float linearInterpolate(float start, float end, double workDone)
    {
        return (float) (start + (end - start) * workDone);
    }
}
