package com.ygg.webapp.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class UtilProperties
{
    
    private Logger logger = Logger.getLogger(UtilProperties.class);
    
    private static UtilProperties instance = null;
    
    private Properties props = null;
    
    private UtilProperties(String propertiesName)
    {
        props = new Properties();
        init(propertiesName);
    }
    
    public void init(String propertiesName)
    {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(propertiesName);
        try
        {
            if (props == null)
                props = new Properties();
            
            props.load(in);
        }
        catch (IOException e)
        {
            logger.error("IOException", e);
        }
        finally
        {
            try
            {
                in.close();
            }
            catch (IOException e)
            {
                logger.error("IOException", e);
            }
        }
    }
    
    public static UtilProperties getInstance(String propertiesName)
    {
    	if(instance == null){
    		instance = new UtilProperties(propertiesName);
    	}
        return instance;
    }
    
    public String getProperties(String key)
    {
        Object value = props.get(key);
        if (value == null)
            return null;
        return value.toString();
    }
}
