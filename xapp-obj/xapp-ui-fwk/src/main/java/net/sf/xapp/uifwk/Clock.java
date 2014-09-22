/*
 *
 * Date: 2010-okt-18
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;



import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Clock extends XPane
{
    public static final int W = 20;
    public static final int H = 20;
    private long endTime;
    private long totalTime;
    private Timer currentTimer;
    private long timeLeft;
    private boolean includeBorder;

    public Clock(boolean includeBorder)
    {
        this.includeBorder = includeBorder;
        setSize(W,H);
        setDefaultAlpha(1f);
    }

    public void init(int totalTime)
    {
        this.totalTime = totalTime;
        endTime = System.currentTimeMillis() + totalTime;
        if(currentTimer!=null)
        {
            currentTimer.stop();
        }
        currentTimer = new Timer(25, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                timeLeft = endTime - System.currentTimeMillis();
                repaint();
                if(timeLeft<0)
                {
                    currentTimer.stop();
                }
            }
        });
        currentTimer.start();
    }

    public void stop()
    {
        if(currentTimer!=null)
        {
            timeLeft=0;
            currentTimer.stop();
            repaint();
        }
    }

    @Override
    protected void paintPane(Graphics2D g)
    {
        drawClock(g, timeLeft, totalTime, getWidth(), getHeight(), 0, 0, Color.black, Color.lightGray, includeBorder);
    }

    public static void drawClock(Graphics2D g, long timeLeft, long totalTime, int w, int h, int x, int y, Color c1, Color c2, boolean includeBorder)
    {
        float p = timeLeft /(float) totalTime;
        int angle = (int) (360 * p);

        if(timeLeft >0)
        {
            if (includeBorder)
            {
                g.setColor(Color.white);
                g.fillOval(x,y,w,h);
            }
            g.setPaint(new GradientPaint(x,y, c1, x + w/2, y + h, c2));
            g.fillArc(x+1,y+1,w-2,  h-2, 90, angle);
        }
    }

    public static void main(String[] args)
    {
        Clock clock = new Clock(true);
        clock.init(20000);
        SwingUtils.showInFrame(clock);
    }
}
