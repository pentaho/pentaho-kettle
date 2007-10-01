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
		if (data.firstselect)
		{
			data.firstselect=false;

			data.fieldnrs=new int[meta.getSelectName().length];
			data.values=new Value[meta.getSelectName().length];

			for (int i=0;i<data.fieldnrs.length;i++) 
			{
				data.fieldnrs[i]=row.searchValueIndex(meta.getSelectName()[i]);
				if (data.fieldnrs[i]<0)
				{
					logError(Messages.getString("SelectValues.Log.CouldNotFindField",meta.getSelectName()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					setErrors(1);
					stopAll();
					return false;
				}
			}
			
			// Check for doubles in the selected fields... AFTER renaming!!
			int cnt[] = new int[meta.getSelectName().length];
			for (int i=0;i<meta.getSelectName().length;i++)
			{
				cnt[i]=0;
				for (int j=0;j<meta.getSelectName().length;j++)
				{
                    String one = Const.NVL( meta.getSelectRename()[i], meta.getSelectName()[i]);
                    String two = Const.NVL( meta.getSelectRename()[j], meta.getSelectName()[j]);
					if (one.equals(two)) cnt[i]++;
					
					if (cnt[i]>1)
					{
						logError(Messages.getString("SelectValues.Log.FieldCouldNotSpecifiedMoreThanTwice",one)); //$NON-NLS-1$ //$NON-NLS-2$
						setErrors(1);
						stopAll();
						return false;
					}
				}
			}
		}

		// Get the field values
		for (int i=0;i<meta.getSelectName().length;i++)
		{
			// Normally this can't happen, except when streams are mixed with different
			// number of fields.
			// 
			if (data.fieldnrs[i]<row.size())
			{
			    // TODO: Clone might be a 'bit' expensive as it is only needed in case you want to copy a single field to 2 or more target fields.
                // And even then it is only required for the last n-1 target fields.
                // Perhaps we can consider the requirements for cloning at init(), store it in a boolean[] and just consider this at runtime
                //
				data.values[i]=row.getValue(data.fieldnrs[i]).Clone(); 
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
				if (log.isDetailed()) logDetailed(Messages.getString("SelectValues.Log.MixingStreamWithDifferentFields")); //$NON-NLS-1$
			}			
		}

		for (int i=0;i<meta.getSelectName().length;i++) // Add in the same order as before!
		{
			if (i>=row.size()) 
            {
                row.addValue(data.values[i]);
            }
			else
            {
                row.setValue(i, data.values[i]);
            }
		}
		for (int i=row.size()-1;i>=meta.getSelectName().length;i--)
		{
			row.removeValue(i);
		}

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
		if (data.firstdeselect)
		{
			data.firstdeselect=false;

			// System.out.println("Fields to remove: "+info.dname.length);
			data.removenrs=new int[meta.getDeleteName().length];
			
			for (int i=0;i<data.removenrs.length;i++) 
			{
				data.removenrs[i]=row.searchValueIndex(meta.getDeleteName()[i]);
				if (data.removenrs[i]<0)
				{
					logError(Messages.getString("SelectValues.Log.CouldNotFindField",meta.getDeleteName()[i])); //$NON-NLS-1$ //$NON-NLS-2$
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
						logError(Messages.getString("SelectValues.Log.FieldCouldNotSpecifiedMoreThanTwice2",meta.getDeleteName()[i])); //$NON-NLS-1$ //$NON-NLS-2$
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
		}

		/*
		 *  Remove the field values
		 *  Take into account that field indexes change once you remove them!!!
		 *  Therefor removenrs is sorted in reverse on index...
		 */
		for (int i=0;i<data.removenrs.length;i++)
		{
			row.removeValue(data.removenrs[i]);
		}

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
		if (data.firstmetadata)
		{
			data.firstmetadata=false;

			data.metanrs=new int[meta.getMetaName().length];
			
			for (int i=0;i<data.metanrs.length;i++) 
			{
				data.metanrs[i]=row.searchValueIndex(meta.getMetaName()[i]);
				if (data.metanrs[i]<0)
				{
					logError(Messages.getString("SelectValues.Log.CouldNotFindField",meta.getMetaName()[i])); //$NON-NLS-1$ //$NON-NLS-2$
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
						logError(Messages.getString("SelectValues.Log.FieldCouldNotSpecifiedMoreThanTwice2",meta.getMetaName()[i])); //$NON-NLS-1$ //$NON-NLS-2$
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
		for (int i=0;i<data.metanrs.length;i++)
		{
			Value v = row.getValue(data.metanrs[i]);
			
			if (meta.getMetaRename()[i]!=null && meta.getMetaRename()[i].length()>0) v.setName(meta.getMetaRename()[i]);
			if (meta.getMetaType()[i]!=Value.VALUE_TYPE_NONE)                v.setType(meta.getMetaType()[i]);
			if (meta.getMetaLength()[i]!=-2)                                 v.setLength(meta.getMetaLength()[i]);
			if (meta.getMetaPrecision()[i]!=-2)                              v.setPrecision(meta.getMetaPrecision()[i]);
		}

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
		if (log.isRowLevel()) logRowlevel(Messages.getString("SelectValues.Log.GotRowFromPreviousStep")+r); //$NON-NLS-1$

		err=true;
		
		if (data.select)   err=selectValues(r);
		if (data.deselect) err=removeValues(r);
		if (data.metadata) err=metadataValues(r);
		
		if (!err) 
		{
			setOutputDone();  // signal end to receiver(s)
			return false;
		} 
		 boolean sendToErrorRow=false;
		 String errorMessage = null;

		try
		{
			putRow(r);      // copy row to possible alternate rowset(s).
			if (log.isRowLevel()) logRowlevel(Messages.getString("SelectValues.Log.WroteRowToNextStep")+r); //$NON-NLS-1$
	
	        if (checkFeedback(linesRead)) logBasic(Messages.getString("SelectValues.Log.LineNumber")+linesRead); //$NON-NLS-1$
		}
		catch(KettleException e)
		{
			if (getStepMeta().isDoingErrorHandling())
			{
		          sendToErrorRow = true;
		          errorMessage = e.toString();
			}
			else
			{
				logError(Messages.getString("SelectValues.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;

			}
			if (sendToErrorRow)
			{
			   // Simply add this row to the error row
			   putError(r, 1, errorMessage, null, "SELVAL001");
			}

		}
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
				logError(Messages.getString("SelectValues.Log.InputShouldContainData")); //$NON-NLS-1$
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
			logBasic(Messages.getString("SelectValues.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("SelectValues.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(e));
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