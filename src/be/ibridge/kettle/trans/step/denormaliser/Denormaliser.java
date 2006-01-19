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
 
package be.ibridge.kettle.trans.step.denormaliser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleValueException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Denormalises data based on key-value pairs
 * 
 * @author Matt
 * @since 17-jan-2006
 */
public class Denormaliser extends BaseStep implements StepInterface
{
	private DenormaliserMeta meta;
	private DenormaliserData data;
	
	public Denormaliser(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(DenormaliserMeta)getStepMeta().getStepMetaInterface();
		data=(DenormaliserData)stepDataInterface;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		debug="processRow";
		
		Row r=getRow();    // get row!
		if (r==null)  // no more input to be expected...
		{
			// Don't forget the last set of rows...
			if (data.previous!=null) 
			{
                deNormalise(data.previous);
                buildResult(data.previous);
				putRow(data.previous);
                //System.out.println("Wrote row: "+data.previous);
			}

			setOutputDone();
			return false;
		}
				
		if (first)
		{
            data.keyFieldNr = r.searchValueIndex(meta.getKeyField() );
            if (data.keyFieldNr<0)
            {
                logError("Key field name ["+meta.getKeyField()+"] couldn't be found!");
                setErrors(1);
                stopAll();
                return false;
            }
            
            Hashtable subjects = new Hashtable();
            data.fieldNameIndex = new int[meta.getDenormaliserTargetField().length];
            for (int i=0;i<meta.getDenormaliserTargetField().length;i++)
			{
                DenormaliserTargetField field = meta.getDenormaliserTargetField()[i];
				int idx = r.searchValueIndex(field.getFieldName());
				if (idx<0)
				{
					logError("Unpivot field name ["+field.getFieldName()+"] couldn't be found!");
					setErrors(1);
					stopAll();
					return false;
				}
                data.fieldNameIndex[i] = idx;
                subjects.put(new Integer(idx), new Integer(idx));
                
                // Fill a hashtable with the key strings and the position of the field in the row to take.
				data.keyValue.put(field.getKeyValue(), new Integer(i));
			}
            
            Set subjectSet = subjects.keySet();
            data.fieldNrs = (Integer[])subjectSet.toArray(new Integer[subjectSet.size()]);
            
            
            
			data.groupnrs = new int[meta.getGroupField().length];
			for (int i=0;i<meta.getGroupField().length;i++)
			{
				data.groupnrs[i] = r.searchValueIndex(meta.getGroupField()[i]);
				if (data.groupnrs[i]<0)
				{
					logError("Grouping field ["+meta.getGroupField()[i]+"] couldn't be found!");
					setErrors(1);
					stopAll();
					return false;
				}
			}
            
            ArrayList removeList = new ArrayList();
            removeList.add(new Integer(data.keyFieldNr));
            for (int i=0;i<data.fieldNrs.length;i++)
            {
                removeList.add(data.fieldNrs[i]);
            }
            Collections.sort(removeList);
            
            data.removeNrs = new int[removeList.size()];
            for (int i=0;i<removeList.size();i++) data.removeNrs[i] = ((Integer)removeList.get(i)).intValue();
            
			data.previous=new Row(r); // copy the row to previous
			newGroup();              // Create a new result row (init)
			
			first=false;
		}

				
		// System.out.println("Check for same group...");
        
		if (!sameGroup(data.previous, r))
		{
            debug="Different group";
            // System.out.println("Different group!");
            
    		buildResult(data.previous);
    		putRow(data.previous);        // copy row to possible alternate rowset(s).
            //System.out.println("Wrote row: "+data.previous);
            newGroup();       // Create a new group aggregate (init)
            deNormalise(r);
		}
        else
        {
            debug="unPivot()";
            deNormalise(r);
        }

		data.previous=new Row(r);
        
		if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);
			
