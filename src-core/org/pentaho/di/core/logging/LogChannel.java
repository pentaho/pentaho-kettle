package org.pentaho.di.core.logging;


public class LogChannel implements LogChannelInterface {
	
	public static LogChannelInterface GENERAL  = new LogChannel("General");
	public static LogChannelInterface METADATA = new LogChannel("Metadata");
	public static LogChannelInterface UI       = new LogChannel("GUI");
	
	private static LogWriter log = LogWriter.getInstance();
	private String logChannelId;
	
	public LogChannel(Object subject) {
		logChannelId = LoggingRegistry.getInstance().registerLoggingSource(subject);
	}
	
	@Override
	public String toString() {
		return logChannelId;
	}
	
	public String getLogChannelId() {
		return logChannelId;
	}
		
    public void logMinimal(String s)
    {
        log.println(new LogMessage(s, logChannelId, LogWriter.LOG_LEVEL_MINIMAL)); //$NON-NLS-1$
    }

    public void logBasic(String s)
    {
        log.println(new LogMessage(s, logChannelId, LogWriter.LOG_LEVEL_BASIC)); //$NON-NLS-1$
    }

    public void logError(String s)
    {
        log.println(new LogMessage(s, logChannelId, LogWriter.LOG_LEVEL_ERROR)); //$NON-NLS-1$
    }

    public void logError(String s, Throwable e)
    {
    	log.println(new LogMessage(s, logChannelId, LogWriter.LOG_LEVEL_ERROR), e); //$NON-NLS-1$
    }

    public void logBasic(String s, Object...arguments) 
    {
    	log.println(new LogMessage(s, logChannelId, arguments, LogWriter.LOG_LEVEL_BASIC));
    }

    public void logDetailed(String s, Object...arguments) 
    {
    	log.println(new LogMessage(s, logChannelId, arguments, LogWriter.LOG_LEVEL_DETAILED));
    }

    public void logError(String s, Object...arguments) 
    {
    	log.println(new LogMessage(s, logChannelId, arguments, LogWriter.LOG_LEVEL_ERROR));
    }

    public void logDetailed(String s)
    {
        log.println(new LogMessage(s, logChannelId, LogWriter.LOG_LEVEL_DETAILED));
    }

    public void logDebug(String s)
    {
        log.println(new LogMessage(s, logChannelId, LogWriter.LOG_LEVEL_DEBUG));
    }
    
	public void logDebug(String message, Object... arguments) {
        log.println(new LogMessage(message, logChannelId, arguments, LogWriter.LOG_LEVEL_DEBUG));
	}

    public void logRowlevel(String s)
    {
        log.println(new LogMessage(s, logChannelId, LogWriter.LOG_LEVEL_ROWLEVEL));
    }

	public void logMinimal(String message, Object... arguments) {
		log.println(new LogMessage(message, logChannelId, arguments, LogWriter.LOG_LEVEL_MINIMAL));
	}

	public void logRowlevel(String message, Object... arguments) {
		log.println(new LogMessage(message, logChannelId, arguments, LogWriter.LOG_LEVEL_ROWLEVEL));
	}

	public boolean isBasic() {
		return log.isBasic();
	}

	public boolean isDebug() {
		return log.isDebug();
	}

	public boolean isDetailed() {
		return log.isDetailed();
	}

	public boolean isRowLevel() {
		return log.isRowLevel();
	}
}
