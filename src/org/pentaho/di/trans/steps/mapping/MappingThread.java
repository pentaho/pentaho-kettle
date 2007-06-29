package org.pentaho.di.trans.steps.mapping;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepInterface;

/**
 * This thread handles the data communication between a certain parent step and a "Mapping Input" step in the mapping.
 * @author matt
 *
 */
public class MappingThread implements Runnable {
	private static LogWriter log = LogWriter.getInstance();
	
	private Mapping mapping;
	private StepInterface sourceStep;
	private StepInterface targetStep;
	
	/**
	 * @param mapping The parent mapping to work with
	 * @param sourceStep the source step to read from
	 * @param targetStep the target step to write to
	 */
	public MappingThread(Mapping mapping, StepInterface sourceStep, StepInterface targetStep) {
		super();
		this.mapping = mapping;
		this.sourceStep = sourceStep;
		this.targetStep = targetStep;
	}
	
	@Override
	public String toString() {
		return "MappingThread "+sourceStep.getStepname();
	}

	public void run() {
		try
		{
			// Read a row
			RowMetaAndData row = getRow();
			while (row!=null && !mapping.isStopped())
			{
				// This row goes to the mapping input step in the mapping...
				targetStep.putRow(row.getRowMeta(), row.getData());
				
				// Grab another row...
				row = getRow();
			}
		}
		catch(Exception e)
		{
			log.logError(toString(), "Unexpected error : "+e.toString());
			log.logError(toString(), Const.getStackTracker(e));
			mapping.setErrors(1);
			mapping.stopAll();
		}
	}

	private RowMetaAndData getRow() throws KettleException {
		if (sourceStep!=null) {
			return mapping.getRowFrom(sourceStep.getStepname());	
		}
		else {
			Object[] rowData = mapping.getRow();
			if (rowData==null) return null;
			RowMetaInterface rowMeta = mapping.getInputRowMeta();
			return new RowMetaAndData(rowMeta, rowData);
		}	
	}
}
