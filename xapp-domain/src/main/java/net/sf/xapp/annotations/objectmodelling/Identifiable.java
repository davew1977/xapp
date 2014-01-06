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
package net.sf.xapp.annotations.objectmodelling;

/**
 * Marks the type as having instances that are identifiable from each other by a fixed generated key. This is required
 * where we need to identify objects over time where their domain level 'primary key' is subject to change.
 *
 * A {@link net.sf.xapp.objectmodelling.core.ClassModelManager} will maintain and generate ids for new and unmarshalled
 * objects. 
 */
public @interface Identifiable
{
}
