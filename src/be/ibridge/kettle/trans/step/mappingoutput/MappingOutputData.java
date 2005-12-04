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
 

package be.ibridge.kettle.trans.step.mappingoutput;

import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.mapping.Mapping;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class MappingOutputData extends BaseStepData implements StepDataInterface
{

	public Mapping mapping;
    public String[] outputField;
    public String[] outputMapping;
    
    /**
	 * 
	 */
	public MappingOutputData()
	{
		super();
	}

}
