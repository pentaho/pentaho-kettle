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
 

package org.pentaho.di.trans.steps.joinrows;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;




/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class JoinRowsData extends BaseStepData implements StepDataInterface
{
	public File             file[];
	public FileInputStream  fileInputStream[];
	public DataInputStream  dataInputStream[];
    public RowMetaInterface fileRowMeta[];

	public int             size[];
	public int             position[];
	public boolean         restart[];
	public RowSet          rs[];
	public List<Object[]>  cache[];
	
	public boolean         caching;

	public FileOutputStream fileOutputStream[];
	public DataOutputStream dataOutputStream[];
	
	public Object[] 		joinrow[];

	/**
	 * Keep track of which file temp file we're using... 
	 */
	public int filenr;
    
    public RowMetaInterface outputRowMeta;

	/**
	 * 
	 */
	public JoinRowsData()
	{
		super();
	}

}
