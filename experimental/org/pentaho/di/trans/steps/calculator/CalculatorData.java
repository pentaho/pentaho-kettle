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
 

package org.pentaho.di.trans.steps.calculator;

import java.util.Hashtable;
import java.util.Map;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 8-sep-2005
 *
 */
public class CalculatorData extends BaseStepData implements StepDataInterface
{
    public Map indexCache;
    
    public RowMetaInterface outputRowMeta;
    public RowMetaInterface calcRowMeta;
    
    public Calculator.FieldIndexes[] fieldIndexes;

    public int[] tempIndexes;
    
	/**
	 * 
	 */
	public CalculatorData()
	{
		super();
        
        indexCache = new Hashtable();
	}

}
