package org.pentaho.di.trans.steps.splitfieldtorows;


import org.pentaho.di.core.Const;
import org.pentaho.di.core.SimpleTokenizer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

 public class SplitFieldToRows extends BaseStep implements StepInterface
{
	private SplitFieldToRowsMeta meta;
	private SplitFieldToRowsData data;
	
	public SplitFieldToRows(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private boolean splitField(RowMetaInterface rowMeta, Object[] rowData) throws KettleException
	{
		if (first)
		{
			first = false;
			
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			data.fieldnr = rowMeta.indexOfValue(meta.getSplitField());
			
			int numErrors = 0;
			if (Const.isEmpty(meta.getNewFieldname()))
			{
				logError(Messages.getString("SplitFieldToRows.Log.NewFieldNameIsNull")); //$NON-NLS-1$
				numErrors++;
			}

			if (data.fieldnr < 0)
			{
				logError(Messages.getString("SplitFieldToRows.Log.CouldNotFindFieldToSplit", meta.getSplitField())); //$NON-NLS-1$
				numErrors++;
			}
			
			if (!rowMeta.getValueMeta(data.fieldnr).isString())
			{
				logError(Messages.getString("SplitFieldToRows.Log.SplitFieldNotValid",meta.getSplitField())); //$NON-NLS-1$
				numErrors++;
			}

			if (numErrors > 0)
			{
				setErrors(numErrors);
				stopAll();
				return false;
			}
			
			data.splitMeta = rowMeta.getValueMeta(data.fieldnr);
		}
		
		String originalString = data.splitMeta.getString(rowData[data.fieldnr]);
		if (originalString == null) {
			originalString = "";
		}
		
		SimpleTokenizer tokenizer = new SimpleTokenizer(originalString, meta.getDelimiter());
		while (tokenizer.hasMoreTokens()) {
			Object[] outputRow = RowDataUtil.createResizedCopy(rowData, data.outputRowMeta.size());
			outputRow[rowMeta.size()] = tokenizer.nextToken();
			putRow(data.outputRowMeta, outputRow);
		}
		
		return true;
	}
	
	public synchronized boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SplitFieldToRowsMeta)smi;
		data=(SplitFieldToRowsData)sdi;

		Object[] r = getRow();   // get row from rowset, wait for our turn, indicate busy!
		if (r == null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		boolean ok = splitField(getInputRowMeta(), r);
		if (!ok)
		{
			setOutputDone();
			return false;
		}
					
        if (checkFeedback(linesRead)) {
			if(log.isBasic()) 
			{
				if(log.isBasic()) logBasic(Messages.getString("SplitFieldToRows.Log.LineNumber")+linesRead); //$NON-NLS-1$
			}
		}
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (SplitFieldToRowsMeta)smi;
		data = (SplitFieldToRowsData)sdi;
		
		if (super.init(smi, sdi))
		{
		    return true;
		}
		return false;
	}

			
	//
	// Run is were the action happens!
	//
	public void run()
	{
		BaseStep.runStepThread(this, meta, data);
	}
}
