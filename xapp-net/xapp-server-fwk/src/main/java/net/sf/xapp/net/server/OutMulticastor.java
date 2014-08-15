/*
 *
 * Date: 2010-sep-18
 * Author: davidw
 *
 */
package net.sf.xapp.net.server;

import net.sf.xapp.net.api.out.Out;
import net.sf.xapp.net.api.out.OutAdaptor;
import net.sf.xapp.net.common.framework.Multicaster;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class OutMulticastor extends OutAdaptor
{
    public OutMulticastor()
    {
        super(new Multicaster<Out>());
    }

    @Autowired
    public void setOuts(List<Out> outs)
    {
        ((Multicaster<Out>) delegate).setDelegates(outs);
    }
}
