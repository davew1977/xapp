package net.sf.xapp.uifwk;

import java.awt.*;

public class ScalableGraphics
{
    XPane pane;
    Graphics2D g;

    public ScalableGraphics(XPane pane, Graphics2D g)
    {
        this.pane = pane;
        this.g = g;
    }

    public void rotate(double theta, float x, float y)
    {
        g.rotate(theta, x(x), y(y));
    }

    public void gradientPaint(float x1, float y1, Color c1, float x2, float y2, Color c2)
    {
        g.setPaint(new GradientPaint(x(x1), y(y1), c1, x(x2), y(y2), c2));
    }

    public void radialPaint(int cx, float cy, int radius, Color c1, Color c2)
    {
        g.setPaint(new RadialGradientPaint(x(cx), y(cy), x(radius), new float[]{0, 1}, new Color[]{c1, c2}));
    }

    public void fillRect(float x, float y, float w, float h)
    {
        g.fillRect(x(x), y(y), x(w), y(h));
    }

    public void roundRect(float x, float y, float w, float h, float a)
    {
        g.fillRoundRect(x(x), y(y), x(w), y(h), x(a), y(a));
    }

    private int x(float x1)
    {
        return pane.x(x1);
    }

    private int y(float y2)
    {
        return pane.y(y2);
    }

    public void setColor(Color color)
    {
        g.setColor(color);
    }

    public void drawRect(float x, float y, float w, float h)
    {
        g.drawRect(x(x), y(y), x(w), y(h));
    }

    public void drawLine(float x1, float y1, float x2, float y2)
    {
        g.drawLine(x(x1), y(y1), x(x2), y(y2));
    }

    public void setPenThickness(float t)
    {
        g.setStroke(new BasicStroke(x(t)));
    }

    public void setPen(float width, int capButt, int joinMiter, float miterLimit, float[] dash, float dash_phase)
    {
        float[] sub = new float[dash.length];
        for (int j = 0; j < dash.length; j++)
        {
            float f = dash[j];
            sub[j] = x(f);
        }
        g.setStroke(new BasicStroke(x(width), capButt, joinMiter, miterLimit, sub, x(dash_phase)));
    }

    public void drawArc(float x, float y, float width, float height, int startAngle, int arcAngle)
    {
        g.drawArc(x(x), y(y), x(width), y(height), startAngle, arcAngle);
    }

    public void fillArc(float x, float y, float width, float height, int startAngle, int arcAngle)
    {
        g.fillArc(x(x), y(y), x(width), y(height), startAngle, arcAngle);
    }

    public void fillOval(float x, float y, float w, float h)
    {
        g.fillOval(x(x), y(y), x(w), y(h));
    }

    public void fillRoundRect(float x, float y, float w, float h, float arcWidth, float arcHeight)
    {
        g.fillRoundRect(x(x), y(y), x(w), y(h), x(arcWidth), y(arcHeight));
    }

    public void drawOval(float x, float y, float w, float h)
    {
        g.drawOval(x(x), y(y), x(w), y(h));
    }

    public void fillPoly(int... xys) {
        int n = xys.length/2;
        int[] x = new int[n];
        int[] y = new int[n];
        for(int i=0;i<n;i++) {
            x[i] = x(xys[2*i]);
            y[i] = y(xys[2*i+1]);
        }
        g.fillPolygon(x,y,n);
    }
}
