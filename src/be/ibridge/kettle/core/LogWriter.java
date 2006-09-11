 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 

package be.ibridge.kettle.core;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import be.ibridge.kettle.core.logging.Log4jConsoleAppender;
import be.ibridge.kettle.core.logging.Log4jFileAppender;
import be.ibridge.kettle.core.logging.Log4jKettleLayout;
import be.ibridge.kettle.core.logging.Log4jMessage;
import be.ibridge.kettle.core.logging.Log4jStringAppender;


/**
 * This class handles the logging.
 * 
 * @author Matt
 * @since 25-04-2003
 *
 */
public class LogWriter 
{
	private static LogWriter lastLog;
	private static Hashtable logs = new Hashtable();
	
	public static final int LOG_LEVEL_ERROR      =  0;
	public static final int LOG_LEVEL_NOTHING    =  1;
	public static final int LOG_LEVEL_MINIMAL    =  2;
	public static final int LOG_LEVEL_BASIC      =  3;
	public static final int LOG_LEVEL_DETAILED   =  4;
	public static final int LOG_LEVEL_DEBUG      =  5;
	public static final int LOG_LEVEL_ROWLEVEL   =  6;
	
	public static final String logLevelDescription[] = 
		{
			"Error",
			"Nothing",
			"Minimal",
			"Basic",
			"Detailed",
			"Debug",
			"Rowlevel"
		};

	public static final String log_level_desc_long[] = 
		{
			"Error logging only",
			"Nothing at all",
			"Minimal logging",
			"Basic logging",
			"Detailed logging",
			"Debugging",
			"Rowlevel (very detailed)"
		};



	// Stream
	private OutputStream stream;
	
	// File
	private String filename;
	private File file;  // Write to a certain file...
	
	// String...
	private int type;
	private int level;
	private String filter;
    private boolean exact;
    
    // Log4j
    private Logger               rootLogger;
    private Log4jFileAppender    fileAppender;
    private Log4jConsoleAppender consoleAppender;
    private Log4jStringAppender  stringAppender;
    
    private Log4jKettleLayout    layout;

	private static final String NO_FILE_NAME = "-";
	
    private File realFilename;

	private static final LogWriter findLogWriter(String filename)
	{
		return (LogWriter)logs.get(filename);
	}
	
	public static final LogWriter getInstance()
	{
		if (lastLog!=null) return lastLog;
		
		throw new RuntimeException("The logging system is not initialized!");
	}
	
	public static final LogWriter getInstance(int lvl)
	{
		LogWriter log = findLogWriter(NO_FILE_NAME);
		
		if (log != null) return log;
		
		lastLog = new LogWriter(lvl);
		logs.put(NO_FILE_NAME, lastLog);
		
		return lastLog;
	}
    
    private LogWriter()
    {
        rootLogger = Logger.getRootLogger();
        
        layout = new Log4jKettleLayout(true);

        consoleAppender = new Log4jConsoleAppender();
        consoleAppender.setLayout(layout);
        consoleAppender.setName("AppendToConsole");

        stringAppender  = new Log4jStringAppender();
        stringAppender.setLayout(layout);
        stringAppender.setName("AppendToString");
    }

	// Default: screen --> out
	private LogWriter(int lvl)
	{
        this();
        
        rootLogger.addAppender(consoleAppender);
        
		level  = lvl;
		filter = null;
	}
    
    public static final LogWriter getInstance(int lvl, OutputStream stream)
    {
        LogWriter log = findLogWriter(NO_FILE_NAME);
        
        if (log != null) return log;
        
        lastLog = new LogWriter(lvl);
        lastLog.stream = stream;
        logs.put(NO_FILE_NAME, lastLog);
        
        return lastLog;
    }


	/**
	 * Get a new log instance for the specified file if it is not open yet! 
	 * @param filename The log file to open
     * @param exact is this an exact filename (false: prefix of name in temp directory)
	 * @param level The log level
	 * @return the LogWriter object
	 */
	public static final LogWriter getInstance(String filename, boolean exact, int level)
	{
		LogWriter log = findLogWriter(filename);
		
		if (log != null) return log;
		
		lastLog = new LogWriter(filename, exact, level);
		logs.put(filename, lastLog);
		return lastLog;
	}
	
