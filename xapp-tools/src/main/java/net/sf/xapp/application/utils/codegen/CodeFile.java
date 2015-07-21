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
package net.sf.xapp.application.utils.codegen;

import java.util.List;

public interface CodeFile extends ClassContext, DocContext {
    String getFileName();
    String getFullPath();

    CodeFile setEnum(List<String> values);
    EnumContext newEnumValue(String name);
    CodeFile addSimpleEnumValue(String name);
    CodeFile setPackage(String p);
    CodeFile addImport(String i);
    CodeFile addImports(List<String> imports);

    void generate();

    String generateString();

    /**
     * will attach the object to the next method created
     * @param attachment
     * @return
     */
    CodeFile attach(Object attachment);

    ClassContext newInnerClass(String name);

    CodeFile setFileHeader(String content, Object... args);

    CodeFile addImportBlankLine();

    CodeFile insertDocBlockAfter(CodeFileSection section, String content, Object... args);
}
