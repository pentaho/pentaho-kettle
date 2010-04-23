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
 

package org.pentaho.di.trans.steps.loadfileinput;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Samatar
 * @since 21-06-2007
 */
public class LoadFileInputData extends BaseStepData implements StepDataInterface 
{
	public RowMetaInterface outputRowMeta;
	public RowMetaInterface convertRowMeta;
	public String thisline, nextline, lastline;
	public Object[] previousRow;
	public int nr_repeats;

	
	public FileInputList files;
	public boolean last_file;
	public FileObject file;
	public int     filenr;

    public long rownr;
    public int indexOfFilenameField;
    public int totalpreviousfields;
    public int nrInputFields;

    public Object[] readrow;
    
    public String filecontent;
    
    public long fileSize;
    
    public RowMetaInterface inputRowMeta;


	/**
	 * 
	 */
	public LoadFileInputData()
	{
		super();

		nr_repeats=0;
		previousRow=null;
		filenr = 0;
		

		totalpreviousfields=0;
		indexOfFilenameField=-1;
		nrInputFields=-1;

		readrow=null;
		fileSize=0;

	}

}
