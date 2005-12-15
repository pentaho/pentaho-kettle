 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.trans.step.uniquerows;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Removes the same consequetive rows from the input stream(s).
 *  
 * @author Matt
 * @since 2-jun-2003
 */
public class UniqueRows extends BaseStep implements StepInterface
{
	private UniqueRowsMeta meta;
	private UniqueRowsData data;
	
	public UniqueRows(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(UniqueRowsMeta)getStepMeta().getStepMetaInterface();
		data=(UniqueRowsData)stepDataInterface; // create new data object.
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(UniqueRowsMeta)smi;
		data=(UniqueRowsData)sdi;

		debug="start of processRow()";
		
		Row r=getRow();    // get row!
		if (r==null)  // no more input to be expected...
		{
			// Don't forget the last set of rows...
			if (data.previous!=null) 
			{
				addCounter(data.previous, data.counter);
				putRow(data.previous);
			} 
			setOutputDone();
			return false;
		}

		if (first)
		{
			debug="first: create new row";
			data.previous=new Row(r); // copy the row
			first=false;
			
			// Cache lookup of fields
			data.fieldnrs=new int[meta.getCompareFields().length];
			data.ascending=new boolean[meta.getCompareFields().length];
            data.caseInsensitive=new boolean[meta.getCaseInsensitive().length];
            
			for (int i=0;i<meta.getCompareFields().length;i++)
			{
			    data.ascending[i] = false;
				data.fieldnrs[i] = r.searchValueIndex(meta.getCompareFields()[i]);
                data.caseInsensitive[i] = meta.getCaseInsensitive()[i];
				if (data.fieldnrs[i]<0)
				{
					logError("Couldn't find field ["+meta.getCompareFields()[i]+" in row!");
					setErrors(1);
					stopAll();
					return false;
				}
			}
		}
		
		debug="check for doubles...";
		boolean isEqual = false;
		
		if (meta.getCompareFields()==null || meta.getCompareFields().length==0)
		{
		    // Compare the complete row...
		    isEqual = data.previous.compare(r)==0;
		}
		else
		{
		    isEqual = data.previous.compare(r, data.fieldnrs, data.ascending, data.caseInsensitive)==0;
		}
		if (!isEqual)
		{
			addCounter(data.previous, data.counter);
			putRow(data.previous); // copy row to possible alternate rowset(s).
			data.previous=new Row(r);
			data.counter=1;
		}
		else
		{
			data.counter++;
		}

		if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);
			
		return true;
	}
	
	private void addCounter(Row r, long count)
	{
		if (meta.isCountRows())
		{
			Value v = new Value(meta.getCountField(), Value.VALUE_TYPE_INTEGER);
			v.setValue(count);
			v.setLength(9,0);
			r.addValue(v);
		}
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(UniqueRowsMeta)smi;
		data=(UniqueRowsData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}

			
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic("Starting to run...");
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error in '"+debug+"' : "+e.toString());
			setErrors(1);
			stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}
