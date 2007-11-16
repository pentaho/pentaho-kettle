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
 

package be.ibridge.kettle.trans.step.exceloutput;

import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.vfs.FileObject;

import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 7-sep-2006
 */
public class ExcelOutputData extends BaseStepData implements StepDataInterface
{
	public int splitnr;

	public Row headerrow;
	public int fieldnrs[];

    public WritableWorkbook workbook;

    public WritableSheet sheet;
    
    public int templateColumns; // inital number of columns in the template

    public WritableFont writableFont;

    public WritableCellFormat writableCellFormat;
    
    public Map formats;

    public int positionX;

    public int positionY;

    public WritableFont headerFont;
    
	public FileObject file;
	public FileObject fo;

	/**
	 * 
	 */
	public ExcelOutputData()
	{
		super();
        
        formats = new Hashtable();
        file=null;
        fo=null;
	}

}
