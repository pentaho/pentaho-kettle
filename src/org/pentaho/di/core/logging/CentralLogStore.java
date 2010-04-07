package org.pentaho.di.core.logging;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.spi.LoggingEvent;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.EnvUtil;

public class CentralLogStore {
	private static CentralLogStore store;
	
	private Log4jBufferAppender appender;

	private Timer logCleanerTimer;
	
	/**
	 * Create the central log store with optional limitation to the size
	 * 
	 * @param maxSize the maximum size
	 * @param maxLogTimeoutMinutes The maximum time that a log line times out in Minutes.
	 */
	private CentralLogStore(int maxSize, int maxLogTimeoutMinutes) {
		this.appender = new Log4jBufferAppender(maxSize);
		LogWriter.getInstance().addAppender(this.appender);
		replaceLogCleaner(maxLogTimeoutMinutes);
	}
	
	public void replaceLogCleaner(final int maxLogTimeoutMinutes) {
		if (logCleanerTimer!=null) {
			logCleanerTimer.cancel();
		}
		logCleanerTimer = new Timer(true);
		final AtomicBoolean busy = new AtomicBoolean(false);
		TimerTask timerTask = new TimerTask() {
			public void run() {
				if (!busy.get()) {
					busy.set(true);
					if (maxLogTimeoutMinutes>0) {
						long minTimeBoundary = new Date().getTime() - maxLogTimeoutMinutes*60*1000;
						synchronized(appender) {
							Iterator<BufferLine> i = appender.getBufferIterator();
							while (i.hasNext()) {
								BufferLine bufferLine = i.next();
	
								if (bufferLine.getEvent().timeStamp < minTimeBoundary) {
									i.remove();
								} else {
									break;
								}
							}
						}
					}
					busy.set(false);
				}
			}
		};

		// Clean out the rows every 10 seconds to get a nice steady purge operation...
		//
		logCleanerTimer.schedule(timerTask, 10000, 10000);

	}

	/**
	 * Initialize the central log store with optional limitation to the size
	 * 
	 * @param maxSize the maximum size
	 * @param maxLogTimeoutHours The maximum time that a log line times out in hours.
	 */
	public static void init(int maxSize, int maxLogTimeoutMinutes) {
		if (maxSize>0 || maxLogTimeoutMinutes>0) {
			store = new CentralLogStore(maxSize, maxLogTimeoutMinutes);
		} else {
			init();
		}
	}
	
	public static void init() {
		int maxSize = Const.toInt(EnvUtil.getSystemProperty(Const.KETTLE_MAX_LOG_SIZE_IN_LINES), 0); 
		int maxLogTimeoutMinutes = Const.toInt(EnvUtil.getSystemProperty(Const.KETTLE_MAX_LOG_TIMEOUT_IN_MINUTES), 0); 
		store = new CentralLogStore(maxSize, maxLogTimeoutMinutes);
	}
	
	private static CentralLogStore getInstance() {
		if (store==null) {
			throw new RuntimeException("Central Log Store is not initialized!!!");
		}
		return store;
	}
	
    /**
     * @return the number (sequence, 1..N) of the last log line.
     * If no records are present in the buffer, 0 is returned.
     */
    public static int getLastBufferLineNr() {
    	return getInstance().appender.getLastBufferLineNr();
    }
    
    /**
     * 
     * Get all the log lines pertaining to the specified parent log channel id (including all children)
     * 
     * @param parentLogChannelId the parent log channel ID to grab
     * @param includeGeneral include general log lines
     * @param from
     * @param to
     * @return the log lines found
     */
    public static List<LoggingEvent> getLogBufferFromTo(String parentLogChannelId, boolean includeGeneral, int from, int to) {
    	return getInstance().appender.getLogBufferFromTo(parentLogChannelId, includeGeneral, from, to);
    }

    
    /**
     * Get all the log lines for the specified parent log channel id (including all children)
     * 
     * @param channelId channel IDs to grab
     * @param includeGeneral include general log lines
     * @param from
     * @param to
     * @return
     */
    public static List<LoggingEvent> getLogBufferFromTo(List<String> channelId, boolean includeGeneral, int from, int to) {
    	return getInstance().appender.getLogBufferFromTo(channelId, includeGeneral, from, to);
    }
    
    /**
     * @return The appender that represents the central logging store.  It is capable of giving back log rows in an incremental fashion, etc. 
     */
    public static Log4jBufferAppender getAppender() {
		return getInstance().appender;
	}

    /**
     * Discard all the lines for the specified log channel id AND all the children.
     *  
     * @param parentLogChannelId the parent log channel id to be removed along with all its children.
     */
	public static void discardLines(String parentLogChannelId, boolean includeGeneralMessages) {
		LoggingRegistry registry = LoggingRegistry.getInstance();
		List<String> ids = registry.getLogChannelChildren(parentLogChannelId);

		// Remove all the rows for these ids
		//
		Log4jBufferAppender bufferAppender = getInstance().appender;
		// int beforeSize = bufferAppender.size();
		for (String id : ids) {
			// Remove it from the central log buffer
			//
			bufferAppender.removeChannelFromBuffer(id);
			
			// Also remove the item from the registry.
			//
			registry.getMap().remove(id);
		}
		
		// Now discard the general lines if this is required
		//
		if (includeGeneralMessages) {
			bufferAppender.removeGeneralMessages();
		}
		
		// int afterSize = bufferAppender.size();
		// System.out.println("Bufferlines discarded for parent log channel id ["+parentLogChannelId+"], before="+beforeSize+", after="+afterSize);
		// System.out.println("Left over lines:");
		// System.out.println(bufferAppender.getBuffer().toString());
	}
}
