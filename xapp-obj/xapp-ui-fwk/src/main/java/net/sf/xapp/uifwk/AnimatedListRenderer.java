package net.sf.xapp.uifwk;

import java.awt.*;

public interface AnimatedListRenderer<E>
{
    void render(Graphics2D g, XPane comp, E data);
}
