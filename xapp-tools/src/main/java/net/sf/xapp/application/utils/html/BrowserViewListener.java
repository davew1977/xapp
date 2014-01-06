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
package net.sf.xapp.application.utils.html;

import java.util.Map;

public interface BrowserViewListener
{
    /**
     * listens for html links &lt;a&gt; tag
     *
     * @param link
     */
    void linkPressed(String link);


    /**
     * called later because it can take a reasonable amount of time to render the html
     */
    void htmlRendered();

    /**
     * A submit button has been pressed
     *
     * @param props the form's properties converted to a map for convenience
     */
    void formSubmitted(Map<String, String> props);

    /**
     * handle uncaught exceptions thrown by the implementations of the above methods
     *
     * @param e
     */
    void handleUncaughtException(Throwable e);

    void comboItemChanged(String componentId, String newValue);

    void textFieldChanged(String compId, String text);

    void checkBoxChanged(String compID, boolean selected);

    /**
     * The default implementation in the {@link BrowserView}
     */
    public static class NullBrowserViewListener implements BrowserViewListener
    {

        public void linkPressed(String link)
        {

        }

        public void htmlRendered()
        {

        }

        public void formSubmitted(Map<String, String> props)
        {

        }

        public void handleUncaughtException(Throwable e)
        {

        }

        public void comboItemChanged(String componentId, String newValue)
        {

        }

        public void textFieldChanged(String compId, String text)
        {

        }

        public void checkBoxChanged(String componentId, boolean selected)
        {

        }
    }
}
