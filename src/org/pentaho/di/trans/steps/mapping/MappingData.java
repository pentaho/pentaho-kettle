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
 

package org.pentaho.di.trans.steps.mapping;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.SingleThreadedTransExecutor;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.mappinginput.MappingInput;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutput;



/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class MappingData extends BaseStepData implements StepDataInterface
{
    public Trans mappingTrans;
    public MappingInput  mappingInput;
    public MappingOutput mappingOutput;
	public List<Integer> renameFieldIndexes;
	public List<String>  renameFieldNames;
    public boolean wasStarted;
    public TransMeta mappingTransMeta;
    public RowMetaInterface outputRowMeta;
	public List<MappingValueRename> inputRenameList;
    protected int linesReadStepNr = -1;
    protected int linesInputStepNr = -1;
    protected int linesWrittenStepNr = -1;
    protected int linesOutputStepNr = -1;
    protected int linesUpdatedStepNr = -1;
    protected int linesRejectedStepNr = -1;
    public SingleThreadedTransExecutor singleThreadedTransExcecutor;
    
	/**
	 * 
	 */
	public MappingData()
	{
		super();
        mappingTrans = null;
        wasStarted = false;
        inputRenameList = new ArrayList<MappingValueRename>();
	}

}
