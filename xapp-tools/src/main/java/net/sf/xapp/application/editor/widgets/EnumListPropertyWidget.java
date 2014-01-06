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

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnumListPropertyWidget<T extends Enum> extends AbstractPropertyWidget<List<T>>
{
    private Class enumClass;
    private JList listComp;
    private JScrollPane jsp;

    public EnumListPropertyWidget(Class<T> enumClass)
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
    public List<T> getValue()
    {
        return new ArrayList(Arrays.asList(listComp.getSelectedValues()));
    }

    @Override
    public void setValue(List<T> values, Object target)
    {
        List<Enum> enumValues = Arrays.asList(EnumListSerializer.getEnumValues(enumClass));
        if (values!=null)
        {
            int[] indexes = new int[values.size()];
            for (int i = 0; i < values.size(); i++)
            {
                Enum anEnum = values.get(i);
                indexes[i] = enumValues.indexOf(anEnum);
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
        EnumListPropertyWidget e = new EnumListPropertyWidget(Color.class);
        e.init(null);
        e.setValue(Arrays.asList(Color.red,Color.blue), null);
        SwingUtils.showInFrame(e.getComponent());
        List<? extends Enum> value = e.getValue();
        System.out.println(value);
    }
}
