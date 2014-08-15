package net.sf.xapp.net.common.util.random;

import net.sf.xapp.net.common.types.Coord;

public class SquareCoordDeck implements CoordDeck
{
    private final ReferenceDeck deck;
    private final int w;

    public SquareCoordDeck(EntropySource entropySource, int w, int h)
    {
        this(entropySource, w, h, w*h);
    }

    public SquareCoordDeck(EntropySource entropySource, int w, int h, int noToDraw)
    {
        this.w = w;
        deck = new ReferenceDeck(w * h, noToDraw, entropySource);
    }

    @Override
    public void use(Coord coord)
    {
        deck.use(coord.getX() + w * coord.getY());
    }

    public void replace(Coord coord)
    {
        deck.replace(coord.getX() + w * coord.getY());
    }

    @Override
    public Coord draw()
    {
        int i = deck.drawNextInt();
        return new Coord(i % w, i / w);
    }

    public int remaining()
    {
        return deck.remaining();
    }

    @Override
    public boolean isEmpty()
    {
        return deck.remaining()==0;
    }
}
