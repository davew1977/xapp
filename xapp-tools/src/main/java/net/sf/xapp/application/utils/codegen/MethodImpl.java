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

import static net.sf.xapp.application.utils.codegen.AbstractCodeFile.*;
import static java.lang.String.format;

public abstract class MethodImpl implements Method
{
    public List<String> m_annotations = new ArrayList<String>();
    public String m_name;
    public String m_returnType;
    public String m_modifier = "public";
    public List<String> m_docLines = new ArrayList<String>();
    public List<String> m_params = new ArrayList<String>();
    public List<String> m_codeLines = new ArrayList<String>();
    private String m_currentIndent = JavaFile.TAB;
    public boolean m_static;
    public boolean m_final;
    public String m_expections;
    public boolean m_constructor;
    public boolean m_abstract;
    public Object attachment;
    private boolean autoSemiColon = true;

    public void lineAtStart(String code, Object... args)
    {
        line(0, code, args);
    }

    @Override
    public void setAutoSemiColon(boolean autoSemi) {
        this.autoSemiColon = autoSemi;
    }

    public void line(String code)
    {
        line(m_codeLines.size(), code);
    }

    public void line(String code, Object... args)
    {
        line(m_codeLines.size(), code, args);
    }

    public void overwriteLine(int lineIndex, String code, Object... args)
    {
        m_codeLines.set(lineIndex, createLine(code, args));
    }

    public String getName()
    {
        return m_name;
    }

    public boolean isConstructor()
    {
        return m_constructor;
    }

    private void line(int index, String code, Object... args)
    {
        m_codeLines.add(index, createLine(code, args));
    }

    private String createLine(String code, Object... args)
    {
        return code.isEmpty() ? "\n" : m_currentIndent + String.format(code, args) + (autoSemiColon ? ";\n" : "\n");
    }

    public void endBlock()
    {
        endBlock("");
    }

    public void endBlock(String code)
    {
        m_currentIndent = m_currentIndent.substring(JavaFile.TAB.length());
        m_codeLines.add(m_currentIndent + "}" + code + "\n");
    }

    public void startBlock(String code, Object... args)
    {
        m_codeLines.add(m_currentIndent + String.format(code, args) + startBlockChars(m_currentIndent));
        m_currentIndent += JavaFile.TAB;
    }

    public boolean isGetter()
    {
        return (m_name.startsWith("get") || m_name.startsWith("is")) && m_params.isEmpty() && !m_returnType.equals("void");
    }

    public boolean isSetter()
    {
        return m_name.startsWith("set") && m_params.size() == 1 && m_returnType.equals("void");
    }

    public abstract String generateSignature();

    protected String paramList(boolean includeTypeDeclarations)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m_params.size(); i++)
        {
            String param = m_params.get(i);
            if(includeTypeDeclarations)
            {
                sb.append(param);
            }
            else
            {
                sb.append(param.substring(param.indexOf(' ')).trim());
            }
            if (i < m_params.size() - 1)
            {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public void addDocLine(String line)
    {
        m_docLines.add(line);
    }

    public void addAnnotation(String line, String... args)
    {
        m_annotations.add(format(line, (Object[]) args));
    }

    public String generateInvocation()
    {
        return m_name + "(" + generateParamStr(false) + ")";
    }

    public String generateParamStr(boolean includeTypeDeclarations)
    {
        return paramList(includeTypeDeclarations);
    }

    @Override
    public List<String> getParams()
    {
        return m_params;
    }

    public boolean isVoid()
    {
        return m_returnType.equals("void");
    }

    @Override
    public Object attachment()
    {
        return attachment;
    }

    public String getReturnType()
    {
        return m_returnType;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodImpl method = (MethodImpl) o;

        if (m_name != null ? !m_name.equals(method.m_name) : method.m_name != null) return false;
        if (m_params != null ? !m_params.equals(method.m_params) : method.m_params != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = m_name != null ? m_name.hashCode() : 0;
        result = 31 * result + (m_params != null ? m_params.hashCode() : 0);
        return result;
    }
}
