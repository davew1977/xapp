/*
 *
 * Date: 2010-jun-16
 * Author: davidw
 *
 */
package xapp.mdda.model;

public enum ApiType
{
    ASYNC_WITH_REPLY_API,SYNCHRONOUS,SYNC_WITH_REPLY_API,ASYNC;

    public boolean hasReplyApi()
    {
        return this==ASYNC_WITH_REPLY_API || this == SYNC_WITH_REPLY_API;
    }

    public boolean isSynchronous() {
        return this  == SYNCHRONOUS || this == SYNC_WITH_REPLY_API;
    }
}
