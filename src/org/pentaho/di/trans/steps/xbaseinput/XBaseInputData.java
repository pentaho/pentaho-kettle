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
 
package org.pentaho.di.trans.steps.xbaseinput;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * Provides data for the XBaseInput step.
 * 
 * @author Matt
 * @since 20-jan-2005
 */
public class XBaseInputData extends BaseStepData implements StepDataInterface
{
	public XBase xbi;
	public RowMetaInterface fields;
    public int fileNr;
    public FileObject file_dbf;
    public FileInputList files;
	public RowMetaInterface outputRowMeta;

	public XBaseInputData()
	{
		super();

		xbi=null;
		fields=null;
	}

}
