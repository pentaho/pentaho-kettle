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
 

package org.pentaho.di.trans.steps.blockuntilstepsfinish;

import java.util.concurrent.ConcurrentHashMap;

import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;

/**
 * @author Samatar
 * @since 16-06-2008
 *
 */
public class BlockUntilStepsFinishData extends BaseStepData implements StepDataInterface
{

	/**
	 * 
	 */ 
	boolean continueLoop;
	public ConcurrentHashMap<Integer, StepInterface> stepInterfaces;
	
	public BlockUntilStepsFinishData()
	{
		super();
		continueLoop = true;
	}

}
