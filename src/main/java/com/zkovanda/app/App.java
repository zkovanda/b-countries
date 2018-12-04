package com.zkovanda.app;

import com.zkovanda.data.CountryVAT;
import com.zkovanda.data.CountryReadException;
import com.zkovanda.data.CountryPrintException;
import java.util.HashMap;

/**
 * Technical Task:
 * Read list of EU countries and printout 3 countries with highest standard VAT,
 * and 3 countries with lowest countries.
 */
public class App 
{
    public static void main(String[] args)
    {
        CountryVAT countries = new CountryVAT();
        try 
        {
            // Read countries
            countries.read("http://jsonvat.com/");
            
            // Prepare parameter "max-count"
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("max-count", new Integer(3));
            
            // Printout required countries
            countries.print(System.out, CountryVAT.CMD_HIGHEST_VAT, params);
            countries.print(System.out, CountryVAT.CMD_LOWEST_VAT, params);
        }
        catch(CountryReadException crex)
        {
            // Reading countries failed by any reason
            System.out.println("Reading countries failed. Reason: " + crex.getMessage());
        }
        catch(CountryPrintException cpex)
        {
            // Printing countries failed by any reason
            System.out.println("Printing countries failed. Reason: " + cpex.getMessage());
        }
        finally
        {
            // Don't forget to cleanup the countries object
            countries.cleanUp();
        }      
    }
}
