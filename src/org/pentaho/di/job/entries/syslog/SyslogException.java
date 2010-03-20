package org.pentaho.di.job.entries.syslog;

import org.pentaho.di.core.exception.KettleException;


/**
 * This exception is throws when and error is found in a Syslog sending process.
 * 
 * @author Samatar
 * @since 01-01-2010
 *
 */

public class SyslogException  extends KettleException {
	
    public static final long serialVersionUID = -1;
    
    /**
	 * Constructs a new throwable with null as its detail message.
	 */
	public SyslogException()
	{
		super();
	}

	/**
	 * Constructs a new throwable with the specified detail message.
	 * @param message - the detail message. The detail message is saved for later retrieval by the getMessage() method.
	 */
	public SyslogException(String message)
	{
		super(message);
	}

}
