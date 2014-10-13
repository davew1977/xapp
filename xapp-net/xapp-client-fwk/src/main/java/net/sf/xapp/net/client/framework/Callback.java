package net.sf.xapp.net.client.framework;

import net.sf.xapp.net.common.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class Callback
{
    private Method method;
    private Object target;
    private Object[] preDeterminedParams;

    public Callback()
    {
    }

    public Callback(String methodName, Object target, Object... params)
    {
        method = ReflectionUtils.findMatchingMethod(target.getClass(), methodName, params.length);
        preDeterminedParams = params;
        this.target = target;

    }
    public Callback(String methodName, Object target)
    {
        this(methodName, target, -1);
    }
    public Callback(String methodName, Object target, int noOfParams)
    {
        method = ReflectionUtils.findMatchingMethod(target.getClass(), methodName, noOfParams);
        assert method!=null;
        this.target = target;
    }

    /**
     * will try and pass as many of the args as possible,
     * simply override when declaring an anonymous callback
     * @param args
     */
    public <T> T call(Object... args)
    {
        Object[] req;
        if (args.length>0)
        {
            req = new Object[method.getParameterTypes().length];
            System.arraycopy(args, 0, req, 0, req.length);
        }
        else
        {
            req = preDeterminedParams;
        }
        return ReflectionUtils.call(target, method, req);
    }

}
