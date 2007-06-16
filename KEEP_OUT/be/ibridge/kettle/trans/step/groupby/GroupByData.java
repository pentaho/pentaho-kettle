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

package be.ibridge.kettle.trans.step.groupby;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class GroupByData extends BaseStepData implements StepDataInterface
{
	public Row previous;
	
	public Row  agg;
	public int  groupnrs[];
	public int  subjectnrs[];
	public long counts[];

    public ArrayList bufferList;

    public File tempFile;

    public FileOutputStream fos;

    public DataOutputStream dos;

    public int rowsOnFile;

    public boolean firstRead;

    public FileInputStream fis;
    public DataInputStream dis;

    public Row groupResult;

    public boolean hasOutput;
    
	/**
	 * 
	 */
	public GroupByData()
	{
		super();

		previous=null;
	}

}
