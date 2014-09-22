/*
 *
 * Date: 2011-feb-25
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk.anim;


import java.awt.*;

import net.sf.xapp.uifwk.XPane;

public class Picture extends XPane
{
    protected Image image;

    public Picture()
    {
    }

    public Picture(Image image)
    {
        setImage(image);
    }

    public void setImage(Image image)
    {
        this.image = image;
        if (image != null) {
            originalWidth = image.getWidth(null);
            originalHeight = image.getHeight(null);
            setSize(originalWidth, originalHeight);
        }
    }

    @Override
    protected void paintPane(Graphics2D g)
    {

        if (image!=null) {
            g.drawImage(image, 0,0,getWidthMinusBorder(), getHeightMinusBorder(),null);
        }
    }

    public void setOriginalSize(int width, int height)
    {
        originalHeight = height;
        originalWidth = width;
        setSize(originalWidth, originalHeight);
    }

    public Image getImage()
    {
        return image;
    }
}