		return true;
	}
	
	private void buildResult(Row inputRow) throws KettleValueException
    {
	    for (int i=data.removeNrs.length-1;i>=0;i--)
        {
	        inputRow.removeValue(data.removeNrs[i]);   
        }
        
        // Add the unpivoted fields...
        for (int i=0;i<data.targetResult.size();i++)
        {
            Value resultValue = data.targetResult.getValue(i);
            DenormaliserTargetField field = meta.getDenormaliserTargetField()[i];
            switch(field.getTargetAggregationType())
            {
            case DenormaliserTargetField.TYPE_AGGR_AVERAGE :
                long count = data.counters[i];
                Value sum  = data.sum[i];
                if (count>0)
                {
                    sum.divide(count);
                    resultValue.setValue(sum);
                }
                break;
            default: break;
            }
            inputRow.addValue(resultValue);
        }
    }

    // Is the row r of the same group as previous?
	private boolean sameGroup(Row previous, Row r)
	{
		debug="sameGroup";
		
		for (int i=0;i<data.groupnrs.length;i++)
		{
			Value prev = previous.getValue(data.groupnrs[i]);
			Value curr = r.getValue(data.groupnrs[i]);
			
			if (!prev.equals(curr)) return false;
		}
		
		return true;
	}

	/** Initialize a new group... */
	private void newGroup()
	{
		debug="newAggregate";
        
        data.targetResult = new Row();
        for (int i=0;i<meta.getDenormaliserTargetField().length;i++)
        {
            DenormaliserTargetField field = meta.getDenormaliserTargetField()[i];
            Value defaultTarget = new Value(field.getTargetName(), field.getTargetType());
            defaultTarget.setLength(field.getTargetLength(), field.getTargetPrecision());
            data.targetResult.addValue(defaultTarget);

            data.counters[i]=0L; // set to 0
            data.sum[i]=new Value(field.getTargetName(), field.getTargetType()); // Sum is 0
        }
	}
	
    /**
     * This method de-normalises a single key-value pair.
     * It looks up the key and determines the value name to store it in.
     * It converts it to the right type and stores it in the result row.
     * @param r
     * @throws KettleValueException
     */
	private void deNormalise(Row r) throws KettleValueException
	{
        String key = r.getValue(data.keyFieldNr).getString();
        Integer keyNr = (Integer) data.keyValue.get(key);
        if (keyNr!=null)
        {
            // keyNr is the field in UnpivotTargetField[]
            //
            int idx = keyNr.intValue();
            DenormaliserTargetField field = meta.getDenormaliserTargetField()[idx];
            
            Value targetValue = r.getValue(data.fieldNameIndex[idx]); // This is the value we need to de-normalise, convert, aggregate.

            // System.out.println("Value type: "+targetValue.getTypeDesc()+"("+targetValue+"), convert to type : "+field.getTargetTypeDesc());

            // See if we need to convert this value
            if (targetValue.getType() != field.getTargetType())
            {
                switch(targetValue.getType())
                {
                case Value.VALUE_TYPE_STRING:
                    switch(field.getTargetType())
                    {
                    case Value.VALUE_TYPE_DATE:      targetValue.str2dat(field.getTargetFormat()); break; 
                    case Value.VALUE_TYPE_INTEGER:   targetValue.setType(targetValue.getType()); break; 
                    case Value.VALUE_TYPE_NUMBER:
                        // System.out.println("target value ["+targetValue+"] ("+targetValue.toStringMeta()+") : convert to number("+field.getTargetFormat()+" / "+field.getTargetDecimalSymbol()+" / "+field.getTargetGroupingSymbol()+" / "+field.getTargetCurrencySymbol());
                        targetValue.str2num(field.getTargetFormat(), field.getTargetDecimalSymbol(), field.getTargetGroupingSymbol(), field.getTargetCurrencySymbol()); break;
                    case Value.VALUE_TYPE_BIGNUMBER: targetValue.setType(targetValue.getType()); break; 
                    case Value.VALUE_TYPE_BOOLEAN:   targetValue.setType(targetValue.getType()); break; 
                    default: break;
                    }
                default:
                    targetValue.setType(targetValue.getType()); break;
                }
            }
     
            Value prevTarget = data.targetResult.getValue(idx);
            // System.out.println("TargetValue="+targetValue+", Prev TargetResult="+prevTarget);
            
            switch(field.getTargetAggregationType())
            {
            case DenormaliserTargetField.TYPE_AGGR_SUM:
                prevTarget.plus(targetValue);
                break;
            case DenormaliserTargetField.TYPE_AGGR_MIN:
                if (targetValue.compare(prevTarget)<0) prevTarget.setValue(targetValue);
                break;
            case DenormaliserTargetField.TYPE_AGGR_MAX:
                if (targetValue.compare(prevTarget)>0) prevTarget.setValue(targetValue);
                break;
            case DenormaliserTargetField.TYPE_AGGR_COUNT_ALL:
                if (!targetValue.isNull()) prevTarget.setValue(prevTarget.getInteger()+1);
                break;
            case DenormaliserTargetField.TYPE_AGGR_AVERAGE:
                if (!targetValue.isNull()) 
                {
                    data.counters[idx]++;
                    data.sum[idx].plus(targetValue);
                }
                break;
            case DenormaliserTargetField.TYPE_AGGR_NONE:
            default:
                prevTarget.setValue(targetValue); // Overwrite the previous
                break;
            }
            
        }
	}
    
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(DenormaliserMeta)smi;
		data=(DenormaliserData)sdi;
		
		if (super.init(smi, sdi))
		{
            data.counters = new long[meta.getDenormaliserTargetField().length];
            data.sum      = new Value[meta.getDenormaliserTargetField().length];

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
