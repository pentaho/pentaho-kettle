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
 
package be.ibridge.kettle.trans.step.valuemapper;

import java.util.Hashtable;

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
 * Convert Values in a certain fields to other values
 * 
 * @author Matt 
 * @since 3-apr-2006
 */
public class ValueMapper extends BaseStep implements StepInterface
{
	private ValueMapperMeta meta;
	private ValueMapperData data;
	
	public ValueMapper(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(ValueMapperMeta)smi;
		data=(ValueMapperData)sdi;
		
	    // Get one row from one of the rowsets...
        //
		Row r = getRow();
		if (r==null)  // means: no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		if (first)
		{
		    first=false;
		    
		    data.keynr     = r.searchValueIndex(meta.getFieldToUse());
            if (data.keynr<0)
            {
                String message = Messages.getString("ValueMapper.RuntimeError.FieldToUseNotFound.VALUEMAPPER0001", meta.getFieldToUse(), Const.CR, r.toString()); //$NON-NLS-1$ 
                log.logError(toString(), message);
                setErrors(1);
                stopAll();
                return false;
            }
            
            // If there is an empty entry: we map null or "" to the target at the index
            // 0 or 1 empty mapping is allowed, not 2 or more.
            // 
            for (int i=0;i<meta.getSourceValue().length;i++)
            {
                if (Const.isEmpty(meta.getSourceValue()[i]))
                {
                    if (data.emptyFieldIndex<0)
                    {
                        data.emptyFieldIndex=i;
                    }
                    else
                    {
                        throw new KettleException(Messages.getString("ValueMapper.RuntimeError.OnlyOneEmptyMappingAllowed.VALUEMAPPER0004"));
                    }
                }
            }
            
		}

        Value value = r.getValue(data.keynr);
        
        String source = value.getString();
        String target = null;
        
        // Null/Empty mapping to value...
        //
        if (data.emptyFieldIndex>=0 && (value.isNull() || Const.isEmpty(source)) )
        {
            target = meta.getTargetValue()[data.emptyFieldIndex]; // that's all there is to it.
        }
        else
        {
            if (!Const.isEmpty(source))
            {
                target=(String)data.hashtable.get(source);
            }
        }

        if (!Const.isEmpty(meta.getTargetField()))
        {
            // Did we find anything to map to?
            Value extraValue = new Value(meta.getTargetField(), target);
            if (Const.isEmpty(target)) extraValue.setNull();
            r.addValue(extraValue);
        }
        else
        {
            int type = value.getType();

            // Don't set the original value to null if we don't have a target.
            if (!Const.isEmpty(target)) value.setValue(target);
            if (type!=value.getType()) // when changed from integer to string, convert back to the original data type...
            {
                value.setType(type);
            }
        }
        
		putRow(r);     // send it off to the next step(s)

		return true;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(ValueMapperMeta)smi;
		data=(ValueMapperData)sdi;

		super.dispose(smi, sdi);
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(ValueMapperMeta)smi;
		data=(ValueMapperData)sdi;
		
		if (super.init(smi, sdi))
		{
		    data.hashtable = new Hashtable();
            data.emptyFieldIndex=-1;
            
            // Add all source to target mappings in here...
            for (int i=0;i<meta.getSourceValue().length;i++)
            {
                String src = meta.getSourceValue()[i];
                String tgt = meta.getTargetValue()[i];
            
                if (!Const.isEmpty(src) && !Const.isEmpty(tgt))
                {
                    data.hashtable.put(src, tgt);
                }
                else
                {
                    if (Const.isEmpty(tgt))
                    {
                        log.logError(toString(), Messages.getString("ValueMapper.RuntimeError.ValueNotSpecified.VALUEMAPPER0002", ""+i)); //$NON-NLS-1$ //$NON-NLS-2$
                        return false;
                    }
                }
            }
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
			logBasic(Messages.getString("ValueMapper.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("ValueMapper.RuntimeError.UnexpectedError.VALUEMAPPER0003", e.toString())); //$NON-NLS-1$
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
