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
