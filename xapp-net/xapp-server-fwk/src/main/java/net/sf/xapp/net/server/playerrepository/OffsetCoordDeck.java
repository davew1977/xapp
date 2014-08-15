package net.sf.xapp.net.server.playerrepository;

import net.sf.xapp.net.common.types.Coord;
import net.sf.xapp.net.common.util.random.CoordDeck;
import net.sf.xapp.net.common.util.random.EntropySource;
import net.sf.xapp.net.common.util.random.SquareCoordDeck;

public class OffsetCoordDeck implements CoordDeck
{
    private final int squareSize;
    private final SquareCoordDeck squareCoordDeck;
    private final int offsX;
    private final int offsY;


    public OffsetCoordDeck(int offsX, int offsY, EntropySource entropySource, int squareSize, int noToDraw)
    {
        this.offsX = offsX;
        this.offsY = offsY;
        this.squareSize = squareSize;

        squareCoordDeck = new SquareCoordDeck(entropySource, this.squareSize, this.squareSize, noToDraw);
    }

    @Override
    public void use(Coord coord)
    {
        squareCoordDeck.use(translate(coord, -offsX, -offsY));
    }

    @Override
    public void replace(Coord coord)
    {
        squareCoordDeck.replace(translate(coord, -offsX, -offsY));
    }

    @Override
    public Coord draw()
    {
        return translate(squareCoordDeck.draw(), offsX, offsY);
    }

    @Override
    public int remaining()
    {
        return squareCoordDeck.remaining();
    }

    @Override
    public boolean isEmpty()
    {
        return squareCoordDeck.isEmpty();
    }

    /**
     * is the coord in this square?
     *
     * @param coord
     * @return
     */
    public boolean intersectedBy(Coord coord)
    {
        Integer y = coord.getY();
        Integer x = coord.getX();
        boolean xIntersects = x >= offsX && x < offsX + squareSize;
        boolean yIntersects = y >= offsY && y < offsY + squareSize;
        return xIntersects && yIntersects;
    }

    public int getOffsX()
    {
        return offsX;
    }

    public int getOffsY()
    {
        return offsY;
    }

    private static Coord translate(Coord coord, int offX, int offY)
    {
        int x = coord.getX() + offX;
        int y = coord.getY() + offY;
        return new Coord(x, y);
    }


}
