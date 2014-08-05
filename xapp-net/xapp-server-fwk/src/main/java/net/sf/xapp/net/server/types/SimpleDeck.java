package net.sf.xapp.net.server.types;

import ngpoker.common.types.Card;
import ngpoker.common.types.DeckData;

import java.util.*;

/**
 * simple deck that can draw determined sequence of cards or random cards (infinite decks)
 */
public class SimpleDeck extends  AbstractDeck
{
    public Random m_random = new Random();
    public List<Card> m_cards ;
    public List<Card> m_drawnCards ;

    public SimpleDeck() {
        this(new Random());
    }
    public SimpleDeck(Random random)
    {
        m_random = random;
        m_cards = new LinkedList<Card>();
        m_drawnCards = new ArrayList<Card>();
        m_cards.addAll(Arrays.asList(Card.CARDS));
    }

    public Card draw(Object attachment)
    {
        if(m_cards.isEmpty()) return null;
        return draw(m_random != null ? m_random.nextInt(m_cards.size()) : 0);
    }

    private Card draw(int pIndex) {
        Card card = m_cards.remove(pIndex);
        m_drawnCards.add(card);
        return card;
    }

    public void shuffle()
    {
        m_cards.addAll(m_drawnCards);
        m_drawnCards.clear();
    }

    public void load(List<Card> cards)
    {
        m_random = null;
        m_drawnCards = new ArrayList<Card>();
        m_cards = new ArrayList<Card>(cards);
    }

    @Override
    public int remainingCards() {
        return m_cards.size();
    }

    public static List<Card> generateRandomDeck()
    {
        SimpleDeck simpleDeck = new SimpleDeck();
        List<Card> cards = new ArrayList<Card>();
        while(!simpleDeck.m_cards.isEmpty())
        {
            cards.add(simpleDeck.draw(null));
        }
        return cards;
    }

    public static void main(String[] args)
    {
        for (int j=0;j<100;j++)
        {
            StringBuilder sb = new StringBuilder();
            SimpleDeck s = new SimpleDeck();
            for(int i=0; i<52;i++)
            {
                sb.append(s.draw(null)).append(",");
            }
            System.out.println(sb);
        }
    }

    public DeckData snapshot()
    {
        return new DeckData(new ArrayList<Card>(m_cards), new ArrayList<Card>(m_drawnCards));
    }

    public void load(DeckData deckData)
    {
        m_random = null;
        m_drawnCards = new ArrayList<Card>(deckData.getDrawnCards());
        m_cards = new ArrayList<Card>(deckData.getCardsRemaining());
    }

    /**
     * forcefully draw the specified card
     * @param card the card
     */
    public void pull(Card card) {
        int i = m_cards.indexOf(card);
        assert i != -1 : String.format("%s not in %s", card, m_cards);
        draw(i); //TODO performance
    }

    public void replace(Object attachment, Card card) {
        m_drawnCards.remove(card);
        m_cards.add(card);
    }
}
