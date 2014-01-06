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
package net.sf.xapp.application.editor.widgets;

import net.sf.xapp.utils.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LiveTemplate
{
    private final String m_template;
    private String m_insertion;
    private TreeMap<Integer, Integer> m_caretSettings;
    private String[] m_subs;
    private String m_content;
    private int m_pointer = 0;
    private int m_initialCaretIndex;
    private List<String> m_paramHelp;

    public LiveTemplate(String template)
    {
        this(template, null);
    }
    public LiveTemplate(String template, List<String> paramHelp)
    {
        m_template = template;
        m_paramHelp = paramHelp ;
    }

    public String getInsertion()
    {
        return m_insertion;
    }

    public LinkedList<Integer> caretIndexes()
    {
        Collection<Integer> ints = m_caretSettings.values();
        return new LinkedList<Integer>(ints);
    }

    public int nextCaretIndex()
    {
        int caret = m_caretSettings.get(m_pointer++);
        return m_initialCaretIndex + caret;
    }

    public String currentParamHelp()
    {
        if(m_paramHelp==null || m_paramHelp.size()<m_pointer)
        {
            return null;
        }
        return m_paramHelp.get(m_pointer-1);
    }

    public boolean hasMore()
    {
        return m_pointer< m_caretSettings.size();
    }

    public void reset(int initialCaretIndex)
    {
        Matcher m = Pattern.compile("\\$\\d+").matcher(m_template);
        m_caretSettings = new  TreeMap<Integer, Integer>();
        String insertion = "";
        int lastStart = 0;
        while(m.find())
        {
            insertion+= m_template.substring(lastStart, m.start());
            lastStart = m.end();
            int id = Integer.parseInt(String.valueOf(m.group().substring(1)));
            m_caretSettings.put(id, insertion.length());
        }
        insertion+= m_template.substring(lastStart, m_template.length());
        m_insertion = insertion;
        if (m_caretSettings.isEmpty())
        {
            m_caretSettings.put(m_caretSettings.size(),insertion.length());
        }
        m_initialCaretIndex = initialCaretIndex;
        m_pointer = 0;
        m_subs = new String[m_caretSettings.size()];
        for (int i = 0; i < m_subs.length; i++)
        {
            m_subs[i] = "";

        }
        m_content = m_insertion;
    }

    public String getContent()
    {
        return m_content;
    }

    /**
     *
     * @param offs
     * @param newText
     * @return false if the template must be cancelled
     */
    public boolean textInserted(int offs, String newText)
    {
        int internalIndex = offs-m_initialCaretIndex;
        if(internalIndex<0 || internalIndex>m_content.length())
        {
            return false;
        }
        int subIndex = internalIndex - m_caretSettings.get(m_pointer-1);
        String sub = m_subs[m_pointer - 1];
        if(subIndex<0 || subIndex> sub.length())
        {
            return false;
        }
        m_content = StringUtils.insert(m_content, internalIndex, newText);
        //update pointers
        for (Map.Entry<Integer, Integer> e : m_caretSettings.entrySet())
        {
            if(e.getValue()>internalIndex)
            {
                m_caretSettings.put(e.getKey(), e.getValue() + newText.length());
            }
        }
        m_subs[m_pointer-1] = StringUtils.insert(sub, subIndex, newText);
        return true;
    }

    public boolean textRemoved(int offs, int length)
    {
        int internalIndex = offs-m_initialCaretIndex;
        if(internalIndex<0 || internalIndex>m_content.length())
        {
            return false;
        }
        int subIndex = internalIndex - m_caretSettings.get(m_pointer-1);
        String sub = m_subs[m_pointer - 1];
        if(subIndex<0 || length > sub.length() - subIndex)
        {
            return false;
        }
        m_content = StringUtils.remove(m_content, internalIndex, length);
        //update pointers
        for (Map.Entry<Integer, Integer> e : m_caretSettings.entrySet())
        {
            if(e.getValue()>internalIndex)
            {
                m_caretSettings.put(e.getKey(), e.getValue() - length);
            }
        }
        m_subs[m_pointer-1] = StringUtils.remove(sub, subIndex, length);
        return true;
    }

    /**
     *
     * @return 2 int array, startindex of current insert and the length
     */
    public int[] indexRange()
    {
        return new int[]{m_initialCaretIndex + m_caretSettings.get(m_pointer-1), m_subs[m_pointer-1].length()};
    }

    public String[] getSubs()
    {
        return m_subs;
    }

    public Map<Integer,Integer> getCaretSettings()
    {
        return m_caretSettings;
    }
}
