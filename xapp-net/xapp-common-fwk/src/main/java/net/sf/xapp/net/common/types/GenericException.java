/*
 *
 * Date: 2010-jun-11
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.types;

public class GenericException extends RuntimeException
{
    private final ErrorCode errorCode;

    public GenericException(ErrorCode errorCode)
    {
        this(errorCode, null);
    }

    public GenericException(ErrorCode errorCode, String message)
    {
        super(message!=null ? message : errorCode.toString());
        this.errorCode = errorCode;
    }


    public ErrorCode getErrorCode()
    {
        return errorCode;
    }
}
