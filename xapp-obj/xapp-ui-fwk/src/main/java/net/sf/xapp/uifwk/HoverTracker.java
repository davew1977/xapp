package net.sf.xapp.uifwk;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *  must add this to a child component when you want to track the hover status of the parent
 *
 *  otherwise mouse in and out events on the child will appear like "hover" is lost on the parent
 *
 */
public class HoverTracker extends MouseAdapter
{
    private Object target;

    public HoverTracker(Object target)
    {
        this.target = target;
        ReflectionUtils.checkMethodExists(target.getClass(), "setHover", true);
    }

    //this listener is added so that the button will be visible when mouse is over
    @Override
    public void mouseExited(MouseEvent e)
    {
        ReflectionUtils.call(target, "setHover", false);
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        ReflectionUtils.call(target, "setHover", true);
    }
}
