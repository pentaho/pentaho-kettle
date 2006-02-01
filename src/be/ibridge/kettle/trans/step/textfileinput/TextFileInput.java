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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.zip.ZipInputStream;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleValueException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Read all sorts of text files, convert them to rows and writes these to one or more output streams.
 * 
 * @author Matt
 * @since 4-apr-2003
 */
public class TextFileInput extends BaseStep implements StepInterface
{
	private TextFileInputMeta meta;
	private TextFileInputData data;
	
	public TextFileInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public static final String getLine(LogWriter log, InputStreamReader reader, String format)
	{
		StringBuffer line=new StringBuffer();
		int c=0;
		
		try
		{
			while (c>=0)
			{
				c=reader.read();
				if (c=='\n' || c=='\r') 
				{
					if (format.equalsIgnoreCase("DOS")) c=reader.read(); // skip \n and \r
                    return line.toString();
				}
				if (c>=0) line.append((char)c);
			}
		}
		catch(Exception e)
		{
			if (line.length()==0) 
			{
				log.logError("get line", "Exception reading line: "+e.toString());
				return null;
			}
			return line.toString();
		}
        if (line.length()>0) return line.toString();
        
		return null;
	}
    
	public static final ArrayList convertLineToStrings(LogWriter log, String line, TextFileInputMeta inf)
        throws KettleException
	{
		ArrayList strings = new ArrayList();
		int fieldnr;
		String pol;  // piece of line
		String debug = "convertLineToStrings start";
		
		try
		{
			if (line==null) return null;

			if (inf.getFileType().equalsIgnoreCase("CSV"))
			{
				// Split string in pieces, only for CSV!
				
				fieldnr=0;
				int pos=0;
				int length=line.length();
				boolean dencl = false;
				
				while(pos<length)
				{
					debug = "convertLineToStrings while start";
                    
					int from=pos;
					int next;
					int len_encl = (inf.getEnclosure()==null?0:inf.getEnclosure().length());
                    int len_esc  = (inf.getEscapeCharacter()==null?0:inf.getEscapeCharacter().length());
                    
					boolean encl_found;
                    boolean contains_escaped_enclosures = false;
                    boolean contains_escaped_separators = false;
					
					// Is the field beginning with an enclosure?  "aa;aa";123;"aaa-aaa";000;...
					if ( len_encl>0 && line.substring(from, from+len_encl).equalsIgnoreCase(inf.getEnclosure()))
					{
						debug = "convertLineToStrings if start";
                        
						log.logRowlevel("convert line to row", "encl substring=["+line.substring(from, from+len_encl)+"]");
						encl_found=true;
						int p=from+len_encl;
						
						boolean is_enclosure = len_encl>0 && p+len_encl<length && line.substring(p, p+len_encl).equalsIgnoreCase(inf.getEnclosure());
                        boolean is_escape    = len_esc >0 && p+len_esc <length && line.substring(p, p+len_esc).equalsIgnoreCase(inf.getEscapeCharacter());

                        boolean enclosure_after = false;
						if ( (is_enclosure || is_escape) && p<length-1) // Is it really an enclosure, see if it's not repeated twice or escaped
						{
							String strnext = line.substring(p+len_encl, p+2*len_encl); 
							if (strnext.equalsIgnoreCase(inf.getEnclosure()))
							{
								p++;
								enclosure_after=true;
								dencl=true; 
                                
                                if (is_escape) contains_escaped_enclosures=true; // remember to replace them later on!
							}
						}
                        
                        // Look for a closing enclosure!
                        while ( (!is_enclosure || enclosure_after) && p<line.length() )
						{
							debug = "convertLineToStrings start while enclosure (p="+p+")";
                            
							p++;
							enclosure_after=false;
							is_enclosure = len_encl>0 && p+len_encl<length && line.substring(p, p+len_encl).equals(inf.getEnclosure());
                            is_escape    = len_esc >0 && p+len_esc <length && line.substring(p, p+len_esc).equals(inf.getEscapeCharacter());
                            
							if ((is_enclosure || is_escape ) && p<length-1) // Is it really an enclosure, see if it's not repeated twice
							{
								String strnext = line.substring(p+len_encl, p+2*len_encl); 
								if (strnext.equals(inf.getEnclosure()))
								{
									p++;
									enclosure_after=true;
									dencl=true;
                                    
                                    if (is_escape) contains_escaped_enclosures=true; // remember to replace them later on!
								}
							}
						}
                        
						if (p>=length) next = p; else next = p+len_encl;
	
						log.logRowlevel("convert line to row", "End of enclosure @ position "+p);
						debug = "convertLineToStrings end of enclosure";
					}
					else
					{
						encl_found=false;
                        boolean found = false;
                        int startpoint = from;
                        int tries=1;
                        do
                        {
    						next=line.indexOf(inf.getSeparator(), startpoint);
                            
                            // System.out.println("tries="+tries+", startpoint="+startpoint+", next="+next+", ["+line.substring(next-len_esc, next+5));
                            
                            // See if this position is preceded by an escape character.
                            if (len_esc>0 && next-len_esc>0)
                            {
                                String before = line.substring(next-len_esc, next);
                                // System.out.println("before string: ["+before+"]");
                                if (inf.getEscapeCharacter().equals(before)) 
                                {
                                    // System.out.println("ESCAPED SEPARATOR FOUND");
                                    startpoint=next+1; // take the next separator, this one is escaped...
                                    tries++;
                                    contains_escaped_separators=true;
                                }
                                else
                                {
                                    found=true;
                                }
                            }
                            else
                            {
                                found=true;
                            }
                        }
                        while(!found && next>=0);
					}
					if (next==-1) next=length;
					
					if (encl_found)
					{
						pol=line.substring(from+len_encl, next-len_encl);
						log.logRowlevel("convert line to row", "Enclosed field found: ["+pol+"]");
					}
					else
					{
						debug = "convertLineToStrings get substring";
						pol=line.substring(from, next);
						log.logRowlevel("convert line to row", "Normal field found: ["+pol+"]");
					}
	
					if (dencl)
					{
						debug = "convertLineToStrings dencl";
						StringBuffer sbpol = new StringBuffer(pol);
						int idx = sbpol.indexOf(inf.getEnclosure()+inf.getEnclosure());
						while (idx>=0)
						{
							sbpol.delete(idx, idx+inf.getEnclosure().length());
							idx = sbpol.indexOf(inf.getEnclosure()+inf.getEnclosure());
						}
						pol=sbpol.toString();
					}
                    
                    if (contains_escaped_enclosures) // replace the escaped enclosures with enclosures...
                    {
                        String replace = inf.getEscapeCharacter()+inf.getEnclosure();
                        String replaceWith = inf.getEnclosure();
                        
                        // System.out.println("Replace ["+replace+"] with ["+replaceWith+"] in ["+pol+"]");
                        
                        pol = Const.replace(pol, replace, replaceWith);
                    }

                    if (contains_escaped_separators) // replace the escaped separators with separators...
                    {
                        String replace = inf.getEscapeCharacter()+inf.getSeparator();
                        String replaceWith = inf.getSeparator();
                        
                        // System.out.println("Replace ["+replace+"] with ["+replaceWith+"] in ["+pol+"]");
                        
                        pol = Const.replace(pol, replace, replaceWith);
                    }

					// Now add pol to the strings found!
					debug = "convertLineToStrings add pol";
					strings.add(pol); 
                    
                    // System.out.println("Found new field: ["+pol+"]");
		
					pos=next+1;
					fieldnr++;
				}
			}
			else
			{
			    debug="convertLineToStrings : Fixed";
                // Fixed file format:
				//   Simply get the strings at the required positions...
				
				for (int i=0;i<inf.getInputFields().length;i++)			
				{
				    TextFileInputField field = inf.getInputFields()[i];
                    debug="convertLineToStrings : Fixed, field ["+field.getName()+"]";
				    
					int length = line.length();
					
					if (field.getPosition() + field.getLength() <= length)
					{
						strings.add(line.substring(field.getPosition(), field.getPosition()+field.getLength()));
					}
					else
					{
                        if (field.getPosition() < length)
                        {
                            strings.add(line.substring(field.getPosition()));
                        }
                        else
                        {
                            strings.add("");
                        }
					}
				}
			}
		}
		catch(Exception e)
		{
            throw new KettleException("Error converting line : "+e.toString()+" in part: "+debug, e);
		}

		debug="end of convertLineToStrings";
		
		return strings;
	}
	
