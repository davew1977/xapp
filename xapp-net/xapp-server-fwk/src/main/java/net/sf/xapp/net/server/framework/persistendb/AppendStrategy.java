package net.sf.xapp.net.server.framework.persistendb;

import net.sf.xapp.net.common.framework.MessageHandler;

public interface AppendStrategy<U> extends MessageHandler<U>
{
    void processingAppend(String key, String updateLine);
}
