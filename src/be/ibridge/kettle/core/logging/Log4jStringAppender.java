package be.ibridge.kettle.core.logging;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import be.ibridge.kettle.core.Const;

public class Log4jStringAppender implements Appender
{
    private Layout layout;
    private Filter filter;
    
    private String  name;
    
    private StringBuffer buffer;
    
    public Log4jStringAppender()
    {
        buffer = new StringBuffer();
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
    }

    public void doAppend(LoggingEvent event)
    {
        String line = layout.format(event)+Const.CR;
        buffer.append(line);
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

    public void setFilter(Filter filter)
    {
        this.filter = filter;
    }
    
    public StringBuffer getBuffer()
    {
        return buffer;
    }
    
    public void setBuffer(StringBuffer buffer)
    {
        this.buffer = buffer;
    }
}
