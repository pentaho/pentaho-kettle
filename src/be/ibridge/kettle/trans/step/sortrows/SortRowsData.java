 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
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
