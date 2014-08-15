package net.sf.xapp.net.server.playerrepository;

import ngpoker.common.types.Coord;
import ngpoker.common.util.deck.DeterministicEntropySource;
import ngpoker.common.util.deck.SquareCoordDeck;
import org.junit.Test;

import static org.junit.Assert.*;

import static net.sf.xapp.net.server.playerrepository.UserspaceCoordDeck.convert;

/**
 * Created with IntelliJ IDEA.
 * UserEntityWrapper: davidw
 * Date: 2/19/14
 * Time: 5:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class OffsetCoordDeckTest {

    @Test
    public void testDraw() throws Exception
    {
        DeterministicEntropySource des = new DeterministicEntropySource(0,1,2,3,4,5);
        SquareCoordDeck coordDeck = new SquareCoordDeck(des,2,3);
        assertEquals(new Coord(0,0), coordDeck.draw());
        assertEquals(new Coord(1,0), coordDeck.draw());
        assertEquals(new Coord(0,1), coordDeck.draw());
        assertEquals(new Coord(1,1), coordDeck.draw());
        assertEquals(new Coord(0,2), coordDeck.draw());
        assertEquals(new Coord(1,2), coordDeck.draw());

        des = new DeterministicEntropySource(0,1,2,3,4,5);
        coordDeck = new SquareCoordDeck(des, 3,2);
        assertEquals(new Coord(0,0), coordDeck.draw());
        assertEquals(new Coord(1,0), coordDeck.draw());
        assertEquals(new Coord(2,0), coordDeck.draw());
        assertEquals(new Coord(0,1), coordDeck.draw());
        assertEquals(new Coord(1,1), coordDeck.draw());
        assertEquals(new Coord(2,1), coordDeck.draw());
        assertEquals(0, coordDeck.remaining());

        des = new DeterministicEntropySource(2,3);
        coordDeck = new SquareCoordDeck(des, 2,2);
        coordDeck.use(new Coord(0,0));
        coordDeck.use(new Coord(1,0));
        assertEquals(new Coord(0,1), coordDeck.draw());
        assertEquals(new Coord(1,1), coordDeck.draw());
        assertEquals(0, coordDeck.remaining());
        coordDeck.replace(new Coord(1,1));
        coordDeck.replace(new Coord(0,1));
        assertEquals(2, coordDeck.remaining());
        assertEquals(new Coord(0,1), coordDeck.draw());
        assertEquals(new Coord(1,1), coordDeck.draw());


        des = new DeterministicEntropySource(0,1,2,3);
        OffsetCoordDeck ocd = new OffsetCoordDeck(-1, -2, des, 2, 4);
        assertEquals(new Coord(-1,-2), ocd.draw());
        assertEquals(new Coord(0,-2), ocd.draw());
        assertEquals(new Coord(-1,-1), ocd.draw());
        assertEquals(new Coord(0,-1), ocd.draw());
        ocd.replace(new Coord(0,-1));
        assertEquals(new Coord(0,-1), ocd.draw());

        des.reset();
        ocd = new OffsetCoordDeck(-10,20,des,10, 100);
        assertTrue(ocd.intersectedBy(new Coord(-10,20)));
        assertFalse(ocd.intersectedBy(new Coord(-1,19)));
        assertFalse(ocd.intersectedBy(new Coord(-11,20)));
        assertTrue(ocd.intersectedBy(new Coord(-9,20)));
        assertTrue(ocd.intersectedBy(new Coord(-1,20)));


        des.reset();
        UserspaceCoordDeck ucd = new UserspaceCoordDeck(des,10,13,2);
        assertEquals(7, ucd.n);
        assertEquals(-10, ucd.coordDeck.getOffsX());
        assertEquals(10, ucd.coordDeck.getOffsY());
        assertEquals(2, ucd.coordDeck.remaining());
        ucd.use(new Coord(-9,11));
        assertEquals(1, ucd.coordDeck.remaining());
        assertEquals(new Coord(-10,10), ucd.draw());

        assertEquals(0, ucd.coordDeck.remaining());
        ucd.replace(new Coord(-9,11));
        assertEquals(1, ucd.coordDeck.remaining());
        assertEquals(new Coord(-9,10), ucd.draw());
        assertEquals(0, ucd.coordDeck.remaining());
    }

    public void testConvert()
    {
        assertEquals(new Coord(0,0), convert(1));
        assertEquals(new Coord(1,1), convert(9));
        assertEquals(new Coord(2,2), convert(25));
        assertEquals(new Coord(3,3), convert(49));
        assertEquals(new Coord(1,0), convert(2));
        assertEquals(new Coord(1,-1), convert(3));
        assertEquals(new Coord(0,-1), convert(4));
        assertEquals(new Coord(-1,-1), convert(5));
        assertEquals(new Coord(-1,0), convert(6));
        assertEquals(new Coord(-1,1), convert(7));
        assertEquals(new Coord(0,1), convert(8));
        assertEquals(new Coord(2,1), convert(10));
        assertEquals(new Coord(2,0), convert(11));
        assertEquals(new Coord(2,-1), convert(12));
        assertEquals(new Coord(2,-2), convert(13));
        assertEquals(new Coord(1,-2), convert(14));
        assertEquals(new Coord(0,-2), convert(15));
        assertEquals(new Coord(-1,-2), convert(16));
        assertEquals(new Coord(-2,-2), convert(17));
        assertEquals(new Coord(-2,-1), convert(18));
        assertEquals(new Coord(-2,0), convert(19));
        assertEquals(new Coord(-2,1), convert(20));
        assertEquals(new Coord(-2,2), convert(21));
        assertEquals(new Coord(-1,2), convert(22));
        assertEquals(new Coord(0,2), convert(23));
        assertEquals(new Coord(1,2), convert(24));
        assertEquals(new Coord(2,2), convert(25));

        assertEquals(new Coord(-1,-3), convert(35));
    }
}
