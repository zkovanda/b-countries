package com.zkovanda.data;

import java.io.PrintStream;
import java.util.HashMap;

/**
 * Common data interface.
 */
public interface IFaceCountries
{
    /**
     * Read data from remote location.
     * @param sURL URL of the data source.
     * @return Number of read countries.
     * @throws CountryReadException The exception is thrown in case of any failure. Get reason by calling method getMessage().
     */
    public int read(String sURL) throws CountryReadException;
    
    /**
     * Print countries to specified print stream.
     * @param ps Print stream for output.
     * @param nCmd Identificator of the requested command. Commands are defined in classes, which implements this interface.
     * @param params List of command parameters. E.g. params.put("param-key", new Integer(10));
     * @return Number of really printed countries.
     * @throws CountryPrintException The exception is thrown in case of any failure. Get reason by calling method getMessage().
     */
    public int print(PrintStream ps, int nCmd, HashMap<String, Object> params) throws CountryPrintException;
    
    /**
     * Cleanup the object.
     */
    public void cleanUp();
}
