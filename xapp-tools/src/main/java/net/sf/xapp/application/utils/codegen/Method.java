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

public interface Method extends DocContext, MethodContext
{
    void lineAtStart(String code, Object... args);

    void line(String code, Object... args);

    void overwriteLine(int lineIndex, String code, Object... args);

    String getName();

    /**
     * example: overWriteLine would return "overwriteLine(lineIndex, code, args)"
     * @return
     */
    String generateInvocation();

    /**
     * as above but without the method name or brackets
     * @return
     */
    String generateParamStr(boolean includeTypeDeclarations);

    List<String> getParams();
    String getReturnType();
    boolean isVoid();

    void endBlock();
    void endBlock(String code);

    void startBlock(String code, Object... args);

    boolean isGetter();

    boolean isSetter();

    <T> T attachment();
}
