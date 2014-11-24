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
package net.sf.xapp.application.api;

import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import java.io.File;

public interface GUIContext<T>
{
    File getCurrentFile();

    ObjectMeta<T> getObjectMeta();
    void setObjMeta(ObjectMeta objMeta);
    T getInstance();

    ClassModel<T> getRootType();

    void newObjectInstance();

    void saveToFile(File file);

    void saveToFile();

    T openFile(File selectedFile);

    ClassDatabase<T> getClassDatabase();

    void init(ApplicationContainer<T> applicationContainer);
}
