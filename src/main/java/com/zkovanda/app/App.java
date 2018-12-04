package com.zkovanda.app;

import com.zkovanda.data.CountryVAT;
import com.zkovanda.data.CountryReadException;
import com.zkovanda.data.CountryPrintException;
import java.util.HashMap;

/**
 * Print countries!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        //System.out.println( "Hello World!" );
        CountryVAT countries = new CountryVAT();
        try 
        {
            countries.read("http://jsonvat.com/");
            
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("max-count", new Integer(3));
            
            countries.print(System.out, CountryVAT.CMD_HIGHEST_VAT, params);
            countries.print(System.out, CountryVAT.CMD_LOWEST_VAT, params);
        }
        catch(CountryReadException crex)
        {
            System.out.println("Reading countries failed. Reason: " + crex.getMessage());
        }
        catch(CountryPrintException cpex)
        {
            System.out.println("Printing countries failed. Reason: " + cpex.getMessage());
        }
        finally
        {
            countries.cleanUp();
        }      
    }
}
