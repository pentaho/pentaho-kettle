 package org.pentaho.di.trans.steps.normaliser;

import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;



/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class NormaliserData extends BaseStepData implements StepDataInterface
{
	public List<String> type_occ;
	public int maxlen;
	public List<Integer> copy_fieldnrs;
	public int fieldnrs[];

	public RowMetaInterface inputRowMeta;
	public RowMetaInterface outputRowMeta;
		
	/**
	 * 
	 */
	public NormaliserData()
	{
		super();
		
		type_occ = null;
	}

}
