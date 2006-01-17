/*
 *
 *
 */

package be.ibridge.kettle.trans.step.textfileinput;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;

/**
 * Takes care of displaying a dialog that will handle the wait while we're finding out what tables, views etc we can
 * reach in the database.
 * 
 * @author Matt
 * @since 07-apr-2005
 */
public class TextFileCSVImportProgressDialog
{
    private Props             props;

    private Shell             shell;

    private TextFileInputMeta meta;

    private int               samples;
    
    private int               clearFields;
    
    private String            message;

    private String            debug;
    
    private long              rownumber;

    private InputStreamReader reader;  
    
    /**
     * Creates a new dialog that will handle the wait while we're finding out what tables, views etc we can reach in the
     * database.
     */
    public TextFileCSVImportProgressDialog( LogWriter log, 
                                            Props props, 
                                            Shell shell, 
                                            TextFileInputMeta meta, 
                                            InputStreamReader reader, 
                                            int samples, 
                                            int clearFields
                                          )
    {
        this.props = props;
        this.shell = shell;
        this.meta = meta;
        this.reader = reader;
        this.samples       = samples;
        this.clearFields   = clearFields;

        message = null;
        debug = "init";
        rownumber = 1L;
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
                    throw new InvocationTargetException(e, "Problem encountered scanning the CSV file in row #"+rownumber+" ("+debug+") : " + e.toString());
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
            new ErrorDialog(shell, props, "Error scanning CSV file", "An error occured scanning the CSV file!", e);
        }
        catch (InterruptedException e)
        {
            new ErrorDialog(shell, props, "Error scanning CSV file", "An error occured scanning the CSV file!", e);
        }
  
