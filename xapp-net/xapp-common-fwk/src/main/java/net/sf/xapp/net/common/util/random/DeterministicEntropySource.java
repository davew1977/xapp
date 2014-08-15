package net.sf.xapp.net.common.util.random;

import java.util.ArrayList;
import java.util.List;

public class DeterministicEntropySource implements EntropySource
{
    List<Integer> data;
    int pointer;

    public DeterministicEntropySource(int... data)
    {
        this.data = new ArrayList<Integer>();
        for (int i : data)
        {
            add(i);
        }
    }

    public void add(int value)
    {
        data.add(value);
    }

    @Override
    public int nextInt(int n)
    {
        return data.get(pointer++ % data.size());
    }

    public void reset()
    {
        pointer = 0;
    }
}
