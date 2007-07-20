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
 
package org.pentaho.di.trans.steps.cubeinput;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 */
public class CubeInputData extends BaseStepData implements StepDataInterface
{
	public InputStream fis;
	public GZIPInputStream zip;
	public DataInputStream dis;

	public RowMetaInterface meta;
	
	/**
	 * 
	 */
	public CubeInputData()
	{
		super();
	}

}