        return message;
    }

    private String doScan(IProgressMonitor monitor) throws KettleException
    {
        if (samples>0) monitor.beginTask("Scanning file...", samples+1);
        else           monitor.beginTask("Scanning file...", 2);
        LogWriter log = LogWriter.getInstance();

        String line = "";
        
        NumberFormat nf = NumberFormat.getInstance();
        DecimalFormat df = (DecimalFormat)nf;
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        SimpleDateFormat daf  = new SimpleDateFormat();
        DateFormatSymbols dafs = new DateFormatSymbols();

        int nrfields = meta.getInputFields().length;

        // How many null values?
        int nrnull[] = new int[nrfields]; // How many times null value?

        // String info
        String minstr[] = new String[nrfields]; // min string
        String maxstr[] = new String[nrfields]; // max string
        boolean firststr[] = new boolean[nrfields]; // first occ. of string?

        // Date info
        boolean isDate[] = new boolean[nrfields]; // is the field perhaps a Date?
        int dateFormatCount[] = new int[nrfields]; // How many date formats work?
        boolean dateFormat[][] = new boolean[nrfields][Const.dateFormats.length]; // What are the date formats that
        // work?
        Date minDate[][] = new Date[nrfields][Const.dateFormats.length]; // min date value
        Date maxDate[][] = new Date[nrfields][Const.dateFormats.length]; // max date value

        // Number info
        boolean isNumber[] = new boolean[nrfields]; // is the field perhaps a Number?
        int numberFormatCount[] = new int[nrfields]; // How many number formats work?
        boolean numberFormat[][] = new boolean[nrfields][Const.numberFormats.length]; // What are the number format that work?
        double minValue[][] = new double[nrfields][Const.dateFormats.length]; // min number value
        double maxValue[][] = new double[nrfields][Const.dateFormats.length]; // max number value
        int numberPrecision[][] = new int[nrfields][Const.numberFormats.length]; // remember the precision?
        int numberLength[][] = new int[nrfields][Const.numberFormats.length]; // remember the length?

        for (int i = 0; i < nrfields; i++)
        {
            TextFileInputField field = meta.getInputFields()[i];

            debug = "init field #" + i;
          
            if (clearFields == SWT.YES) // Clear previous info...
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
                field.setTrimType(TextFileInputMeta.TYPE_TRIM_NONE);
            }

            nrnull[i] = 0;
            minstr[i] = "";
            maxstr[i] = "";
            firststr[i] = true;

            // Init data guess
            isDate[i] = true;
            for (int j = 0; j < Const.dateFormats.length; j++)
            {
                dateFormat[i][j] = true;
                minDate[i][j] = Const.MAX_DATE;
                maxDate[i][j] = Const.MIN_DATE;
            }
            dateFormatCount[i] = Const.dateFormats.length;

            // Init number guess
            isNumber[i] = true;
            for (int j = 0; j < Const.numberFormats.length; j++)
            {
                numberFormat[i][j] = true;
                minValue[i][j] = Double.MAX_VALUE;
                maxValue[i][j] = -Double.MAX_VALUE;
                numberPrecision[i][j] = -1;
                numberLength[i][j] = -1;
            }
            numberFormatCount[i] = Const.numberFormats.length;
        }

        TextFileInputMeta strinfo = (TextFileInputMeta) meta.clone();
        for (int i = 0; i < nrfields; i++)
            strinfo.getInputFields()[i].setType(Value.VALUE_TYPE_STRING);

        // Sample <samples> rows...
        debug = "get first line";

        // If the file has a header we overwrite the first line
        // However, if it doesn't have a header, take a new line
        if (meta.hasHeader()) 
        {
            line = TextFileInput.getLine(log, reader, meta.getFileFormat());
            int skipped=1;
            while (line!=null && skipped<meta.getNrHeaderLines())
            {
                line = TextFileInput.getLine(log, reader, meta.getFileFormat());
                skipped++;
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
            monitor.subTask("Scanning line "+linenr);
            if (samples>0) monitor.worked(1);
            
            debug = "convert line #" + linenr + " to row";
            Row r = TextFileInput.convertLineToRow(log, line, strinfo, df, dfs, daf, dafs, meta.getFiles()[0], rownumber);

            rownumber++;
            for (int i = 0; i < nrfields && i < r.size(); i++)
            {
                TextFileInputField field = meta.getInputFields()[i];

                debug = "Start of for loop, get new value " + i;
                Value v = r.getValue(i);
                debug = "Start of for loop over " + r.size() + " elements in Row r, now at #" + i + " containing value : [" + v.toString() + "]";
                if (!v.isNull() && v.getString() != null)
                {
                    String fieldValue = v.getString();

                    int trimthis = TextFileInputMeta.TYPE_TRIM_NONE;

                    boolean spacesBefore = Const.nrSpacesBefore(fieldValue) > 0;
                    boolean spacesAfter = Const.nrSpacesAfter(fieldValue) > 0;

                    fieldValue = Const.trim(fieldValue);

                    if (spacesBefore) trimthis |= TextFileInputMeta.TYPE_TRIM_LEFT;
                    if (spacesAfter) trimthis |= TextFileInputMeta.TYPE_TRIM_RIGHT;

                    debug = "change trim type[" + i + "]";
                    field.setTrimType(field.getTrimType() | trimthis);

                    debug = "Field #" + i + " has type : " + Value.getTypeDesc(field.getType());

                    // See if the field has only numeric fields
                    if (isNumber[i])
                    {
                        debug = "Number checking of [" + fieldValue.toString() + "] on line #" + linenr;

                        boolean containsDot = false;
                        boolean containsComma = false;

                        for (int x = 0; x < fieldValue.length() && field.getType() == Value.VALUE_TYPE_NUMBER; x++)
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
                                        int indexDot = fieldValue.indexOf(".");
                                        int indexComma = fieldValue.indexOf(",");
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
                            for (int x = 0; x < Const.numberFormats.length; x++)
                            {
                                if (numberFormat[i][x])
                                {
                                    try
                                    {
                                        df2.setDecimalFormatSymbols(dfs2);
                                        df2.applyPattern(Const.numberFormats[x]);
                                        double d = df2.parse(fieldValue.toString()).doubleValue();

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

                    debug = "Check max length on field #" + i + " called " + field.getName() + " : [" + fieldValue + "]";
                    // Capture the maximum length of the field (trimmed)
                    if (fieldValue.length() > field.getLength()) field.setLength(fieldValue.length());

                    // So is it really a string or a date field?
                    // Check it as long as we found a format that works...
                    if (isDate[i])
                    {
                        for (int x = 0; x < Const.dateFormats.length; x++)
                        {
                            if (dateFormat[i][x])
                            {
                                try
                                {
                                    daf2.applyPattern(Const.dateFormats[x]);
                                    Date date = daf2.parse(fieldValue.toString());

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
                        minstr[i] = fieldValue.toString();
                        maxstr[i] = fieldValue.toString();
                    }
                    if (minstr[i].compareTo(fieldValue.toString()) > 0) minstr[i] = fieldValue.toString();
                    if (maxstr[i].compareTo(fieldValue.toString()) < 0) maxstr[i] = fieldValue.toString();

                    debug = "End of for loop";
                } else
                {
                    nrnull[i]++;
                }
            }

            if (!r.isIgnored())
                linenr++;
            else
                rownumber--;

            // Grab another line...
            debug = "Grab another line";
            line = TextFileInput.getLine(log, reader, meta.getFileFormat());
            debug = "End of while loop";
        }

        monitor.worked(1);
        monitor.setTaskName("Analysing results...");
        
        // Include the results from the number, date & string search!
        // some cleanup of format fields for strings...
        for (int i = 0; i < nrfields; i++)
        {
            TextFileInputField field = meta.getInputFields()[i];

            if (field.getType() == Value.VALUE_TYPE_STRING)
            {
                if (isDate[i])
                {
                    field.setType(Value.VALUE_TYPE_DATE);
                    for (int x = Const.dateFormats.length - 1; x >= 0; x--)
                    {
                        if (dateFormat[i][x])
                        {
                            field.setFormat(Const.dateFormats[x]);
                            field.setLength(TextFileInputDialog.dateLengths[x]);
                            field.setPrecision(-1);
                        }
                    }
                } else
                    if (isNumber[i])
                    {
                        field.setType(Value.VALUE_TYPE_NUMBER);
                        for (int x = Const.numberFormats.length - 1; x >= 0; x--)
                        {
                            if (numberFormat[i][x])
                            {
                                field.setFormat(Const.numberFormats[x]);
                                field.setLength(numberLength[i][x]);
                                field.setPrecision(numberPrecision[i][x]);

                                if (field.getPrecision() == 0 && field.getLength() < 18)
                                {
                                    field.setType(Value.VALUE_TYPE_INTEGER);
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
        }
        
        
        // Show information on items using dialog box
        String message = "";
        message += "Result after scanning " + (linenr - 1) + " lines." + Const.CR;
        message += "----------------------------------------------------" + Const.CR;
        for (int i = 0; i < nrfields; i++)
        {
            TextFileInputField field = meta.getInputFields()[i];

            message += "Field nr. " + (i + 1) + " :" + Const.CR;

            message += "  Field name           : " + field.getName() + Const.CR;
            message += "  Field type           : " + field.getTypeDesc() + Const.CR;

            switch (field.getType())
            {
            case Value.VALUE_TYPE_NUMBER:
                message += "  Estimated length     : " + (field.getLength() < 0 ? "-" : "" + field.getLength()) + Const.CR;
                message += "  Estimated precision  : " + (field.getPrecision() < 0 ? "-" : "" + field.getPrecision()) + Const.CR;
                message += "  Number format        : " + field.getFormat() + Const.CR;
                if (numberFormatCount[i] > 1)
                {
                    message += "    WARNING: More then 1 number format seems to match all sampled records:" + Const.CR;
                }
                for (int x = 0; x < Const.numberFormats.length; x++)
                {
                    if (numberFormat[i][x])
                    {
                        message += "    Number format        : " + Const.numberFormats[x] + Const.CR;
                        Value minnum = new Value("minnum", minValue[i][x]);
                        Value maxnum = new Value("maxnum", maxValue[i][x]);
                        minnum.setLength(numberLength[i][x], numberPrecision[i][x]);
                        maxnum.setLength(numberLength[i][x], numberPrecision[i][x]);
                        message += "      Minimum value      : " + minnum.toString() + Const.CR;
                        message += "      Maximum value      : " + maxnum.toString() + Const.CR;

                        try
                        {
                            df2.applyPattern(Const.numberFormats[x]);
                            df2.setDecimalFormatSymbols(dfs2);
                            double mn = df2.parse(minstr[i]).doubleValue();
                            Value val = new Value("min", mn);
                            val.setLength(numberLength[i][x], numberPrecision[i][x]);
                            message += "      Example            : " + Const.numberFormats[x] + ", number [" + minstr[i] + "] gives "
                                    + val.toString() + Const.CR;
                        }
                        catch (Exception e)
                        {
                            log.logBasic(toString(), "This is unexpected: parsing [" + minstr[i] + "] with format [" + Const.numberFormats[x]
                                    + "] did not work.");
                        }
                    }
                }
                message += "  Nr of null values    : " + nrnull[i] + Const.CR;
                break;
            case Value.VALUE_TYPE_STRING:
                message += "  Maximum length       : " + (field.getLength() < 0 ? "-" : "" + field.getLength()) + Const.CR;
                message += "  Minimum value        : " + minstr[i] + Const.CR;
                message += "  Maximum value        : " + maxstr[i] + Const.CR;
                message += "  Nr of null values    : " + nrnull[i] + Const.CR;
                break;
            case Value.VALUE_TYPE_DATE:
                message += "  Maximum length       : " + (field.getLength() < 0 ? "-" : "" + field.getLength()) + Const.CR;
                message += "  Date format          : " + field.getFormat() + Const.CR;
                if (dateFormatCount[i] > 1)
                {
                    message += "    WARNING: More then 1 date format seems to match all sampled records:" + Const.CR;
                }
                for (int x = 0; x < Const.dateFormats.length; x++)
                {
                    if (dateFormat[i][x])
                    {
                        message += "    Date format          : " + Const.dateFormats[x] + Const.CR;
                        Value mindate = new Value("mindate", minDate[i][x]);
                        Value maxdate = new Value("maxdate", maxDate[i][x]);
                        message += "      Minimum value      : " + mindate.toString() + Const.CR;
                        message += "      Maximum value      : " + maxdate.toString() + Const.CR;

                        daf2.applyPattern(Const.dateFormats[x]);
                        try
                        {
                            Date md = daf2.parse(minstr[i]);
                            Value val = new Value("min", md);
                            val.setLength(field.getLength());
                            message += "      Example            : " + Const.dateFormats[x] + ", date [" + minstr[i] + "] gives " + val.toString()
                                    + Const.CR;
                        }
                        catch (Exception e)
                        {
                            log.logError(toString(), "This is unexpected: parsing [" + minstr[i] + "] with format [" + Const.dateFormats[x]
                                    + "] did not work.");
                        }
                    }
                }
                message += "  Nr of null values    : " + nrnull[i] + Const.CR;
                break;
            default:
                break;
            }
            if (nrnull[i] == linenr - 1)
            {
                message += "  ALL NULL VALUES!" + Const.CR;
            }
            message += Const.CR;
        }
        
        monitor.worked(1);
        monitor.done();
        
        return message;

    }
}
