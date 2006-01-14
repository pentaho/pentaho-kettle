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
 
package be.ibridge.kettle.trans.step.selectvalues;

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
 * Select, re-order, remove or change the meta-data of the fields in the inputstreams.
 * 
 * @author Matt
 * @since 5-apr-2003
 *
 */
public class SelectValues extends BaseStep implements StepInterface
{
	private SelectValuesMeta meta;
	private SelectValuesData data;
	
	public SelectValues(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	/**
	   Only select the values that are still needed...<p>
	   Put the values in the right order...<p>
	   Change the meta-data information if needed...<p>
	   
	   @param row The row to manipulate
	   @return true if everything went well, false if we need to stop because of an error!
	   
	*/
	private synchronized boolean selectValues(Row row)
	{
		debug="Start of selectValues";
		
		if (data.firstselect)
		{
			debug="Init (first)";
			data.firstselect=false;

			data.fieldnrs=new int[meta.getSelectName().length];
			data.values=new Value[meta.getSelectName().length];

			for (int i=0;i<data.fieldnrs.length;i++) 
			{
				data.fieldnrs[i]=row.searchValueIndex(meta.getSelectName()[i]);
				if (data.fieldnrs[i]<0)
				{
					logError("Couldn't find field '"+meta.getSelectName()[i]+"' in row!");
					setErrors(1);
					stopAll();
					return false;
				}
			}
			
			// Check for doubles in the selected fields...
			int cnt[] = new int[meta.getSelectName().length];
			for (int i=0;i<meta.getSelectName().length;i++)
			{
				cnt[i]=0;
				for (int j=0;j<meta.getSelectName().length;j++)
				{
					if (meta.getSelectName()[i].equals(meta.getSelectName()[j])) cnt[i]++;
					
					if (cnt[i]>1)
					{
						logError("Field '"+meta.getSelectName()[i]+"' is specified twice with the same name!");
						setErrors(1);
						stopAll();
						return false;
					}
				}
			}
		}

		debug="get fields values";
		// Get the field values
		for (int i=0;i<meta.getSelectName().length;i++)
		{
			debug="get start loop (fieldnrs["+i+"]="+data.fieldnrs[i]+")";
			// Normally this can't happen, except when streams are mixed with different
			// number of fields.
			// 
			if (data.fieldnrs[i]<row.size())
			{
				data.values[i]=row.getValue(data.fieldnrs[i]);
				if (meta.getSelectRename()[i]!=null && meta.getSelectRename()[i].length()>0)
				{
					data.values[i].setName(meta.getSelectRename()[i]);
				}
				else
				{
					data.values[i].setName(meta.getSelectName()[i]);
				}
				if (meta.getSelectLength()[i]!=-2)    data.values[i].setLength(meta.getSelectLength()[i]);
				if (meta.getSelectPrecision()[i]!=-2) data.values[i].setPrecision(meta.getSelectPrecision()[i]);
			}
			else
			{
				logDetailed("WARNING: Mixing streams with different nr of fields.");
			}
			
		}
		debug="add values to row in correct order...";
		for (int i=0;i<meta.getSelectName().length;i++) // Add in the same order as before!
		{
			if (i>row.size()) row.addValue(data.values[i]);
			else              row.setValue(i, data.values[i]);
		}
		debug="remove unwanted/unselected fields.";
		for (int i=row.size()-1;i>=meta.getSelectName().length;i--)
		{
			row.removeValue(i);
		}

		debug="End of selectValues";

		return true;
	}
	
	/**
	   
	   Remove the values that are no longer needed.<p>
	   This, we can do VERY fast.<p>
	   
	   @param row The row to manipulate
	   @return true if everything went well, false if we need to stop because of an error!
	   
	*/
	private synchronized boolean removeValues(Row row)
	{		
		debug="Start of removeValues";
		
		if (data.firstdeselect)
		{
			debug="Init (first)";
			data.firstdeselect=false;

			// System.out.println("Fields to remove: "+info.dname.length);
			data.removenrs=new int[meta.getDeleteName().length];
			
			for (int i=0;i<data.removenrs.length;i++) 
			{
				data.removenrs[i]=row.searchValueIndex(meta.getDeleteName()[i]);
				if (data.removenrs[i]<0)
				{
					logError("Couldn't find field '"+meta.getDeleteName()[i]+"' in row!");
					setErrors(1);
					stopAll();
					return false;
				}
			}
			
			// Check for doubles in the selected fields...
			int cnt[] = new int[meta.getDeleteName().length];
			for (int i=0;i<meta.getDeleteName().length;i++)
			{
				cnt[i]=0;
				for (int j=0;j<meta.getDeleteName().length;j++)
				{
					if (meta.getDeleteName()[i].equals(meta.getDeleteName()[j])) cnt[i]++;
					
					if (cnt[i]>1)
					{
						logError("Field '"+meta.getDeleteName()[i]+"' is specified twice with the same name!");
						setErrors(1);
						stopAll();
						return false;
					}
				}
			}
			
			// Sort removenrs descending.  So that we can delete in ascending order...
			for (int i=0;i<data.removenrs.length;i++)
			{
				for (int j=0;j<data.removenrs.length-1;j++)
				{
					if (data.removenrs[j] < data.removenrs[j+1]) // swap
					{
						int dummy          = data.removenrs[j];
						data.removenrs[j]  = data.removenrs[j+1];
						data.removenrs[j+1]= dummy;
					}
				}
			}

			/*
			for (int i=0;i<removenrs.length;i++)
			{
				System.out.println("Remove index : "+removenrs[i]);
			}
			*/
		}

		/*
		 *  Remove the field values
		 *  Take into account that field indexes change once you remove them!!!
		 *  Therefor removenrs is sorted in reverse on index...
		 */
		debug="remove field values";
		for (int i=0;i<data.removenrs.length;i++)
		{
			row.removeValue(data.removenrs[i]);
		}

		debug="End of removeValues";

		return true;
	}

	/**
	   
	   Change the meta-data of certain fields.<p>
	   This, we can do VERY fast.<p>
	   
	   @param row The row to manipulate
	   @return true if everything went well, false if we need to stop because of an error!
	   
	*/
	private synchronized boolean metadataValues(Row row)
	{
		debug="Start of metadataValues";
		
		if (data.firstmetadata)
		{
			debug="Init (first)";
			data.firstmetadata=false;

			data.metanrs=new int[meta.getMetaName().length];
			
			for (int i=0;i<data.metanrs.length;i++) 
			{
				data.metanrs[i]=row.searchValueIndex(meta.getMetaName()[i]);
				if (data.metanrs[i]<0)
				{
					logError("Couldn't find field '"+meta.getMetaName()[i]+"' in row!");
					setErrors(1);
					stopAll();
					return false;
				}
			}
			
			// Check for doubles in the selected fields...
			int cnt[] = new int[meta.getMetaName().length];
			for (int i=0;i<meta.getMetaName().length;i++)
			{
				cnt[i]=0;
				for (int j=0;j<meta.getMetaName().length;j++)
				{
					if (meta.getMetaName()[i].equals(meta.getMetaName()[j])) cnt[i]++;
					
					if (cnt[i]>1)
					{
						logError("Field '"+meta.getMetaName()[i]+"' is specified twice with the same name!");
						setErrors(1);
						stopAll();
						return false;
					}
				}
			}
		}

		/*
		 * Change the meta-data! 
		 */
		debug="change metadata of fields";
		for (int i=0;i<data.metanrs.length;i++)
		{
			Value v = row.getValue(data.metanrs[i]);
			
			if (meta.getMetaRename()[i]!=null && meta.getMetaRename()[i].length()>0) v.setName(meta.getMetaRename()[i]);
			if (meta.getMetaType()[i]!=Value.VALUE_TYPE_NONE)                v.setType(meta.getMetaType()[i]);
			if (meta.getMetaLength()[i]!=-2)                                 v.setLength(meta.getMetaLength()[i]);
			if (meta.getMetaPrecision()[i]!=-2)                              v.setPrecision(meta.getMetaPrecision()[i]);
		}

		debug="End of metadataValues";

		return true;
	}

	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SelectValuesMeta)smi;
		data=(SelectValuesData)sdi;

