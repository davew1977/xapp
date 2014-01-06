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

import java.awt.*;

public interface HTML
{
    HTML bean(Object bean);
    HTML color(Color color);
    HTML size(Integer size);
    HTML font(String name);
    HTML b();
    HTML b(boolean bold);
    HTML i();
    HTML p(String p, String... args);
    HTML h(int size, String p, String... args);
    HTML table();
    HTML table(String headers);
    HTML table(String widths, String headers);
    HTML table(String widths, String headers, String colors);
    HTML tr(String... tds);
    HTML td(String td, Integer width, String... args);
    HTML td(String td, String... args);

    HTML td(Color red);

    HTML td(String td, Color bgColor);

    HTML td();
    HTML td(Integer height);

    HTML tdHeight(Integer height);
    HTML tdWidth(Integer width);

    HTMLImpl tdColSpan(Integer i);

    HTML tdAlign(String align);

    HTML tdBgColor(Color bgColor);

    HTML anchor(String name);

    HTML link(String url, String text);

    HTML append(String text, String... args);

    HTML border(int i);

    HTML form(String action);

    HTML endForm();

    HTML checkbox(String id, String label);
    HTML checkbox(String id, String label, boolean defaultValue);

    HTML textfield(String id, String label);

    HTML textfield(String id, String label, String value);

    HTML combo(String id, java.util.List<String> options, String defaultOption);


    HTML submit(String id, String label);

    String html();

    String htmlDoc();

    HTML br();

    HTML endTable();

    void setStyle(String css);
}