	public static final Row convertLineToRow(
												LogWriter log, 
												String line, 
												TextFileInputMeta info,
												DecimalFormat ldf, DecimalFormatSymbols ldfs,
												SimpleDateFormat ldaf, DateFormatSymbols ldafs,
												String fname,
												long linenr
											) 
        throws KettleException
	{
        if (line==null || line.length()==0) return null;
        
        int fieldnr;
		Row r=new Row();
		Value value;
        
        Value errorCount=null;
        if (info.isErrorIgnored() && info.getErrorCountField()!=null && info.getErrorCountField().length()>0)
        {
            errorCount = new Value(info.getErrorCountField(), 0L);
        }
        Value errorFields=null;
        if (info.isErrorIgnored() && info.getErrorFieldsField()!=null && info.getErrorFieldsField().length()>0)
        {
            errorFields = new Value(info.getErrorFieldsField(), "");
        }
        Value errorText=null;
        if (info.isErrorIgnored() && info.getErrorTextField()!=null && info.getErrorTextField().length()>0)
        {
            errorText = new Value(info.getErrorTextField(), "");
        }
        
		int  nrfields = info.getInputFields().length;
		
		try
		{
            // System.out.println("Convertings line to string ["+line+"]");
			ArrayList strings = convertLineToStrings(log, line, info);
			
			for (fieldnr=0 ; fieldnr<nrfields ; fieldnr++)
			{
                TextFileInputField f = info.getInputFields()[fieldnr];
			    
				String field     = fieldnr<nrfields?f.getName():"empty"+fieldnr;
				int    type      = fieldnr<nrfields?f.getType():Value.VALUE_TYPE_STRING;
				String format    = fieldnr<nrfields?f.getFormat():"";
				int    length    = fieldnr<nrfields?f.getLength():-1;
				int    precision = fieldnr<nrfields?f.getPrecision():-1;
				String group     = fieldnr<nrfields?f.getGroupSymbol():"";
				String decimal   = fieldnr<nrfields?f.getDecimalSymbol():"";
				String currency  = fieldnr<nrfields?f.getCurrencySymbol():"";
				String nullif    = fieldnr<nrfields?f.getNullString():"";
				int    trim_type = fieldnr<nrfields?f.getTrimType():TextFileInputMeta.TYPE_TRIM_NONE;

                if ( fieldnr<strings.size() )
                {
                    String pol = (String)strings.get(fieldnr);
					try
					{
                        value = convertValue
						(
							pol, 
							field, type, format, length, precision,
							group, decimal, currency, nullif, trim_type,
							ldf, ldfs,
							ldaf, ldafs
						)
						;
					}
					catch(Exception e)
					{
                        if (info.isErrorIgnored())
                        {
                            // OK, give some feedback!
                            String message = "Couldn't parse field ["+field+"] with value ["+pol+"] : "+e.getMessage();
                            log.logBasic(fname, "WARNING: "+message);
                            
                            value = new Value(field, type);
                            value.setNull();
                            
                            if (errorCount!=null)
                            {
                                errorCount.plus(1L);
                            }
                            if (errorFields!=null)
                            {
                                StringBuffer sb = new StringBuffer(errorFields.getString());
                                if (sb.length()>0) sb.append(", ");
                                sb.append(field);
                                errorFields.setValue(sb);
                            }
                            if (errorText!=null)
                            {
                                StringBuffer sb = new StringBuffer(errorText.getString());
                                if (sb.length()>0) sb.append(Const.CR);
                                sb.append(message);
                                errorText.setValue(sb);
                            }
                        }
                        else
                        {
                            throw new KettleException("Couldn't parse field ["+f.getName()+"] with value ["+pol+"]", e);
                        }
					}
                }
                else
                {
                    // No data found:  TRAILING NULLCOLS: add null value...
                    value = new Value(field, type);
                    value.setNull();
                }
                
				// Now add value to the row!
				r.addValue(value); 
			}
            
            // Add the error handling fields...
            if (errorCount !=null) r.addValue(errorCount);
            if (errorFields!=null) r.addValue(errorFields);
            if (errorText  !=null) r.addValue(errorText);
			
			// Support for trailing nullcols!
			if (fieldnr<info.getInputFields().length)
			{
				for (int i=fieldnr;i<info.getInputFields().length;i++)
				{
				    TextFileInputField f = info.getInputFields()[i];
				    
					value=new Value(f.getName(), f.getType()); // build a value!
					value.setLength(f.getLength(), f.getPrecision());
					value.setNull();
					r.addValue(value);
				}
			}
		}
		catch(Exception e)
		{
            throw new KettleException("Error converting line", e);
		}
			
		// Possibly add a filename...
		if (info.includeFilename() && r!=null)
		{
			Value inc = new Value(info.getFilenameField(), fname);
			inc.setLength(100);
			r.addValue(inc);
		}

		// Possibly add a row number...
		if (info.includeRowNumber() && r!=null)
		{
			Value inc = new Value(info.getRowNumberField(), Value.VALUE_TYPE_INTEGER);
			inc.setValue(linenr);
			inc.setLength(9);
			r.addValue(inc);
		}

		return r;
	}
	
