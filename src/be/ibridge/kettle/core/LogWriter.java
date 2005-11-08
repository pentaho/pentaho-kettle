 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/
 

package be.ibridge.kettle.core;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;


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

	public static final int LOG_TYPE_STREAM      =  1;
	public static final int LOG_TYPE_PIPE        =  2;

	// Stream
	private OutputStream stream;
	private PipedOutputStream pstream;
	
	// File
	private String filename;
	private File file;  // Write to a certain file...
	
	// String...
	private boolean capture_string;
	private String string;

	private int type;
	private int level;
	private String filter;
	private boolean time_enabled;
    private boolean exact;

	private static final String NO_FILE_NAME = "-";
	
	private SimpleDateFormat sdf; // Used for date conversion... (optimise: only init once!)
    
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
	
	// Default: screen --> out
	private LogWriter(int lvl)
	{
		stream = System.out;
		level  = lvl;
		type   = LOG_TYPE_STREAM;
		filter = null;
		time_enabled=true;
		sdf=null;
		capture_string = false;
		string = "";
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
		this.filename = filename;
		this.level = level;
        this.exact = exact;
        
		type  = LOG_TYPE_STREAM;
		
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
			stream = new FileOutputStream(file);
		}
		catch(Exception e)
		{
			System.out.println("ERROR OPENING LOG FILE ["+filename+"] --> "+e.toString());
		}
		time_enabled=true;
		sdf=null;
		capture_string = false;
		string="";
	}
	
	public int getType()
	{
		return type;
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public void setStringCapture(boolean capture_string)
	{
		this.capture_string = capture_string;
	}
	
	public boolean getStringCapture()
	{
		return capture_string;
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
		time_enabled=true;
	}

	public void disableTime()
	{
		time_enabled=false;
	}
	
	public boolean getTime()
	{
		return time_enabled;
	}

	public void setTime(boolean tim)
	{
		time_enabled=tim;
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
	
	public String getString()
	{
		return string;
	}
	
	public void setString(String string)
	{
		this.string = string;
	}


	public void println(int lvl, String msg)
	{
		println(lvl, "General", msg);
	}
	
	public void println(int lvl, String subject, String msg)
	{
		// Are the message filtered?
		if (filter!=null && 
		    filter.length()>0 && 
		    subject.indexOf(filter)<0 && 
		    msg.indexOf(filter)<0
		    ) return; // "filter" not found in row: don't show!
		    
		if (level==0) return;  // Nothing, not even errors...
		if (level<lvl) return; // not for our eyes.
		
		String o;
		String ti="";
		
		if (time_enabled)
		{
			if (sdf==null) // init
			{
				sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
			}
			ti=sdf.format(new Date() )+" - ";
		}
				
		if (lvl!=LOG_LEVEL_ERROR) 
			o=ti+subject+" - "+msg + Const.CR;
		else
			o=ti+subject+" - ERROR: "+msg + Const.CR ;

		if (capture_string) string+=o;

		try
		{
			switch(type)
			{
			case LOG_TYPE_STREAM: stream.write( o.getBytes() ); stream.flush(); break;
			case LOG_TYPE_PIPE  : pstream.write( o.getBytes() ); pstream.flush(); break;
			default: break;
			}			
		}
		catch(Exception e)
		{
			System.out.println("ERROR WRITING TO LOG! -> "+e.toString());
		}
	}
	
	public void logBasic(String subject, String message) { println(LOG_LEVEL_BASIC, subject, message) ; }
	public void logDetailed(String subject, String message) { println(LOG_LEVEL_DETAILED, subject, message); }
	public void logDebug(String subject, String message) { println(LOG_LEVEL_DEBUG, subject, message); }
	public void logRowlevel(String subject, String message) { println(LOG_LEVEL_ROWLEVEL, subject, message); }
	public void logError(String subject, String message) { println(LOG_LEVEL_ERROR, subject, message); }
	
	public Object getStream()
	{
		if (type == LOG_TYPE_STREAM) return stream;
		return pstream;
	}
	
	public void setFilter(String flt)
	{
		filter=flt;
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
		return new FileInputStream(file);
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
    

}
