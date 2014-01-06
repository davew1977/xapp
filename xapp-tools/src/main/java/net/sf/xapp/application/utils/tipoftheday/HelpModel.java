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
package net.sf.xapp.application.utils.tipoftheday;

import net.sf.xapp.annotations.objectmodelling.ListType;

import java.util.List;

public class HelpModel
{
    private List<Tip> m_tipsOfTheDay;

    public HelpModel()
    {
    }

    @ListType(Tip.class)
    public List<Tip> getTipsOfTheDay()
    {
        return m_tipsOfTheDay;
    }

    public void setTipsOfTheDay(List<Tip> tipsOfTheDay)
    {
        m_tipsOfTheDay = tipsOfTheDay;
    }
}
