/*
 *
 * Date: 2010-sep-21
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework.memdb;

import net.sf.xapp.net.common.types.LobbyEntity;

public class LobbyEntityPropertyInspector implements PropertyInspector<LobbyEntity>
{
    @Override
    public String getValue(LobbyEntity item, String property)
    {
        return item.get(property);
    }
}
