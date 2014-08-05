/*
 *
 * Date: 2010-sep-09
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import org.springframework.beans.factory.FactoryBean;

public class TestClusterSharedStateFactoryBean implements FactoryBean
{
    static TestClusterSharedState clusterSharedState = new TestClusterSharedState(2);
    @Override
    public Object getObject() throws Exception
    {
        return clusterSharedState;
    }

    @Override
    public Class getObjectType()
    {
        return ClusterSharedState.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }
}
