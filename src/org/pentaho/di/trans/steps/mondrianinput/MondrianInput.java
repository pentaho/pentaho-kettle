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
 

package org.pentaho.di.trans.steps.mondrianinput;

import java.util.List;

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
 * Reads information from a database table by using freehand SQL
 * 
 * @author Matt
 * @since 8-apr-2003
 */
public class MondrianInput extends BaseStep implements StepInterface
{
	private MondrianInputMeta meta;
	private MondrianData data;
	
	public MondrianInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		if (first) // we just got started
		{
			first=false;
			String mdx = meta.getSQL();
			if(meta.isVariableReplacementActive()) mdx = environmentSubstitute(meta.getSQL());
			
			String catalog = environmentSubstitute(meta.getCatalog());
			data.mondrianHelper = new MondrianHelper(meta.getDatabaseMeta(), catalog, mdx, this);
			data.mondrianHelper.openQuery();
			data.mondrianHelper.createRectangularOutput();
			
			data.outputRowMeta = data.mondrianHelper.getOutputRowMeta().clone(); //
			
			data.rowNumber = 0;
		}

        if (data.rowNumber>=data.mondrianHelper.getRows().size())
        {
            setOutputDone(); // signal end to receiver(s)
            return false; // end of data or error.
        }
        
        List<Object> row = data.mondrianHelper.getRows().get(data.rowNumber++);
        Object[] outputRowData = RowDataUtil.allocateRowData(row.size());
        for (int i=0;i<row.size();i++) {
        	outputRowData[i] = row.get(i);
        }
        
        putRow(data.outputRowMeta, outputRowData);
        
		return true;
	}
    
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		if(log.isBasic()) logBasic("Finished reading query, closing connection.");
		
	    data.mondrianHelper.close();

	    super.dispose(smi, sdi);
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(MondrianInputMeta)smi;
		data=(MondrianData)sdi;

		if (super.init(smi, sdi))
		{
			return true;
		}
		
		return false;
	}
	
}