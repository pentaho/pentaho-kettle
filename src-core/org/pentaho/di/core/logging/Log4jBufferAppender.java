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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.pentaho.di.core.Const;

/**
 * This class keeps the last N lines in a buffer
 * @author matt
 *
 */
public class Log4jBufferAppender implements Appender
{
    private Layout layout;
    private Filter filter;
    
    private String  name;
    
    private List<BufferLine> buffer;
    private int bufferSize;
    
    public Log4jBufferAppender(int bufferSize)
    {
    	this.bufferSize = bufferSize;
        buffer = Collections.synchronizedList(new LinkedList<BufferLine>());
        layout = new Log4jKettleLayout(true);
    }
    
    /**
     * @return the number (sequence, 1..N) of the last log line.
     * If no records are present in the buffer, 0 is returned.
     */
    public int getLastBufferLineNr() {
      synchronized(buffer) {
      	if (buffer.size()>0) {
      		return buffer.get(buffer.size()-1).getNr();
      	} else {
      		return 0;
      	}
      }
    }
    
    /**
     * 
     * @param channelId channel IDs to grab
     * @param includeGeneral include general log lines
     * @param from
     * @param to
     * @return
     */
    public List<LoggingEvent> getLogBufferFromTo(List<String> channelId, boolean includeGeneral, int from, int to) {
    	List<LoggingEvent> lines = new ArrayList<LoggingEvent>();
    	
    	synchronized(buffer) {
    		for (BufferLine line : buffer) {
    			if (line.getNr()>	from && line.getNr()<=to) {
    				Object payload = line.getEvent().getMessage();
    				if (payload instanceof LogMessage) {
    					LogMessage message = (LogMessage) payload;
    					
    					// Typically, the log channel id is the one from the transformation or job running currently.
    					// However, we also want to see the details of the steps etc.
    					// So we need to look at the parents all the way up if needed...
    					//
    					boolean include = channelId==null;
    					
    					// See if we should include generic messages
    					//
    					if (!include) {
    						LoggingObjectInterface loggingObject = LoggingRegistry.getInstance().getLoggingObject(message.getLogChannelId());

    						if (loggingObject!=null && includeGeneral && LoggingObjectType.GENERAL.equals(loggingObject.getObjectType())) {
    							include = true;
    						}

    						// See if we should include a certain channel id (zero, one or more)
    						//
    						if (!include) {
        						for (String id : channelId) {
        							if (message.getLogChannelId().equals(id)) {
        								include = true;
        								break;
        							}
        						}
        					}
    					}
    					
    					if (include) {
    						
    						try {
	    						// String string = layout.format(line.getEvent());
	    						lines.add(line.getEvent());
    						} catch(Exception e) {
    							e.printStackTrace();
    						}
    					}
    				}
    			}
    		}
    	}
    	
    	return lines;
    }
    
    /**
     * 
     * @param parentLogChannelId the parent log channel ID to grab
     * @param includeGeneral include general log lines
     * @param from
     * @param to
     * @return
     */
    public List<LoggingEvent> getLogBufferFromTo(String parentLogChannelId, boolean includeGeneral, int from, int to) {

		// Typically, the log channel id is the one from the transformation or job running currently.
		// However, we also want to see the details of the steps etc.
		// So we need to look at the parents all the way up if needed...
		//
		List<String> childIds = LoggingRegistry.getInstance().getLogChannelChildren(parentLogChannelId);

		return getLogBufferFromTo(childIds, includeGeneral, from, to);
    }

    public StringBuffer getBuffer(String parentLogChannelId, boolean includeGeneral, int startLineNr, int endLineNr) {
    	StringBuffer stringBuffer = new StringBuffer(10000);
    	
    	List<LoggingEvent> events = getLogBufferFromTo(parentLogChannelId, includeGeneral, startLineNr, endLineNr);
    	for (LoggingEvent event : events) {
			stringBuffer.append( layout.format(event) ).append(Const.CR);
		}
    	
    	return stringBuffer;
    }
    
    public StringBuffer getBuffer(String parentLogChannelId, boolean includeGeneral) {
    	return getBuffer(parentLogChannelId, includeGeneral, 0);
    }

    public StringBuffer getBuffer(String parentLogChannelId, boolean includeGeneral, int startLineNr) {
    	return getBuffer(parentLogChannelId, includeGeneral, startLineNr, getLastBufferLineNr());
    }
    
    public StringBuffer getBuffer() {
    	return getBuffer(null, true);
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
    	buffer.add(new BufferLine(event));
    	if (bufferSize>0 && buffer.size()>bufferSize) {
    		buffer.remove(0);
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
    
    public void clear() {
    	buffer.clear();
    }
    
	/**
	 * @return the maximum number of lines that this buffer contains, 0 or lower means: no limit
	 */
	public int getMaxNrLines() {
		return bufferSize;
	}

	/**
	 * @param maxNrLines the maximum number of lines that this buffer should contain, 0 or lower means: no limit
	 */
	public void setMaxNrLines(int maxNrLines) {
		this.bufferSize = maxNrLines;
	}

	/**
	 * @return the nrLines
	 */
	public int getNrLines() {
		return buffer.size();
	}

	/**
	 * Removes all rows for the channel with the specified id
	 * @param id the id of the logging channel to remove
	 */
	public void removeChannelFromBuffer(String id) {
		synchronized(buffer) {
			Iterator<BufferLine> iterator = buffer.iterator();
			while (iterator.hasNext()) {
				BufferLine bufferLine = iterator.next();
    			Object payload = bufferLine.getEvent().getMessage();
    			if (payload instanceof LogMessage) {
    				LogMessage message = (LogMessage) payload;
    				if (id.equals(message.getLogChannelId())) {
    					iterator.remove();
    				}
    			}
    		}
		}
	}

	public int size() {
		return buffer.size();
	}

	public void removeGeneralMessages() {
		synchronized(buffer) {
			Iterator<BufferLine> iterator = buffer.iterator();
			while (iterator.hasNext()) {
				BufferLine bufferLine = iterator.next();
    			Object payload = bufferLine.getEvent().getMessage();
    			if (payload instanceof LogMessage) {
    				LogMessage message = (LogMessage) payload;
    				LoggingObjectInterface loggingObject = LoggingRegistry.getInstance().getLoggingObject(message.getLogChannelId());
					if (loggingObject!=null && LoggingObjectType.GENERAL.equals(loggingObject.getObjectType())) {
						iterator.remove();
					}
    			}
    		}
		}
	}
	
	public Iterator<BufferLine> getBufferIterator() {
		return buffer.iterator();
	}
}
