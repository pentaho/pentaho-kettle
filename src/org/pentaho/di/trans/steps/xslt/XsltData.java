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

package org.pentaho.di.trans.steps.xslt;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 24-jan-2005
 *
 */
public class XsltData extends BaseStepData implements StepDataInterface
{

	public RowMetaInterface outputRowMeta;
	public int fieldposition;
	public int fielxslfiledposition;
	public String xslfilename;
	
	public int fields_used[];
	
	/**
	 * 
	 */
	public XsltData()
	{
		super();
		fieldposition=-1;
		fielxslfiledposition=-1;
		xslfilename=null;
	}

}
