/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
/*
 *
 *
 */

package org.pentaho.di.ui.trans.steps.textfileinput;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.textfileinput.InputFileMetaInterface;
import org.pentaho.di.trans.steps.textfileinput.TextFileInput;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileLine;
import org.pentaho.di.ui.core.dialog.ErrorDialog;

/**
 * Takes care of displaying a dialog that will handle the wait while we're finding out what tables, views etc we can
 * reach in the database.
 * 
 * @author Matt
 * @since 07-apr-2005
 */
public class TextFileCSVImportProgressDialog
{
	private static Class<?> PKG = TextFileInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private Shell             shell;

    private InputFileMetaInterface     meta;

    private int               samples;
    
    private boolean           replaceMeta;
    
    private String            message;

    private String            debug;
    
    private long              rownumber;

    private InputStreamReader reader;
    
    private TransMeta         transMeta;

	private LogChannelInterface	log;
    
    /**
     * Creates a new dialog that will handle the wait while we're finding out what tables, views etc we can reach in the
     * database.
     */
    public TextFileCSVImportProgressDialog(Shell shell, InputFileMetaInterface meta, TransMeta transMeta, InputStreamReader reader, int samples, boolean replaceMeta )
    {
        this.shell       = shell;
        this.meta        = meta;
        this.reader      = reader;
        this.samples     = samples;
        this.replaceMeta = replaceMeta;
        this.transMeta   = transMeta;

        message = null;
        debug = "init";
        rownumber = 1L;
        
        this.log = new LogChannel(transMeta);
    }

    public String open()
    {
        IRunnableWithProgress op = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                try
                {
                    message = doScan(monitor);
                }
                catch (Exception e)
                {
                	e.printStackTrace();
                    throw new InvocationTargetException(e, BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Exception.ErrorScanningFile", ""+rownumber, debug, e.toString()));
                }
            }
        };

