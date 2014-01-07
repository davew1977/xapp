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

/**
 * Allows an application to apply a filter on the main tree in the lefthand pane
 */
public interface TreeNodeFilter
{
    /**
     *
     * @param node
     * @return true if the node should be visible in the tree
     */
    boolean accept(Node node);
}