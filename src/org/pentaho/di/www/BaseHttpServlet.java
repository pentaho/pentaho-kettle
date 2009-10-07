package org.pentaho.di.www;

import javax.servlet.http.HttpServlet;

import org.pentaho.di.core.logging.LogMessage;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.logging.LoggingRegistry;

public class BaseHttpServlet extends HttpServlet {
	
	protected static final long	serialVersionUID	= -1348342810327662788L;
	private static LogWriter log = LogWriter.getInstance();
    private static LoggingRegistry registry = LoggingRegistry.getInstance();
    
    private String loggingId; 
    
    private void checkLoggingId() {
    	if (loggingId==null) {
    		loggingId = registry.registerLoggingSource(this);
    		
    		try {
    			throw new Exception();
    		} catch(Exception e) {
    			StackTraceElement[] stackTrace = e.getStackTrace();
    			for (StackTraceElement element : stackTrace) {
    				System.out.println(element.getClass().toString());
    			}
    		}
    	}
    }

    public void logMinimal(String s)
    {
    	checkLoggingId();
        log.println(new LogMessage(s, loggingId, LogWriter.LOG_LEVEL_MINIMAL)); //$NON-NLS-1$
    }

    public void logBasic(String s)
    {
    	checkLoggingId();
        log.println(new LogMessage(s, loggingId, LogWriter.LOG_LEVEL_BASIC)); //$NON-NLS-1$
    }

    public void logError(String s)
    {
    	checkLoggingId();
        log.println(new LogMessage(s, loggingId, LogWriter.LOG_LEVEL_ERROR)); //$NON-NLS-1$
    }

    public void logError(String s, Throwable e)
    {
    	checkLoggingId();
    	log.println(new LogMessage(s, loggingId, LogWriter.LOG_LEVEL_ERROR), e); //$NON-NLS-1$
    }

    public void logBasic(String s, Object...arguments) 
    {
    	checkLoggingId();
    	log.println(new LogMessage(s, loggingId, arguments, LogWriter.LOG_LEVEL_BASIC));
    }

    public void logDetailed(String s, Object...arguments) 
    {
    	checkLoggingId();
    	log.println(new LogMessage(s, loggingId, arguments, LogWriter.LOG_LEVEL_DETAILED));
    }

    public void logError(String s, Object...arguments) 
    {
    	checkLoggingId();
    	log.println(new LogMessage(s, loggingId, arguments, LogWriter.LOG_LEVEL_ERROR));
    }

    public void logDetailed(String s)
    {
    	checkLoggingId();
        log.println(new LogMessage(s, loggingId, LogWriter.LOG_LEVEL_DETAILED));
    }

    public void logDebug(String s)
    {
    	checkLoggingId();
        log.println(new LogMessage(s, loggingId, LogWriter.LOG_LEVEL_DEBUG));
    }

    public void logRowlevel(String s)
    {
    	checkLoggingId();
        log.println(new LogMessage(s, loggingId, LogWriter.LOG_LEVEL_ROWLEVEL));
    }

}
