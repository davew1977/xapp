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
package net.sf.xapp.application.utils.html;

import static net.sf.xapp.application.utils.html.DocState.*;
import net.sf.xapp.marshalling.stringserializers.IntegerArraySerializer;
import net.sf.xapp.utils.XappException;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTMLImpl implements HTML
{
    private StringBuilder m_sb;
    private DocState m_state;
    private Object m_bean;
    private Map<String, Object> m_beanMeta;
    private boolean m_bold;
    private boolean m_italic;
    private boolean m_underline;
    private Color m_color;
    private Integer m_size;
    private String m_font;
    private int m_border = 0;
    private String m_tdAlign;
    private String m_tdHeight;
    private String m_tdWidth;
    private String m_tdColor;
    private String m_tdColSpan;
    private String m_style;

    public HTMLImpl()
    {
        m_sb = new StringBuilder();
        m_state = P;
    }

    public HTML border(int i)
    {
        m_border = i;
        return this;
    }

    public void setStyle(String style)
    {
        m_style = style;
    }

    public HTML form(String action)
    {
        m_sb.append("<form action=\"").append(action).append("\">");
        return this;
    }

    public HTML endForm()
    {
        m_sb.append("</form>");
        return this;
    }

    public HTML checkbox(String id, String label)
    {
        return checkbox(id, label, false);
    }

    public HTML checkbox(String id, String label, boolean defaultValue)
    {
        m_sb.append("<input type=\"checkbox\" ").append(defaultValue ? "checked " : "").append("name=\"").append(id).append("\">");
        m_sb.append(label).append("</input>");
        return this;
    }

    public HTML textfield(String id, String label)
    {
        return textfield(id,label,"");
    }
    public HTML textfield(String id, String label, String value)
    {
        String valueAttr = value!=null ? " value=\"" + value + "\"" : "";
        m_sb.append("<input type=\"text\" name=\"").append(id).append("\"").append(valueAttr).append(">");
        m_sb.append(label).append("</input>");
        return this;
    }
    public HTML combo(String id, List<String> options, String defaultOption)
    {
        m_sb.append("<select name=\""+id+"\">");

        for (String option : options)
        {
            m_sb.append("<option value=\"").append(option).append("\"").append(defaultOption.equals(option) ? " SELECTED " : "").append(">").append(option).append("</option>");
        }
        m_sb.append("</select>");
        return this;
    }

    public HTML submit(String id, String label)
    {
        m_sb.append("<input type=\"submit\" name=\"").append(id).append("\" value=\"").append(label).append("\"/>");
        return this;
    }

    public HTML p(String p, String... args)
    {
        if(p==null)return this;
        setState(P);
        m_sb.append("<p>");
        appendWithStyle(p);
        m_sb.append("</p>");
        return this;
    }

    private void setState(DocState newState)
    {
        if(m_state==TD)
        {
            m_sb.append("</td>");
            m_state = TR;
        }
        if(m_state==TR && newState!=TD)
        {
            m_sb.append("</tr>");
            m_state = TABLE;
        }
        if(m_state == TABLE && newState!=TR)
        {
            m_sb.append("</table>");
        }
        m_state = newState;
    }

    public HTML bean(Object bean)
    {
        m_bean = bean;
        return this;
    }

    public HTML table()
    {
        setState(TABLE);
        m_sb.append("<table border=\"").append(m_border).append("\">");
        return this;
    }

    public HTML table(String headers)
    {
        return table(null, headers);
    }

    public HTML table(String widths, String headers)
    {
        return table(widths, headers, null);
    }

    public HTML table(String widths, String headers, String colors)
    {
        setState(TABLE);
        m_sb.append("<table border=\"").append(m_border).append("\">");
        m_sb.append("<tr>");
        int[] w = IntegerArraySerializer.doRead(widths);
        String[] h = headers.split(",");
        String[] c = colors!=null ? colors.split(",") : null;
        for (int i = 0; i < h.length; i++)
        {
            String header = h[i];
            tdWidth(widths!=null ? w[i] : null);
            tdBgColor(colors!=null ? c[i]:null);
            doTDOpeningTag();
            appendWithStyle(header);
            doTDClosingTag();
        }
        m_sb.append("</tr>");
        m_state = TABLE;
        return this;
    }

    public HTML tr(String... tds)
    {
        setState(TR);
        m_sb.append("<tr>");
        for (String td : tds)
        {
            doTDOpeningTag();
            appendWithStyle(td);
            doTDClosingTag();
        }
        return this;
    }

    public HTML color(Color color)
    {
        m_color = color;
        return this;
    }

    public HTML size(Integer size)
    {
        m_size = size;
        return this;
    }

    public HTML font(String name)
    {
        m_font = name;
        return this;
    }

    public HTML b()
    {
        m_bold = !m_bold;
        return this;
    }

    public HTML b(boolean bold)
    {
        m_bold = bold;
        return this;
    }

    public HTML i()
    {
        m_italic = !m_italic;
        return this;
    }

    private void appendWithStyle(String s)
    {
        if(s==null)s="";
        if(m_bold) m_sb.append("<b>");
        if(m_italic) m_sb.append("<i>");
        if(m_underline) m_sb.append("<u>");
        if(m_size!=null || m_color!=null || m_font !=null)
        {
            String color = m_color!=null ? " color=\"#"+Integer.toHexString(m_color.getRGB() & 0x00FFFFFF) + "\"" : "";
            String size = m_size!=null ? " size=\""+m_size  + "\"": "";
            String face = m_font!=null ? " face=\""+m_font  + "\"": "";
            m_sb.append("<font").append(color).append(size).append(face).append(">");
        }
        m_sb.append(s.replace("\n", "<br/>"));
        if(m_size!=null || m_color!=null)
        {
            m_sb.append("</font>");
        }
        if(m_underline) m_sb.append("</u>");
        if(m_italic) m_sb.append("</i>");
        if(m_bold) m_sb.append("</b>");

    }

    public HTML anchor(String name)
    {
        m_sb.append("<a name=\"").append(name).append("\"/>");
        return this;
    }

    public HTML link(String url, String text)
    {
        m_sb.append("<a href=\"").append(url).append("\">");
        appendWithStyle(text);
        append("</a>");
        return this;
    }

    public HTML h(int size, String p, String... args)
    {
        m_sb.append("<h"+size+">");
        appendWithStyle(p);
        m_sb.append("</h"+size+">");
        return this;
    }

    public HTML td(String td, String... args)
    {
        setState(TD);
        doTDOpeningTag();
        appendWithStyle(td);
        doTDClosingTag();
        return this;
    }

    public HTML td(Color bgColor)
    {
        setState(TD);
        tdBgColor(bgColor);
        doTDOpeningTag();
        tdBgColor((String) null);
        return this;
    }

    private String colorStr(Color bgColor)
    {
        String bg = "#" + Integer.toHexString(bgColor.getRGB() & 0x00FFFFFF);
        return bg;
    }

    public HTML td(String td, Color bgColor)
    {
        setState(TD);
        tdBgColor(bgColor);
        doTDOpeningTag();
        tdBgColor((String) null);
        appendWithStyle(td);
        doTDClosingTag();
        return this;
    }

    private void doTDClosingTag()
    {
        m_sb.append("</td>");
    }

    public HTML td(String td, Integer width, String... args)
    {
        setState(TD);
        tdWidth(width);
        doTDOpeningTag();
        tdWidth(null);
        appendWithStyle(td);
        doTDClosingTag();
        return this;
    }

    public HTML td()
    {
        return td((Integer)null);
    }

    public HTML td(Integer height)
    {
        setState(TD);
        tdHeight(height);
        doTDOpeningTag();
        tdHeight(null);
        return this;
    }

    public HTML tdHeight(Integer height)
    {
        m_tdHeight = height!=null ? String.valueOf(height) : null;
        return this;
    }

    public HTML tdWidth(Integer width)
    {
        m_tdWidth = width!=null ? String.valueOf(width) : null;
        return this;
    }

    public HTMLImpl tdColSpan(Integer i)
    {
        m_tdColSpan = i!=null ? String.valueOf(i) : null;
        return this;
    }

    public HTML tdAlign(String align)
    {
        m_tdAlign = align;
        return this;
    }

    public HTML tdBgColor(String bgColor)
    {
        m_tdColor = bgColor;
        return this;
    }
    public HTML tdBgColor(Color bgColor)
    {
        return tdBgColor(colorStr(bgColor));
    }

    private void doTDOpeningTag()
    {
        String heightStr = m_tdHeight!=null ? " height=\"" + m_tdHeight + "\"":"";
        String widthStr = m_tdWidth!=null ? " width=\"" + m_tdWidth + "\"" :"";
        String alignStr = m_tdAlign!=null ? " align=\"" + m_tdAlign + "\"": "";
        String colorStr = m_tdColor!=null ? " bgColor=\"" + m_tdColor + "\"": "";
        String colSpanStr = m_tdColSpan!=null ? " colspan=\"" + m_tdColSpan+"\"" : "";
        m_sb.append("<td").append(heightStr).append(widthStr).append(alignStr).append(colSpanStr).append(colorStr).append(">");
    }

    public HTML append(String text, String... args)
    {
        appendWithStyle(text);
        return this;
    }

    public String html()
    {
        return resolveVars(m_sb.toString());
    }

    public String htmlDoc()
    {
        return String.format("<html><head><style type=\"text/css\">%s</style></head><body>%s</body></html>", m_style, html());
    }

    public HTML br()
    {
        m_sb.append("<br>");
        return this;

    }

    public HTML endTable()
    {
        setState(P);
        return this;
    }

    private String resolveVars(String s)
    {
        String[] s1 = s.split("\\$\\{|\\}");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < s1.length; i++)
        {
            String chunk = s1[i];
            if (i % 2 == 0)
            {
                result.append(chunk);
            }
            else
            {
                result.append(resolveVar(chunk));
            }
        }
        return result.toString();
    }

    private String resolveVar(String var)
    {
        if(m_bean==null)return "${" + var + "}";
        if (m_beanMeta == null)
        {
            try
            {
                m_beanMeta = new HashMap<String, Object>();
                Class<? extends Object> beanClass = m_bean.getClass();
                while (!beanClass.equals(Object.class))
                {
                    Field[] declaredFields = beanClass.getDeclaredFields();
                    for (Field field : declaredFields)
                    {
                        field.setAccessible(true);
                        String key = field.getName();
                        if(key.startsWith("m_"))key = key.substring(2);
                        if (m_beanMeta.get(key) == null)
                        {
                            m_beanMeta.put(key, field.get(m_bean));
                        }
                    }
                    beanClass = beanClass.getSuperclass();
                }
            }
            catch (IllegalAccessException e)
            {
                throw new XappException(e);
            }
        }
        return String.valueOf(m_beanMeta.get(var));
    }

    private static class Person
    {
        private String m_name;
        private int m_age;

        public Person(String name, int age)
        {
            m_name = name;
            m_age = age;
        }
    }

    public static void main(String[] args)
    {
        Person p = new Person("Frederik Harold Gobson", 34);
        HTMLImpl h = new HTMLImpl();
        h.bean(p);
        String s = h.resolveVars("${name} was a very cool ${age} year old");
        System.out.println(s);
        System.out.println(h.resolveVars("this has no vars"));
        System.out.println(h.resolveVars("this has a false ${var} no vars"));
    }
}
