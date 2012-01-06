/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.logging;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.commons.vfs.FileObject;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;




/**
 * This class handles the logging.
 * 
 * @author Matt
 * @since 25-04-2003
 *
 */
public class LogWriter 
{
	// private static Class<?> PKG = LogWriter.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static LogWriter logWriter;
		
	public static final String STRING_PENTAHO_DI_LOGGER_NAME = "org.pentaho.di";
	public static final String STRING_PENTAHO_DI_CONSOLE_APPENDER = "ConsoleAppender:"+STRING_PENTAHO_DI_LOGGER_NAME;
	
	// String...
	private int type;
	private String filter;
    
    // Log4j
    private Logger               pentahoLogger;
    private Log4jFileAppender    fileAppender;
    
    private File realFilename;

    private static Layout layout;

	// synchronizing logWriter singleton instance PDI-6840
	synchronized public static final LogWriter getInstance()
	{
		if (logWriter != null)
        {
            return logWriter;
        }
		
		logWriter = new LogWriter();
		
		return logWriter;
	}
    
    private LogWriter()
    {
        pentahoLogger = Logger.getLogger(STRING_PENTAHO_DI_LOGGER_NAME);
        pentahoLogger.setAdditivity(false);

        // ensure all messages get logged in this logger since we filtered it above
        // we do not set the level in the rootLogger so the rootLogger can decide by itself (e.g. in the platform) 
		//
        pentahoLogger.setLevel(Level.ALL); 

        layout = new Log4jKettleLayout();
        
        // Add a console logger to see something on the console as well...
        //
        boolean consoleAppenderFound = false;
        Enumeration<?> appenders = pentahoLogger.getAllAppenders();
        while (appenders.hasMoreElements()) {
			Appender appender = (Appender) appenders.nextElement();
			if (appender instanceof ConsoleAppender) {
				consoleAppenderFound = true;
				break;
			}
		}
        
        // Play it safe, if another console appender exists for org.pentaho, don't add another one...
        //
        if (!consoleAppenderFound) {
        	Layout patternLayout = new PatternLayout("%-5p %d{dd-MM HH:mm:ss,SSS} - %m%n");
        	ConsoleAppender consoleAppender = new ConsoleAppender(patternLayout);
        	consoleAppender.setName(STRING_PENTAHO_DI_CONSOLE_APPENDER);
        	pentahoLogger.addAppender(consoleAppender);
        }
        
        // Get rid of the VFS info messages...
        //
        LogManager.getLogger("org.apache.commons.vfs").setLevel(Level.WARN);
        
        // Hide info messages from Jetty too...
        //
        // LogManager.getLogger("org.mortbay.log").setLevel(Level.WARN);
        // LogManager.getLogger("org.slf4j").setLevel(Level.WARN);
        
        
    }

	/**
	 * Get a new log instance for the specified file if it is not open yet! 
	 * @param filename The log file to open
     * @param exact is this an exact filename (false: prefix of name in temp directory)
	 * @return the LogWriter object
	 */
	// synchronizing logWriter singleton instance PDI-6840
	synchronized public static final LogWriter getInstance(String filename, boolean exact) throws KettleException
	{
		if (logWriter != null) 
	    {
			// OK, see if we have a file appender already for this 
			//
			if (logWriter.pentahoLogger.getAppender(LogWriter.createFileAppenderName(filename, exact))==null)
			{
				logWriter.fileAppender = createFileAppender(filename, exact);
                logWriter.addAppender(logWriter.fileAppender);
			}
			return logWriter;
	    }
		
		logWriter = new LogWriter(filename, exact);
		return logWriter;
	}
	
	/**
	 * Closes the file appender opened by the getInstance(filename, exact, level) method
	 */
	public static final void closeAndRemoveFileAppender() {
		if (logWriter.fileAppender!=null) {
			logWriter.fileAppender.close();
			logWriter.pentahoLogger.removeAppender(logWriter.fileAppender);
		}
	}
	