        try
        {
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
            pmd.run(true, true, op);
        }
        catch (InvocationTargetException e)
        {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.ErrorScanningFile.Title"), BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.ErrorScanningFile.Message"), e);
        }
        catch (InterruptedException e)
        {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.ErrorScanningFile.Title"), BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.ErrorScanningFile.Message"), e);
        }
  
        return message;
    }

    private String doScan(IProgressMonitor monitor) throws KettleException
    {
        if (samples>0) monitor.beginTask(BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Task.ScanningFile"), samples+1);
        else           monitor.beginTask(BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Task.ScanningFile"), 2);

        String line = "";
        long fileLineNumber = 0;
        
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();

        int nrfields = meta.getInputFields().length;
        
        RowMetaInterface outputRowMeta = new RowMeta();
        meta.getFields(outputRowMeta, null, null, null, transMeta);
        
        // Remove the storage meta-data (don't go for lazy conversion during scan)
        for (ValueMetaInterface valueMeta : outputRowMeta.getValueMetaList()) {
        	valueMeta.setStorageMetadata(null);
        	valueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
        }

        RowMetaInterface convertRowMeta = outputRowMeta.clone();
        for (int i=0;i<convertRowMeta.size();i++) convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);

        // How many null values?
        int nrnull[] = new int[nrfields]; // How many times null value?

        // String info
        String minstr[] = new String[nrfields]; // min string
        String maxstr[] = new String[nrfields]; // max string
        boolean firststr[] = new boolean[nrfields]; // first occ. of string?

        // Date info
        boolean isDate[] = new boolean[nrfields]; // is the field perhaps a Date?
        int dateFormatCount[] = new int[nrfields]; // How many date formats work?
        boolean dateFormat[][] = new boolean[nrfields][Const.getDateFormats().length]; // What are the date formats that
        // work?
        Date minDate[][] = new Date[nrfields][Const.getDateFormats().length]; // min date value
        Date maxDate[][] = new Date[nrfields][Const.getDateFormats().length]; // max date value

        // Number info
        boolean isNumber[] = new boolean[nrfields]; // is the field perhaps a Number?
        int numberFormatCount[] = new int[nrfields]; // How many number formats work?
        boolean numberFormat[][] = new boolean[nrfields][Const.getNumberFormats().length]; // What are the number format that work?
        double minValue[][] = new double[nrfields][Const.getDateFormats().length]; // min number value
        double maxValue[][] = new double[nrfields][Const.getDateFormats().length]; // max number value
        int numberPrecision[][] = new int[nrfields][Const.getNumberFormats().length]; // remember the precision?
        int numberLength[][] = new int[nrfields][Const.getNumberFormats().length]; // remember the length?

        for (int i = 0; i < nrfields; i++)
        {
            TextFileInputField field = meta.getInputFields()[i];

            if (log.isDebug()) debug = "init field #" + i;
          
            if (replaceMeta) // Clear previous info...
            {
                field.setName(meta.getInputFields()[i].getName());
                field.setType(meta.getInputFields()[i].getType());
                field.setFormat("");
                field.setLength(-1);
                field.setPrecision(-1);
                field.setCurrencySymbol(dfs.getCurrencySymbol());
                field.setDecimalSymbol("" + dfs.getDecimalSeparator());
                field.setGroupSymbol("" + dfs.getGroupingSeparator());
                field.setNullString("-");
                field.setTrimType(ValueMetaInterface.TRIM_TYPE_NONE);
            }

            nrnull[i] = 0;
            minstr[i] = "";
            maxstr[i] = "";
            firststr[i] = true;

            // Init data guess
            isDate[i] = true;
            for (int j = 0; j < Const.getDateFormats().length; j++)
            {
                dateFormat[i][j] = true;
                minDate[i][j] = Const.MAX_DATE;
                maxDate[i][j] = Const.MIN_DATE;
            }
            dateFormatCount[i] = Const.getDateFormats().length;

            // Init number guess
            isNumber[i] = true;
            for (int j = 0; j < Const.getNumberFormats().length; j++)
            {
                numberFormat[i][j] = true;
                minValue[i][j] = Double.MAX_VALUE;
                maxValue[i][j] = -Double.MAX_VALUE;
                numberPrecision[i][j] = -1;
                numberLength[i][j] = -1;
            }
            numberFormatCount[i] = Const.getNumberFormats().length;
        }

        InputFileMetaInterface strinfo = (InputFileMetaInterface) meta.clone();
        for (int i = 0; i < nrfields; i++)
            strinfo.getInputFields()[i].setType(ValueMetaInterface.TYPE_STRING);

        // Sample <samples> rows...
        debug = "get first line";

        StringBuilder lineBuffer = new StringBuilder(256);
        int fileFormatType = meta.getFileFormatTypeNr();
        
        // If the file has a header we overwrite the first line
        // However, if it doesn't have a header, take a new line
        //
        
        line = TextFileInput.getLine(log, reader, fileFormatType, lineBuffer);
        fileLineNumber++;
        int skipped=1;
        
        if (meta.hasHeader()) 
        {
            
            while (line!=null && skipped<meta.getNrHeaderLines())
            {
                line = TextFileInput.getLine(log, reader, fileFormatType, lineBuffer);
                skipped++;
                fileLineNumber++;
            }
        }
        int linenr = 1;

        // Allocate number and date parsers
        DecimalFormat df2 = (DecimalFormat) NumberFormat.getInstance();
        DecimalFormatSymbols dfs2 = new DecimalFormatSymbols();
        SimpleDateFormat daf2 = new SimpleDateFormat();

        boolean errorFound = false;
        while (!errorFound && line != null && (linenr <= samples || samples == 0) && !monitor.isCanceled())
        {
            monitor.subTask(BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Task.ScanningLine", ""+linenr));
            if (samples>0) monitor.worked(1);
            
            if (log.isDebug()) debug = "convert line #" + linenr + " to row";
            RowMetaInterface rowMeta = new RowMeta();
            meta.getFields(rowMeta, "stepname", null, null, transMeta);
            // Remove the storage meta-data (don't go for lazy conversion during scan)
            for (ValueMetaInterface valueMeta : rowMeta.getValueMetaList()) {
            	valueMeta.setStorageMetadata(null);
            	valueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
            }
            
    		String delimiter = transMeta.environmentSubstitute(meta.getSeparator());
            Object[] r = TextFileInput.convertLineToRow(log, new TextFileLine(line, fileLineNumber, null), strinfo, null, 0, outputRowMeta, convertRowMeta, meta.getFilePaths(transMeta)[0], rownumber, delimiter, null);

            if(r == null )
            {
            	errorFound = true;
            	continue;
            }
            rownumber++;
            for (int i = 0; i < nrfields && i < r.length; i++)
            {
                TextFileInputField field = meta.getInputFields()[i];

                if (log.isDebug()) debug = "Start of for loop, get new value " + i;
                ValueMetaInterface v = rowMeta.getValueMeta(i);
                if (log.isDebug()) debug = "Start of for loop over " + r.length + " elements in Row r, now at #" + i + " containing value : [" + v.toString() + "]";
                if (r[i]!=null)
                {
                    String fieldValue = rowMeta.getString(r, i);

                    int trimthis = ValueMetaInterface.TRIM_TYPE_NONE;

                    boolean spacesBefore = Const.nrSpacesBefore(fieldValue) > 0;
                    boolean spacesAfter = Const.nrSpacesAfter(fieldValue) > 0;

                    fieldValue = Const.trim(fieldValue);

                    if (spacesBefore) trimthis |= ValueMetaInterface.TRIM_TYPE_LEFT;
                    if (spacesAfter) trimthis |= ValueMetaInterface.TRIM_TYPE_RIGHT;

                    if (log.isDebug()) debug = "change trim type[" + i + "]";
                    field.setTrimType(field.getTrimType() | trimthis);

                    if (log.isDebug()) debug = "Field #" + i + " has type : " + ValueMeta.getTypeDesc(field.getType());

                    // See if the field has only numeric fields
                    if (isNumber[i])
                    {
                        if (log.isDebug()) debug = "Number checking of [" + fieldValue + "] on line #" + linenr;

                        boolean containsDot = false;
                        boolean containsComma = false;

                        for (int x = 0; x < fieldValue.length() && field.getType() == ValueMetaInterface.TYPE_NUMBER; x++)
                        {
                            char ch = fieldValue.charAt(x);
                            if (!Character.isDigit(ch) && ch != '.' && ch != ',' && (ch != '-' || x > 0) && ch != 'E' && ch != 'e' // exponential
                            )
                            {
                                isNumber[i] = false;
                            } else
                            {
                                if (ch == '.') containsDot = true;
                                if (ch == ',') containsComma = true;
                            }
                        }
                        // If it's still a number, try to parse it as a double
                        if (isNumber[i])
                        {
                            if (containsDot && !containsComma) // american 174.5
                            {
                                dfs2.setDecimalSeparator('.');
                                field.setDecimalSymbol(".");
                                dfs2.setGroupingSeparator(',');
                                field.setGroupSymbol(",");
                            } else
                                if (!containsDot && containsComma) // Belgian 174,5
                                {
                                    dfs2.setDecimalSeparator(',');
                                    field.setDecimalSymbol(",");
                                    dfs2.setGroupingSeparator('.');
                                    field.setGroupSymbol(".");
                                } else
                                    if (containsDot && containsComma) // Both appear!
                                    {
                                        // What's the last occurance: decimal point!
                                        int indexDot = fieldValue.indexOf('.');
                                        int indexComma = fieldValue.indexOf(',');
                                        if (indexDot > indexComma)
                                        {
                                            dfs2.setDecimalSeparator('.');
                                            field.setDecimalSymbol(".");
                                            dfs2.setGroupingSeparator(',');
                                            field.setGroupSymbol(",");
                                        } else
                                        {
                                            dfs2.setDecimalSeparator(',');
                                            field.setDecimalSymbol(",");
                                            dfs2.setGroupingSeparator('.');
                                            field.setGroupSymbol(".");
                                        }
                                    }

                            // Try the remaining possible number formats!
                            for (int x = 0; x < Const.getNumberFormats().length; x++)
                            {
                                if (numberFormat[i][x])
                                {
                                    try
                                    {
                                        df2.setDecimalFormatSymbols(dfs2);
                                        df2.applyPattern(Const.getNumberFormats()[x]);
                                        double d = df2.parse(fieldValue).doubleValue();

                                        // System.out.println("("+i+","+x+") : Converted ["+field.toString()+"]
                                        // to ["+d+"] with format ["+numberFormats[x]+"] and dfs2
                                        // ["+dfs2.getDecimalSeparator()+dfs2.getGroupingSeparator()+"]");

                                        // After everything, still a number?
                                        // Then guess the precision
                                        int prec = TextFileInputDialog.guessPrecision(d);
                                        if (prec > numberPrecision[i][x]) numberPrecision[i][x] = prec;

                                        int leng = TextFileInputDialog.guessLength(d) + prec; // add precision!
                                        if (leng > numberLength[i][x]) numberLength[i][x] = leng;

                                        if (d < minValue[i][x]) minValue[i][x] = d;
                                        if (d > maxValue[i][x]) maxValue[i][x] = d;
                                    }
                                    catch (Exception e)
                                    {
                                        numberFormat[i][x] = false; // Don't try it again in the future.
                                        numberFormatCount[i]--; // One less that works..
                                    }
                                }
                            }

                            // Still not found: just a string
                            if (numberFormatCount[i] == 0)
                            {
                                isNumber[i] = false;
                            }
                        }
                    }

                    if (log.isDebug()) debug = "Check max length on field #" + i + " called " + field.getName() + " : [" + fieldValue + "]";
                    // Capture the maximum length of the field (trimmed)
                    if (fieldValue.length() > field.getLength()) field.setLength(fieldValue.length());

                    // So is it really a string or a date field?
                    // Check it as long as we found a format that works...
                    if (isDate[i])
                    {
                        for (int x = 0; x < Const.getDateFormats().length; x++)
                        {
                            if (dateFormat[i][x])
                            {
                                try
                                {
                                    daf2.applyPattern(Const.getDateFormats()[x]);
                                    Date date = daf2.parse(fieldValue);

                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(date);
                                    int year = cal.get(Calendar.YEAR);

                                    if (year < 1800 || year > 2200)
                                    {
                                        dateFormat[i][x] = false; // Don't try it again in the future.
                                        dateFormatCount[i]--; // One less that works..
                                        // System.out.println("Field #"+i+", pattern ["+dateFormats[x]+"],
                                        // year="+year+", field=["+field+"] : year<1800 or year>2200!! not a
                                        // date!");
                                    }

                                    if (minDate[i][x].compareTo(date) > 0) minDate[i][x] = date;
                                    if (maxDate[i][x].compareTo(date) < 0) maxDate[i][x] = date;
                                }
                                catch (Exception e)
                                {
                                    dateFormat[i][x] = false; // Don't try it again in the future.
                                    dateFormatCount[i]--; // One less that works..
                                    // System.out.println("field ["+field+"] is not a date,
                                    // format=["+dateFormats[x]+", x="+x+", error: ("+e.toString()+")");
                                }
                            }
                        }

                        // Still not found: just a string
                        if (dateFormatCount[i] == 0)
                        {
                            isDate[i] = false;
                            // System.out.println("Field #"+i+" is not a date!");
                        }
                    }

                    // Determine maximum & minimum string values...
                    if (firststr[i])
                    {
                        firststr[i] = false;
                        minstr[i] = fieldValue;
                        maxstr[i] = fieldValue;
                    }
                    if (minstr[i].compareTo(fieldValue) > 0) minstr[i] = fieldValue;
                    if (maxstr[i].compareTo(fieldValue) < 0) maxstr[i] = fieldValue;

                    debug = "End of for loop";
                } else
                {
                    nrnull[i]++;
                }
            }

            fileLineNumber++;
            if (r!=null)
                linenr++;
            else
                rownumber--;

            // Grab another line...
            debug = "Grab another line";
            line = TextFileInput.getLine(log, reader, fileFormatType, lineBuffer);
            debug = "End of while loop";
        }

        monitor.worked(1);
        monitor.setTaskName(BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Task.AnalyzingResults"));
        
        // Include the results from the number, date & string search!
        // some cleanup of format fields for strings...
        for (int i = 0; i < nrfields; i++)
        {
            TextFileInputField field = meta.getInputFields()[i];

            if (field.getType() == ValueMetaInterface.TYPE_STRING)
            {
            	if (nrnull[i] != linenr - 1)
            	{
            		// If all values in a column where "null"/empty it makes more sense
            		// to keep the field as a string, previously it would be seen as a date.
                    if (isDate[i])
                    {
                        field.setType(ValueMetaInterface.TYPE_DATE);
                        for (int x = Const.getDateFormats().length - 1; x >= 0; x--)
                        {
                            if (dateFormat[i][x])
                            {
                                field.setFormat(Const.getDateFormats()[x]);
                                field.setLength(TextFileInputDialog.dateLengths[x]);
                                field.setPrecision(-1);
                            }
                        }
                    } else
                        if (isNumber[i])
                        {
                            field.setType(ValueMetaInterface.TYPE_NUMBER);
                            for (int x = Const.getNumberFormats().length - 1; x >= 0; x--)
                            {
                                if (numberFormat[i][x])
                                {
                                    field.setFormat(Const.getNumberFormats()[x]);
                                    field.setLength(numberLength[i][x]);
                                    field.setPrecision(numberPrecision[i][x]);

                                    if (field.getPrecision() == 0 && field.getLength() < 18)
                                    {
                                        field.setType(ValueMetaInterface.TYPE_INTEGER);
                                        field.setFormat("");
                                    }
                                }
                            }
                        } else
                        {
                            field.setDecimalSymbol("");
                            field.setGroupSymbol("");
                            field.setCurrencySymbol("");
                        }
            	    }
            	    else
                    {
                        field.setDecimalSymbol("");
                        field.setGroupSymbol("");
                        field.setCurrencySymbol("");
                    }
            }
        }
        
        
        // Show information on items using dialog box
        String message = "";
        message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.ResultAfterScanning", ""+(linenr-1));
        message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.HorizontalLine");
        for (int i = 0; i < nrfields; i++)
        {
            TextFileInputField field = meta.getInputFields()[i];

            message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.FieldNumber", ""+(i + 1));

            message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.FieldName", field.getName());
            message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.FieldType", field.getTypeDesc());

            switch (field.getType())
            {
            case ValueMetaInterface.TYPE_NUMBER:
                message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.EstimatedLength", (field.getLength() < 0 ? "-" : "" + field.getLength()));
                message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.EstimatedPrecision", field.getPrecision() < 0 ? "-" : "" + field.getPrecision());
                message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.NumberFormat", field.getFormat());
                if (numberFormatCount[i] > 1)
                {
                    message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.WarnNumberFormat");
                }
                for (int x = 0; x < Const.getNumberFormats().length; x++)
                {
                    if (numberFormat[i][x])
                    {
                        message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.NumberFormat2", Const.getNumberFormats()[x]);
                        double minnum = minValue[i][x];
                        double maxnum = maxValue[i][x];
                        message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.NumberMinValue", Double.toString(minnum));
                        message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.NumberMaxValue", Double.toString(maxnum));

                        try
                        {
                            df2.applyPattern(Const.getNumberFormats()[x]);
                            df2.setDecimalFormatSymbols(dfs2);
                            double mn = df2.parse(minstr[i]).doubleValue();
                            message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.NumberExample", Const.getNumberFormats()[x], minstr[i], Double.toString(mn));
                        }
                        catch (Exception e)
                        {
                            if (log.isDetailed()) log.logDetailed("This is unexpected: parsing [" + minstr[i] + "] with format [" + Const.getNumberFormats()[x] + "] did not work.");
                        }
                    }
                }
                message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.NumberNrNullValues", ""+nrnull[i]);
                break;
            case ValueMetaInterface.TYPE_STRING:
                message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.StringMaxLength", ""+field.getLength());
                message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.StringMinValue", minstr[i]);
                message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.StringMaxValue", maxstr[i]);
                message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.StringNrNullValues", ""+nrnull[i]);
                break;
            case ValueMetaInterface.TYPE_DATE:
                message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.DateMaxLength", field.getLength() < 0 ? "-" : "" + field.getLength());
                message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.DateFormat", field.getFormat());
                if (dateFormatCount[i] > 1)
                {
                    message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.WarnDateFormat");
                }
                if (!Const.isEmpty(minstr[i])) 
                {
	                for (int x = 0; x < Const.getDateFormats().length; x++)
	                {
	                    if (dateFormat[i][x])
	                    {
	                        message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.DateFormat2", Const.getDateFormats()[x]);
	                        Date mindate = minDate[i][x];
	                        Date maxdate = maxDate[i][x];
	                        message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.DateMinValue", mindate.toString());
	                        message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.DateMaxValue", maxdate.toString());
	
	                        daf2.applyPattern(Const.getDateFormats()[x]);
	                        try
	                        {
	                            Date md = daf2.parse(minstr[i]);
	                            message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.DateExample", Const.getDateFormats()[x], minstr[i], md.toString());
	                        }
	                        catch (Exception e)
	                        {
	                        	if (log.isDetailed()) log.logDetailed("This is unexpected: parsing [" + minstr[i] + "] with format [" + Const.getDateFormats()[x] + "] did not work.");
	                        }
	                    }
	                }
                }
                message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.DateNrNullValues", ""+nrnull[i]);
                break;
            default:
                break;
            }
            if (nrnull[i] == linenr - 1)
            {
                message += BaseMessages.getString(PKG, "TextFileCSVImportProgressDialog.Info.AllNullValues");
            }
            message += Const.CR;
        }
        
        monitor.worked(1);
        monitor.done();
        
        return message;

    }
}