	private LogWriter(String filename, boolean exact, int level)
	{
        this();
        
		this.filename = filename;
		this.level = level;
        this.exact = exact;
                
		try
		{
            if (!exact)
            {
                file = File.createTempFile(filename+".", ".log");
                file.deleteOnExit();
            }
            else
            {
                file = new File(filename);
            }
            realFilename = file.getAbsoluteFile();

            fileAppender = new Log4jFileAppender(realFilename);
            fileAppender.setLayout(layout);
            fileAppender.setName("AppendToFile");
                        
            rootLogger.addAppender(fileAppender);
		}
		catch(Exception e)
		{
			System.out.println("ERROR OPENING LOG FILE ["+filename+"] --> "+e.toString());
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

    public boolean isExact()
    {
        return exact;
    }
	
	public boolean close()
	{
		boolean retval=true;
		try
		{
			stream.close();
			// Remove this one from the hashtable...
			logs.remove(getFilename());
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
	
	public void enableTime()
	{
        layout.setTimeAdded(true);
	}

	public void disableTime()
	{
		layout.setTimeAdded(false);
	}
	
	public boolean getTime()
	{
        return layout.isTimeAdded();
	}

	public void setTime(boolean tim)
	{
        layout.setTimeAdded(tim);
	}

	public String getFilename()
	{
		if (filename!=null)
		{
			return filename;
		}
		else
		{
			return NO_FILE_NAME;
		}
	}
	
	public void println(int lvl, String msg)
	{
		println(lvl, "General", msg);
	}
	
	public void println(int lvl, String subj, String msg)
	{
        String subject = subj;
        if (subject==null) subject="Kettle";
        
		// Are the message filtered?
		if (filter!=null && filter.length()>0)
        {
            if (subject.indexOf(filter)<0 && msg.indexOf(filter)<0)
            {
                return; // "filter" not found in row: don't show!
            }
        }
		    
		if (level==0) return;  // Nothing, not even errors...
		if (level<lvl) return; // not for our eyes.
		
        
        Logger logger = Logger.getLogger(subject);
        
        Log4jMessage message = new Log4jMessage(msg, subject, lvl);
        
        switch(level)
        {
        case LOG_LEVEL_ERROR:    logger.error(message); break;
        case LOG_LEVEL_ROWLEVEL: 
        case LOG_LEVEL_DEBUG:    logger.debug(message); break;
        default:                 logger.info(message); break;
        }
	}
	
    public void logMinimal(String subject, String message)  { println(LOG_LEVEL_MINIMAL, subject, message) ; }
	public void logBasic(String subject, String message)    { println(LOG_LEVEL_BASIC, subject, message) ; }
	public void logDetailed(String subject, String message) { println(LOG_LEVEL_DETAILED, subject, message); }
	public void logDebug(String subject, String message)    { println(LOG_LEVEL_DEBUG, subject, message); }
	public void logRowlevel(String subject, String message) { println(LOG_LEVEL_ROWLEVEL, subject, message); }
	public void logError(String subject, String message)    { println(LOG_LEVEL_ERROR, subject, message); }
	
    /** @deprecated */
	public Object getStream()
	{
        return fileAppender.getFileOutputStream();
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
	
	public FileInputStream getFileInputStream() throws IOException
	{
		return new FileInputStream(fileAppender.getFile());
	}
    
    public boolean isBasic()
    {
        return level==LOG_LEVEL_BASIC;
    }

    public boolean isDetailed()
    {
        return level==LOG_LEVEL_DETAILED;
    }

    public boolean isDebug()
    {
        return level==LOG_LEVEL_DEBUG;
    }

    public boolean isRowLevel()
    {
        return level==LOG_LEVEL_ROWLEVEL;
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
    

    public void startStringCapture()
    {
        Logger logger = Logger.getRootLogger();
        logger.addAppender(stringAppender);
    }
    

    public void endStringCapture()
    {
        Logger logger = Logger.getRootLogger();
        logger.removeAppender(stringAppender);
    }

    /**
     * @return The logging text from since startStringCapture() is called until endStringCapture().
     */
    public String getString()
    {
        return stringAppender.getBuffer().toString();
    }

    public void setString(String string)
    {
        stringAppender.setBuffer(new StringBuffer(string));
    }
}
