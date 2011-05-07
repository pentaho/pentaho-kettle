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
 

package org.pentaho.di.trans.steps.olapinput;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Reads information from an OLAP datasource using MDX
 * 
 * @author Paul Stoellberger
 * @since 11-MAR-2010
 */
public class OlapInput extends BaseStep implements StepInterface
{
	private OlapInputMeta meta;
	private OlapData data;
	
	public OlapInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
	    try {
	        
		if (first) // we just got started
		{
			first=false;
			meta.initData(this);
			
			data.rowNumber = 0;
		}

		for (;data.rowNumber < data.olapHelper.getRows().length;data.rowNumber++) {
		    String[] row = data.olapHelper.getRows()[data.rowNumber];
	        Object[] outputRowData = RowDataUtil.allocateRowData(row.length);
	        outputRowData = row;
	        
	        putRow(data.outputRowMeta, outputRowData);
	        
		}
        
        setOutputDone(); // signal end to receiver(s)
        return false; // end of data or error.
        
        
		}
	    catch (Exception e) {
	        logError("An error occurred, processing will be stopped",e);
            setErrors(1);
            stopAll();
            return false;
        }
	}
    
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		if(log.isBasic()) logBasic("Finished reading query, closing connection.");
		
	    try {
            data.olapHelper.close();
        } catch (KettleDatabaseException e) {
            logError("Error closing connection: ",e);
        }

	    super.dispose(smi, sdi);
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(OlapInputMeta)smi;
		data=(OlapData)sdi;

		if (super.init(smi, sdi))
		{
			return true;
		}
		
		return false;
	}
	
}