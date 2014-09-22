/*
 *
 * Date: 2011-feb-24
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk.anim;


import java.awt.*;

import net.sf.xapp.uifwk.XPane;


public interface Scene
{
    Scene newSequence();
    Scene loop();
    Scene pause(int duration);
    Scene moveTo(Component comp, int x, int y, int duration, Effect effect);
    Scene move(XPane comp, int startX, int startY, int endX, int endY, int duration, Effect effect);
    Scene rotate(XPane comp, float startAngle, float endAngle, int duration, Effect effect);
    //Scene rotate(XPane comp, startAngle:Number, endAngle:Number, duration:uint=500, e:String=Effect.NONE);
    Scene fadeTo(XPane comp, float alpha, int duration);
    Scene fadeOut(XPane comp);
    Scene fadeIn(XPane comp);
    Scene fade(XPane comp, float startAlpha, float endAlpha, int duration);
    Scene addFuntion(Function function, int duration, Effect effect, Object... args);
    Scene addFuntion(Object target, String method, Object... args);
    Scene show(XPane comp);//posts a task to make the sprite visible
    Scene hide(XPane comp);//posts a task to make the sprite invisible
    Scene resize(XPane comp, float startScale, float endScale, int duration, Effect effect, Point anchor);
    Scene resizeXY(XPane comp, float startScaleX, float startScaleY, float endScaleX,
                   float endScaleY, int duration, Effect effect, Point anchor);
    Scene run(Task task, int duration, Effect effect);
    Scene start();
    Scene stop();//stops the animation dead
    Scene end();//stops the animation and moves it to the end state
    Scene mark(String name); // Creates a time mark at the current time.
    Scene newSequenceFrom(String name); // Creates a new scene starting from mark.

    /**
     * will merge all sequences from the supplies scene into this scene
     *
     * WARNING: be careful with the reference to the scene that's passed in here. It should be thrown away,
     * because any lifecycle method invocations could have undesired side effects
     *
     * @param scene
     * @return
     */
    Scene mergeScene(Scene scene);

    /**
     * Returns the duration of the scene when run uninterupted. In other words will return the length
     * of the longest sequence
     */
    int getTotalDuration();
}
