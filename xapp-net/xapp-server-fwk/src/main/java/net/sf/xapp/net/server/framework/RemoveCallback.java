package net.sf.xapp.net.server.framework;

/**
 * called by a cashgame or a tour when it is ready for removal
 */
public interface RemoveCallback
{
    void removeMe(String key);

    public static class NullImpl implements RemoveCallback
    {
        @Override
        public void removeMe(String key)
        {

        }
    }
}
