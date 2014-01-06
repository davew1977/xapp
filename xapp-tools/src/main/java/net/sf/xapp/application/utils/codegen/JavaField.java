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

public class JavaField extends Field {

    public String generateDeclaration() {
        StringBuilder sb = new StringBuilder();
        sb.append(m_modifier).append(m_transient ? " transient" : "").
                append(m_static ? " static" : "").
                append(m_final ? " final" : "").append(" ").append(m_type).
                append(m_static ? " " : " " + m_prefix).append(m_name).
                append(m_defaultValue != null ? " = " + m_defaultValue : "").append(";");
        return sb.toString();
    }
}
