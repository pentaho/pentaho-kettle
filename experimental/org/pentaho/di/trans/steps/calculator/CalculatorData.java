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
    public RowMetaInterface tempRowMeta;

    public int nrTemporaryFields;

    public Object[] tempData;

    // TODO: turn this into a class/structure and allocate one row for each calculation
    // That way we can speed this up even more.
    // We just want to be able to pick up the values the indexs and perform the calculations with those.
    // 
    public int[] keyNrs;

    public int[] keyNrsA;

    public int[] keyNrsB;

    public int[] keyNrsC;
    
    

	/**
	 * 
	 */
	public CalculatorData()
	{
		super();
        
        indexCache = new Hashtable();
	}

}
