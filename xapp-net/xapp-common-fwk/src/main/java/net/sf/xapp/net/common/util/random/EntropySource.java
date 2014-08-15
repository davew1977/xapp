package net.sf.xapp.net.common.util.random;

public interface EntropySource
{
    /**
     * @param n must be larger than 0.
     * @return an integer in the range [0..n)
     * @throws IllegalArgumentException if n <= 0.
     */
    int nextInt(int n);
}
