/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/


package org.pentaho.di.trans.steps.propertyoutput;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Properties;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.apache.commons.vfs.FileObject;


/**
 * Output rows to Properties file and create a file.
 * 
 * @author Samatar
 * @since 13-Apr-2008
 */
 
public class PropertyOutputData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface inputRowMeta;
	public RowMetaInterface outputRowMeta;
	private static final String DATE_FORMAT = "yyyy-MM-dd H:mm:ss";
	DateFormat dateParser;
	
	public int indexOfKeyField;
	public int indexOfValueField;
	
	public int indexOfFieldfilename;
	public HashSet<String> KeySet;
	public FileObject file;
	public String filename;
	
	public Properties pro;
	
	public String previousFileName;
    
	public PropertyOutputData()
	{
		super();

		dateParser = new SimpleDateFormat(DATE_FORMAT);
		
		indexOfKeyField=-1;
		indexOfValueField=-1;
		
		indexOfFieldfilename=-1;
		file=null;
		previousFileName="";
		KeySet = new HashSet<String>();
		filename=null;
	}

}
