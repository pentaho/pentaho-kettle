/*
 *
 *
 */

package be.ibridge.kettle.trans.step.textfileinput;

import java.io.InputStream;
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

    private InputStream       inputStream;

    private int               samples;
    
    private int               clearFields;
    
    private String            message;

    private String            debug;
    
    private long              rownumber;  
    
    /**
     * Creates a new dialog that will handle the wait while we're finding out what tables, views etc we can reach in the
     * database.
     */
    public TextFileCSVImportProgressDialog( LogWriter log, 
                                            Props props, 
                                            Shell shell, 
                                            TextFileInputMeta meta, 
                                            InputStream inputStream, 
                                            int samples, 
                                            int clearFields
                                          )
    {
        this.props = props;
        this.shell = shell;
        this.meta = meta;
        this.inputStream = inputStream;
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
        boolean isdate[] = new boolean[nrfields]; // is the field perhaps a Date?
        int datefmt_cnt[] = new int[nrfields]; // How many date formats work?
        boolean datefmt[][] = new boolean[nrfields][Const.date_formats.length]; // What are the date formats that
        // work?
        Date mindat[][] = new Date[nrfields][Const.date_formats.length]; // min date value
        Date maxdat[][] = new Date[nrfields][Const.date_formats.length]; // max date value

        // Number info
        boolean isnumber[] = new boolean[nrfields]; // is the field perhaps a Number?
        int numfmt_cnt[] = new int[nrfields]; // How many number formats work?
        boolean numfmt[][] = new boolean[nrfields][Const.number_formats.length]; // What are the number format that work?
        double minval[][] = new double[nrfields][Const.date_formats.length]; // min number value
        double maxval[][] = new double[nrfields][Const.date_formats.length]; // max number value
        int numprec[][] = new int[nrfields][Const.number_formats.length]; // remember the precision?
        int numleng[][] = new int[nrfields][Const.number_formats.length]; // remember the length?

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
            isdate[i] = true;
            for (int j = 0; j < Const.date_formats.length; j++)
            {
                datefmt[i][j] = true;
                mindat[i][j] = Const.MAX_DATE;
                maxdat[i][j] = Const.MIN_DATE;
            }
            datefmt_cnt[i] = Const.date_formats.length;

            // Init number guess
            isnumber[i] = true;
            for (int j = 0; j < Const.number_formats.length; j++)
            {
                numfmt[i][j] = true;
                minval[i][j] = Double.MAX_VALUE;
                maxval[i][j] = -Double.MAX_VALUE;
                numprec[i][j] = -1;
                numleng[i][j] = -1;
            }
            numfmt_cnt[i] = Const.number_formats.length;
        }

        TextFileInputMeta strinfo = (TextFileInputMeta) meta.clone();
        for (int i = 0; i < nrfields; i++)
            strinfo.getInputFields()[i].setType(Value.VALUE_TYPE_STRING);

        // Sample <samples> rows...
        debug = "get first line";

        // if ( nr_non_empty==0 && !info.header)

        // If the file has a header we overwrite the first line
        // However, if it doesn't have a header, take a new line
        if (meta.hasHeader()) line = TextFileInput.getLine(log, inputStream, meta.getFileFormat());
        int linenr = 1;

        // Allocate number and date parsers
        DecimalFormat df2 = (DecimalFormat) NumberFormat.getInstance();
        DecimalFormatSymbols dfs2 = new DecimalFormatSymbols();
        SimpleDateFormat daf2 = new SimpleDateFormat();

        boolean error_found = false;
        while (!error_found && line != null && (linenr <= samples || samples == 0) && !monitor.isCanceled())
        {
            monitor.subTask("Scanning line "+linenr);
            if (samples>0) monitor.worked(1);
            
            debug = "convert line #" + linenr + " to row";
            Row r = TextFileInput.convertLineToRow(log, line, strinfo, true, df, dfs, daf, dafs, meta.getFiles()[0], rownumber);

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

                    boolean spaces_before = Const.nrSpacesBefore(fieldValue) > 0;
                    boolean spaces_after = Const.nrSpacesAfter(fieldValue) > 0;

                    fieldValue = Const.trim(fieldValue);

                    if (spaces_before) trimthis |= TextFileInputMeta.TYPE_TRIM_LEFT;
                    if (spaces_after) trimthis |= TextFileInputMeta.TYPE_TRIM_RIGHT;

                    debug = "change trim type[" + i + "]";
                    field.setTrimType(field.getTrimType() | trimthis);

                    debug = "Field #" + i + " has type : " + Value.getTypeDesc(field.getType());

                    // See if the field has only numeric fields
                    if (isnumber[i])
                    {
                        debug = "Number checking of [" + fieldValue.toString() + "] on line #" + linenr;

                        boolean contains_dot = false;
                        boolean contains_comma = false;

                        for (int x = 0; x < fieldValue.length() && field.getType() == Value.VALUE_TYPE_NUMBER; x++)
                        {
                            char ch = fieldValue.charAt(x);
                            if (!Character.isDigit(ch) && ch != '.' && ch != ',' && (ch != '-' || x > 0) && ch != 'E' && ch != 'e' // exponential
                            )
                            {
                                isnumber[i] = false;
                            } else
                            {
                                if (ch == '.') contains_dot = true;
                                if (ch == ',') contains_comma = true;
                            }
                        }
                        // If it's still a number, try to parse it as a double
                        if (isnumber[i])
                        {
                            if (contains_dot && !contains_comma) // american 174.5
                            {
                                dfs2.setDecimalSeparator('.');
                                field.setDecimalSymbol(".");
                                dfs2.setGroupingSeparator(',');
                                field.setGroupSymbol(",");
                            } else
                                if (!contains_dot && contains_comma) // Belgian 174,5
                                {
                                    dfs2.setDecimalSeparator(',');
                                    field.setDecimalSymbol(",");
                                    dfs2.setGroupingSeparator('.');
                                    field.setGroupSymbol(".");
                                } else
                                    if (contains_dot && contains_comma) // Both appear!
                                    {
                                        // What's the last occurance: decimal point!
                                        int idx_dot = fieldValue.indexOf(".");
                                        int idx_com = fieldValue.indexOf(",");
                                        if (idx_dot > idx_com)
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
                            for (int x = 0; x < Const.number_formats.length; x++)
                            {
                                if (numfmt[i][x])
                                {
                                    try
                                    {
                                        df2.setDecimalFormatSymbols(dfs2);
                                        df2.applyPattern(Const.number_formats[x]);
                                        // if (x==0) System.out.println("Def. Number format :
                                        // ["+number_formats[x]+"]");
                                        double d = df2.parse(fieldValue.toString()).doubleValue();

                                        // System.out.println("("+i+","+x+") : Converted ["+field.toString()+"]
                                        // to ["+d+"] with format ["+number_formats[x]+"] and dfs2
                                        // ["+dfs2.getDecimalSeparator()+dfs2.getGroupingSeparator()+"]");

                                        // After everything, still a number?
                                        // Then guess the precision
                                        int prec = TextFileInputDialog.guessPrecision(d);
                                        if (prec > numprec[i][x]) numprec[i][x] = prec;

                                        int leng = TextFileInputDialog.guessLength(d) + prec; // add precision!
                                        if (leng > numleng[i][x]) numleng[i][x] = leng;

                                        if (d < minval[i][x]) minval[i][x] = d;
                                        if (d > maxval[i][x]) maxval[i][x] = d;
                                    }
                                    catch (Exception e)
                                    {
                                        numfmt[i][x] = false; // Don't try it again in the future.
                                        numfmt_cnt[i]--; // One less that works..
                                    }
                                }
                            }

                            // Still not found: just a string
                            if (numfmt_cnt[i] == 0)
                            {
                                isnumber[i] = false;
                            }
                        }
                    }

                    debug = "Check max length on field #" + i + " called " + field.getName() + " : [" + fieldValue + "]";
                    // Capture the maximum length of the field (trimmed)
                    if (fieldValue.length() > field.getLength()) field.setLength(fieldValue.length());

                    // So is it really a string or a date field?
                    // Check it as long as we found a format that works...
                    if (isdate[i])
                    {
                        for (int x = 0; x < Const.date_formats.length; x++)
                        {
                            if (datefmt[i][x])
                            {
                                try
                                {
                                    daf2.applyPattern(Const.date_formats[x]);
                                    Date date = daf2.parse(fieldValue.toString());

                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(date);
                                    int year = cal.get(Calendar.YEAR);

                                    if (year < 1800 || year > 2200)
                                    {
                                        datefmt[i][x] = false; // Don't try it again in the future.
                                        datefmt_cnt[i]--; // One less that works..
                                        // System.out.println("Field #"+i+", pattern ["+date_formats[x]+"],
                                        // year="+year+", field=["+field+"] : year<1800 or year>2200!! not a
                                        // date!");
                                    }

                                    if (mindat[i][x].compareTo(date) > 0) mindat[i][x] = date;
                                    if (maxdat[i][x].compareTo(date) < 0) maxdat[i][x] = date;
                                }
                                catch (Exception e)
                                {
                                    datefmt[i][x] = false; // Don't try it again in the future.
                                    datefmt_cnt[i]--; // One less that works..
                                    // System.out.println("field ["+field+"] is not a date,
                                    // format=["+date_formats[x]+", x="+x+", error: ("+e.toString()+")");
                                }
                            }
                        }

                        // Still not found: just a string
                        if (datefmt_cnt[i] == 0)
                        {
                            isdate[i] = false;
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
            line = TextFileInput.getLine(log, inputStream, meta.getFileFormat());
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
                if (isdate[i])
                {
                    field.setType(Value.VALUE_TYPE_DATE);
                    for (int x = Const.date_formats.length - 1; x >= 0; x--)
                    {
                        if (datefmt[i][x])
                        {
                            field.setFormat(Const.date_formats[x]);
                            field.setLength(TextFileInputDialog.date_lengths[x]);
                            field.setPrecision(-1);
                        }
                    }
                } else
                    if (isnumber[i])
                    {
                        field.setType(Value.VALUE_TYPE_NUMBER);
                        for (int x = Const.number_formats.length - 1; x >= 0; x--)
                        {
                            if (numfmt[i][x])
                            {
                                field.setFormat(Const.number_formats[x]);
                                field.setLength(numleng[i][x]);
                                field.setPrecision(numprec[i][x]);

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
                if (numfmt_cnt[i] > 1)
                {
                    message += "    WARNING: More then 1 number format seems to match all sampled records:" + Const.CR;
                }
                for (int x = 0; x < Const.number_formats.length; x++)
                {
                    if (numfmt[i][x])
                    {
                        message += "    Number format        : " + Const.number_formats[x] + Const.CR;
                        Value minnum = new Value("minnum", minval[i][x]);
                        Value maxnum = new Value("maxnum", maxval[i][x]);
                        minnum.setLength(numleng[i][x], numprec[i][x]);
                        maxnum.setLength(numleng[i][x], numprec[i][x]);
                        message += "      Minimum value      : " + minnum.toString() + Const.CR;
                        message += "      Maximum value      : " + maxnum.toString() + Const.CR;

                        try
                        {
                            df2.applyPattern(Const.number_formats[x]);
                            df2.setDecimalFormatSymbols(dfs2);
                            double mn = df2.parse(minstr[i]).doubleValue();
                            Value val = new Value("min", mn);
                            val.setLength(numleng[i][x], numprec[i][x]);
                            message += "      Example            : " + Const.number_formats[x] + ", number [" + minstr[i] + "] gives "
                                    + val.toString() + Const.CR;
                        }
                        catch (Exception e)
                        {
                            log.logBasic(toString(), "This is unexpected: parsing [" + minstr[i] + "] with format [" + Const.number_formats[x]
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
                if (datefmt_cnt[i] > 1)
                {
                    message += "    WARNING: More then 1 date format seems to match all sampled records:" + Const.CR;
                }
                for (int x = 0; x < Const.date_formats.length; x++)
                {
                    if (datefmt[i][x])
                    {
                        message += "    Date format          : " + Const.date_formats[x] + Const.CR;
                        Value mindate = new Value("mindate", mindat[i][x]);
                        Value maxdate = new Value("maxdate", maxdat[i][x]);
                        message += "      Minimum value      : " + mindate.toString() + Const.CR;
                        message += "      Maximum value      : " + maxdate.toString() + Const.CR;

                        daf2.applyPattern(Const.date_formats[x]);
                        try
                        {
                            Date md = daf2.parse(minstr[i]);
                            Value val = new Value("min", md);
                            val.setLength(field.getLength());
                            message += "      Example            : " + Const.date_formats[x] + ", date [" + minstr[i] + "] gives " + val.toString()
                                    + Const.CR;
                        }
                        catch (Exception e)
                        {
                            log.logError(toString(), "This is unexpected: parsing [" + minstr[i] + "] with format [" + Const.date_formats[x]
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
