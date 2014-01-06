/*
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 *
 * The contents of this file may be used under the terms of the GNU Lesser
 * General Public License Version 2.1 or later.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */
package net.sf.xapp.utils;

import java.util.*;

import static java.lang.Character.*;
import static java.lang.Character.isLowerCase;

public class StringUtils
{
    public static String decapitaliseFirst(String name)
    {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }


    public static String leaf(String path, String pathSeparator)
    {
        int lastDot = path.lastIndexOf(pathSeparator);
        return path.substring(lastDot != -1 ? lastDot + 1 : 0).trim();
    }

    public static String nodePath(String path, String pathSeparator)
    {
        return removeLastToken(path, pathSeparator);
    }

    public static String breakText(String text, String join, int maxLineLength)
    {
        String result = "";
        text = text.trim();
        while (text.length() > maxLineLength)
        {
            boolean found = false;
            for (int i = maxLineLength; i >= 0; i--)
            {
                if (text.charAt(i) == ' ')
                {
                    result += text.substring(0, i) + join;
                    text = text.substring(i + 1);
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                result += text.substring(0, maxLineLength) + join;
                text = text.substring(maxLineLength);
            }

        }
        result += text;

        return result;
    }

    public static String trimRoot(String path, String pathSeparator)
    {
        int i = path.indexOf(pathSeparator);
        return i == -1 ? "" : path.substring(i + 1);
    }

    public static String insert(String src, int offs, String insert)
    {
        return src.substring(0, offs) + insert + src.substring(offs);
    }

    public static String remove(String src, int offs, int len)
    {
        return src.substring(0, offs) + src.substring(offs + len);
    }

    public static List<String> convertToStringList(String str)
    {
        if (str == null || str.equals("")) return new ArrayList<String>();
        String[] strs = str.split(",");
        List<String> ss = new ArrayList<String>();
        Collections.addAll(ss, strs);
        return ss;
    }

    public static Set<String> convertToStringSet(String str)
    {
        if (str == null || str.equals("")) return new LinkedHashSet<String>();
        String[] strs = str.split(",");
        Set<String> ss = new LinkedHashSet<String>();
        Collections.addAll(ss, strs);
        return ss;
    }

    public static Collection<String> appendToCollection(Collection<String> col, String str){
        if(str!=null && !str.isEmpty()) {
            Collections.addAll(col, str.split(","));
        }
        return col;
    }

    public static String convertToString(Collection<?> ss)
    {
        StringBuilder sb = new StringBuilder();
        for (Object i : ss)
        {
            sb.append(i).append(',');
        }
        return sb.toString();
    }

    public static void main(String[] args)
    {
        String path = nodePath("hello.this.is.the.best", ".");
        String lastPathElement = leaf("hello.this.is.the.best", ".");
        System.out.println(path);
        System.out.println(lastPathElement);
        System.out.println(leaf("sjdhsjd", "."));
        System.out.println(nodePath("sjdhsjd", "."));
        System.out.println(trimRoot("asa.s.ddfdf.d", "."));
    }

    public static String capitalizeFirst(String s)
    {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }


    public Tokenizer createTokenizer(String str)
    {
        return createTokenizer(str, ",");
    }

    private Tokenizer createTokenizer(String str, String delimeter)
    {
        return new Tokenizer(str, delimeter);
    }

    /**
     * @param data
     * @param delimeter
     * @return
     */
    public static Map<String, String> parsePropertyString(String data, String delimeter)
    {
        Map<String, String> propMatchMap = null;
        if (data != null && !data.equals(""))
        {
            propMatchMap = new HashMap<String, String>();
            String[] nameValuePairs = data.split(delimeter);
            for (String nameValuePair : nameValuePairs)
            {
                String[] args = nameValuePair.split("=");
                propMatchMap.put(args[0].toLowerCase(), args[1].toLowerCase());
            }
        }
        return propMatchMap;

    }

    public static String toAsciiString(String s)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if (c > 127)
            {
                sb.append(toUnicodeStr(c));
            }
            else
            {
                sb.append((char) c);
            }
        }
        return sb.toString();
    }

    public static String toUnicodeStr(int i)
    {
        char c1 = Integer.toHexString((i >> 12) & 0x0000000F).charAt(0);
        char c2 = Integer.toHexString((i >> 8) & 0x0000000F).charAt(0);
        char c3 = Integer.toHexString((i >> 4) & 0x0000000F).charAt(0);
        char c4 = Integer.toHexString((i) & 0x0000000F).charAt(0);
        return "\\u" + c1 + c2 + c3 + c4;
    }

    public static String toCamelCase(String s)
    {
        String result = "";
        String[] args = s.split("\\s+");
        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if (i > 0)
            {
                arg = capitalizeFirst(arg);
            }
            result += arg;
        }
        return result;
    }


    public static String fromCamelCase(String str)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if (i == 0)
            {
                sb.append(c);
            }
            else if (i > 0)
            {
                char previous = str.charAt(i - 1);
                if (i == str.length() - 1) //if last char
                {
                    if (isLowerCase(previous) && isUpperCase(c))
                    {
                        sb.append('_');
                    }
                }
                else
                {
                    char next = str.charAt(i + 1);
                    if (isUpperCase(c) && (isLowerCase(next) || isLowerCase(previous)))
                    {
                        sb.append('_');
                    }
                }

                sb.append(c);
            }
        }
        return sb.toString().toLowerCase();
    }

    public static String camelToUpper(String str)
    {
        return fromCamelCase(str).toUpperCase();
    }

    public static String xmlRootTag(String xml)
    {
        String[] args = xml.split("\\s|/|>", 2);
        return args[0].substring(1);
    }

    public static boolean isNullOrEmpty(String s)
    {
        return s == null || s.equals("");
    }

    public static String depluralise(String name)
    {
        //default: shave off an "s"
        if (name.charAt(name.length() - 1) == 's')
        {
            return name.substring(0, name.length() - 1);
        }
        else
        {
            return name;
        }
    }

    public static String removeLastToken(String str, String delim)
    {
        int i = str.lastIndexOf(delim);
        return i == -1 ? "" : str.substring(0, i);
    }

    public static String removeTokenAt(String str, String delim, int index)
    {
        String[] args = str.split(delim);
        index = index<0 ? args.length + index : index;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if(i!=index)
            {
                sb.append(arg).append(",");
            }
        }
        String result = sb.toString();
        return result.endsWith(delim) ? removeLastToken(result, delim): result;
    }

    public static String stripIfStartsWith(String str, String s)
    {
        return str.startsWith(s) ? str.substring(s.length()) : str;
    }

    /**
     * pads a string with spaces
     */
    public static String pad(String pVal, boolean pNumber) {
        return pad(pVal, pNumber, 18);
    }

    /**
     * pads a string with spaces
     */
    public static String pad(String pVal, boolean pNumber, int pMaxWidth) {
        StringBuilder tPad = new StringBuilder();
        for (int i = pVal.length(); i < pMaxWidth; i++) {
            tPad.append(" ");
        }
        return (pNumber ? tPad.toString() : "") + pVal + (pNumber ? "" : tPad.toString());
    }

    public static String pad(String pVal) {
        return pad(pVal, false);
    }

    public static String pad(Long pVal) {
        return pad(pVal + "", true);
    }
    public static String line(int length, char c) {
        StringBuilder sb = new StringBuilder();
        for (int i=0 ; i< length; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
