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
package net.sf.xapp.objectmodelling.difftracking;

public interface KeyChangeDictionary
{
    void init(ChangeSet changeSet);

    void primaryKeyChange(PrimaryKeyChange change);

    void objectRemoved(String className, String key);
    void objectRemoved(String className, String key, boolean trackNewAndRemoved);

    boolean isEmpty();

    ChangeSet createChangeSet();

    PrimaryKeyChange findByOld(String className, String oldValue);
    PrimaryKeyChange findByNew(String className, String newValue);
}
