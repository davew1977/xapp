package net.sf.xapp.net.common.framework;

import junit.framework.TestCase;
import net.sf.xapp.net.common.util.GeneralUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GeneralUtilsTest extends TestCase
{
    public void testDistribute() throws Exception
    {
        Random r = new MyRandom();

        List<String> src = Arrays.asList("andy","bill","colin");
        List<List<String>> result = GeneralUtils.distribute(src, 2, r);
        assertEquals(2, result.size());
        assertEquals("[andy, colin]", result.get(0).toString());
        assertEquals("[bill]", result.get(1).toString());

        src = Arrays.asList("a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z".split(","));
        result = GeneralUtils.distribute(src, 2, r);
        assertEquals(13, result.size());
        assertEquals("[a, n]", result.get(0).toString());
        result = GeneralUtils.distribute(src, 2, r);
        assertEquals(13, result.size());
        result = GeneralUtils.distribute(src, 10, r);
        assertEquals(3, result.size());
        assertEquals("[a, d, g, j, m, p, s, v, y]", result.get(0).toString());
        assertEquals("[b, e, h, k, n, q, t, w, z]", result.get(1).toString());
        assertEquals("[c, f, i, l, o, r, u, x]", result.get(2).toString());
    }

    private static class MyRandom extends Random
    {
        @Override
        public int nextInt(int n)
        {
            return 0;
        }
    }
}
