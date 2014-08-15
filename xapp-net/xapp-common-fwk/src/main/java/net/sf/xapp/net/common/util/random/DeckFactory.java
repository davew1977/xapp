package net.sf.xapp.net.common.util.random;

public interface DeckFactory {

    /**
     * Creates a new deck object, independent of all other deck objects.
     * @param size the number of cards in the deck.
     * @param entropySource the entropy source needed by spec.Deck#drawNextCard()
     * @return the newly created deck.
     */
    Deck createDeck(int size);
}
