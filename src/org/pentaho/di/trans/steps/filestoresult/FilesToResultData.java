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

package org.pentaho.di.trans.steps.filestoresult;

import java.util.ArrayList;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 26-may-2006
 */
public class FilesToResultData extends BaseStepData implements StepDataInterface
{
	public ArrayList filenames;

	public int filenameIndex;

	public RowMetaInterface outputRowMeta;

	/**
	 * 
	 */
	public FilesToResultData()
	{
		super();

		filenames = new ArrayList();
	}

}
