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
package net.sf.xapp.application.utils.codegen;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public abstract class Field implements DocContext
{
    public String m_modifier = "private";
    public List<String> m_docLines = new ArrayList<String>();
    public List<String> annotations = new ArrayList<String>();
    public String m_type;
    public String m_name;
    public String m_prefix;
    public String m_defaultValue;

    public boolean m_static;
    public boolean m_final;
    public boolean m_transient;

    public void addDocLine(String line)
    {
        m_docLines.add(line);

    }

    public void addAnnotation(String line, String... args)
    {
        annotations.add(format(line, (Object[]) args));

    }

    abstract String generateDeclaration();
}
