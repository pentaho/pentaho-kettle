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
 
package be.ibridge.kettle.trans.step.normaliser;

import java.util.ArrayList;

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
 * Normalise de-normalised input data.
 * 
 * @author Matt
 * @since 5-apr-2003
 */
public class Normaliser extends BaseStep implements StepInterface
{
	private NormaliserMeta meta;
	private NormaliserData data;

	public Normaliser(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(NormaliserMeta)smi;
		data=(NormaliserData)sdi; 
		
		Row r=getRow();   // get row from rowset, wait for our turn, indicate busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		if (first) // INITIALISE
		{
			// Get the list of occurances...
			data.type_occ = new ArrayList();
			data.maxlen=0;
			for (int i=0;i<meta.getFieldValue().length;i++)
			{
				boolean found=false;
				for (int j=0;j<data.type_occ.size();j++)
				{
					if (((String)data.type_occ.get(j)).equalsIgnoreCase(meta.getFieldValue()[i])) found=true;
				}
				if (!found) data.type_occ.add(meta.getFieldValue()[i]);
				if (meta.getFieldValue()[i].length()>data.maxlen) data.maxlen=meta.getFieldValue()[i].length();
			}
			
			// Which fields are not impacted? just copy these, leave them alone.
			data.copy_fieldnrs = new ArrayList();
			
			for (int i=0;i<r.size();i++)
			{
				Value v = r.getValue(i);
				boolean found=false;
				for (int j=0;j<meta.getFieldName().length && !found;j++)
				{
					if (v.getName().equalsIgnoreCase(meta.getFieldName()[j]))
					{
						found = true;
					}
				}
				if (!found) 
				{
					data.copy_fieldnrs.add(new Integer(i));
				} 
			}
			
			// Cache lookup of fields
			data.fieldnrs=new int[meta.getFieldName().length];
			for (int i=0;i<meta.getFieldName().length;i++)
			{
				data.fieldnrs[i] = r.searchValueIndex(meta.getFieldName()[i]);
				if (data.fieldnrs[i]<0)
				{
					logError("Couldn't find field ["+meta.getFieldName()[i]+" in row!");
					setErrors(1);
					stopAll();
					return false;
				}
			}
		}
		
		for (int e=0;e<data.type_occ.size();e++)
		{
			String typevalue = (String)data.type_occ.get(e);

			Row newrow = new Row();
			
			// First add the copy fields:
			for (int i=0;i<data.copy_fieldnrs.size();i++)
			{
				int nr = ((Integer)data.copy_fieldnrs.get(i)).intValue();
				Value v = r.getValue(nr);
				newrow.addValue(new Value(v));
			}
			
			// Add the typefield_value
			Value typefield_value = new Value(meta.getTypeField(), typevalue);
			typefield_value.setLength(data.maxlen);
			newrow.addValue(typefield_value);
			
			// Then add the norm fields...
			for (int i=0;i<data.fieldnrs.length;i++)
			{
				Value v = r.getValue(data.fieldnrs[i]);
				if (meta.getFieldValue()[i].equalsIgnoreCase(typevalue))
				{
					v.setName(meta.getFieldNorm()[i]);
					newrow.addValue(v);
				}
			}
			
			// The row is constructed, now give it to the next step...
			putRow(newrow);
		}

		if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("linenr "+linesRead);
			
		return true;
	}
			
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(NormaliserMeta)smi;
		data=(NormaliserData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}

	//
	// Run is were the action happens!
	//
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
