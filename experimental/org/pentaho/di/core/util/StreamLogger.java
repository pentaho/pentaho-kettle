package org.pentaho.di.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;



public class StreamLogger implements Runnable
{
    private InputStream is;

    private String      type;

    public StreamLogger(InputStream is, String type)
    {
        this.is = is;
        this.type = type;
    }

    public void run()
    {
        LogWriter log = LogWriter.getInstance();
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null)
            {
                log.logBasic(type, line);
            }
        }
        catch (IOException ioe)
        {
            log.logError(type, Const.getStackTracker(ioe));
        }
    }
}
