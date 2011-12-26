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
 

package org.pentaho.di.trans.steps.getfilesrowscount;

import java.io.InputStream;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar Hassan
 * @since 06-Sept-2007
 */
public class GetFilesRowsCountData extends BaseStepData implements StepDataInterface 
{
	public String thisline;
	public RowMetaInterface outputRowMeta;
	public RowMetaInterface convertRowMeta;
	public Object[] previousRow;

	public FileInputList        files;
	public boolean              last_file;
	public FileObject           file;
	public  long                filenr;
	
	public InputStream 			fr;
    public long                rownr;
    public int fileFormatType;
    public StringBuffer lineStringBuffer;
    public int totalpreviousfields;
    public int indexOfFilenameField;
    public Object[] readrow;
    public RowMetaInterface inputRowMeta;
    public char separator;
    
    public boolean foundData;

	/**
	 * 
	 */
	public GetFilesRowsCountData()
	{
		super();
		previousRow = null;
		thisline=null;
		previousRow=null;
		
		fr=null;
		lineStringBuffer = new StringBuffer(256);
		totalpreviousfields=0;
		indexOfFilenameField=-1;
		readrow=null;
		separator='\n';
		foundData=false;
	}
}