	public static final Value convertValue(
					String pol,
					String field_name,
					int    field_type,
					String field_format,
					int    field_length,
					int    field_precision,
					String num_group,
					String num_decimal,
					String num_currency,
					String nullif,
					int    trim_type,
					DecimalFormat ldf, DecimalFormatSymbols ldfs,
					SimpleDateFormat ldaf, DateFormatSymbols ldafs
				) throws Exception
	{
		Value value=new Value(field_name, field_type); // build a value!
			
		// If no nullif field is supplied, take the default.
		String null_value = nullif;
		if (null_value == null)
		{
			switch(field_type)
			{
			case Value.VALUE_TYPE_BOOLEAN   : null_value=Const.NULL_BOOLEAN;    break;
			case Value.VALUE_TYPE_STRING    : null_value=Const.NULL_STRING;     break;
            case Value.VALUE_TYPE_BIGNUMBER : null_value=Const.NULL_BIGNUMBER;  break;
			case Value.VALUE_TYPE_NUMBER    : null_value=Const.NULL_NUMBER;     break;
			case Value.VALUE_TYPE_INTEGER   : null_value=Const.NULL_INTEGER;    break;
			case Value.VALUE_TYPE_DATE      : null_value=Const.NULL_DATE  ;     break;
			default : null_value=Const.NULL_NONE;  break;
			}
		}
		String null_cmp = Const.rightPad(new StringBuffer(null_value), pol.length());
			
		if (pol==null || pol.length()==0 || pol.equalsIgnoreCase(null_cmp))
		{
			value.setNull();
		}
        else
		if (value.isNumeric())
		{
			try
			{
				StringBuffer strpol=new StringBuffer(pol);
				
				switch(trim_type)
				{
				case TextFileInputMeta.TYPE_TRIM_LEFT : 
					while(strpol.length()>0 && strpol.charAt(0)==' ') 
						strpol.deleteCharAt(0); 
					break;
				case TextFileInputMeta.TYPE_TRIM_RIGHT: 
					while (strpol.length()>0 && strpol.charAt(strpol.length()-1)==' ')
						strpol.deleteCharAt(strpol.length()-1);
					break;
				case TextFileInputMeta.TYPE_TRIM_BOTH : 
					while(strpol.length()>0 && strpol.charAt(0)==' ') 
						strpol.deleteCharAt(0); 
					while (strpol.length()>0 && strpol.charAt(strpol.length()-1)==' ')
						strpol.deleteCharAt(strpol.length()-1);
					break;
				default: break;
				}
				pol=strpol.toString();

				if (value.isNumber())
				{
					if (field_format!=null)
					{
						ldf.applyPattern(field_format);
						
						if (num_decimal!=null && num_decimal.length()>=1) ldfs.setDecimalSeparator( num_decimal.charAt(0) );
						if (num_group!=null && num_group.length()>=1) ldfs.setGroupingSeparator( num_group.charAt(0) );
						if (num_currency!=null && num_currency.length()>=1) ldfs.setCurrencySymbol( num_currency );
							
						ldf.setDecimalFormatSymbols(ldfs);
					}
						
					value.setValue( ldf.parse(pol).doubleValue() );
				}
				else if (value.isInteger())
				{
					value.setValue( Long.parseLong(pol) );
				}
                else if (value.isBigNumber())
                {
                    value.setValue(new BigDecimal(pol) );
                }
                else
                {
                    throw new KettleValueException("Unknown numeric type: contact vendor!");
                }
			}
			catch(Exception e)
			{
				throw(e);
			}
		}
		else
		if (value.isString())
		{
			value.setValue(pol);
			switch(trim_type)
			{
			case TextFileInputMeta.TYPE_TRIM_LEFT : value.ltrim(); break;
			case TextFileInputMeta.TYPE_TRIM_RIGHT: value.rtrim(); break;
			case TextFileInputMeta.TYPE_TRIM_BOTH : value.trim(); break;
			default: break;
			}
			if (pol.length()==0) value.setNull();
		}
		else
		if (value.isDate())
		{
			try
			{
				if (field_format!=null)
				{
					ldaf.applyPattern(field_format);
					ldaf.setDateFormatSymbols(ldafs);
				}
					
				value.setValue( ldaf.parse(pol) );
			}
			catch(Exception e)
			{
				throw(e);
			}
		}
		value.setLength(field_length, field_precision);
				
		return value;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		Row r=null;
		boolean retval=true;
		boolean putrow = false;
		
		debug="start of readRowOfData";
		
		if (first) // we just got started
		{
            debug="first";

            first=false;

		    // Open the first file & read the required rows in the buffer, stop if it fails...
		    if (!openNextFile()) 
		    {
                closeLastFile();
                setOutputDone();
		        return false;
		    }
            
            debug="first repeat fields";
			// Count the number of repeat fields...
			for (int i=0;i<meta.getInputFields().length;i++)
			{
				if (meta.getInputFields()[i].isRepeated()) data.nr_repeats++;
			}
		}
		else
		{
            if (!data.doneReading)
            {
                int repeats = 1;
                if (meta.isLineWrapped()) repeats=  meta.getNrWraps()>0  ?  meta.getNrWraps() :repeats;
                
                // Read a number of lines...
                for (int i=0;i<repeats && !data.doneReading;i++)
                {
                    debug="get a new line of data";
                    String line = getLine(log, data.isr, meta.getFileFormat()); // Get one line of data;
                    if (line!=null)
                    {
                        linesInput++;
                        boolean filterOK=true;
                        
                        // Filter row?
                        for (int f=0;f<meta.getFilter().length && filterOK;f++)
                        {
                            TextFileFilter filter = meta.getFilter()[f];
                            if (filter.getFilterString()!=null && filter.getFilterString().length()>0)
                            {
                                int from = filter.getFilterPosition();
                                if (from>=0)
                                {
                                    int to   = from + filter.getFilterString().length();
                                    debug="verify filter : get substring("+from+", "+to+") line length="+line.length();
                                    if (line.length()>=from && line.length()>=to)
                                    {
                                        String sub = line.substring(filter.getFilterPosition(), to);
                                        if (sub.equalsIgnoreCase(filter.getFilterString()))
                                        {
                                            filterOK=false; // skip this one!
                                        }
                                    }
                                }
                                else // anywhere on the line
                                {
                                    int idx = line.indexOf(filter.getFilterString());
                                    if (idx>=0)
                                    {
                                        filterOK=false;
                                    }
                                }
                                
                                if (!filterOK)
                                {
                                    if (filter.isFilterLastLine())
                                    {
                                        data.doneReading=true;
                                    }
                                    repeats++; // grab another line, this one got filtered
                                }
                            }
                        }

                        if (filterOK)
                        {
                            // logRowlevel("LINE READ: "+line);
                            data.lineBuffer.add(line);
                        }
                    }
                    else
                    {
                        data.doneReading = true;
                    }
                }
            }
		}

        // If the buffer is empty: open the next file. (if nothing in there, open the next, etc.)
        while (data.lineBuffer.size()==0)
        {
            debug="empty buffer: open next file";
            if (!openNextFile())  // Open fails: done processing!
            {
                debug="empty buffer: close last file";
                closeLastFile();
                setOutputDone();  // signal end to receiver(s)
                return false;
            }
        }

        // Take the first line available in the buffer & remove the line from the buffer
        debug="take first line of buffer";
        String line = (String) data.lineBuffer.get(0);

        debug="remove first line of buffer";
        data.lineBuffer.remove(0);
        
        if (meta.isLayoutPaged()) 
        {
            debug="paged layout";

            // Different rules apply: on each page: 
            //   a header
            //   a number of data lines 
           //   a footer
            // 
            if (!data.doneWithHeader && data.pageLinesRead==0) // We are reading header lines
            {
                debug="paged layout : header line "+data.headerLinesRead;
                logRowlevel("P-HEADER ("+data.headerLinesRead+") : "+line);
                data.headerLinesRead++;
                if (data.headerLinesRead>=meta.getNrHeaderLines())
                {
                    data.doneWithHeader=true;
                }
            }
            else // data lines or footer on a page
            {
                debug="paged layout : data or footer";
                if (data.pageLinesRead<meta.getNrLinesPerPage())
                {
                    // See if we are dealing with wrapped lines:
                    if (meta.isLineWrapped())
                    {
                        for (int i=0;i<meta.getNrWraps();i++)
                        {
                            String extra="";
                            if (data.lineBuffer.size()>0)
                            {
                                extra = (String)data.lineBuffer.get(0);
                                data.lineBuffer.remove(0);
                            }
                            line+=extra;
                        }
                    }

                    debug="paged layout : data line";
                    logRowlevel("P-DATA: "+line);
                    // Read a normal line on a page of data.
                    data.pageLinesRead++;
                    r = convertLineToRow(log, line, meta, data.df, data.dfs, data.daf, data.dafs, data.filename, linesWritten+1);
                    if (r!=null) putrow = true;
                }
                else // done reading the data lines, skip the footer lines
                {
                    debug="paged layout : footer line";
                    if (meta.hasFooter() && data.footerLinesRead<meta.getNrFooterLines())
                    {
                        logRowlevel("P-FOOTER: "+line);
                        data.footerLinesRead++;
                    }
                    
                    if (!meta.hasFooter() || data.footerLinesRead>=meta.getNrFooterLines())
                    {
                        debug="paged layout : end of page: restart";
                        
                        // OK, we are done reading the footer lines, start again on 'next page' with the header
                        data.doneWithHeader=false;
                        data.headerLinesRead=0;
                        data.pageLinesRead=0;
                        data.footerLinesRead=0;
                        logRowlevel("RESTART PAGE");
                    }
                }
            }
        }
        else // A normal data line, can also be a header or a footer line
        {
            debug="normal";

            if (!data.doneWithHeader) // We are reading header lines
            {
                debug="normal : header line";
                data.headerLinesRead++;
                if (data.headerLinesRead>=meta.getNrHeaderLines())
                {
                    data.doneWithHeader=true;
                }
            }
            else
            {
                debug="normal : data and footer";
                // IF we are done reading and we have a footer 
                // AND the number of lines in the buffer is smaller or equal to the number of footer lines
                // THEN we can remove the remaining rows from the buffer: they are all footer rows.
                if (data.doneReading && meta.hasFooter() && data.lineBuffer.size()<= meta.getNrFooterLines()) 
                {
                    debug="normal : footer";
                    data.lineBuffer.clear();
                }
                else // Not yet a footer line: it's a normal data line.
                {
                    // See if we are dealing with wrapped lines:
                    if (meta.isLineWrapped())
                    {
                        for (int i=0;i<meta.getNrWraps();i++)
                        {
                            String extra="";
                            if (data.lineBuffer.size()>0)
                            {
                                extra = (String)data.lineBuffer.get(0);
                                data.lineBuffer.remove(0);
                            }
                            line+=extra;
                        }
                    }
                    debug="normal : data";
                    r = convertLineToRow(log, line, meta, data.df, data.dfs, data.daf, data.dafs, data.filename, linesWritten+1);
                    if (r!=null) 
                    {
                        // System.out.println("Found data row: "+r);
                        putrow = true;
                    }
                }
            }
        }
		
        if (putrow && r!=null && !r.isIgnored())
        {
    		// See if the previous values need to be repeated!
    		if (data.nr_repeats>0)
    		{
    			debug = "repeats";
    
    			if (data.previous_row==null) // First invocation...
    			{
    				debug = "init repeats";
    
    				data.previous_row=new Row();
    				for (int i=0;i<meta.getInputFields().length;i++)
    				{
    					if (meta.getInputFields()[i].isRepeated())
    					{
    						Value value    = r.getValue(i);
    						data.previous_row.addValue(new Value(value)); // Copy the first row
    					}
    				}
    			}
    			else
    			{
    				debug = "check repeats";
    
    				int repnr=0;
    				for (int i=0;i<meta.getInputFields().length;i++)
    				{
    					if (meta.getInputFields()[i].isRepeated())
    					{
    						Value value = r.getValue(i);
    						if (value.isNull()) // if it is empty: take the previous value!
    						{
    							Value prev = data.previous_row.getValue(repnr);
    							r.removeValue(i);
    							r.addValue(i, prev);
    						}
    						else // not empty: change the previous_row entry!
    						{
    							data.previous_row.removeValue(repnr);
    							data.previous_row.addValue(repnr, new Value(value));
    						}
    						repnr++;
    					}
    				}
    			}
    		}
            
            logRowlevel("Putting row: "+r.toString());
            putRow(r);

        }

		
		if ((linesInput>0) && (linesInput%Const.ROWS_UPDATE)==0) logBasic("linenr "+linesInput);
		
		debug="end of readRowOfData";

		return retval;
	}
		
