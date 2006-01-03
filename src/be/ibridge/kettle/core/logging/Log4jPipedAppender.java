package be.ibridge.kettle.core.logging;

import java.io.IOException;
import java.io.PipedOutputStream;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import be.ibridge.kettle.core.Const;

public class Log4jPipedAppender implements Appender
{
    private Layout layout;
    private Filter filter;
    
    private String  name;
    
    private PipedOutputStream pipedOutputStream;
    
    public Log4jPipedAppender() throws IOException
    {
        pipedOutputStream = new PipedOutputStream();
    }
    
    public void addFilter(Filter filter)
    {
        this.filter = filter;
    }

    public Filter getFilter()
    {
        return filter;
    }

    public void clearFilters()
    {
        filter=null;
    }

    public void close()
    {
        try
        {
            pipedOutputStream.close();
        }
        catch(IOException e)
        {
            System.out.println("Unable to close piped output stream: "+e.getMessage());
        }
    }

    public void doAppend(LoggingEvent event)
    {
        String line = layout.format(event)+Const.CR;
        try
        {
            pipedOutputStream.write(line.getBytes());
        }
        catch(IOException e)
        {
            System.out.println("Unable to write to piped output stream : "+e.getMessage());
        }
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setErrorHandler(ErrorHandler arg0)
    {
    }

    public ErrorHandler getErrorHandler()
    {
        return null;
    }

    public void setLayout(Layout layout)
    {
        this.layout = layout;
    }

    public Layout getLayout()
    {
        return layout;
    }

    public boolean requiresLayout()
    {
        return true;
    }

    public PipedOutputStream getPipedOutputStream()
    {
        return pipedOutputStream;
    }

    public void setPipedOutputStream(PipedOutputStream pipedOutputStream)
    {
        this.pipedOutputStream = pipedOutputStream;
    }

    public void setFilter(Filter filter)
    {
        this.filter = filter;
    }
}
