package com.zkovanda.data;

import java.io.PrintStream;
import java.util.HashMap;

public interface IFaceCountries
{
    public int read(String sURL) throws CountryReadException;
    public int print(PrintStream ps, int nCmd, HashMap<String, Object> params) throws CountryPrintException;
    public void cleanUp();
}