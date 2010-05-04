/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
