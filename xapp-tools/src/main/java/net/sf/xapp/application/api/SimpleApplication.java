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

import net.sf.xapp.objectmodelling.core.ListProperty;
import net.sf.xapp.objectmodelling.core.PropertyChangeTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimpleApplication<T> implements Application<T>
{
    protected ApplicationContainer<T> m_appContainer;

    public void handleUncaughtException(Throwable e)
    {
        e.printStackTrace();
    }

    public SpecialTreeGraphics createSpecialTreeGraphics()
    {
        return null;
    }

    @Override
    public void nodeDoubleClicked(Node selectedNode)
    {

    }

    @Override
    public void nodesSelected(List<Node> nodes, Class commonClass)
    {

    }

    public List<Command> getCommands(Node node)
    {
        return new ArrayList<Command>();
    }

    public List<Command> getCommands(List<Node> nodes, Class commonType)
    {
        return new ArrayList<Command>();
    }

    public boolean nodeSelected(Node node)
    {
        return false;
    }

    public void nodeRemoved(Node node, boolean wasCut)
    {

    }

    public void nodeAdded(Node node)
    {

    }

    public void nodeAboutToBeAdded(ListProperty listProperty, Object parent, Object newChild)
    {

    }

    public void nodeUpdated(Node objectNode, Map<String,PropertyChangeTuple> changes)
    {

    }

    public void nodeMovedUp(Node node)
    {

    }

    public void nodeMovedDown(Node node)
    {

    }

    public void init(ApplicationContainer<T> applicationContainer)
    {
        m_appContainer = applicationContainer;
    }

    public void nodesUpdated(List<PropertyChangeTuple> changes)
    {

    }

    public ApplicationContainer<T> getAppContainer()
    {
        return m_appContainer;
    }

    public T model() {
        return m_appContainer.getGuiContext().getInstance();
    }
}