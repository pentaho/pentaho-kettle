 

package org.pentaho.reporting.birt.plugin;

import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Bart Maertens
 * @since 24-aug-2011
 */
public class BIRTOutputData extends BaseStepData implements StepDataInterface
{
    public int inputFieldIndex;
    public int outputFieldIndex;
    
    /**
	 * 
	 */
	public BIRTOutputData()
	{
		super();
	}

}
