 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.trans.steps.yamlinput;



import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 21-06-2007
 */
public class YamlInputData extends BaseStepData implements StepDataInterface 
{
	public RowMetaInterface outputRowMeta;
	
    public int nrInputFields;
    public Object[] readrow;
    public int totalPreviousFields;   
    public int totalOutFields;
    public int totalOutStreamFields;
    
	/**
	 * The YAML files to read
	 */
	public FileInputList files;
	public FileObject file;
	public int     filenr;
	
    public long rownr;
    public int indexOfYamlField;
    
    public YamlReader yaml;
    
	public RowMetaInterface rowMeta;
    

	/**
	 * 
	 */
	public YamlInputData()
	{
		super();

		this.filenr = 0;
		this.indexOfYamlField=-1;
		this.nrInputFields=-1;
		this.readrow=null;
		this.totalPreviousFields=0;
		this.file=null;
		this.totalOutFields=0;
		this.totalOutStreamFields=0;
	}
}