	private LogWriter(String filename, boolean exact)
	{
        this();
        
		try
		{
            fileAppender = createFileAppender(filename, exact); 
			addAppender(fileAppender);
		}
		catch(Exception e)
		{
			System.out.println("ERROR OPENING LOG FILE ["+filename+"] --> "+e.toString());
		}
	}
	
	/**
	 * Create a file appender 
	 * @param filename The (VFS) filename (URL) to write to.
	 * @param exact is this an exact filename of a filename to be stored in "java.io.tmp"
	 * @return A new file appender
	 * @throws KettleFileException In case there is a problem opening the file.
	 */
	public static final Log4jFileAppender createFileAppender(String filename, boolean exact) throws KettleFileException
	{
		try
		{
            FileObject file;
	        if (!exact)
	        {
	            file = KettleVFS.createTempFile(filename, ".log", System.getProperty("java.io.tmpdir"));
	        }
	        else
	        {
	            file = KettleVFS.getFileObject(filename);
	        }
	        
	        Log4jFileAppender appender = new Log4jFileAppender(file);
	        appender.setLayout(new Log4jKettleLayout(true));
	        appender.setName(LogWriter.createFileAppenderName(filename, exact));
            
            return appender;
		}
		catch(IOException e)
		{
			throw new KettleFileException("Unable to add Kettle file appender to Log4J", e);
		}
    }
	/**
	 * Create a file appender 
	 * @param filename The (VFS) filename (URL) to write to.
	 * @param exact is this an exact filename of a filename to be stored in "java.io.tmp"
	 * @param append
	 * @return A new file appender
	 * @throws KettleFileException In case there is a problem opening the file.
	 */
	public static final Log4jFileAppender createFileAppender(String filename, boolean exact,boolean append) throws KettleFileException
	{
		try
		{
            FileObject file;
	        if (!exact)
	        {
	            file = KettleVFS.createTempFile(filename, ".log", System.getProperty("java.io.tmpdir"));
	        }
	        else
	        {
	            file = KettleVFS.getFileObject(filename);
	        }
	        
	        Log4jFileAppender appender = new Log4jFileAppender(file,append);
	        appender.setLayout(new Log4jKettleLayout(true));
	        appender.setName(LogWriter.createFileAppenderName(filename, exact));
            
            return appender;
		}
		catch(IOException e)
		{
			throw new KettleFileException("Unable to add Kettle file appender to Log4J", e);
		}
    }
	
	public static final String createFileAppenderName(String filename, boolean exact)
	{
		if (!exact)
		{
			return "<temp file> : "+filename;
		}
		else
		{
			return filename;
		}
	}
    
	/**
	 * 
	 * @return a new String appender object capable of capturing the log stream.
	 * It starts to work the instant you add this appender to the Kettle logger.
	 * 
	 * @deprecated Please use {@link CentralLogStore.getAppender()} instead.  This uses a central logging buffer in stead of a distributed one.
	 * It also supports incremental buffer gets, and much more.
	 */
    public static final Log4jStringAppender createStringAppender()
    {
        Log4jStringAppender appender = new Log4jStringAppender();
        appender.setLayout(new Log4jKettleLayout(true));
        
        return appender;
    }

    public static void setConsoleAppenderDebug() {
        Enumeration<?> appenders = Logger.getLogger(STRING_PENTAHO_DI_LOGGER_NAME).getAllAppenders();
        
        while(appenders.hasMoreElements())
        {
            Object appender = appenders.nextElement();
            if (appender instanceof ConsoleAppender || appender instanceof Log4jConsoleAppender) {
                if(appender instanceof ConsoleAppender) {
                    ((ConsoleAppender)appender).setThreshold(Level.DEBUG);
                }
            }
        }
    }
    
	public int getType()
	{
		return type;
	}
	
	public void setType(int type)
	{
		this.type = type;
	}

	public boolean close()
	{
		boolean retval=true;
		try
		{
			// Close all appenders...
            Logger logger = Logger.getLogger(STRING_PENTAHO_DI_LOGGER_NAME);
            Enumeration<?> appenders = logger.getAllAppenders();
            while (appenders.hasMoreElements())
            {
                Appender appender = (Appender) appenders.nextElement();
                appender.close();
            }
            pentahoLogger.removeAllAppenders();
            LogWriter.unsetLogWriter();
		}
		catch(Exception e) 
		{ 
			retval=false; 
		}
		
		return retval;
	}
	// synchronizing logWriter singleton instance PDI-6840
	synchronized static private void unsetLogWriter(){
		logWriter = null;
	}
	
