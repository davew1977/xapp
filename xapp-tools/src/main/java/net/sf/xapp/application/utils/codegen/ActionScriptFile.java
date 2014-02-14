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

import java.io.File;

public class ActionScriptFile extends AbstractCodeFile
{

    public ActionScriptFile(File outPath)
    {
        super(outPath, true);
    }

    MethodImpl createMethod()
    {
        return new ActionScriptMethod();
    }

    Field createField()
    {
        return new ActionScriptField();
    }

    void doEndClass(StringBuilder sb)
    {
        deIndent();
        sb.append(currentIndent).append("}\n");
        deIndent();
        sb.append(currentIndent).append("}\n");
    }

    void doPackageDeclaration(StringBuilder sb)
    {
        sb.append("package ").append(mPackage).append("\n{\n");
        indent();
    }

    String getFileSuffix()
    {
        return "as";
    }
}