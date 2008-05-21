package org.pentaho.di.trans.steps.stepmeta;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.delete.Messages;


/**
 * Return the structure of the stream
 * 
 * @author Ingo Klose
 * @since  22-april-2008
 */

public class StepMetastructure extends BaseStep implements StepInterface
{   
	private StepMetastructureMeta meta;
	private StepMetastructureData data;
	private Object[] lastRow;
	private int rowCount;
	
	public StepMetastructure(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(StepMetastructureMeta)smi;
		data=(StepMetastructureData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
			rowCount = 0;
		    return true;
		}
		return false;
	} 
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
    	
		Object[] r=getRow();       // Get row from input rowset & set row busy!
		Object[] metastructureRow = null;		
		
		
		// initialize 
		if (first && r!=null)
		{
		    
		    
		}
		
		//fill the row structure for the outpout rows
		if (data.outputRowMeta==null)
        {
            data.outputRowMeta = new RowMeta();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
        }
	    
		
	    if (r==null || !meta.isOutputRowcount())  // no more input to be expected...
		{
	    	if (lastRow != null){
	    		metastructureRow = RowDataUtil.allocateRowData(7);
			    
			    RowMetaInterface row = getInputRowMeta().clone();
			    
			    for (int i = 0; i < row.size(); i++) {
			    	
			        ValueMetaInterface v = row.getValueMeta(i);
			        
			        ValueMetaInterface v_position = data.outputRowMeta.getValueMeta(0);
			        metastructureRow=RowDataUtil.addValueData(metastructureRow, 0,v_position.convertDataCompatible(v_position,new Long(i+1)));
			        
			        metastructureRow=RowDataUtil.addValueData(metastructureRow, 1, v.getName());
			        metastructureRow=RowDataUtil.addValueData(metastructureRow, 2, v.getComments());
			        metastructureRow=RowDataUtil.addValueData(metastructureRow, 3, v.getTypeDesc());
			        
			        ValueMetaInterface v_length = data.outputRowMeta.getValueMeta(4);
			        metastructureRow=RowDataUtil.addValueData(metastructureRow, 4, v_length.convertDataCompatible(v_length,new Long(v.getLength())));
			        
			        ValueMetaInterface v_precision = data.outputRowMeta.getValueMeta(5);
			        metastructureRow=RowDataUtil.addValueData(metastructureRow, 5, v_precision.convertDataCompatible(v_precision,new Long(v.getPrecision())));
			        
			        metastructureRow=RowDataUtil.addValueData(metastructureRow, 6, v.getOrigin());
			        
			        if(meta.isOutputRowcount()){
				        ValueMetaInterface v_rowCount = data.outputRowMeta.getValueMeta(7);
				        metastructureRow=RowDataUtil.addValueData(metastructureRow, 7,v_rowCount.convertDataCompatible(v_rowCount,new Long(rowCount)));
			        }
			        putRow(data.outputRowMeta, metastructureRow.clone());
			    }
		    	setOutputDone();
		    	return false;
	    	}
	    }
	    
	    rowCount++;
	    lastRow = r;
	    return true;
        
    }
	
	//
    // Run is were the action happens!
    public void run()
    {
    	//BaseStep.runStepThread(this, meta, data);
    	try
		{
			logBasic(Messages.getString("System.Log.StartingToRun")); //$NON-NLS-1$
			
			while (processRow(meta, data) && !isStopped());
		}
		catch(Throwable t)
		{
			logError(Messages.getString("System.Log.UnexpectedError")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(t));
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
