package net.sf.xapp.uifwk;

import java.awt.*;

public class BigLabel extends XPane
{
    String text;

    public BigLabel(String text, String font, int width, int height)
    {
        this.text = text;
        setSize(width, height);
        setDefaultFont(Font.decode(font));
        setDefaultAlpha(0.8f);
    }

    @Override
    protected void paintPane(Graphics2D g)
    {
        g.setPaint(new GradientPaint(0, 0, Color.gray, 0, 10, new Color(255, 204, 204)));
        g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        g.setColor(Color.BLACK);
        int x = getWidth() / 2 - g.getFontMetrics().stringWidth(text) / 2;
        int y = getHeight() / 2 + g.getFontMetrics().getAscent() / 2;
        g.drawString(text, x, y);
    }

    public void setText(String text)
    {
        this.text = text;
    }
}
