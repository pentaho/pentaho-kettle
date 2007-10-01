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
 

package org.pentaho.di.trans.steps.exceloutput;

import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Map;

import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 7-sep-2006
 */
public class ExcelOutputData extends BaseStepData implements StepDataInterface
{
	public int splitnr;

	public Object[] headerrow;
	public RowMetaInterface previousMeta;
	public RowMetaInterface outputMeta;
	public int fieldnrs[];

    public WritableWorkbook workbook;

    public WritableSheet sheet;
    
    public int templateColumns; // inital number of columns in the template

    public WritableFont writableFont;

    public WritableCellFormat writableCellFormat;
    
    public Map<String,WritableCellFormat> formats;

    public int positionX;

    public int positionY;

    public WritableFont headerFont;

	public OutputStream outputStream;

	/**
	 * 
	 */
	public ExcelOutputData()
	{
		super();
        
        formats = new Hashtable<String,WritableCellFormat>();
	}

}