	private boolean closeLastFile()
	{
	    try
	    {
            debug="closeLastFile: close";
		    // Close previous file!
		    if (data.filename!=null)
		    {
			    if (meta.isZipped())
				{
					data.zi.closeEntry();
					data.zi.close();
				}
				data.fr.close();
                data.isr.close();
                data.filename=null; // send it down the next time.
		    }
	    }
	    catch(Exception e)
	    {
			logError("Couldn't close file : "+data.filename+" --> "+e.toString());
			stopAll();
			setErrors(1);
			return false;
	    }
        
        debug="closeLastFile: end.";
        
	    return !data.isLastFile;
	}
	
	private boolean openNextFile()
	{
		try
		{
            debug="openNextFile : close last file";
		    if (!closeLastFile()) return false;
            
		    // Is this the last file?
			data.isLastFile = ( data.filenr==data.files.length-1);
			data.filename = data.files[data.filenr];

            debug="openNextFile : open file";
			logBasic("Opening file: "+data.filename);

            data.fr=new FileInputStream(new File(data.filename));

            if (meta.isZipped())
            {
                data.zi = new ZipInputStream(data.fr);
                data.zi.getNextEntry();
                
                if (meta.getEncoding()!=null && meta.getEnclosure().length()>0)
                {
                    data.isr = new InputStreamReader(new BufferedInputStream(data.zi), meta.getEncoding());
                }
                else
                {
                    data.isr = new InputStreamReader(new BufferedInputStream(data.zi));
                }
            }
            else
            {
                if (meta.getEncoding()!=null && meta.getEnclosure().length()>0)
                {
                    data.isr = new InputStreamReader(new BufferedInputStream(data.fr), meta.getEncoding());
                }
                else
                {
                    data.isr = new InputStreamReader(new BufferedInputStream(data.fr));
                }
            }
			
            debug="openNextFile : set all kinds of parameters";

			// Move file pointer ahead!
			data.filenr++;
            
            ///////////////////////////////////////////////////////////////////////////////
            // Read the first lines...
            
            // Keep track of the status of the file: are there any lines left to read?
            data.doneReading = false;
            
            // OK, read a number of lines in the buffer:
            // The header rows
            // The nr rows in the page : optional
            // The footer rows
            int bufferSize = 1; // try to read at least one line.
            bufferSize += meta.hasHeader() ? meta.getNrHeaderLines() : 0;
            bufferSize += meta.isLayoutPaged() ? meta.getNrLinesPerPage() : 0;
            bufferSize += meta.hasFooter() ? meta.getNrFooterLines() : 0;
            
            
            // See if we need to skip the document header lines...
            if (meta.isLayoutPaged())
            {
                for (int i=0;i<meta.getNrLinesDocHeader();i++)
                {
                    // Just skip these...
                    getLine(log, data.isr, meta.getFileFormat()); // header and footer: not wrapped   
                }
            }

            String line;
            for (int i=0;i<bufferSize && !data.doneReading;i++)
            {
                line = getLine(log, data.isr, meta.getFileFormat());
                if (line!=null)
                {
                    // logRowlevel("LINE READ: "+line);
                    linesInput++;
                    data.lineBuffer.add(line); // Store it in the line buffer...
                }
                else
                {
                    data.doneReading = true;
                }
            }
            
            // Reset counters etc.
            data.headerLinesRead = 0;
            data.footerLinesRead = 0;
            data.pageLinesRead   = 0;
            
            // Set a flags
            data.doneWithHeader = !meta.hasHeader();
		}
		catch(Exception e)
		{
			logError("Couldn't open file #"+data.filenr+" : "+data.filename+" ("+debug+") --> "+e.toString());
			stopAll();
			setErrors(1);
			return false;
		}
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(TextFileInputMeta)smi;
		data=(TextFileInputData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.files = meta.getFiles();
			if (data.files==null || data.files.length==0)
			{
				logError("No file(s) specified! Stop processing.");
				return false;
			}
			
		    return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(TextFileInputMeta)smi;
		data=(TextFileInputData)sdi;

		super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	//
	//
	public void run()
	{			    
		try
		{
			logBasic("Starting to run...");
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error in '"+debug+"' : "+e.toString());
			setErrors(1);
			stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}