	/**
	 * 
	 * @param logMessage
	 * @param channelLogLevel
	 */
	public void println(LogMessageInterface logMessage, LogLevel channelLogLevel)
	{
	  String subject = null;
	  
	  LogLevel logLevel = logMessage.getLevel();
	  
		if (!logLevel.isVisible(channelLogLevel)) {
		  return; // not for our eyes.
		}
		
		if (subject==null) subject="Kettle";
        
		// Are the message filtered?
    //
		if (!logLevel.isError() && !Const.isEmpty(filter))
        {
            if (subject.indexOf(filter)<0 && logMessage.toString().indexOf(filter)<0)
            {
                return; // "filter" not found in row: don't show!
            }
        }
		        
        switch(logLevel)
        {
        case ERROR:    pentahoLogger.error(logMessage); break;
        case ROWLEVEL: 
        case DEBUG:    pentahoLogger.debug(logMessage); break;
        default:       pentahoLogger.info(logMessage); break;
        }
	}

	public void println(LogMessageInterface message, Throwable e, LogLevel channelLogLevel) { 
		println(message, channelLogLevel); 

		String stackTrace = Const.getStackTracker(e);
		LogMessage traceMessage = new LogMessage(stackTrace, message.getLogChannelId(), LogLevel.ERROR);
		println(traceMessage, channelLogLevel); 
	}
	
	public void setFilter(String filter)
	{
        this.filter=filter;
	}
	
	public String getFilter()
	{
		return filter;
	}

    /**
     * This is not thread safe: please try to get the file appender yourself using the static constructor and work from there
     */
	public InputStream getFileInputStream() throws IOException
	{
		return KettleVFS.getInputStream(fileAppender.getFile());
	}

    /**
     * This is not thread safe: please try to get the file appender yourself using the static constructor and work from there
     */
	public FileObject getFileAppenderFile() throws IOException
	{
		return fileAppender.getFile();
	}

    /**
     * Get the file input stream for a certain appender.
     * The appender is looked up using the filename
     * @param filename The exact filename (with path: c:\temp\logfile.txt) or just a filename (spoon.log)
     * @param exact true if this is the exact filename or just the last part of the complete path.
     * @return The file input stream of the appender
     * @throws IOException in case the appender ocan't be found
     */
    public FileInputStream getFileInputStream(String filename, boolean exact) throws IOException
    {
        Appender appender = pentahoLogger.getAppender(createFileAppenderName(filename, exact));
        if (appender==null)
        {
            throw new IOException("Unable to find appender for file: "+filename+" (exact="+exact+")");
        }
        return new FileInputStream( ((Log4jFileAppender)appender).getFile().getName().getPathDecoded() );
    }

    /**
     * @return Returns the realFilename.
     */
    public File getRealFilename()
    {
        return realFilename;
    }

    /**
     * @param realFilename The realFilename to set.
     */
    public void setRealFilename(File realFilename)
    {
        this.realFilename = realFilename;
    }

    public void addAppender(Appender appender)
    {
        pentahoLogger.addAppender(appender);
    }
    
    public void removeAppender(Appender appender)
    {
        pentahoLogger.removeAppender(appender);
    }
    
    public static void setLayout(Layout layout)
    {
        LogWriter.layout = layout; // save for later creation of new files...
        
        Enumeration<?> appenders = logWriter.pentahoLogger.getAllAppenders();
        while (appenders.hasMoreElements())
        {
            Appender appender = (Appender) appenders.nextElement();
            if (appender instanceof Log4jConsoleAppender  ||
                appender instanceof Log4jFileAppender ||
                appender instanceof Log4jBufferAppender
               )
            {
                appender.setLayout(layout);
            }
        }
    }
    
    public static Layout getLayout()
    {
        return layout;
    }
}
