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

public enum ConflictType
{
    SIMPLE_NEW_NEW,
    SIMPLE_DIFF_REMOVED,
    SIMPLE_REMOVED_DIFF,
    SIMPLE_DIFF_DIFF,
    SIMPLE_REF_LIST,
    COMPLEX_NEW_NEW,
    COMPLEX_DIFF_REMOVED,
    COMPLEX_REMOVED_DIFF,
    COMPLEX_DIFF_DIFF    
}
