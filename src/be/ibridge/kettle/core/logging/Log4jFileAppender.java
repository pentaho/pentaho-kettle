package be.ibridge.kettle.core.logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import be.ibridge.kettle.core.Const;

public class Log4jFileAppender implements Appender
{
    private Layout layout;
    private Filter filter;
    
    private File    file;
    
    private String  name;
    
    private FileOutputStream fileOutputStream;
    
    public Log4jFileAppender(File file) throws IOException
    {
        this.file = file;
        
        fileOutputStream = new FileOutputStream(file);
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
            fileOutputStream.close();
        }
        catch(IOException e)
        {
            System.out.println("Unable to close Logging file ["+file.getName()+"] : "+e.getMessage());
        }
    }

    public void doAppend(LoggingEvent event)
    {
        String line = layout.format(event)+Const.CR;
        try
        {
            fileOutputStream.write(line.getBytes());
        }
        catch(IOException e)
        {
            System.out.println("Unable to close Logging file ["+file.getName()+"] : "+e.getMessage());
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

    public File getFile()
    {
        return file;
    }

    public void setFilename(File file)
    {
        this.file = file;
    }

    public FileOutputStream getFileOutputStream()
    {
        return fileOutputStream;
    }

    public void setFileOutputStream(FileOutputStream fileOutputStream)
    {
        this.fileOutputStream = fileOutputStream;
    }

    public void setFilter(Filter filter)
    {
        this.filter = filter;
    }
}
