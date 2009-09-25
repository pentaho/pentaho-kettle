
/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.execprocess;


/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class ProcessResult
{
   	private String outputStream;
   	private String errorStream;
   	private long exitValue;
    
	/**
	 * 
	 */
	public ProcessResult()
	{
		super();
		 this.outputStream=null;
		 this.errorStream=null;
		 this.exitValue=1;
	}

	public String getOutputStream()
	{
		return this.outputStream;
	}
	public void setOutputStream(String string)
	{
		this.outputStream=string;
	}
	public String getErrorStream()
	{
		return this.errorStream;
	}
	public void setErrorStream(String string)
	{
		this.errorStream=string;
	}
	public long getExistStatus()
	{
		return this.exitValue;
	}
	public void setExistStatus(long value)
	{
		this.exitValue=value;
	}
}
