/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.util;

import net.sf.xapp.net.common.framework.StringBuildable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StringUtils
{
    static Pattern STARTS_WITH_DELIM = Pattern.compile("^[\\[\\{].*", Pattern.DOTALL);
    public static final String[] NUMBERS = {"Zero","One", "Two","Three","Four","Five","Six","Seven","Eight","Nine","Ten"};
    private static final int MINUTE = 1000 * 60;
    private static final int HOUR = MINUTE * 60;
    private static final int DAY = HOUR * 24;
    private static final int WEEK = DAY * 7;
    private static DecimalFormat formatter = new DecimalFormat("00");
    private static DecimalFormat hourFormatter = new DecimalFormat("#0");

    public static void writePrimitiveList(StringBuilder sb, Collection<?> list)
    {
        sb.append('[');
        int i = 0;
        for (Object o : list)
        {
            if (o instanceof String)
            {
                sb.append(escapeSpecialChars((String) o));
            }
            else
            {
                sb.append(o.toString());
            }
            if (i++ < list.size() - 1) sb.append(',');
        }
        sb.append(']');
    }

    public static void writeObjList(StringBuilder sb, Collection<? extends StringBuildable> list)
    {
        sb.append('[');
        int i = 0;
        for (StringBuildable s : list)
        {
            s.writeString(sb);
            if (i++ < list.size() - 1) sb.append(',');
        }
        sb.append(']');
    }

    //    {ass,dsd,{5,6,7,fgdfg},[{RED,3},{RED,5}],yyy}

    public static List<Object> parse(String data)
    {
        //strip outer curlies
        data = data.trim();
        data = STARTS_WITH_DELIM.matcher(data).matches() ? data.substring(1, data.length() - 1) : data;
        ArrayList<Object> list = new ArrayList<Object>();
        while (!data.equals(""))
        {
            String[] s = data.split(",", 2);
            String head = s[0].trim();
            Matcher matcher = STARTS_WITH_DELIM.matcher(head);
            if (matcher.matches())
            {
                //scan for closing }
                s = splitClause(data, head.charAt(0));
                list.add(parse(s[0]));
            }
            else
            {
                list.add(head);
            }
            data = s.length == 2 ? s[1].trim() : "";
        }
        return list;
    }

    public static String serialize(List<? extends Object> data)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        {
            for (int i = 0; i < data.size(); i++)
            {
                Object o = data.get(i);
                if (o instanceof List)
                {
                    sb.append(serialize((List<Object>) o));
                }
                else if (o instanceof String)
                {
                    sb.append(escapeSpecialChars((String) o));
                }
                else
                {
                    sb.append(o == null ? "" : o);
                }
                sb.append(',');
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * add newlines and tabs to make the data more readable
     *
     * @param lispObj an unformatted lispobj string
     * @return
     */
    public static String format(String lispObj)
    {
        assert !lispObj.contains("\n") && !lispObj.contains("\t");
        List<Object> list = parse(lispObj);
        StringBuilder sb = new StringBuilder();
        formatAppend(sb, list, "");
        return sb.toString();
    }

    public static List<Object> parseFormatted(String formattedLispObj)
    {
        return parse(unformat(formattedLispObj));
    }

    public static String unformat(String formattedLispObj)
    {
        return formattedLispObj.replace("\n", "").replace("\t", "");
    }

    private static void formatAppend(StringBuilder sb, List list, String indent)
    {
        if (depth(list) <= 3)
        {
            sb.append(serialize(list));
        }
        else
        {
            sb.append("[\n");
            for (int i = 0; i < list.size(); i++)
            {
                Object item = list.get(i);
                sb.append(indent + "\t");
                if (item instanceof String)
                {
                    sb.append(item);
                }
                else
                {
                    formatAppend(sb, (List) item, indent + "\t");
                }
                sb.append(",");
                /*if(i<list.size()-1)
                {
                    sb.append(",");
                }*/
                sb.append("\n");
            }
            sb.append(indent).append("]");
        }
    }

    public static int depth(List items)
    {
        int maxDepth = 1;
        for (Object item : items)
        {
            if (item instanceof List)
            {
                maxDepth = Math.max(maxDepth, depth((List) item) + 1);
            }
        }
        return maxDepth;
    }

    private static String[] splitClause(String data, char delim)
    {
        char OPENING = delim;
        char CLOSING = delim == '[' ? ']' : '}';
        int nesting = 0;
        int index = -1;
        for (int i = 1; i < data.length(); i++) //skip first opening
        {
            char c = data.charAt(i);
            if (c == OPENING) nesting++;
            else if (c == CLOSING && nesting == 0)
            {
                index = i + 1;
                break;
            }
            else if (c == CLOSING && nesting > 0) nesting--;
        }
        if (index == -1) throw new RuntimeException("bad nesting in " + data);
        String tail = data.substring(index);
        if (tail.length() > 0)
        {
            if (tail.charAt(0) != ',') throw new RuntimeException("illegal char at char " + index + " in " + data);
            tail = tail.substring(1); //lop of preceding comma
        }
        return new String[]{data.substring(0, index), tail};
    }

    public static String hyphenatedToCamelCase(String str)
    {
        String[] chunks = str.split("-");
        String result = decapitaliseFirst(chunks[0]);
        for (int i = 1; i < chunks.length; i++)
        {
            String chunk = chunks[i];
            result += capitalizeFirst(chunk);
        }
        return result;
    }

    public static String decapitaliseFirst(String name)
    {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    public static String capitalizeFirst(String s)
    {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String escapeSpecialChars(String str)
    {
        return str.replace(",", "&comma;").replace("[", "&lsqb;").replace("]", "&rsqb;").replace("\n", "&nl;");
    }

    public static String unescapeSpecialChars(String str)
    {
        return str.replace("&comma;", ",").replace("&lsqb;", "[").replace("&rsqb;", "]").replace("&nl;", "\n");
    }

    public static String formatTime(long time)
    {
        long weeks = time / WEEK;
        long days = (time % WEEK) / DAY;
        long hours = (time % DAY) / HOUR;
        long minutes = (time % HOUR) / MINUTE;
        long seconds = (time % MINUTE) / 1000;

        String h = hourFormatter.format(hours);
        String m = formatter.format(minutes);
        String s = formatter.format(seconds);

        String w = weeks > 0 ? weeks + " week" + (weeks == 1 ? "" : "s") + ", " : "";
        String d = days > 0 ? days + " day" + (days == 1 ? "" : "s") + ", " : "";
        return w + d + h + ":" + m + ":" + s;

    }

    public static String makeReadable(String name)
    {
        name = name.toLowerCase();
        String[] chunks = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (String chunk : chunks)
        {
            sb.append(capitalizeFirst(chunk)).append(' ');
        }
        return sb.substring(0, sb.length()-1);
    }

    public static String number(int i)
    {
        return i<0 || i>10 ? "" + i : NUMBERS[i];
    }

    public static String ordinal(int n)
    {
        return n + (n==1 ? "st" : n==2 ? "nd" : n==3 ? "rd" : "th");
    }

    public static String upperToLower(String s)
    {
        return s.toLowerCase().replace('_', ' ');
    }
}