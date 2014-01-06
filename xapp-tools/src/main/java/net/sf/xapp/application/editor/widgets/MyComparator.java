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

import java.util.Comparator;

public class MyComparator implements Comparator
{
    public int compare(Object o1, Object o)
    {
        //sort on class name first
        if (!o.getClass().equals(o1.getClass()))
        {
            return o.getClass().getSimpleName().compareTo(o1.getClass().getSimpleName());
        }
        else
        {
            //sort by string rep
            String s1 = o.toString();
            String s2 = o1.toString();
            return s1.compareTo(s2);
        }
    }
}
