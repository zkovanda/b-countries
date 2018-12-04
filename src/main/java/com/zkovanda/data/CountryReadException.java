package com.zkovanda.data;

import java.lang.Exception;

/**
 * The exception is thrown if reading of countries fails by any reason.
 * Call getMessage() to retrieve reason of the exception.
 */
public class CountryReadException extends Exception
{
    public CountryReadException(String sMessage)
    {
        super(sMessage);
    }
}
