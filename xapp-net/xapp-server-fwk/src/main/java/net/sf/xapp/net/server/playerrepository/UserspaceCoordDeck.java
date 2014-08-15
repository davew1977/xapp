package net.sf.xapp.net.server.playerrepository;

import net.sf.xapp.net.common.types.Coord;
import net.sf.xapp.net.common.util.MathUtils;
import net.sf.xapp.net.common.util.random.CoordDeck;
import net.sf.xapp.net.common.util.random.EntropySource;

import static net.sf.xapp.net.common.util.MathUtils.*;

public class UserspaceCoordDeck implements CoordDeck
{
    private final EntropySource entropySource;
    private final int squareSize;
    OffsetCoordDeck coordDeck;
    int n; //the number of the userspace square we're on (starts at 1, not 0!)
    private final int usersPerSquare;

    public UserspaceCoordDeck(EntropySource entropySource, int squareSize, int initialNoUsers, int usersPerSquare)
    {
        this.usersPerSquare = usersPerSquare;
        this.entropySource = entropySource;
        this.squareSize = squareSize;
        n = 1 + initialNoUsers / usersPerSquare;
        initCoordDeck();
    }

    private void initCoordDeck()
    {
        Coord c = convert(n);
        int offX = c.getX() * squareSize;
        int offY = c.getY() * squareSize;
        coordDeck = new OffsetCoordDeck(offX,offY,entropySource, squareSize, usersPerSquare);
    }

    @Override
    public void use(Coord coord)
    {
        if(coordDeck.intersectedBy(coord))
        {
            coordDeck.use(coord);
        }
    }

    @Override
    public void replace(Coord coord)
    {
        if(coordDeck.intersectedBy(coord))
        {
            coordDeck.replace(coord);
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public synchronized Coord draw()
    {
        if(coordDeck.isEmpty())
        {
            n++;
            initCoordDeck();
        }
        return coordDeck.draw();
    }

    @Override
    public int remaining()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    static Coord convert(int n)
    {
        double v = Math.sqrt(n);
        if(isWhole(v) && isOdd((int) v))
        {
            int x = ((int) v - 1) / 2;
            return new Coord(x,x);
        }
        int nextOdd = nextOdd(v);
        int previousOdd = previousOdd(v);
        int startX = (nextOdd-1)/2;
        int startY = (nextOdd-1)/2;
        int nextSq = (int) Math.pow(nextOdd, 2);
        int prevSq = (int) Math.pow(previousOdd, 2);
        int perimeter = nextSq - prevSq;
        int side = perimeter / 4;
        int diff = n - prevSq;
        int q = diff / side; //q should be 0,1,2 or 3
        int xOff = -1;
        int yOff = -1;
        int mod = diff % side;
        switch (q)
        {
        case 0:
            xOff = 0;
            yOff = mod;
            break;
        case 1:
            xOff = -mod;
            yOff = side;
            break;
        case 2:
            xOff = -side;
            yOff = side - mod;
            break;
        case 3:
            xOff = -side + mod;
            yOff = 0;
            break;
        }
        //if diff
        return new Coord(startX + xOff,startY - yOff);
    }

}
