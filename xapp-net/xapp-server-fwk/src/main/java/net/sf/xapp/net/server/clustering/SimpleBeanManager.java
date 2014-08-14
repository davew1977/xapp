/*
 *
 * Date: 2010-sep-09
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import net.sf.xapp.net.api.out.Out;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.server.ResultHandlingProxy;
import net.sf.xapp.utils.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SimpleBeanManager implements BeanManager, ApplicationContextAware
{
    private ApplicationContext appContext;
    private Out out;

    public SimpleBeanManager()
    {
    }

    public void setOut(Out out)
    {
        this.out = out;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        appContext = applicationContext;
    }

    @Override
    public <A> MessageHandler<A> findBean(Class<A> apiType)
    {
        return new ResultHandlingProxy<A>(out, localLookup(apiType));
    }

    private <T> T localLookup(Class<T> apiType)
    {
        String beanName = apiType.getSimpleName();
        beanName = StringUtils.decapitaliseFirst(beanName);
        return (T) appContext.getBean(beanName);
    }
}
