package be.ibridge.kettle.core.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;

import be.ibridge.kettle.core.Const;

public class Log4jKettleLayout extends Layout
{
    private boolean timeAdded;
    
    public Log4jKettleLayout(boolean addTime)
    {
        this.timeAdded = addTime;
    }

    public String format(LoggingEvent event)
    {
        // OK, perhaps the logging information has multiple lines of data.
        // We need to split this up into different lines and all format these lines...
        String line="";
        
        String dateTimeString = "";
        if (timeAdded)
        {
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            dateTimeString = df.format(new Date(event.timeStamp))+" - ";
        }

        Object object = event.getMessage();
        if (object instanceof Log4jMessage)
        {
            Log4jMessage message = (Log4jMessage)object;

            String parts[] = message.getMessage().split(Const.CR);
            for (int i=0;i<parts.length;i++)
            {
                // Start every line of the output with a dateTimeString
                line+=dateTimeString;
                
                // Include the subject too on every line...
                if (message.getSubject()!=null)
                {
                    line+=message.getSubject()+" - ";
                }
                
                if (message.isError())  
                {
                    line+="ERROR : ";
                }
                
                line+=parts[i];
                if (i<parts.length-1) line+=Const.CR; // put the CR's back in there!
            }
        }
        else
        {
            line+=dateTimeString+object.toString();
        }
        
        return line;
    }

    public boolean ignoresThrowable()
    {
        return false;
    }

    public void activateOptions()
    {
    }

    public boolean isTimeAdded()
    {
        return timeAdded;
    }

    public void setTimeAdded(boolean addTime)
    {
        this.timeAdded = addTime;
    }

}
