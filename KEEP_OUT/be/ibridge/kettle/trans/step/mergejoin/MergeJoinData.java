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
 

package be.ibridge.kettle.trans.step.mergejoin;

import java.util.ArrayList;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;

/**
 * @author Biswapesh
 * @since 24-nov-2005
 *
 */

public class MergeJoinData extends BaseStepData implements StepDataInterface
{
	public Row one, two;
	public Row one_dummy, two_dummy;
    public ArrayList ones, twos;
    public Row one_next, two_next;
    public boolean one_optional, two_optional;
    public int[] keyNrs1;
    public int[] keyNrs2;
    
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
