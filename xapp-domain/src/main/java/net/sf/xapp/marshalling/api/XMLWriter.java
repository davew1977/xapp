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
package net.sf.xapp.marshalling.api;

import net.sf.xapp.marshalling.namevaluepair.ComparableNameValuePair;
import net.sf.xapp.objectmodelling.core.Property;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public interface XMLWriter
{
    void writeOpeningTag(String tagName, List<ComparableNameValuePair> attrs, boolean elementsExist) throws IOException;

    void writeClosingTag(String tagName) throws IOException;

    void writeSimpleTag(String tagName, String content, Property property) throws IOException;

    Writer getWriter();

    void flush() throws IOException;

    void nullTag(String tagName) throws IOException;
}
