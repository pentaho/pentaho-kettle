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
 
package be.ibridge.kettle.trans.step.flattener;

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
 * Pivots data based on key-value pairs
 * 
 * @author Matt
 * @since 17-jan-2006
 */
public class Flattener extends BaseStep implements StepInterface
{
	private FlattenerMeta meta;
	private FlattenerData data;
	
	public Flattener(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(FlattenerMeta)getStepMeta().getStepMetaInterface();
		data=(FlattenerData)stepDataInterface;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		debug="processRow";
		
		Row r=getRow();    // get row!
		if (r==null)  // no more input to be expected...
		{
			// Don't forget the last set of rows...
			if (data.processed>0) 
			{
                // Remove value to flatten
                data.previousRow.removeValue( data.fieldNr );

                // Add the fields we flattened
                data.previousRow.addRow(data.targetResult);
                
                // send out inputrow + the flattened part
                putRow(data.previousRow);
			}

			setOutputDone();
			return false;
		}
		
		if (first)
		{
            data.fieldNr = r.searchValueIndex(meta.getFieldName() );
            if (data.fieldNr<0)
            {
                logError("field ["+meta.getFieldName()+"] couldn't be found!");
                setErrors(1);
                stopAll();
                return false;
            }
            
            data.targetRow = new Row();
            Value flattenValue = r.getValue(data.fieldNr);
            for (int i=0;i<meta.getTargetField().length;i++)
            {
                Value v = new Value(meta.getTargetField()[i], flattenValue.getType());
                v.setLength(flattenValue.getLength());
                v.setPrecision(flattenValue.getPrecision());
                v.setNull();
                data.targetRow.addValue(v);
            }
			
            data.targetResult = new Row(data.targetRow);
			first=false;
		}

        Value flat = r.getValue(data.fieldNr);
        
        // set it to value # data.processed
        data.targetResult.getValue(data.processed).setValue(flat);
        data.processed++;
        
        if (data.processed>=meta.getTargetField().length)
        {
            // Remove value to flatten
            r.removeValue( data.fieldNr );

            // Add the fields we flattened
            r.addRow(data.targetResult);
            
            // send out inputrow + the flattened part
            putRow(r);
            
            // clear the result row
            data.targetResult = new Row(data.targetRow);
            
            data.processed=0;
        }
        
        // Keep track in case we want to send out the last couple of flattened values.
        data.previousRow = r;

        if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(FlattenerMeta)smi;
		data=(FlattenerData)sdi;
		
		if (super.init(smi, sdi))
		{
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
