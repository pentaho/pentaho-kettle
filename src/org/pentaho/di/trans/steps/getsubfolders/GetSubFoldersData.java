/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.getsubfolders;


import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.apache.commons.vfs.FileObject;

/**
 * @author Samatar
 * @since 18-July-2008
 */
public class GetSubFoldersData extends BaseStepData implements StepDataInterface
{

	public Object[] previous_row;

	public RowMetaInterface outputRowMeta;

	public FileInputList files;

	public boolean isLastFile;

	public int filenr;
	
	public int filessize;
	
	public FileObject file;
	
    public long                rownr;
    
    public int totalpreviousfields;
    
    public int indexOfFoldernameField;
    
    public RowMetaInterface inputRowMeta;
    
    public Object[] readrow;
    
    public int nrStepFields;
    

	/**
	 * 
	 */
	public GetSubFoldersData()
	{
		super();
		previous_row = null;
		filenr = 0;
		filessize=0;
		file=null;
		totalpreviousfields=0;
		indexOfFoldernameField=-1;
		readrow=null;
		nrStepFields=0;
	}

}
