package be.ibridge.kettle.job.entry.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;

public class StreamLogger implements Runnable
{
    private InputStream is;

    private String      type;

    StreamLogger(InputStream is, String type)
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
                log.logDetailed(type, line);
            }
        }
        catch (IOException ioe)
        {
            log.logError(type, Const.getStackTracker(ioe));
        }
    }
}
