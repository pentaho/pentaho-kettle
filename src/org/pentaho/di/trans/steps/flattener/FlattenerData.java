 package org.pentaho.di.trans.steps.flattener;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;



/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class FlattenerData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface inputRowMeta;
	public RowMetaInterface outputRowMeta;
	
    public Object[] targetRow;
	public Object[] targetResult;
    public int  processed;
    public int  fieldNr;
    public Object[] previousRow;

	/**
	 * 
	 */
	public FlattenerData()
	{
		super();

	}

}
