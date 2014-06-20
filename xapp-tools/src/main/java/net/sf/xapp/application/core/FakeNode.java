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
package net.sf.xapp.application.core;

public class FakeNode extends NodeImpl
{
    private Object m_obj;

    public FakeNode(Object obj)
    {
        super(null, null, null, null);
        m_obj = obj;
    }

    @Override
    public Object wrappedObject()
    {
        return m_obj;
    }

    @Override
    public boolean isReference()
    {
        return false;
    }

    @Override
    public ListNodeContext getListNodeContext()
    {
        return null;
    }
}
