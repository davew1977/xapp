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

import net.sf.xapp.application.core.CommandContext;

import java.util.List;

/**
 * Nodes in a djwastor application tree can be both nodes in a list and also manage nodes in a list.
 * The NodeContext tries to encapsulate these separate responsibilities
 */
public interface NodeContext
{
    List<Command> createCommands(Node node, CommandContext forPopUp);
}
