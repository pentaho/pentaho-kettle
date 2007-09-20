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
 

package org.pentaho.di.trans.steps.closure;

import java.util.Map;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 18-Sep-2007
 */
public class ClosureGeneratorData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface outputRowMeta;
	public int parentIndex;
	public int childIndex;
	public boolean reading;
	public ValueMetaInterface parentValueMeta;
	public ValueMetaInterface childValueMeta;
	public Map<Object, Object> map;
	public Map<Object, Long> parents;
	public Object topLevel;
	
	public ClosureGeneratorData()
	{
		super();
	}
}
