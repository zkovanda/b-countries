package com.zkovanda.data;

import java.lang.Exception;

/**
 * The exception is thrown if printing of countries fails by any reason.
 * Call getMessage() to retrieve reason of the exception.
 */
public class CountryPrintException extends Exception
{
    public CountryPrintException(String sMessage)
    {
        super(sMessage);
    }
}