		Row r=null;
		boolean err=true;
		
		r=getRow();   // get row from rowset, wait for our turn, indicate busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        logRowlevel("Got row from previous step: "+r);

		err=true;
		
		if (data.select)   err=selectValues(r);
		if (data.deselect) err=removeValues(r);
		if (data.metadata) err=metadataValues(r);
		
		if (!err) 
		{
			setOutputDone();  // signal end to receiver(s)
			return false;
		} 

		putRow(r);      // copy row to possible alternate rowset(s).
        logRowlevel("Wrote row to next step: "+r);

		if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("linenr "+linesRead);
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SelectValuesMeta)smi;
		data=(SelectValuesData)sdi;

		if (super.init(smi, sdi))
		{
			data.firstselect   = true;
			data.firstdeselect = true;
			data.firstmetadata = true;

			data.select=false;
			data.deselect=false;
			data.metadata=false;
			
			if (meta.getSelectName()!=null && meta.getSelectName().length>0) data.select   = true;
			if (meta.getDeleteName()!=null && meta.getDeleteName().length>0) data.deselect = true;
			if (meta.getMetaName()!=null && meta.getMetaName().length>0) data.metadata = true;
			
			boolean atLeaseOne = data.select || data.deselect || data.metadata;
			if (!atLeaseOne)
			{
				setErrors(1);
				logError("At lease one of select, remove or meta screens should contain data.");
			}
			
			return atLeaseOne; // One of those three has to work!
		}
		else
		{
			return false;
		}
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
