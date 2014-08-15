package net.sf.xapp.net.common.util.random;

import net.sf.xapp.net.common.types.Coord;

public interface CoordDeck
{
    void use(Coord coord);
    void replace(Coord coord);

    Coord draw();

    int remaining();

    boolean isEmpty();
}
