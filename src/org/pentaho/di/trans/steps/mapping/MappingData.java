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
 

package org.pentaho.di.trans.steps.mapping;

import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.mappinginput.MappingInput;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutput;



/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class MappingData extends BaseStepData implements StepDataInterface
{
    public Trans trans;
    public MappingInput  mappingInput;
    public MappingOutput mappingOutput;
	public List<Integer> renameFieldIndexes;
	public List<String>  renameFieldNames;
    public boolean wasStarted;
    public TransMeta mappingTransMeta;
    public RowMetaInterface outputRowMeta;
    
	/**
	 * 
	 */
	public MappingData()
	{
		super();
        trans = null;
        wasStarted = false;
	}

}
