package net.sf.xapp.net.server.framework.smartconverter;

public class ConvertResult<T>
{
    private final boolean converted;
    private final T target;

    public ConvertResult(boolean converted, T target)
    {
        this.converted = converted;
        this.target = target;
    }

    public boolean isConverted()
    {
        return converted;
    }

    public T getTarget()
    {
        return target;
    }
}
