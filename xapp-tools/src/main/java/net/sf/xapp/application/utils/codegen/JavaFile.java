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

public class JavaFile extends AbstractCodeFile
{

    public JavaFile(File outPath)
    {
        super(outPath);
    }

    MethodImpl createMethod()
    {
        return new JavaMethod();
    }

    Field createField()
    {
        return new JavaField();
    }

    void doEndClass(StringBuilder sb)
    {
        deIndent();
        sb.append(currentIndent).append("}\n");
    }

    void doPackageDeclaration(StringBuilder sb)
    {
        if (mPackage !=null)
        {
            sb.append("package ").append(mPackage).append(";\n\n");
        }
    }


    protected String getFileSuffix()
    {
        return "java";
    }
}
