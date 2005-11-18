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
 

package be.ibridge.kettle.trans.step.excelinput;

import jxl.Sheet;
import jxl.Workbook;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class ExcelInputData extends BaseStepData implements StepDataInterface
{
	/**
	 * Empty row containing the expected fields.
	 */
	public Row row;
    
    /**
     * The previous row in case we want to repeat values...
     */
    public Row previousRow;
	
	/**
	 * The maximum length of all filenames...
	 */
	public int maxfilelength;
	
	/**
	 * The maximum length of all sheets...
	 */
	public int maxsheetlength;

	/**
	 * The Excel files to read
	 */
	public String files[];

	/**
	 * The file number that's being handled...
	 */
	public int filenr;
	
	/**
	 * The workbook that's being processed...
	 */
	public Workbook workbook;
	
	/**
	 * The sheet number that's being processed...
	 */
	public int sheetnr;
	
	/**
	 * The sheet that's being processed...
	 */
	public Sheet sheet;
	
	
	/**
	 * The row where we left off the previous time...
	 */
	public int rownr;
	
	/**
	 * The column where we left off previous time...
	 */
	public int colnr;
	
	/**
	 * 
	 */
	public ExcelInputData()
	{
		super();
		workbook=null;
		filenr=0;
		sheetnr=0;
		rownr=-1;
		colnr=-1;
	}

}
