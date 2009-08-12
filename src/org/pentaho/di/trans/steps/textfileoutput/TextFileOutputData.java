 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 

package org.pentaho.di.trans.steps.textfileoutput;

import java.io.OutputStream;
import java.text.*;
import java.util.*;
import java.util.zip.ZipOutputStream;

import org.pentaho.di.core.row.*;
import org.pentaho.di.trans.step.*;

/**
 * @author Matt
 * @since 22-jan-2005
 */
public class TextFileOutputData extends BaseStepData implements StepDataInterface
{
    public int fieldnrs[];

	public NumberFormat nf;
	public DecimalFormat df;
	public DecimalFormatSymbols dfs;
	
	public SimpleDateFormat daf;
	public DateFormatSymbols dafs;

    public DecimalFormat        defaultDecimalFormat;
    public DecimalFormatSymbols defaultDecimalFormatSymbols;

    public SimpleDateFormat  defaultDateFormat;
    public DateFormatSymbols defaultDateFormatSymbols;

    public RowMetaInterface outputRowMeta;

	public byte[] binarySeparator;
	public byte[] binaryEnclosure;
	public byte[] binaryNewline;

	public boolean hasEncoding;

	public byte[] binaryNullValue[];
	
	public boolean oneFileOpened;

    public boolean isSplitting;
    public long extraLinesWritten;
    
    public String lastRowFileName;
    public int  fileNameFieldIndex;
    public ValueMetaInterface fileNameMeta;

    public ZipOutputStream parentZipOutputStream;
    public TextFileOutputData.OutputMeta outputMeta;
    public TextFileOutputData.OutputMeta parentZipOutputMeta;
    public Map<String,TextFileOutputData.OutputMeta> outputMetaMap;

    public boolean delayedHeaderWrite;
    
    public static class OutputMeta
    {
        public String name;
        public OutputStream writer;
        public Process cmdProc;
        public int splitnr;
    }

    public TextFileOutputData()
    {
        super();

        nf = NumberFormat.getInstance();
        df = (DecimalFormat)nf;
        dfs=new DecimalFormatSymbols();

		daf = new SimpleDateFormat();
		dafs= new DateFormatSymbols();
        
        defaultDecimalFormat = (DecimalFormat)NumberFormat.getInstance();
        defaultDecimalFormatSymbols =  new DecimalFormatSymbols();

        defaultDateFormat = new SimpleDateFormat();
        defaultDateFormatSymbols = new DateFormatSymbols();

        fileNameFieldIndex = -1;
        oneFileOpened=false;

        outputMetaMap = new HashMap<String,TextFileOutputData.OutputMeta>();
    }
}
