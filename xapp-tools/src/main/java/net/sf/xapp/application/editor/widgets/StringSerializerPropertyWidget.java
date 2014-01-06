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

import net.sf.xapp.marshalling.api.StringSerializer;
import net.sf.xapp.marshalling.stringserializers.StringListSerializer;

import java.util.List;

public class StringSerializerPropertyWidget extends FreeTextPropertyWidget
{
    private final StringSerializer ss;

    public StringSerializerPropertyWidget(StringSerializer ss)
    {
        this.ss = ss;
    }

    @Override
    public Object getValue()
    {
        String text = getTextArea().getText();
        return ss.read(text);
    }


    @Override
    public void setValue(Object value, Object target)
    {
        if(value!=null)
        {
            getTextArea().setText(ss.write(value));
        }
        else
        {
            getTextArea().setText("");
        }
    }
}
