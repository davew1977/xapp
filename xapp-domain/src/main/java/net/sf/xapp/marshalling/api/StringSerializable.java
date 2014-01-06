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

/**
 * An object that can be serialized as a string that can be written in an XML attribute. Therefore
 * the string must be a plain string that does not use any illegal chars
 */
public interface StringSerializable
{
    void readString(String str);
    String writeString();
}
