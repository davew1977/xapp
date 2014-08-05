package net.sf.xapp.net.server.types;

import ngpoker.common.types.Card;
import ngpoker.common.types.DeckData;

import java.util.List;


/**
 * Encapsulates game's view of a card deck
 */
public interface Deck {

    Card draw(Object attachment);

    void shuffle();

    void load(String cardStr);

    void load(List<Card> cards);

    int remainingCards();

    DeckData snapshot();

    void replace(Object attachment, Card card);
}
