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

import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.marshalling.stringserializers.EnumListSerializer;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import javax.swing.*;
import java.util.*;

public class EnumListPropertyWidget<T extends Enum> extends AbstractPropertyWidget<Collection<T>>
{
    private Class enumClass;
    private JList listComp;
    private JScrollPane jsp;
    private boolean list; //or set

    public EnumListPropertyWidget(Class<T> enumClass, boolean list)
    {
        this.enumClass = enumClass;
        listComp = new JList(EnumListSerializer.getEnumValues(enumClass));
    }

    @Override
    public JComponent getComponent()
    {
        if (jsp == null)
        {
            jsp = new JScrollPane(listComp);
        }
        return jsp;
        
    }

    @Override
    public Collection<T> getValue()
    {
        Collection c = list ? new ArrayList() : new LinkedHashSet();
        c.addAll(listComp.getSelectedValuesList());
        return c;
    }

    @Override
    public void setValue(Collection<T> values, ObjectMeta target)
    {
        List<Enum> enumValues = Arrays.asList(EnumListSerializer.getEnumValues(enumClass));
        if (values!=null)
        {
            int[] indexes = new int[values.size()];
            int i=0;
            for (T value : values) {
               indexes[i++] = enumValues.indexOf(value);
            }

            listComp.setSelectedIndices(indexes);
        }
    }

    @Override
    public void setEditable(boolean editable)
    {
        listComp.setEnabled(true);
    }

    private enum Color{red,blue,green}

    public static void main(String[] args)
    {
        EnumListPropertyWidget e = new EnumListPropertyWidget(Color.class, true);
        e.init(null);
        e.setValue(Arrays.asList(Color.red,Color.blue), null);
        SwingUtils.showInFrame(e.getComponent());
        Collection<? extends Enum> value = e.getValue();
        System.out.println(value);
    }
}
