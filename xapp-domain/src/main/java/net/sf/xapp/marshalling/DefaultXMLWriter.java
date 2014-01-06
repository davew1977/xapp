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
package net.sf.xapp.marshalling;

import net.sf.xapp.marshalling.api.XMLWriter;
import net.sf.xapp.marshalling.namevaluepair.ComparableNameValuePair;
import net.sf.xapp.marshalling.namevaluepair.NameValuePair;
import net.sf.xapp.objectmodelling.core.Property;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

public class DefaultXMLWriter implements XMLWriter
{
    private Writer m_out;
    private int m_depth;
    private boolean m_formatted;

    public DefaultXMLWriter(Writer writer, boolean formatted)
    {
        m_out = writer;
        m_depth = 0;
        m_formatted = formatted;
    }


    /**
     * Simple method for quoting standard XML entities in strings.
     * We need to do this to produce standard conforming XML files.
     *
     * @param string
     * @return
     */
    private String quoteEntities(String string)
    {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < string.length(); i++)
        {
            char c = string.charAt(i);

            switch (c)
            {
            case '&':
                s.append("&amp;");
                break;

            case '<':
                s.append("&lt;");
                break;

            case '>':
                s.append("&gt;");
                break;

            case '"':
                s.append("&quot;");
                break;

            case '\'':
                s.append("&apos;");
                break;

            default:
                s.append(c);
            }

        }

        return s.toString();
    }


    public void writeOpeningTag(String tagName, List<ComparableNameValuePair> attrs, boolean elementsExist) throws IOException
    {
        m_out.write(getWhiteSpace());
        m_out.write("<");
        m_out.write(tagName);
        if (attrs != null && !attrs.isEmpty())
        {
            // Make sure the attributes are sorted in order to get a deterministic result with different Java versions and OS
            Collections.sort(attrs);

            m_out.write(' ');
            for (int i = 0; i < attrs.size(); i++)
            {
                NameValuePair propertyValuePair = attrs.get(i);
                String name = propertyValuePair.getName();
                m_out.write(name.substring(0, 1).toLowerCase());
                m_out.write(name.substring(1));
                m_out.write('=');
                m_out.write('"');
                m_out.write(quoteEntities(propertyValuePair.getValue().toString()));
                m_out.write('"');
                if (i < attrs.size() - 1) m_out.write(' ');
            }
        }
        if (!elementsExist) m_out.write('/');
        m_out.write('>');
        writeNewLine();
        if (elementsExist) m_depth++;
    }

    public void writeClosingTag(String tagName) throws IOException
    {
        m_depth--;
        m_out.write(getWhiteSpace());
        m_out.write("</");
        m_out.write(tagName);
        m_out.write('>');
        writeNewLine();
    }

    public void writeSimpleTag(String tagName, String content, Property property) throws IOException
    {
        boolean cdata = property != null && (property.isFormattedText() || Marshaller.isMultiline(content));
        m_out.write(getWhiteSpace());
        m_out.write("<" + tagName + ">");
        if (cdata) m_out.write("<![CDATA[");
        m_out.write(content);
        if (cdata) m_out.write("]]>");
        m_out.write("</" + tagName + ">");
        writeNewLine();
    }

    public Writer getWriter()
    {
        return m_out;
    }

    public void flush() throws IOException
    {
        m_out.flush();
    }

    public void nullTag(String tagName) throws IOException
    {
        m_out.write(getWhiteSpace());
        m_out.write("<" + tagName + "/>");
        writeNewLine();
    }

    private void writeNewLine() throws IOException
    {
        if (m_formatted)
        {
            m_out.write('\n');
        }
    }

    private String getWhiteSpace()
    {
        if (m_formatted)
        {
            char[] c = new char[m_depth * 2];
            for (int i = 0; i < c.length; i++)
            {
                c[i] = ' ';
            }
            return new String(c);
        }
        else
        {
            return "";
        }
    }
}
