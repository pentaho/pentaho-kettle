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
package org.pentaho.di.core.lifecycle;

public class LifecycleException extends Exception
{

	private static final long serialVersionUID = 1L;
	
	/**
	 * Indicates an error that prevents the application from starting succesfully.
	 */
	private boolean severe;
	
	/**
	 * 
	 * @param severe true if Spoon should quit because of this exception
	 */
	public LifecycleException(boolean severe)
	{
		this.severe = severe;
	}
	
	/**
	 * 
	 * @param message The (localized) message
	 * @param severe true if Spoon should quit because of this exception
	 */
	public LifecycleException(String message,boolean severe)
	{
		super(message);
		this.severe = severe;
	}
	
	/**
	 * 
	 * @param cause
	 * @param severe true if Spoon should quit because of this exception
	 */
	public LifecycleException(Throwable cause,boolean severe)
	{
		super(cause);
		this.severe = severe;
	}
	
	/**
	 * 
	 * @param message
	 * @param cause
	 * @param severe true if Spoon should quit because of this exception
	 */
	public LifecycleException(String message, Throwable cause,boolean severe)
	{
		super(message, cause);
		this.severe = severe;
	}
	
	/**
	 * 
	 * @return true if Spoon should quit because of this exception
	 */
	public boolean isSevere()
	{
		return severe;
	}

}
