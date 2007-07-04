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
 

package org.pentaho.di.trans.steps.mergejoin;

import java.util.List;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Biswapesh
 * @since 24-nov-2005
 *
 */

public class MergeJoinData extends BaseStepData implements StepDataInterface
{
	public Object[] one, two;
	public RowMetaInterface oneMeta, twoMeta;
	public RowMetaInterface outputRowMeta; //just for speed: oneMeta+twoMeta
	public Object[] one_dummy, two_dummy;
    public List<Object[]> ones, twos;
    public Object[] one_next, two_next;
    public boolean one_optional, two_optional;
    public int[] keyNrs1;
    public int[] keyNrs2;
	
    public RowSet oneRowSet;
	public RowSet twoRowSet;
    
	/**
	 * Default initializer
	 */
	public MergeJoinData()
	{
		super();
		ones = null;
		twos = null;
		one_next = null;
		two_next = null;
		one_dummy = null;
		two_dummy = null;
		one_optional = false;
		two_optional = false;
		keyNrs1 = null;
		keyNrs2 = null;
	}

}
