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
 

package org.pentaho.di.trans.steps.fieldsplitter;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class FieldSplitterData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface previousMeta;
	public RowMetaInterface outputMeta;
	public int fieldnr;

	//TODO check if it can be deleted
//	public NumberFormat nf;
//	public DecimalFormat df;
//	public DecimalFormatSymbols dfs;
//	public SimpleDateFormat daf;
//	public DateFormatSymbols dafs;
		

	/**
	 * 
	 */
	public FieldSplitterData()
	{
		super();

//		nf = NumberFormat.getInstance();
//		df = (DecimalFormat)nf;
//		dfs=new DecimalFormatSymbols();
//		daf = new SimpleDateFormat();
//		dafs= new DateFormatSymbols();
	}

}
