package net.sf.xapp.net.server.types;

import ngpoker.common.types.Card;

/**
 * Encapsulates ...
 */
public abstract class AbstractDeck implements Deck {

     @Override
    public void load(String cardStr)
    {
        load(Card.decodeList(cardStr));
    }

}
