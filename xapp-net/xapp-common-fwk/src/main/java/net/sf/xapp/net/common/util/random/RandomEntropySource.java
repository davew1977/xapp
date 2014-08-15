package net.sf.xapp.net.common.util.random;

import java.util.Random;

public class RandomEntropySource implements EntropySource
{
    private final Random random = new Random();
    @Override
    public int nextInt(int n)
    {
        return random.nextInt(n);
    }
}
