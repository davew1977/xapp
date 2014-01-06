/*
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 *
 * The contents of this file may be used under the terms of the GNU Lesser
 * General Public License Version 2.1 or later.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */
package net.sf.xapp.application.api;

/**
 * class for runtime exceptions that the application code throws, differentiating them
 * from exceptions produced as a result of a programming bug
 */
public class ApplicationException extends RuntimeException
{
    public ApplicationException()
    {
    }

    public ApplicationException(String message)
    {
        super(message);
    }

    public ApplicationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ApplicationException(Throwable cause)
    {
        super(cause);
    }
}
