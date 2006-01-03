package be.ibridge.kettle.core.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;

public class Log4jKettleLayout extends Layout
{
    private boolean timeAdded;
    
    public Log4jKettleLayout(boolean addTime)
    {
        this.timeAdded = addTime;
    }

    public String format(LoggingEvent event)
    {
        String line="";
        if (timeAdded)
        {
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            line+=df.format(new Date(event.timeStamp))+" - ";
        }

        Object object = event.getMessage();
        if (object instanceof Log4jMessage)
        {
            Log4jMessage message = (Log4jMessage)object;
            
            if (message.getSubject()!=null)
            {
                line+=message.getSubject()+" - ";
            }

            if (event.getLevel().equals(Level.ERROR))  
            {
                line+="ERROR : ";
            }
            
            line+=message.getMessage();
        }
        else
        {
            line+=object.toString();
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
