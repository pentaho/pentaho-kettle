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

package org.pentaho.di.trans.steps.textfileinput;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.playlist.FilePlayList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandler;


/**
 * @author Matt
 * @since 22-jan-2005
 */
public class TextFileInputData extends BaseStepData implements
		StepDataInterface {
	
	public ArrayList lineBuffer;

	public Object[] previous_row;

	public int nr_repeats;

	public int nrLinesOnPage;

	public NumberFormat nf;

	public DecimalFormat df;

	public DecimalFormatSymbols dfs;

	public SimpleDateFormat daf;

	public DateFormatSymbols dafs;

	public FileInputList files;

	public boolean isLastFile;

	public String filename;
	
	public int lineInFile;
	
	public FileObject file;

	public int filenr;

	public InputStream fr;

	public ZipInputStream zi;

	public GZIPInputStream gzi;

	public InputStreamReader isr;

	public boolean doneReading;

	public int headerLinesRead;

	public int footerLinesRead;

	public int pageLinesRead;

	public boolean doneWithHeader;

	public FileErrorHandler dataErrorLineHandler;

	public FilePlayList filePlayList;
	
	public TextFileFilterProcessor filterProcessor;

    public RowMetaInterface outputRowMeta;

    public StringBuffer lineStringBuffer;

    public int fileFormatType;

    public int fileType;
    
	/**
	 * 
	 */
	public TextFileInputData() {
		super();

		lineBuffer = new ArrayList();
		nf = NumberFormat.getInstance();
		df = (DecimalFormat) nf;
		dfs = new DecimalFormatSymbols();
		daf = new SimpleDateFormat();
		dafs = new DateFormatSymbols();

		nr_repeats = 0;
		previous_row = null;
		filenr = 0;

		nrLinesOnPage = 0;

		fr = null;
		zi = null;
		
		filterProcessor = null;
        
         lineStringBuffer = new StringBuffer(256);
	}

	public void setDateFormatLenient(boolean lenient) {
		daf.setLenient(lenient);
	}
}