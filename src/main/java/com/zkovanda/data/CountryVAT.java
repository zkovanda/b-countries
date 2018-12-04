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

public class CountryVAT implements IFaceCountries
{
    public static final int CMD_HIGHEST_VAT = 1;
    public static final int CMD_LOWEST_VAT = 2;

    private ArrayList<CountryItem> m_arrCountries = null;

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

    public int read(String sURL) 
        throws CountryReadException
    {
        // Read JSON country data from remote source
        
        HttpURLConnection oConnection = null;
        try
        {
		    URL oUrl = new URL(sURL);
		    oConnection = (HttpURLConnection)oUrl.openConnection();
            oConnection.setRequestMethod("GET");
        }
        catch(IOException ioex)
        {
             throw new CountryReadException("Opening connection to data server failed. Verify validity of the URL: " + sURL);
        }

		int nResponseCode = 0;
        try
        {
            nResponseCode = oConnection.getResponseCode();
        }
        catch(IOException ex)
        {
            throw new CountryReadException("Unable to get data server response: " + sURL);
        }

        StringBuffer sbResponseJSON = null;
        if( nResponseCode == 200 ) {
            sbResponseJSON = new StringBuffer();
        
	        BufferedReader in = null;
            try
            {
                in = new BufferedReader(new InputStreamReader(oConnection.getInputStream()));
                
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
        
        // Parse JSON data
        
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
    
    public int print(PrintStream ps, int nCmd, HashMap<String, Object> params) 
        throws CountryPrintException
    {
        if( m_arrCountries == null )
            throw new CountryPrintException("Country list is empty.");
    
        int nMaxCount = 1;
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
    
        int nPrintedCount = 0;
        
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
    
    public void cleanUp()
    {
    }
}