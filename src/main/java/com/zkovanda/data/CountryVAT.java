package com.zkovanda.data;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import org.json.*;

/**
 * Read and print out countries and their VAT rates.
 */
public class CountryVAT implements IFaceCountries
{
    /**
     * Command: Print out countries with highest standard VATs.
     * Use parameter "max-count" to define number of printed countries.
     * Parameter value: Integer(x), x = number of countries. 
     * If the parameter is missing, 1 country is printed.
     */
    public static final int CMD_HIGHEST_VAT = 1;
    /**
     * Command: Print out countries with lowest standard VATs.
     * Use parameter "max-count" to define number of printed countries.
     * Parameter value: Integer(x), x = number of countries.     
     * If the parameter is missing, 1 country is printed.
     */    
    public static final int CMD_LOWEST_VAT = 2;

    private ArrayList<CountryItem> m_arrCountries = null;

    /**
     * Country item local class.
     */
    private class CountryItem
    {
        public String m_sCountryName;
        public String m_sCountryCode;
        public double m_dblStandardVAT;
        
        public CountryItem(String sCountryName, String sCountryCode, double dblStandardVAT)
        {
            m_sCountryName = sCountryName;
            m_sCountryCode = sCountryCode;
            m_dblStandardVAT = dblStandardVAT;
        }
    } 
    
    /**
     * Comparator: User to sort countries in the array 'm_arrCountries'.
     */
    private class CountryItemComp implements Comparator<CountryItem>
    {
        public int compare(CountryItem ci1, CountryItem ci2) 
        {
            if( ci1.m_dblStandardVAT < ci2.m_dblStandardVAT )
                return 1;
            else if( ci1.m_dblStandardVAT > ci2.m_dblStandardVAT )
                return -1;
            else
                return 0;
        }
    }

    /**
     * Read country list with VAT information from remote data source.
     * @param sURL URL of the data source.
     * @return Number of read countries.
     * @throws CountryReadException The exception is thrown in case of any failure. Get reason by calling method getMessage().
     */
    public int read(String sURL) 
        throws CountryReadException
    {
        // Read JSON country data from remote source
        
        HttpURLConnection oConnection = null;
        try
        {
            // Create and open connection to the remote server
		    URL oUrl = new URL(sURL);
		    oConnection = (HttpURLConnection)oUrl.openConnection();
            oConnection.setRequestMethod("GET");
        }
        catch(IOException ioex)
        {
             throw new CountryReadException("Opening connection to data server failed. Verify validity of the URL: " + sURL);
        }

        // Read HTTP response code.
		int nResponseCode = 0;
        try
        {
            nResponseCode = oConnection.getResponseCode();
        }
        catch(IOException ex)
        {
            throw new CountryReadException("Unable to get data server response: " + sURL);
        }

        // Read response body if response code is 200 (i.e. success)
        StringBuffer sbResponseJSON = null;
        if( nResponseCode == 200 ) {
            sbResponseJSON = new StringBuffer();
        
	        BufferedReader in = null;
            try
            {
                // Create input stream...
                in = new BufferedReader(new InputStreamReader(oConnection.getInputStream()));
                
                // ... and read it line by line
                String sLine;
		        while((sLine = in.readLine()) != null)
                    sbResponseJSON.append(sLine);
             }         
            catch(IOException ioex)
            {
                throw new CountryReadException("Reading data from server failed.");
            }
            finally 
            {
                // Don't forget to close input stream!
                if( in != null )
                {
                    try
                    {
                        in.close();
                    }
                    catch(IOException ioex)
                    {}
                }
            }
        }
        else
            throw new CountryReadException("Request to get data failed. Server response code = " + nResponseCode);
        
        /*  Parse JSON data using 3rd party JAR: org.json.jar:
            Origin:
                http://www.java2s.com/Code/Jar/o/Downloadorgjsonjar.htm
                
            JSON data has following expected format:
            
            {
              "details": "http://github.com/adamcooke/vat-rates",
              "version": null,
              "rates": [	
                {	
                  "name": "Spain",
                  "code": "ES",
                  "country_code": "ES",
                  "periods": [	
                    {	
                      "effective_from": "0000-01-01",
                      "rates":
                        {
                          "super_reduced": 4,
                          "reduced": 10,                  
                          "standard": 21
                        }
                    }
                  ]
                },
                {	
                  "name": "Bulgaria",
                  "code": "BG",
                  "country_code": "BG",
                  "periods": [	
                    {	
                      "effective_from": "0000-01-01",
                      "rates":
                        {
                          "reduced": 9,                  
                          "standard": 20
                        }
                    }
                  ]
                },
                ...
              ]
            }            
        */
        
        JSONObject objData = null;
        JSONArray arrRates = null;
        try
        {
            objData = new JSONObject(sbResponseJSON.toString());
            arrRates = objData.getJSONArray("rates");  
        }
        catch(JSONException jsonex)    
        {
            throw new CountryReadException("Unexpected JSON format of country data (1).");
        }       
        
        // Get country data: country name, coutry code, standard VAT rate
        // and push them to the array list.
        String sCountryName = null;
        String sCountryCode = null;
        double dblVATRate = 0.0;
        
        m_arrCountries = new ArrayList<CountryItem>(); 
        
        for(int i = 0; i < arrRates.length(); i++) {
            // Country name and VAT rate
            try                  
            {
                JSONObject objCountry = (JSONObject) arrRates.get(i);
                sCountryName = objCountry.getString("name");
                sCountryCode = objCountry.getString("country_code");
                JSONArray arrPeriods = objCountry.getJSONArray("periods");
                JSONObject objRates = ((JSONObject)arrPeriods.get(0)).getJSONObject("rates");
                dblVATRate = objRates.getDouble("standard");
            }
            catch(JSONException jsonex)    
            {
                throw new CountryReadException("Unexpected JSON format of country data (2).");
            }
                
            // Store it to simple data array
            m_arrCountries.add(new CountryItem(sCountryName, sCountryCode, dblVATRate));
        }    
        
        // Sort countries by VAT
        Collections.sort(m_arrCountries, new CountryItemComp());
        
        // Return number of read countries
        return m_arrCountries.size();
    }
    
