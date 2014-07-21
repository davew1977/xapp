/*
 *
 * Date: 2010-nov-19
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.util;

public class MathUtils
{
    public static int divideAndRoundUp(int numerator, int denominator)
    {
        return (numerator + denominator - 1) / denominator;
    }

    public static boolean isWhole(double d)
    {
        return d - Math.floor(d)==0;
    }

    public static boolean isOdd(int i)
    {
        return i%2==1;
    }

    /**
     * shaves the remainder off the number
     * @param n
     * @param mod
     * @return
     */
    public static int floor(int n, int divider)
    {
        return n - n % divider;
    }

    /**
     *
     * @param v
     * @return if v==3 then 5 is returned
     */
    public static int nextOdd(double v)
    {
        if(isWhole(v)&&isOdd((int)v))
        {
            return (int) (v + 2);
        }
        int nextWhole = (int) Math.ceil(v);
        return nextWhole + (isOdd(nextWhole) ? 0:1);
    }

    /**
     *
     * @param v
     * @return if v==3 then 3 is returned
     */
    public static int previousOdd(double v)
    {
        int prevWhole = (int) Math.floor(v);
        return prevWhole - (isOdd(prevWhole) ? 0:1);
    }
}
