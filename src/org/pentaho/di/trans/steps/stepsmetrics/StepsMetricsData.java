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
 

package org.pentaho.di.trans.steps.stepsmetrics;


import java.util.concurrent.ConcurrentHashMap;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;

/**
 * @author Samatar
 * @since 16-06-2008
 *
 */
public class StepsMetricsData extends BaseStepData implements StepDataInterface
{

	/**
	 * 
	 */
	boolean continueLoop;
	public ConcurrentHashMap<Integer, StepInterface> stepInterfaces;
    /** The metadata we send out */
    public RowMetaInterface outputRowMeta;
	
    public String realstepnamefield;
    public String realstepidfield;
    public String realsteplinesinputfield;
    public String realsteplinesoutputfield;
    public String realsteplinesreadfield;
    public String realsteplinesupdatedfield;
    public String realsteplineswrittentfield;
    public String realsteplineserrorsfield;
    public String realstepsecondsfield;

	
	public StepsMetricsData()
	{
		super();
		continueLoop = true;
		
	    realstepnamefield=null;
	    realstepidfield=null;
	    realsteplinesinputfield=null;
	    realsteplinesoutputfield=null;
	    realsteplinesreadfield=null;
	    realsteplinesupdatedfield=null;
	    realsteplineswrittentfield=null;
	    realsteplineserrorsfield=null;
	    realstepsecondsfield=null;
	}
}
