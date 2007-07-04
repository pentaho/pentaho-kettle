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

package org.pentaho.di.trans.steps.groupby;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class GroupByData extends BaseStepData implements StepDataInterface
{
	public Object previous[];
	
	public Object  agg[];
	public RowMetaInterface aggMeta;
	public RowMetaInterface groupMeta;
	public RowMetaInterface groupAggMeta; // for speed: groupMeta+aggMeta
	public int  groupnrs[];
	public int  subjectnrs[];
	public long counts[];

    public ArrayList<Object[]> bufferList;

    public File tempFile;

    public FileOutputStream fos;

    public DataOutputStream dos;

    public int rowsOnFile;

    public boolean firstRead;

    public FileInputStream fis;
    public DataInputStream dis;

    public Object groupResult[];

    public boolean hasOutput;
    
    
    public RowMetaInterface inputRowMeta;
    public RowMetaInterface outputRowMeta;
    
	/**
	 * 
	 */
	public GroupByData()
	{
		super();

		previous=null;
	}

}
