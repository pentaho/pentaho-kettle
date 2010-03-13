 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

 

package org.pentaho.di.core.logging;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
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
	private static LogWriter logWriter;
	
	public static final int LOG_LEVEL_NOTHING    =  0;
    public static final int LOG_LEVEL_ERROR      =  1;
	public static final int LOG_LEVEL_MINIMAL    =  2;
	public static final int LOG_LEVEL_BASIC      =  3;
	public static final int LOG_LEVEL_DETAILED   =  4;
	public static final int LOG_LEVEL_DEBUG      =  5;
	public static final int LOG_LEVEL_ROWLEVEL   =  6;
	
	public static final String logLevelDescription[] = 
		{
			"Nothing",
            "Error",
			"Minimal",
			"Basic",
			"Detailed",
			"Debug",
			"Rowlevel",
		};

	public static final String log_level_desc_long[] = 
		{
            Messages.getString("LogWriter.Level.Nothing.LongDesc"),
            Messages.getString("LogWriter.Level.Error.LongDesc"),
            Messages.getString("LogWriter.Level.Minimal.LongDesc"),
            Messages.getString("LogWriter.Level.Basic.LongDesc"),
            Messages.getString("LogWriter.Level.Detailed.LongDesc"),
            Messages.getString("LogWriter.Level.Debug.LongDesc"),
            Messages.getString("LogWriter.Level.Rowlevel.LongDesc"),
		};
	
	public static final String STRING_PENTAHO_DI_LOGGER_NAME = "org.pentaho.di";
	public static final String STRING_PENTAHO_DI_CONSOLE_APPENDER = "ConsoleAppender:"+STRING_PENTAHO_DI_LOGGER_NAME;
	
	// String...
	private int type;
	private int level;
	private String filter;
    
    // Log4j
    private Logger               pentahoLogger;
    private Log4jFileAppender    fileAppender;
    
    private File realFilename;

    private static Layout layout;

    public static final LogWriter getInstance()
	{
		if (logWriter!=null) return logWriter;
		
        return getInstance(LOG_LEVEL_BASIC);
	}
	
	public static final LogWriter getInstance(int lvl)
	{
		if (logWriter != null)
        {
            logWriter.setLogLevel(lvl);
            return logWriter;
        }
		
		logWriter = new LogWriter(lvl);
		
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
        
    }

	// Default: screen --> out
	private LogWriter(int lvl)
	{
        this();

		level  = lvl;
		filter = null;
	}

	/**
	 * Get a new log instance for the specified file if it is not open yet! 
	 * @param filename The log file to open
     * @param exact is this an exact filename (false: prefix of name in temp directory)
	 * @param level The log level
	 * @return the LogWriter object
	 */
	public static final LogWriter getInstance(String filename, boolean exact, int level) throws KettleException
	{
		if (logWriter != null) 
	    {
			// Set the logging level as requested.
			//
			logWriter.setLogLevel(level);
			
			// OK, see if we have a file appender already for this 
			//
			if (logWriter.pentahoLogger.getAppender(LogWriter.createFileAppenderName(filename, exact))==null)
			{
				logWriter.fileAppender = createFileAppender(filename, exact);
                logWriter.addAppender(logWriter.fileAppender);
			}
			return logWriter;
	    }
		
		logWriter = new LogWriter(filename, exact, level);
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
	
	private LogWriter(String filename, boolean exact, int level)
	{
        this();
        
		this.level = level;
                
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
                    ((ConsoleAppender)appender).setThreshold(Priority.DEBUG);
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
            logWriter=null;
		}
		catch(Exception e) 
		{ 
			retval=false; 
		}
		
		return retval;
	}
	
	public void setLogLevel(int lvl)
	{
		level = lvl;
	}

	public void setLogLevel(String lvl)
	{
		level = getLogLevel(lvl);
	}
	
	public int getLogLevel()
	{
		return level;
	}

	public String getLogLevelDesc()
	{
		return logLevelDescription[level];
	}
	
	public String getLogLevelLongDesc()
	{
		return log_level_desc_long[level];
	}
	
	public void println(int lvl, String msg)
	{
		println(lvl, "General", msg);
	}
	
	public void println(int lvl, String subj, String msg, Object... args)
	{
		// do cheapest filtering checks first
		if (level==LOG_LEVEL_NOTHING) return;  // Nothing, not even errors...
		if (level<lvl) return; // not for our eyes.
		
        String subject = subj;
        if (subject==null) subject="Kettle";
        
        msg = (args.length <= 0) ? msg : MessageFormat.format(msg, args);
        
		// Are the message filtered?
		if (lvl!=LOG_LEVEL_ERROR && !Const.isEmpty(filter))
        {
            if (subject.indexOf(filter)<0 && msg.indexOf(filter)<0)
            {
                return; // "filter" not found in row: don't show!
            }
        }
		        
        Log4jMessage message = new Log4jMessage(msg, subject, lvl);
        
        switch(lvl)
        {
        case LOG_LEVEL_ERROR:    pentahoLogger.error(message); break;
        case LOG_LEVEL_ROWLEVEL: 
        case LOG_LEVEL_DEBUG:    pentahoLogger.debug(message); break;
        default:                 pentahoLogger.info(message); break;
        }
	}
	
	public void logMinimal(String subject, String message, Object... args)  { println(LOG_LEVEL_MINIMAL, subject, message, args) ; }
    public void logBasic(String subject, String message, Object... args)    { println(LOG_LEVEL_BASIC, subject, message, args) ; }
	public void logDetailed(String subject, String message, Object... args) { println(LOG_LEVEL_DETAILED, subject, message, args); }
	public void logDebug(String subject, String message, Object... args)    { println(LOG_LEVEL_DEBUG, subject, message, args); }
	public void logRowlevel(String subject, String message, Object... args) { println(LOG_LEVEL_ROWLEVEL, subject, message, args); }
	public void logError(String subject, String message, Object... args)    { println(LOG_LEVEL_ERROR, subject, message, args); }
	public void logError(String subject, String message, Throwable e) { 
		String stackTrace = Const.getStackTracker(e);
		println(LOG_LEVEL_ERROR, subject, message); 
		println(LOG_LEVEL_ERROR, subject, stackTrace); 
	}
	
	public void setFilter(String filter)
	{
        this.filter=filter;
	}
	
	public String getFilter()
	{
		return filter;
	}

	public static final int getLogLevel(String lvl)
	{
		if (lvl==null) return LOG_LEVEL_ERROR;
		for (int i=0;i<logLevelDescription.length;i++)
		{
			if (logLevelDescription[i].equalsIgnoreCase(lvl)) return i;
		}
		for (int i=0;i<log_level_desc_long.length;i++)
		{
			if (log_level_desc_long[i].equalsIgnoreCase(lvl)) return i;
		}
		
		return LOG_LEVEL_BASIC;
	}

	public static final String getLogLevelDesc(int l)
	{
		if (l<0 || l>=logLevelDescription.length) return logLevelDescription[LOG_LEVEL_BASIC];
		return logLevelDescription[l];
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
    
    public boolean isBasic()
    {
        return level>=LOG_LEVEL_BASIC;
    }

    public boolean isDetailed()
    {
        return level>=LOG_LEVEL_DETAILED;
    }

    public boolean isDebug()
    {
        return level>=LOG_LEVEL_DEBUG;
    }

    public boolean isRowLevel()
    {
        return level>=LOG_LEVEL_ROWLEVEL;
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
                appender instanceof Log4jStringAppender
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
    
    public List<Appender> getAppenders() {
    	
    	List<Appender> list = new ArrayList<Appender>();
    	
        Enumeration<?> appenders = logWriter.pentahoLogger.getAllAppenders(); 
        while (appenders.hasMoreElements()) {
        	list.add((Appender)appenders.nextElement());
        }
        return list;
    }
}
