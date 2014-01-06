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

import net.sf.xapp.objectmodelling.core.Property;

import javax.swing.*;

/**
 * This interface encapsulates a graphical editor for a property. DJWastor framework
 * includes editors for the simple and primitive types in Java (String, enums, ints, booleans etc)
 * as well as for properties that refer to existing instances ({@link net.sf.xapp.application.editor.widgets.ReferencePropertyWidget})
 */
public interface PropertyWidget<T>
{
    /**
     * The graphical component must be a {@link JComponent}
     * @return the component containing one or more editable widgets
     */
    JComponent getComponent();

    /**
     * The implementation should construct a value of type T from the data contained
     * in the graphical components or return null. It is important to return null when the
     * editor is cleared rather than say, empty string, or empty list. Violation of this can
     * cause data to be wiped when the multiedit command is used
     * @return the value data contained in the editable component(s)
     */
    T getValue();

    /**
     * The implementation should take the value and represent it graphically in the component.
     * @param value the property value. Can be null
     * @param target the object whose property's value it is
     */
    void setValue(T value, Object target);

    /**
     * Get the property object currently associated with 
     * @return
     */
    Property<T> getProperty();

    /**
     * Called by the container before the a new property value is set into the component.
     * @param context
     */
    void init(WidgetContext<T> context);

    /**
     * Implementation should check the validity of the value that would be returned by {@link #getValue()}
     * For example an integer editor could check that the data entered into the textfield can be
     * parsed as an integer
     * @return
     */
    String validate();

    /**
     * The implementation is responsible for disabling input in the component if not editable
     * @param editable
     */
    void setEditable(boolean editable);
}