    /**                                                                                                         
     * Print countries to specified print stream.
     * @param ps Print stream for output.
     * @param nCmd Identificator of the requested command. See constants CMD_XXX in this class.
     * @param params List of command parameters. E.g. params.put("param-key", new Integer(10));
     * @return Number of really printed countries.
     * @throws CountryPrintException The exception is thrown in case of any failure. Get reason by calling method getMessage().
     */
    public int print(PrintStream ps, int nCmd, HashMap<String, Object> params) 
        throws CountryPrintException
    {
        // If there is no country data avaiable, throw an exception. The operation cannot be process.
        // Call read() prior this function call.
        if( m_arrCountries == null )
            throw new CountryPrintException("Country list is empty.");
    
        // Get expected paramater "max-count".
        int nMaxCount = 1;      // Default value.
        if( params.containsKey("max-count") )
        {
            try
            {
                nMaxCount = ((Integer)params.get("max-count")).intValue();
            }
            catch(Exception ex)
            {
                throw new CountryPrintException("Invalid format of parameter 'max-count'. Must be integer value.");
            }
        }
    
        // Counter of really printed countries.
        int nPrintedCount = 0;
        
        // Process commands
        switch( nCmd )
        {
            case CMD_HIGHEST_VAT:
                ps.println("====================================");
                ps.println("Countries with highest standard VAT:");
                ps.println("------------------------------------");
                for(nPrintedCount = 0; nPrintedCount < nMaxCount; nPrintedCount++)
                {
                    if( nPrintedCount < m_arrCountries.size() )
                    {
                        CountryItem oCountry = m_arrCountries.get(nPrintedCount);
                        ps.println("  " + oCountry.m_sCountryName + " (" + oCountry.m_sCountryCode + "): standard VAT = " + Double.toString(oCountry.m_dblStandardVAT) + "%");
                    }
                    else
                        break;
                }
                ps.println("====================================");
                break;                                           
                
            case CMD_LOWEST_VAT:
                ps.println("====================================");
                ps.println("Countries with lowest standard VAT:");
                ps.println("------------------------------------");
                for(nPrintedCount = 0; nPrintedCount < nMaxCount; nPrintedCount++)
                {
                    if( nPrintedCount < m_arrCountries.size() )
                    {
                        CountryItem oCountry = m_arrCountries.get(m_arrCountries.size() - 1 - nPrintedCount);
                        ps.println("  " + oCountry.m_sCountryName + " (" + oCountry.m_sCountryCode + "): standard VAT = " + Double.toString(oCountry.m_dblStandardVAT) + "%");
                    }
                    else
                        break;
                }
                ps.println("====================================");
                break;                                            
                
            default:
                throw new CountryPrintException("Unexpected command.");
        }
        return nPrintedCount;
    }
    
    /**
     * Cleanup the object.
     */    
    public void cleanUp()
    {
        // Nothing to do in this class.
    }
}
