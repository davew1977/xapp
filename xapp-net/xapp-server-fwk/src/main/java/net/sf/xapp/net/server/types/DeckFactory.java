package net.sf.xapp.net.server.types;

public interface DeckFactory<T>
{
    Deck createDeck(T arg);
}
