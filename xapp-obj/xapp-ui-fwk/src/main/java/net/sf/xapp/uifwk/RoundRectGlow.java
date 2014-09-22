package net.sf.xapp.uifwk;


import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import net.sf.xapp.uifwk.anim.Effect;
import net.sf.xapp.uifwk.anim.Function;
import net.sf.xapp.uifwk.anim.Helper;
import net.sf.xapp.uifwk.anim.Scene;


public class RoundRectGlow extends XPane
{
    private static final Color transparent = new Color(255, 255, 255, 0);
    private int width;
    private int height;
    private int maxThickness;
    private int arcWidth;
    private Color color = Color.blue;
    private int _tempThickness;
    private int thickness;
    private BufferedImage bi;
    private int twinkleCount = 4;

    public void init(int arcWidth, int width, int height, int maxThickness)
    {
        this.arcWidth = arcWidth;
        this.width = width;
        this.height = height;
        this.maxThickness = maxThickness;
        thickness = maxThickness;
        int w = width + (maxThickness * 2);
        int h = height + (maxThickness * 2);
        setDefaultSize(w, h);
        bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    protected void paintPane(Graphics2D g)
    {
        Graphics2D graphics = bi.createGraphics();
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1));
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics.setPaintMode();
        paintGlow(graphics);
        g.drawImage(bi, 0,0, null);
    }

    private void paintGlow(Graphics2D g)
    {
        int w = getWidth();
        int h = getHeight();
        if(_tempThickness < maxThickness)
        {
            int offsx = maxThickness - _tempThickness;
            w = w - offsx*2;
            h = h - offsx * 2;
            g = (Graphics2D) g.create(offsx, offsx, w, h);
        }
        int offs = _tempThickness + arcWidth / 2;
        g.setPaint(new GradientPaint(0, 0, transparent, 0, _tempThickness, color));
        g.fillRect(offs, 0, width - arcWidth, _tempThickness);
        g.setPaint(new GradientPaint(0, _tempThickness + height, color, 0, h, transparent));
        g.fillRect(offs, _tempThickness + height, width - arcWidth, h);
        g.setPaint(new GradientPaint(0, 0, transparent, _tempThickness, 0, color));
        g.fillRect(0, offs, _tempThickness, height - arcWidth);
        g.setPaint(new GradientPaint(w - _tempThickness, 0, color, w, 0, transparent));
        g.fillRect(_tempThickness + width, offs, _tempThickness, height - arcWidth);

        int lx = w - offs; //left edge of rect
        int by = h - offs; //bottom edge of rect
        int cx = w - offs * 2;
        int cy = h - offs * 2;
        //top left
        int rad = (_tempThickness * 2) + arcWidth;
        float q = arcWidth / (float)rad;
        //int startGlow =
        //20 80 :
        //20 40 : 0.5
        //20 0 : 0
        //20 10 : 0.2
        if (q<1.0)
        {
            g.setPaint(new RadialGradientPaint(offs, offs, offs, new float[]{q, 1}, new Color[]{color, transparent}));
            g.fillArc(0, 0, rad, rad, 180, -90);
            //top right
            g.setPaint(new RadialGradientPaint(lx, offs, offs, new float[]{q, 1}, new Color[]{color, transparent}));
            g.fillArc(cx, 0, rad, rad, 0, 90);
            //bottom left
            g.setPaint(new RadialGradientPaint(offs, by, offs, new float[]{q, 1}, new Color[]{color, transparent}));
            g.fillArc(0, cy, rad, rad, 180, 90);
            //bottom right
            g.setPaint(new RadialGradientPaint(lx, by, offs, new float[]{q, 1}, new Color[]{color, transparent}));
            g.fillArc(cx, cy, rad, rad, 0, -90);
        }

        //g.setColor(Color.black);
        //g.fillRoundRect(thickness, thickness, width, height, arcWidth, arcWidth);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1));
        g.setColor(transparent);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRoundRect(_tempThickness, _tempThickness, width, height, arcWidth, arcWidth);
    }

    public void glow(boolean show)
    {
        if(show)
        {
            if (twinkleCount>0)
            {
                twinkle();
            }
            else
            {
                showGlow();
            }
        }
        else
        {
            hideGlow();
        }
    }

    public void setTwinkleCount(int twinkleCount)
    {
        this.twinkleCount = twinkleCount;
    }

    private void twinkle()
    {
        Scene scene = newScene();
        for (int i=0; i< twinkleCount; i++)
        {
            scene.addFuntion(new ChangeGlow(thickness, thickness/2), 200, Effect.BOTH);
            scene.addFuntion(new ChangeGlow(thickness/2, thickness), 200, Effect.BOTH);
        }
        scene.start();
    }

    public static void main(String[] args)
    {
        RoundRectGlow glow = new RoundRectGlow();
        glow.init(40, 100, 80, 20);
        JFrame jFrame = SwingUtils.showInFrame(glow);
        jFrame.setBackground(Color.black);
        glow.twinkle();
    }

    private void hideGlow()
    {
        Scene scene = newScene();
        scene.addFuntion(new ChangeGlow(thickness, 0), 200, Effect.BOTH);
        scene.start();
    }

    private void showGlow()
    {
        Scene scene = newScene();
        scene.addFuntion(new ChangeGlow(0, thickness), 200, Effect.BOTH);
        scene.start();
    }

    public void setThickness(int thickness)
    {
        newScene(); //kill any running scene
        assert thickness<=maxThickness;
        this.thickness= thickness;
        _tempThickness =  thickness;
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    private class ChangeGlow extends Function
    {
        private final int startThickness;
        private final int endThickness;

        public ChangeGlow(int startThickness, int endThickness)
        {
            this.startThickness = startThickness;
            this.endThickness = endThickness;
        }

        @Override
        public void step(double workDone, Object... args)
        {
            _tempThickness = (int) Helper.linearInterpolate(startThickness, endThickness, workDone);
            repaint();
        }
    }
}
