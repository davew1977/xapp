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

import javax.swing.*;

/**
 * Encapsulates an editor for an object. This allows for a level of customisation over and above
 * what {@link net.sf.xapp.application.api.PropertyWidget} provides because
 * it allows the application to implement a special editor for complex objects where the default
 * gui is not rich enough.
 */
public interface ObjectWidget<T>
{
    /**
     * Get the Swing component encapsulating all the Editor Graphical interface
     * @return
     */
    JComponent getComponent();

    /**
     * Instruct the editor to set its displayed values into the object
     * @param instance
     */
    void setToObject(T instance);

    /**
     * Instruct the editor to display the values stored in the object
     * @param instance
     */
    void getFromObject(T instance);

    /**
     * Called when the framework creates the ObjectWidget
     */
    void init();
}
