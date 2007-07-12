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
 

package org.pentaho.di.trans.steps.valuemapper;

import java.util.Hashtable;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 */
public class ValueMapperData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface previousMeta;
	public RowMetaInterface outputMeta;

	public int   keynr;
	
    public Hashtable<String,String> hashtable;

    public int emptyFieldIndex;

	/**
	 * 
	 */
	public ValueMapperData()
	{
		super();
		
		hashtable = null;
	}

}
