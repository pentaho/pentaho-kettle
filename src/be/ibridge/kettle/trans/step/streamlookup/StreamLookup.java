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
 
package be.ibridge.kettle.trans.step.streamlookup;

import java.text.DateFormat;
import java.util.Date;
import java.util.Hashtable;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Looks up information by first reading data into a hash table (in memory)
 * 
 * TODO: add warning with conflicting types OR modify the lookup values to the input row type. (this is harder to do as currently we don't know the types)
 * 
 * @author Matt
 * @since  26-apr-2003
 */
public class StreamLookup extends BaseStep implements StepInterface
{
	private StreamLookupMeta meta;
	private StreamLookupData data;

	public StreamLookup(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private void handleNullIf()
	{
	    data.nullIf = new Value[meta.getValue().length];
	    
		for (int i=0;i<meta.getValue().length;i++)
		{
			if (meta.getValueDefaultType()[i]<0)
			{
				//logError("unknown default value type: "+dtype+" for value "+value[i]+", default to type: String!");
				meta.getValueDefaultType()[i]=Value.VALUE_TYPE_STRING;
			}
			data.nullIf[i]=new Value(meta.getValueName()[i], meta.getValueDefaultType()[i]);
			switch(meta.getValueDefaultType()[i])
			{
			case Value.VALUE_TYPE_STRING: 
				data.nullIf[i].setValue(meta.getValueDefault()[i]); 
				break;
			case Value.VALUE_TYPE_DATE  :
				try{ data.nullIf[i].setValue( DateFormat.getInstance().parse(meta.getValueDefault()[i]) ); }
				catch(Exception e) { data.nullIf[i].setValue(new Date()); data.nullIf[i].setNull(); }
				break;
			case Value.VALUE_TYPE_NUMBER: 
				try { data.nullIf[i].setValue( Double.parseDouble(meta.getValueDefault()[i]) ); }
				catch(Exception e) { data.nullIf[i].setValue(0.0); data.nullIf[i].setNull(); }
				break;
			case Value.VALUE_TYPE_INTEGER: 
				try { data.nullIf[i].setValue( Long.parseLong(meta.getValueDefault()[i]) ); }
				catch(Exception e) { data.nullIf[i].setValue(0L); data.nullIf[i].setNull(); }
				break;
			case Value.VALUE_TYPE_BOOLEAN: 
				if ("TRUE".equalsIgnoreCase(meta.getValueDefault()[i]) || //$NON-NLS-1$
				    "Y".equalsIgnoreCase(meta.getValueDefault()[i]) )  //$NON-NLS-1$
				    data.nullIf[i].setValue(true); 
				else
				    data.nullIf[i].setValue(false); 
				;
				break;
			default: data.nullIf[i].setNull(); break;
			}
		}
	}

	
	private boolean readLookupValues() throws KettleStepException
	{
		Row r, value_part, key_part;
		
		data.look=new Hashtable();
		data.firstrow=null;
			
		if (meta.getLookupFromStep()==null)
		{
			logError(Messages.getString("StreamLookup.Log.NoLookupStepSpecified")); //$NON-NLS-1$
			return false;
		}
		if (log.isDetailed()) logDetailed(Messages.getString("StreamLookup.Log.ReadingFromStream")+meta.getLookupFromStep().getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
		
		r=getRowFrom(meta.getLookupFromStep().getName()); // rows are originating from "lookup_from"
		while (r!=null)
		{
			if (log.isRowLevel()) logRowlevel(Messages.getString("StreamLookup.Log.ReadLookupRow")+r.toString()); //$NON-NLS-1$

            key_part   = new Row();
            value_part = new Row();
            
            // Look up the keys in the source rows
            for (int i=0;i<meta.getKeylookup().length;i++)
            {
                Value keyValue = r.searchValue(meta.getKeylookup()[i]);
                if (keyValue==null)
                {
                    throw new KettleStepException(Messages.getString("StreamLookup.Exception.UnableToFindField",meta.getKeylookup()[i])); //$NON-NLS-1$ //$NON-NLS-2$
                }
                key_part.addValue(keyValue);
            }
            
            // Save the data types of the keys to optionally convert input rows later on...
            if (data.keyTypes==null)
            {
                data.keyTypes=new Row(key_part);
            }
			
			for (int v=0;v<meta.getValue().length;v++)
			{
                Value returnValue = r.searchValue(meta.getValue()[v]);
				if (returnValue==null)
				{
                    throw new KettleStepException(Messages.getString("StreamLookup.Exception.UnableToFindField",meta.getValue()[v])); //$NON-NLS-1$ //$NON-NLS-2$
				}
                value_part.addValue(returnValue);
			}
		
            data.look.put(key_part, value_part);
			
			if (data.firstrow==null) data.firstrow=new Row(value_part);
			
			r=getRowFrom(meta.getLookupFromStep().getName());
		}
		
		return true;
	}
	
	private boolean lookupValues(Row row)
	{
		Row lu=new Row();
		Row add=null;		

		debug = Messages.getString("StreamLookup.Debug.StartLookupValues"); //$NON-NLS-1$

		if (first)
		{
			debug = Messages.getString("StreamLookup.Debug.FirstPart"); //$NON-NLS-1$
			first=false;
			
			// read the lookup values!
			data.keynrs = new int[meta.getKeystream().length];
			
			for (int i=0;i<meta.getKeystream().length;i++)
			{
				// Find the keynr in the row (only once)
				data.keynrs[i]=row.searchValueIndex(meta.getKeystream()[i]);
				if (data.keynrs[i]<0)
				{
					logError(Messages.getString("StreamLookup.Log.FieldNotFound",meta.getKeystream()[i],""+row)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					setErrors(1);
					stopAll();
					return false;
				}
				else
				{
					if (log.isDetailed()) logDetailed(Messages.getString("StreamLookup.Log.FieldInfo",meta.getKeystream()[i],""+data.keynrs[i])); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
			
			// Handle the NULL values (not found...)
			handleNullIf();
            
			debug = Messages.getString("StreamLookup.Debug.FirstDoLookupValues"); //$NON-NLS-1$
			try
			{
				if (meta.getKeystream().length>0)
				{
					add=(Row)data.look.get(lu);
				}
				else
				{
					// Just take the first element in the hashtable...
					add=data.firstrow;
					if (log.isRowLevel()) logRowlevel(Messages.getString("StreamLookup.Log.GotRowWithoutKeys")+add); //$NON-NLS-1$
				}
			}
			catch(Exception e)
			{
				add=null;
			}
		}

		// See if we need to stop.
		if (stopped) return false;
		
		// Copy value references to lookup table.
		debug = Messages.getString("StreamLookup.Debug.CopyValueReference"); //$NON-NLS-1$
		for (int i=0;i<meta.getKeystream().length;i++) 
        {
            int valueNr = data.keynrs[i];
            Value value = row.getValue(valueNr); 
            lu.addValue( value );
        }
        debug=Messages.getString("StreamLookup.Debug.HandleConflictingTypes"); //$NON-NLS-1$
        // Handle conflicting types (Number-Integer-String conversion to lookup type in hashtable)
        debug = Messages.getString("StreamLookup.Debug.LookupSize")+lu.size(); //$NON-NLS-1$
        if (data.keyTypes!=null)
        {
            for (int i=0;i<lu.size();i++)
            {
                Value inputValue  = lu.getValue(i);
                Value lookupValue = data.keyTypes.getValue(i);
                if (inputValue.getType()!=lookupValue.getType())
                {
                    // Change the type for the lookup only!
                    Value newValue = new Value(inputValue);
                    newValue.setType(lookupValue.getType());
                    lu.setValue(i, newValue);
                }
            }
        }
        
		try
		{
			debug = "do lookup"; //$NON-NLS-1$
			if (meta.getKeystream().length>0)
			{
				add=(Row)data.look.get(lu);
			}
			else
			{
				// Just take the first element in the hashtable...
				add=data.firstrow;
				if (log.isRowLevel()) logRowlevel(Messages.getString("StreamLookup.Log.GotRowWithoutKeys")+add); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			add=null;
		}
		
		if (add==null) // nothing was found, unknown code: add null-values
		{
			debug = Messages.getString("StreamLookup.Debug.AddNullValues"); //$NON-NLS-1$
			add=new Row();
			for (int i=0;i<meta.getValue().length;i++)
			{
				add.addValue(new Value(data.nullIf[i]));
			}
		} 
		
		debug = Messages.getString("StreamLookup.Debug.AddReturnedValues"); //$NON-NLS-1$
		try
		{
		for (int i=0;i<add.size();i++)
		{
			Value v = add.getValue(i);
		    debug = Messages.getString("StreamLookup.Debug.AddReturnedValues2")+i+" : "+v.toString()+" ("+v.toStringMeta()+")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			//v.setName(info.valuename[i]);
		    
			if (v.getType() != meta.getValueDefaultType()[i])
			{
				v.setType(meta.getValueDefaultType()[i]);
			}
            if (meta.getValueName()[i]!=null && meta.getValueName()[i].length()>0)
            {
                v.setName(meta.getValueName()[i]);
            }
			row.addValue( v );
		}
		}
		catch(Exception e)
		{
		    throw new RuntimeException(e);
		}
	
		debug = Messages.getString("StreamLookup.Debug.FinishedLookupValues"); //$NON-NLS-1$

		return true;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
	    meta = (StreamLookupMeta)smi;
	    data = (StreamLookupData)sdi;
	    
	    if (data.readLookupValues)
	    {
	        data.readLookupValues = false;
	        
			logBasic(Messages.getString("StreamLookup.Log.ReadingLookupValuesFromStep")+meta.getLookupFromStep()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
			if (readLookupValues()) // Read values in lookup table (look)
			{
				logBasic(Messages.getString("StreamLookup.Log.ReadValuesInMemory",data.look.size()+"")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else
			{
				logError(Messages.getString("StreamLookup.Log.UnableToReadDataFromLookupStream")); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				return false;
			}
	    }
	    
		Row r=getRow();      // Get row from input rowset & set row busy!
		if (r==null)         // no more input to be expected...
		{
			if (log.isDetailed()) logDetailed(Messages.getString("StreamLookup.Log.StoppedProcessingWithEmpty",linesRead+"")); //$NON-NLS-1$ //$NON-NLS-2$
			setOutputDone();
			return false;
		}
		
		boolean err=lookupValues(r); // Do the actual lookup in the hastable.
		if (!err)
		{
			setOutputDone();  // signal end to receiver(s)
			return false;
		}
		
		putRow(r);       // copy row to output rowset(s);
			
		if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0)  logBasic(Messages.getString("StreamLookup.Log.LineNumber")+linesRead); //$NON-NLS-1$
			
		return true;
	}
		
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (StreamLookupMeta)smi;
	    data = (StreamLookupData)sdi;
	    
	    if (super.init(smi, sdi))
	    {
	        data.readLookupValues = true;
	        
	        return true;
	    }
	    return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("StreamLookup.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data)  && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("StreamLookup.Log.UnexpectedError")+debug+"' : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
