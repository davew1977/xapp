/*
 *
 * Date: 2010-okt-31
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import net.sf.xapp.uifwk.anim.Scene;
import net.sf.xapp.uifwk.anim.SceneImpl;


public class XPane extends JPanel
{
    private float defaultAlpha = 1.0f;
    public static Font defaultFont = Font.decode("Tahoma-10");
    private boolean stopTimer;
    private long timerStart;
    private Timer currentTimer;
    protected int originalWidth;
    protected int originalHeight;
    protected Scene currentScene;
    private List<Callback> onMouseEnter;
    private List<Callback> onMouseExit;
    private List<Callback> onMouseClick;
    private MyMouseAdapter mouseAdapter;
    private int borderWidth;
    protected boolean activated;
    protected float rotation;
    protected int tickCount;


    public XPane()
    {
        setLayout(null);
        setOpaque(false);
    }

    public void setDefaultSize(int width, int height)
    {
        originalWidth = width;
        originalHeight = height;
        setSize(width, height);
    }

    public void setDefaultFont(Font font)
    {
        this.defaultFont= font;
    }

    public void addAction(final Callback action, String kstr) {
        addAction(action, KeyStroke.getKeyStroke(kstr));

    }
    public void addAction(final Callback action, KeyStroke kstr) {
        addAction(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                action.call();
            }
        }, kstr);
    }
    public void addAction(AbstractAction action, String kstr)
    {
        KeyStroke ks = KeyStroke.getKeyStroke(kstr);
        addAction(action, ks);
    }

    public void addControlAction(Callback action, int keyCode)
    {
        KeyStroke stroke = KeyStroke.getKeyStroke(keyCode, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        addAction(action, stroke);
    }

    private void addAction(AbstractAction action, KeyStroke ks)
    {
        getInputMap().put(ks, ks);
        getActionMap().put(ks, action);
    }

    public Scene newScene()
    {
        if(currentScene!=null)
        {
            currentScene.end();
        }
        currentScene = new SceneImpl();
        return currentScene;
    }

    public void setDefaultAlpha(float defaultAlpha)
    {
        this.defaultAlpha = defaultAlpha;
    }

    public float getDefaultAlpha()
    {
        return defaultAlpha;
    }

    public void startTimer(int delayBetweenFrames, final long stopAfter)
    {
        stopTimer = false;
        if(currentTimer!=null)
        {
            currentTimer.stop();
        }
        timerStart = System.currentTimeMillis();
        currentTimer = new Timer(delayBetweenFrames, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                long timeSinceStart = System.currentTimeMillis() - timerStart;
                if((timeSinceStart<stopAfter || stopAfter==0) && !stopTimer)
                {
                    onTick(timeSinceStart);
                }
                else
                {
                    currentTimer.stop();
                }
            }
        });
        currentTimer.setRepeats(true);
        currentTimer.start();
    }

    public void stopTimer()
    {
        stopTimer = true;
    }

    public static void setAlpha(Graphics g2d, float alpha)
    {
        ((Graphics2D) g2d).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    }

    public void setSize(int width, int height)
    {
        super.setSize(width, height);
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));
    }

    @Override
    protected final void paintComponent(Graphics g)
    {
        if(borderWidth!=0)
        {
            g = g.create(borderWidth, borderWidth, getWidthMinusBorder(), getHeightMinusBorder());
        }
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        setAlpha(g2d, defaultAlpha);
        g.setFont(defaultFont);
        paintPane(g2d);
        super.paintComponent(g);
    }

    protected int getHeightMinusBorder()
    {
        return getHeight()- borderWidth*2;
    }

    protected int getWidthMinusBorder()
    {
        return getWidth() - borderWidth * 2;
    }

    public void onTick(long timeSinceStart)
    {
        tickCount++;
    }

    protected void paintPane(Graphics2D g)
    {
        paintPane(new ScalableGraphics( this, g));
    }

    protected void paintPane(ScalableGraphics g)
    {

    }


    public int getOriginalWidth()
    {
        return originalWidth;
    }

    public int getOriginalHeight()
    {
        return originalHeight;
    }


    public final int x(float g)
    {
        return (int) (g / (float) getOriginalWidth() * getWidth());
    }

    public final int y(float g)
    {
        return (int) (g / (float) getOriginalWidth() * getHeight());
    }

    public void addOnMouseEnter(Callback onMouseEnter)
    {
        initListener();
        this.onMouseEnter.add(onMouseEnter);
    }

    public void addOnMouseExit(Callback onMouseExit)
    {
        initListener();
        this.onMouseExit.add(onMouseExit);
    }

    public void addOnMouseClick(Callback onMouseClick)
    {
        initListener();
        this.onMouseClick.add(onMouseClick);
    }

    private void initListener()
    {
        if(mouseAdapter==null)
        {
            mouseAdapter = new MyMouseAdapter();
            addMouseListener(mouseAdapter);
            onMouseClick = new ArrayList<Callback>();
            onMouseEnter = new ArrayList<Callback>();
            onMouseExit = new ArrayList<Callback>();
        }
    }

    public void removeListeners() {
        removeMouseListener(mouseAdapter);
    }

    public void setBorderWidth(int borderWidth)
    {
        this.borderWidth = borderWidth;
    }

    public void setRotation(float newRotation)
    {
        this.rotation =  newRotation;
        repaint();
    }

    private class MyMouseAdapter extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            call(onMouseClick);
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            call(onMouseEnter);
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            call(onMouseExit);
        }

        private void call(List<Callback> callbacks)
        {
            if (isEnabled())
            {
                for (Callback callback : callbacks)
                {
                    callback.call();
                }
            }
        }
    }

    public void removeComp(Component c)
    {
        remove(c);
        repaint();
    }

    public void activate()
    {
        activated = true;
    }
}
