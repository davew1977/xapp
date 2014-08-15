package net.sf.xapp.net.common.util.random;

//TODO generify and unite this and coord deck
public interface Deck {

    /**
     * Permanently removes a card from the deck and returns it.
     * Valid cards are in the range [0, size).
     * A card may not be returned more than once for each deck.
     *
     * @return a card from the deck.
     * @throws EmptyDeckException if the deck has no more cards.
     */
    int drawNextInt() throws EmptyDeckException;

    /**
     * Use the int from the deck
     * @param n
     * @throws RuntimeException if the deck has already drawn the card
     */
    void use(int n);

    /**
     * Put an int back into the deck so that it is available again
     * @param n
     */
    void replace(int n);
}
