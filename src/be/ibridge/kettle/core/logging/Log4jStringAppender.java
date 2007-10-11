package be.ibridge.kettle.core.logging;

import java.util.ArrayList;
import java.util.List;

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
    
    private int nrLines;
    
    private int maxNrLines;
    
    private List bufferChangedListeners;
    
    public Log4jStringAppender()
    {
        buffer = new StringBuffer();
        bufferChangedListeners=new ArrayList();
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
        
        // See if we don't have too many lines on board...
        nrLines++;
        if (maxNrLines>0 && nrLines>maxNrLines)
        {
        	buffer.delete(0, line.length());
        	nrLines--;
        }
        
        for (int i=0;i<bufferChangedListeners.size();i++)
        {
        	BufferChangedListener listener = (BufferChangedListener) bufferChangedListeners.get(i);
        	listener.contentWasAdded(buffer, line, nrLines);
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
    
	/**
	 * @return the maximum number of lines that this buffer contains
	 */
	public int getMaxNrLines() {
		return maxNrLines;
	}

	/**
	 * @param maxNrLines the maximum number of lines that this buffer should contain
	 */
	public void setMaxNrLines(int maxNrLines) {
		this.maxNrLines = maxNrLines;
	}

	/**
	 * @return the nrLines
	 */
	public int getNrLines() {
		return nrLines;
	}

	public void addBufferChangedListener(BufferChangedListener bufferChangedListener) {
		bufferChangedListeners.add(bufferChangedListener);
	}
	
	public void removeBufferChangedListener(BufferChangedListener bufferChangedListener) {
		bufferChangedListeners.remove(bufferChangedListener);
	}

	/**
	 * @param nrLines the nrLines to set
	 */
	public void setNrLines(int nrLines) {
		this.nrLines = nrLines;
	}
}
