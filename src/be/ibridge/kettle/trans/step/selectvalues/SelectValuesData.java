 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/
 

package be.ibridge.kettle.trans.step.selectvalues;

import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 */
public class SelectValuesData extends BaseStepData implements StepDataInterface
{
	public int fieldnrs[];
	public int removenrs[];
	public int metanrs[];
	
	public boolean firstselect;
	public boolean firstdeselect;
	public boolean firstmetadata;
	
	public Value values[];
	
	// The MODE, default = select...
	public boolean select;      // "normal" selection of fields.
	public boolean deselect;    // de-select mode
	public boolean metadata;    // change meta-data (rename & change length/precision)

	/**
	 * 
	 */
	public SelectValuesData()
	{
		super();
	}

}
