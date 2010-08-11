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
 

package org.pentaho.di.trans.steps.fieldschangesequence;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 16-06-2008
 *
 */
public class FieldsChangeSequenceData extends BaseStepData implements StepDataInterface
{

	/**
	 * 
	 */
	public RowMetaInterface previousMeta;
    public RowMetaInterface outputRowMeta;
    
	public int     fieldnrs[]; 
	public ValueMetaInterface fieldnrsMeta[];
	public Object   previousValues[]; 
	public int     fieldnr;
	public long     startAt;
	public long     incrementBy;
	public long     seq;
	public int nextIndexField;
	
	public FieldsChangeSequenceData()
	{
		super();
	}

}
