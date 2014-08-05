package net.sf.xapp.net.server.util;

import java.util.List;

/**
 *
 */
public class ListUtils
{
    /**
     * Selects n items from src from the first occurence of fromMatch.
     * If frommatch is null then return the first n items
     * @param src
     * @param fromMatch
     * @param n
     * @param <T>
     * @return
     */
    public static <T> List<T> pick(List<T> src, T fromMatch, int n)
    {
        int i = src.indexOf(fromMatch);
        int startIndex = i!=-1 ? i + 1 : 0;
        int endIndex = Math.min(src.size(), startIndex + n);
        return src.subList(startIndex, endIndex);
    }
}
