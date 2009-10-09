/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core.logging;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.pentaho.di.core.Const;

/**
 * @deprecated please use the centralized CentralLogStore to get your log lines from.
 * Pass the log channel id of the parent object that you want the log for.
 * 
 * @author matt
 *
 */
public class Log4jStringAppender implements Appender
{
    private Layout layout;
    private Filter filter;
    
    private String  name;
    
    private StringBuffer buffer;
    
    private int nrLines;
    
    private int maxNrLines;
    
    private List<BufferChangedListener> bufferChangedListeners;
    
    public Log4jStringAppender()
    {
        buffer = new StringBuffer();
        nrLines = 0;
        maxNrLines = -1;
        bufferChangedListeners=new ArrayList<BufferChangedListener>();
    }
    
    public String toString() {
    	return buffer.toString();
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
        
        for (BufferChangedListener listener : bufferChangedListeners)
        {
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
	 * @return the maximum number of lines that this buffer contains, 0 or lower means: no limit
	 */
	public int getMaxNrLines() {
		return maxNrLines;
	}

	/**
	 * @param maxNrLines the maximum number of lines that this buffer should contain, 0 or lower means: no limit
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
