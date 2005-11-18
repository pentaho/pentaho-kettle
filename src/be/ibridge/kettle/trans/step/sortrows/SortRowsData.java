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
 

package be.ibridge.kettle.trans.step.sortrows;

import java.util.ArrayList;
import java.util.Vector;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 */
public class SortRowsData extends BaseStepData implements StepDataInterface
{
	public ArrayList files;
	public Vector    buffer;
	public ArrayList fis, dis;
	public ArrayList rowbuffer;

	public int     fieldnrs[];      // the corresponding field numbers;

	/**
	 * 
	 */
	public SortRowsData()
	{
		super();
		
		buffer=new Vector(Const.SORT_SIZE);
		files=new ArrayList();
		fis  =new ArrayList();
		dis  =new ArrayList();
		rowbuffer=new ArrayList();
		
	}

}
