package net.sf.xapp.net.server.framework.persistendb;

import ngpoker.common.framework.MessageHandler;

public interface AppendStrategy<U> extends MessageHandler<U>
{
    void processingAppend(String key, String updateLine);
}
