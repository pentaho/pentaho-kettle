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

package be.ibridge.kettle.trans.step.denormaliser;

import java.util.Hashtable;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * Data structure used by Denormaliser during processing
 * @author Matt
 * @since 19-jan-2006
 *
 */
public class DenormaliserData extends BaseStepData implements StepDataInterface
{
	public Row previous;
	
	public int     groupnrs[];
	public Integer fieldNrs[];
	public Row     targetResult;

    public int keyFieldNr;
    
    public Hashtable keyValue;

    public int[] removeNrs;

    public int[] fieldNameIndex;

    public long[] counters;

    public Value[] sum;

	/**
	 * 
	 */
	public DenormaliserData()
	{
		super();

		previous=null;
        keyValue = new Hashtable();
	}

}
