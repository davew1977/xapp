package net.sf.xapp.net.common.util.random;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ReferenceDeck implements Deck
{
    private final EntropySource entropySource;
    private Map<Integer, Integer> used = new HashMap<Integer, Integer>();
    public final int range;
    public final int drawLimit;
    public int intsRemaining;
    public int remaining;

    public ReferenceDeck(int range, EntropySource entropySource)
    {
        this(range, range, entropySource);
    }

    public ReferenceDeck(int range, int drawLimit, EntropySource entropySource)
    {
        this.range = range;
        this.drawLimit = drawLimit;
        this.intsRemaining = range;
        this.remaining = drawLimit;
        this.entropySource = entropySource;
        assert intsRemaining >= drawLimit;
    }

    public int drawNextInt() throws EmptyDeckException
    {
        if (remaining == 0)
        {
            throw new EmptyDeckException();
        }
        int i = draw(entropySource.nextInt(intsRemaining));
        print("draw");
        return i;
    }

    private void print(String title)
    {
        /*System.out.println(title);
        for (Map.Entry<Integer, Integer> e : used.entrySet())
        {
            System.out.println(e.getKey() + " " + e.getValue());
        }*/
    }

    public void use(int card)
    {
        if (used.containsKey(card))
        {
            throw new RuntimeException("int already used! " + card);
        }
        draw(card);
    }

    public void replace(final int card)
    {
        remaining = drawLimit;
        intsRemaining = range;

        if (!used.containsKey(card))
        {
            throw new RuntimeException("card not drawn");
        }
        /**
         * this is a poor algorithm, because it completely reconstructs the deck when replacing a "card"
         */
        Set<Integer> usedInts = this.used.keySet();
        used = new HashMap<Integer, Integer>();
        for (Integer usedInt : usedInts)
        {
            if (usedInt!=card)
            {
                use(usedInt);
            }
        }
        print("unuse " + card);
    }

    private int draw(Integer card)
    {
        remaining--;
        Integer redirectTo = --intsRemaining;

        while (true)
        {
            Integer redirect = used.put(card, redirectTo);
            if (redirect == null)
            {
                return card;
            }
            card = redirect;
        }
    }

    public int remaining()
    {
        return remaining;
    }
}
