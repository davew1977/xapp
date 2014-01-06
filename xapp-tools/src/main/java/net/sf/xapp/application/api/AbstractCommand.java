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
package net.sf.xapp.application.api;

/**
 * Base class for commands to reuse. Implements the {@link Command} getters
 */
public abstract class AbstractCommand<T> implements Command<T>
{
    private String m_name;
    private String m_description;
    private String m_keyStroke;

    protected AbstractCommand(String name, String description, String keyStroke)
    {
        m_name = name;
        m_description = description;
        m_keyStroke = keyStroke;
    }

    public String getName()
    {
        return m_name;
    }

    public String getDescription()
    {
        return m_description;
    }

    public String getKeyStroke()
    {
        return m_keyStroke;
    }

    @Override
    public String toString()
    {
        return m_name;
    }
}
