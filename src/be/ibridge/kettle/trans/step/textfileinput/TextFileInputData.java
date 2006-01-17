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
 

package be.ibridge.kettle.trans.step.textfileinput;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.zip.ZipInputStream;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 22-jan-2005
 */
public class TextFileInputData extends BaseStepData implements StepDataInterface 
{
    /** @deprecated */
	public String thisline, nextline, lastline;
    
    public ArrayList lineBuffer;
    
	public Row previous_row;
	public int nr_repeats;
    
    public int nrLinesOnPage;
	
	public NumberFormat nf;
	public DecimalFormat df;
	public DecimalFormatSymbols dfs;
	public SimpleDateFormat daf;
	public DateFormatSymbols dafs;
	
	public String  files[];
	public boolean isLastFile;
	public String  filename;
	public int     filenr;
	
	public FileInputStream fr;
	public ZipInputStream zi;
    public InputStreamReader isr;

    public boolean doneReading;

    public int headerLinesRead;

    public int footerLinesRead;

    public int pageLinesRead;

    public boolean doneWithHeader;

	/**
	 * 
	 */
	public TextFileInputData()
	{
		super();

		thisline=null;
		nextline=null;
        
        lineBuffer = new ArrayList();
		nf = NumberFormat.getInstance();
		df = (DecimalFormat)nf;
		dfs=new DecimalFormatSymbols();
		daf = new SimpleDateFormat();
		dafs= new DateFormatSymbols();

		nr_repeats=0;
		previous_row=null;
		filenr = 0;
        
        nrLinesOnPage=0;
		
		fr=null;
		zi=null;
	}

}
