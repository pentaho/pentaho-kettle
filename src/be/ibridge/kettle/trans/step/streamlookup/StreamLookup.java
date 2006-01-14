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
				if ("TRUE".equalsIgnoreCase(meta.getValueDefault()[i]) ||
				    "Y".equalsIgnoreCase(meta.getValueDefault()[i]) ) 
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
			logError("No lookup step specified.");
			return false;
		}
		logDetailed("Reading from stream ["+meta.getLookupFromStep().getName()+"]");
		
		r=getRowFrom(meta.getLookupFromStep().getName()); // rows are originating from "lookup_from"
		while (r!=null)
		{
            logRowlevel("Read lookup row: "+r.toString());

            key_part   = new Row();
            value_part = new Row();
            
            // Look up the keys in the source rows
            for (int i=0;i<meta.getKeylookup().length;i++)
            {
                Value keyValue = r.searchValue(meta.getKeylookup()[i]);
                if (keyValue==null)
                {
                    throw new KettleStepException("Unable to find field ["+meta.getKeylookup()[i]+"] in the source rows");
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
                    throw new KettleStepException("Unable to find field ["+meta.getValue()[v]+"] in the source rows");
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

		debug = "start lookupValues";

		if (first)
		{
			debug = "First part";
			first=false;
			
			// read the lookup values!
			data.keynrs = new int[meta.getKeystream().length];
			
			for (int i=0;i<meta.getKeystream().length;i++)
			{
				// Find the keynr in the row (only once)
				data.keynrs[i]=row.searchValueIndex(meta.getKeystream()[i]);
				if (data.keynrs[i]<0)
				{
					logError("Field ["+meta.getKeystream()[i]+"] not found in row ["+row+"]");
					setErrors(1);
					stopAll();
					return false;
				}
				else
				{
					logDetailed("Field ["+meta.getKeystream()[i]+"] has nr ["+data.keynrs[i]+"]");
				}
			}
			
			// Handle the NULL values (not found...)
			handleNullIf();
            
			debug = "first: do lookup";
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
					logRowlevel("Got row without keys: "+add);
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
		debug = "Copy value references to lookup table";
		for (int i=0;i<meta.getKeystream().length;i++) 
        {
            int valueNr = data.keynrs[i];
            Value value = row.getValue(valueNr); 
            lu.addValue( value );
        }
        debug="Handle conflicting types";
        // Handle conflicting types (Number-Integer-String conversion to lookup type in hashtable)
        debug = "lookup size="+lu.size();
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
			debug = "do lookup";
			if (meta.getKeystream().length>0)
			{
				add=(Row)data.look.get(lu);
			}
			else
			{
				// Just take the first element in the hashtable...
				add=data.firstrow;
				logRowlevel("Got row without keys: "+add);
			}
		}
		catch(Exception e)
		{
			add=null;
		}
		
		if (add==null) // nothing was found, unknown code: add null-values
		{
			debug = "add null values";
			add=new Row();
			for (int i=0;i<meta.getValue().length;i++)
			{
				add.addValue(new Value(data.nullIf[i]));
			}
		} 
		
		debug = "add returned values";
		try
		{
		for (int i=0;i<add.size();i++)
		{
			Value v = add.getValue(i);
		    debug = "add returned value #"+i+" : "+v.toString()+" ("+v.toStringMeta()+")";
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
	
		debug = "Finished lookupValues";

		return true;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
	    meta = (StreamLookupMeta)smi;
	    data = (StreamLookupData)sdi;
	    
	    if (data.readLookupValues)
	    {
	        data.readLookupValues = false;
	        
			logBasic("Reading lookup values from step ["+meta.getLookupFromStep()+"]");
			if (readLookupValues()) // Read values in lookup table (look)
			{
				logBasic("Read "+data.look.size()+" values in memory for lookup!");
			}
			else
			{
				logError("Unable to read data from lookup-stream.");
				setErrors(1);
				stopAll();
				return false;
			}
	    }
	    
		Row r=getRow();      // Get row from input rowset & set row busy!
		if (r==null)         // no more input to be expected...
		{
			logDetailed("Stopped processing with empty row after "+linesRead+" rows.");
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
			
		if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0)  logBasic("linenr "+linesRead);
			
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
			logBasic("Starting to run...");
			while (processRow(meta, data)  && !isStopped());
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
