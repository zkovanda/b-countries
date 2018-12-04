package com.zkovanda.data;

import java.lang.Exception;

public class CountryReadException extends Exception
{
    public CountryReadException(String sMessage)
    {
        super(sMessage);
    }
}