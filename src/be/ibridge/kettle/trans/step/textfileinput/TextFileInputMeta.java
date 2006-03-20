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

/* 
 * 
 * Created on 4-apr-2003
 * 
 */

package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;

public class TextFileInputMeta extends BaseStepMeta implements StepMetaInterface
{
    private static final String NO = "N";

	private static final String YES = "Y";

	public final static int    TYPE_TRIM_NONE  = 0;

    public final static int    TYPE_TRIM_LEFT  = 1;

    public final static int    TYPE_TRIM_RIGHT = 2;

    public final static int    TYPE_TRIM_BOTH  = 3;

    public final static String trimTypeDesc[]  = { "none", "left", "right", "both" };

    /** Array of filenames */
    private String             fileName[];

    /** Wildcard or filemask (regular expression) */
    private String             fileMask[];
    
    /** Array of boolean values as string, indicating if a file is required. */
    private String             fileRequired[];

    /** Type of file: CSV or fixed */
    private String             fileType;

    /** String used to separated field (;) */
    private String             separator;

    /** String used to enclose separated fields (") */
    private String             enclosure;

    /** Escape character used to escape the enclosure String (\) */
    private String             escapeCharacter;
    
    /** Switch to allow breaks (CR/LF) in Enclosures */
    private boolean            breakInEnclosureAllowed;

    /** Flag indicating that the file contains one header line that should be skipped. */
    private boolean            header;
    
    /** The number of header lines, defaults to 1 */
    private int                nrHeaderLines;

    /** Flag indicating that the file contains one footer line that should be skipped. */
    private boolean            footer;

    /** The number of footer lines, defaults to 1 */
    private int                nrFooterLines;

    /** Flag indicating that a single line is wrapped onto one or more lines in the text file. */
    private boolean            lineWrapped;

    /** The number of times the line wrapped */
    private int                nrWraps;

    /** Flag indicating that the text-file has a paged layout. */
    private boolean            layoutPaged;

    /** The number of lines in the document header */
    private int                nrLinesDocHeader;

    /** The number of lines to read per page */
    private int                nrLinesPerPage;

    
    /** Flag indicating that the text file to be read is stored in a ZIP archive */
    private boolean            zipped;

    /** Flag indicating that we should skip all empty lines */
    private boolean            noEmptyLines;

    /** Flag indicating that we should include the filename in the output */
    private boolean            includeFilename;

    /** The name of the field in the output containing the filename */
    private String             filenameField;

    /** Flag indicating that a row number field should be included in the output */
    private boolean            includeRowNumber;

    /** The name of the field in the output containing the row number */
    private String             rowNumberField;

    /** The file format: DOS or UNIX */
    private String             fileFormat;

    /** The maximum number or lines to read */
    private long               rowLimit;

    /** The fields to import... */
    private TextFileInputField inputFields[];

    /** The filters to use... */
    private TextFileFilter filter[];
    
    /** The encoding to use for reading: null or empty string means system default encoding */
    private String encoding;
    
    /** Ignore error : turn into warnings */
    private boolean errorIgnored;
    
    /** The name of the field that will contain the number of errors in the row*/
    private String  errorCountField;
    
    /** The name of the field that will contain the names of the fields that generated errors, separated by , */
    private String  errorFieldsField;
    
    /** The name of the field that will contain the error texts, separated by CR */
    private String  errorTextField;
    
    /** The directory that will contain warning files */
    private String warningFilesDestinationDirectory;
    
    /** The extension of warning files */
    private String warningFilesExtension;
    
    /** The directory that will contain error files */
    private String errorFilesDestinationDirectory;
    
    /** The extension of error files */
    private String errorFilesExtension;
    
    /** The directory that will contain line number files */
    private String lineNumberFilesDestinationDirectory;
    
    /** The extension of line number files */
    private String lineNumberFilesExtension;
    
    private boolean dateFormatLenient;

    /** If error line are skipped, you can replay without introducing doubles.*/
	private boolean errorLineSkipped;
    
    
    /**
     * @return Returns the encoding.
     */
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * @param encoding The encoding to set.
     */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public TextFileInputMeta()
    {
        super(); // allocate BaseStepMeta
    }

    /**
     * @return Returns the input fields.
     */
    public TextFileInputField[] getInputFields()
    {
        return inputFields;
    }

    /**
     * @param inputFields The input fields to set.
     */
    public void setInputFields(TextFileInputField[] inputFields)
    {
        this.inputFields = inputFields;
    }

    /**
     * @return Returns the enclosure.
     */
    public String getEnclosure()
    {
        return enclosure;
    }

    /**
     * @param enclosure The enclosure to set.
     */
    public void setEnclosure(String enclosure)
    {
        this.enclosure = enclosure;
    }

    /**
     * @return Returns the breakInEnclosureAllowed.
     */
    public boolean isBreakInEnclosureAllowed()
    {
        return breakInEnclosureAllowed;
    }

    /**
     * @param breakInEnclosureAllowed The breakInEnclosureAllowed to set.
     */
    public void setBreakInEnclosureAllowed(boolean breakInEnclosureAllowed)
    {
        this.breakInEnclosureAllowed = breakInEnclosureAllowed;
    }

    /**
     * @return Returns the fileFormat.
     */
    public String getFileFormat()
    {
        return fileFormat;
    }

    /**
     * @param fileFormat The fileFormat to set.
     */
    public void setFileFormat(String fileFormat)
    {
        this.fileFormat = fileFormat;
    }

    /**
     * @return Returns the fileMask.
     */
    public String[] getFileMask()
    {
        return fileMask;
    }
    
    /**
     * @return Returns the fileRequired.
     */
    public String[] getFileRequired() {
    	return fileRequired;
	}

    /**
     * @param fileMask The fileMask to set.
     */
    public void setFileMask(String[] fileMask)
    {
        this.fileMask = fileMask;
    }
    
    /**
     * @param fileRequired The fileRequired to set.
     */
    public void setFileRequired(String[] fileRequired)
    {
        this.fileRequired = fileRequired;
    }

    /**
     * @return Returns the fileName.
     */
    public String[] getFileName()
    {
        return fileName;
    }

    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String[] fileName)
    {
        this.fileName = fileName;
    }

    /**
     * @return Returns the filenameField.
     */
    public String getFilenameField()
    {
        return filenameField;
    }

    /**
     * @param filenameField The filenameField to set.
     */
    public void setFilenameField(String filenameField)
    {
        this.filenameField = filenameField;
    }

    /**
     * @return Returns the fileType.
     */
    public String getFileType()
    {
        return fileType;
    }

    /**
     * @param fileType The fileType to set.
     */
    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    /**
     * @return The array of filters for the metadata of this text file input step. 
     */
    public TextFileFilter[] getFilter()
    {
        return filter;
    }
    
    /**
     * @param filter The array of filters to use
     */
    public void setFilter(TextFileFilter[] filter)
    {
        this.filter = filter;
    }
 
    /**
     * @return Returns the footer.
     */
    public boolean hasFooter()
    {
        return footer;
    }

    /**
     * @param footer The footer to set.
     */
    public void setFooter(boolean footer)
    {
        this.footer = footer;
    }

    /**
     * @return Returns the header.
     */
    public boolean hasHeader()
    {
        return header;
    }

    /**
     * @param header The header to set.
     */
    public void setHeader(boolean header)
    {
        this.header = header;
    }

    /**
     * @return Returns the includeFilename.
     */
    public boolean includeFilename()
    {
        return includeFilename;
    }

    /**
     * @param includeFilename The includeFilename to set.
     */
    public void setIncludeFilename(boolean includeFilename)
    {
        this.includeFilename = includeFilename;
    }

    /**
     * @return Returns the includeRowNumber.
     */
    public boolean includeRowNumber()
    {
        return includeRowNumber;
    }

    /**
     * @param includeRowNumber The includeRowNumber to set.
     */
    public void setIncludeRowNumber(boolean includeRowNumber)
    {
        this.includeRowNumber = includeRowNumber;
    }

    /**
     * @return Returns the noEmptyLines.
     */
    public boolean noEmptyLines()
    {
        return noEmptyLines;
    }

    /**
     * @param noEmptyLines The noEmptyLines to set.
     */
    public void setNoEmptyLines(boolean noEmptyLines)
    {
        this.noEmptyLines = noEmptyLines;
    }

    /**
     * @return Returns the rowLimit.
     */
    public long getRowLimit()
    {
        return rowLimit;
    }

    /**
     * @param rowLimit The rowLimit to set.
     */
    public void setRowLimit(long rowLimit)
    {
        this.rowLimit = rowLimit;
    }

    /**
     * @return Returns the rowNumberField.
     */
    public String getRowNumberField()
    {
        return rowNumberField;
    }

    /**
     * @param rowNumberField The rowNumberField to set.
     */
    public void setRowNumberField(String rowNumberField)
    {
        this.rowNumberField = rowNumberField;
    }

    /**
     * @return Returns the separator.
     */
    public String getSeparator()
    {
        return separator;
    }

    /**
     * @param separator The separator to set.
     */
    public void setSeparator(String separator)
    {
        this.separator = separator;
    }

    /**
     * @return Returns the zipped.
     */
    public boolean isZipped()
    {
        return zipped;
    }

    /**
     * @param zipped The zipped to set.
     */
    public void setZipped(boolean zipped)
    {
        this.zipped = zipped;
    }

    public void loadXML(Node stepnode, ArrayList databases, Hashtable counters) throws KettleXMLException
    {
        readData(stepnode);
    }

    public Object clone()
    {
        TextFileInputMeta retval = (TextFileInputMeta) super.clone();

        int nrfiles = fileName.length;
        int nrfields = inputFields.length;
        int nrfilters = filter.length;

        retval.allocate(nrfiles, nrfields, nrfilters);

        for (int i = 0; i < nrfields; i++)
        {
            retval.inputFields[i] = (TextFileInputField) inputFields[i].clone();
        }

        for (int i = 0; i < nrfilters; i++)
        {
            retval.filter[i] = (TextFileFilter) filter[i].clone();
        }

        return retval;
    }

    public void allocate(int nrfiles, int nrfields, int nrfilters)
    {
        fileName = new String[nrfiles];
        fileMask = new String[nrfiles];
        fileRequired = new String[nrfiles];

        inputFields = new TextFileInputField[nrfields];
        
        filter = new TextFileFilter[nrfilters];
    }

    public void setDefault()
    {
        separator = ";";
        enclosure = "\"";
        breakInEnclosureAllowed=false;
        header = true;
        nrHeaderLines = 1;
        footer = false;
        nrFooterLines = 1;
        lineWrapped = false;
        nrWraps = 1;
        layoutPaged = false;
        nrLinesPerPage = 80;
        nrLinesDocHeader = 0;
        zipped = false;
        noEmptyLines = true;
        fileFormat = "DOS";
        fileType = "CSV";
        includeFilename = false;
        filenameField = "";
        includeRowNumber = false;
        rowNumberField = "";
        warningFilesDestinationDirectory = null;
        warningFilesExtension = "warning";
        errorFilesDestinationDirectory = null;
        errorFilesExtension = "error"; 
        lineNumberFilesDestinationDirectory = null;
        lineNumberFilesExtension = "line";
        dateFormatLenient = true;

        int nrfiles = 0;
        int nrfields = 0;
        int nrfilters = 0;
        
        allocate(nrfiles, nrfields, nrfilters);

        for (int i = 0; i < nrfiles; i++)
        {
            fileName[i] = "filename" + (i + 1);
            fileMask[i] = "";
            fileRequired[i] = NO;
        }

        for (int i = 0; i < nrfields; i++)
        {
            inputFields[i] = new TextFileInputField("field" + (i + 1), 1, -1);
        }

        rowLimit = 0L;
    }

    public Row getFields(Row r, String name, Row info)
    {
        Row row;
        if (r == null)
            row = new Row(); // give back values
        else
            row = r; // add to the existing row of values...

        int i;
        for (i = 0; i < inputFields.length; i++)
        {
            TextFileInputField field = inputFields[i];

            int type = field.getType();
            if (type == Value.VALUE_TYPE_NONE) type = Value.VALUE_TYPE_STRING;
            Value v = new Value(field.getName(), type);
            v.setLength(field.getLength(), field.getPrecision());
            v.setOrigin(name);
            row.addValue(v);
        }
        if (errorIgnored)
        {
            if (errorCountField!=null && errorCountField.length()>0)
            {
                Value v = new Value(errorCountField, Value.VALUE_TYPE_INTEGER);
                v.setOrigin(name);
                row.addValue(v);
            }
            if (errorFieldsField!=null && errorFieldsField.length()>0)
            {
                Value v = new Value(errorFieldsField, Value.VALUE_TYPE_STRING);
                v.setOrigin(name);
                row.addValue(v);
            }
            if (errorTextField!=null && errorTextField.length()>0)
            {
                Value v = new Value(errorTextField, Value.VALUE_TYPE_STRING);
                v.setOrigin(name);
                row.addValue(v);
            }
        }
        if (includeFilename)
        {
            Value v = new Value(filenameField, Value.VALUE_TYPE_STRING);
            v.setLength(100, -1);
            v.setOrigin(name);
            row.addValue(v);
        }
        if (includeRowNumber)
        {
            Value v = new Value(rowNumberField, Value.VALUE_TYPE_NUMBER);
            v.setLength(7, 0);
            v.setOrigin(name);
            row.addValue(v);
        }
        return row;
    }

    public String getXML()
    {
        String retval = "";

        retval += "    " + XMLHandler.addTagValue("separator", separator);
        retval += "    " + XMLHandler.addTagValue("enclosure", enclosure);
        retval += "    " + XMLHandler.addTagValue("enclosure_breaks", breakInEnclosureAllowed);
        retval += "    " + XMLHandler.addTagValue("escapechar", escapeCharacter);
        retval += "    " + XMLHandler.addTagValue("header", header);
        retval += "    " + XMLHandler.addTagValue("nr_headerlines", nrHeaderLines);
        retval += "    " + XMLHandler.addTagValue("footer", footer);
        retval += "    " + XMLHandler.addTagValue("nr_footerlines", nrFooterLines);
        retval += "    " + XMLHandler.addTagValue("line_wrapped", lineWrapped);
        retval += "    " + XMLHandler.addTagValue("nr_wraps", nrWraps);
        retval += "    " + XMLHandler.addTagValue("layout_paged", layoutPaged);
        retval += "    " + XMLHandler.addTagValue("nr_lines_per_page", nrLinesPerPage);
        retval += "    " + XMLHandler.addTagValue("nr_lines_doc_header", nrLinesDocHeader);
        retval += "    " + XMLHandler.addTagValue("noempty", noEmptyLines);
        retval += "    " + XMLHandler.addTagValue("include", includeFilename);
        retval += "    " + XMLHandler.addTagValue("include_field", filenameField);
        retval += "    " + XMLHandler.addTagValue("rownum", includeRowNumber);
        retval += "    " + XMLHandler.addTagValue("rownum_field", rowNumberField);
        retval += "    " + XMLHandler.addTagValue("format", fileFormat);
        retval += "    " + XMLHandler.addTagValue("encoding", encoding);

        retval += "    <file>" + Const.CR;
        for (int i = 0; i < fileName.length; i++)
        {
            retval += "      " + XMLHandler.addTagValue("name", fileName[i]);
            retval += "      " + XMLHandler.addTagValue("filemask", fileMask[i]);
            retval += "      " + XMLHandler.addTagValue("file_required", fileRequired[i]);
        }
        retval += "      " + XMLHandler.addTagValue("type", fileType);
        retval += "      " + XMLHandler.addTagValue("zipped", zipped);
        retval += "      </file>" + Const.CR;
        
        retval += "    <filters>" + Const.CR;
        for (int i = 0; i < filter.length; i++)
        {
            retval += "      <filter>" + Const.CR;
            retval += "        " + XMLHandler.addTagValue("filter_string", filter[i].getFilterString(), false);
            retval += "        " + XMLHandler.addTagValue("filter_position", filter[i].getFilterPosition(), false);
            retval += "        " + XMLHandler.addTagValue("filter_is_last_line", filter[i].isFilterLastLine(), false);
            retval += "      <filter>" + Const.CR;
        }
        retval += "      </filters>" + Const.CR;
        

        retval += "    <fields>" + Const.CR;
        for (int i = 0; i < inputFields.length; i++)
        {
            TextFileInputField field = inputFields[i];

            retval += "      <field>" + Const.CR;
            retval += "        " + XMLHandler.addTagValue("name", field.getName());
            retval += "        " + XMLHandler.addTagValue("type", field.getTypeDesc());
            retval += "        " + XMLHandler.addTagValue("format", field.getFormat());
            retval += "        " + XMLHandler.addTagValue("currency", field.getCurrencySymbol());
            retval += "        " + XMLHandler.addTagValue("decimal", field.getDecimalSymbol());
            retval += "        " + XMLHandler.addTagValue("group", field.getGroupSymbol());
            retval += "        " + XMLHandler.addTagValue("nullif", field.getNullString());
            retval += "        " + XMLHandler.addTagValue("position", field.getPosition());
            retval += "        " + XMLHandler.addTagValue("length", field.getLength());
            retval += "        " + XMLHandler.addTagValue("precision", field.getPrecision());
            retval += "        " + XMLHandler.addTagValue("trim_type", field.getTrimTypeDesc());
            retval += "        " + XMLHandler.addTagValue("repeat", field.isRepeated());
            retval += "        </field>" + Const.CR;
        }
        retval += "      </fields>" + Const.CR;
        retval += "    " + XMLHandler.addTagValue("limit", rowLimit);

        // ERROR HANDLING
        retval += "    " + XMLHandler.addTagValue("error_ignored", errorIgnored);
        retval += "    " + XMLHandler.addTagValue("error_line_skipped", errorLineSkipped);
        retval += "    " + XMLHandler.addTagValue("error_count_field", errorCountField);
        retval += "    " + XMLHandler.addTagValue("error_fields_field", errorFieldsField);
        retval += "    " + XMLHandler.addTagValue("error_text_field", errorTextField);
        
        retval += "    " + XMLHandler.addTagValue("bad_line_files_destination_directory", warningFilesDestinationDirectory);
        retval += "    " + XMLHandler.addTagValue("bad_line_files_extension", warningFilesExtension);
        retval += "    " + XMLHandler.addTagValue("error_line_files_destination_directory", errorFilesDestinationDirectory);
        retval += "    " + XMLHandler.addTagValue("error_line_files_extension", errorFilesExtension); 
        retval += "    " + XMLHandler.addTagValue("line_number_files_destination_directory", lineNumberFilesDestinationDirectory);
        retval += "    " + XMLHandler.addTagValue("line_number_files_extension", lineNumberFilesExtension);
        retval += "    " + XMLHandler.addTagValue("date_format_lenient", dateFormatLenient);
        

        return retval;
    }

    private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {
            separator = XMLHandler.getTagValue(stepnode, "separator");
            enclosure = XMLHandler.getTagValue(stepnode, "enclosure");
            breakInEnclosureAllowed = YES.equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "enclosure_breaks"));
            escapeCharacter = XMLHandler.getTagValue(stepnode, "escapechar");
            header = YES.equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "header"));
            nrHeaderLines = Const.toInt( XMLHandler.getTagValue(stepnode, "nr_headerlines"), 1);
            footer = YES.equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "footer"));
            nrFooterLines = Const.toInt( XMLHandler.getTagValue(stepnode, "nr_footerlines"), 1);
            lineWrapped = YES.equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "line_wrapped"));
            nrWraps = Const.toInt( XMLHandler.getTagValue(stepnode, "nr_wraps"), 1);
            layoutPaged = YES.equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "layout_paged"));
            nrLinesPerPage = Const.toInt( XMLHandler.getTagValue(stepnode, "nr_lines_per_page"), 1);
            nrLinesDocHeader = Const.toInt( XMLHandler.getTagValue(stepnode, "nr_lines_doc_header"), 1);

            String nempty = XMLHandler.getTagValue(stepnode, "noempty");
            noEmptyLines = YES.equalsIgnoreCase(nempty) || nempty == null;
            includeFilename = YES.equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include"));
            filenameField = XMLHandler.getTagValue(stepnode, "include_field");
            includeRowNumber = YES.equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "rownum"));
            rowNumberField = XMLHandler.getTagValue(stepnode, "rownum_field");
            fileFormat = XMLHandler.getTagValue(stepnode, "format");
            encoding = XMLHandler.getTagValue(stepnode, "encoding");

            Node filenode    = XMLHandler.getSubNode(stepnode, "file");
            Node fields      = XMLHandler.getSubNode(stepnode, "fields");
            Node filtersNode = XMLHandler.getSubNode(stepnode, "filters");
            int nrfiles   = XMLHandler.countNodes(filenode, "name");
            int nrfields  = XMLHandler.countNodes(fields, "field");
            int nrfilters = XMLHandler.countNodes(fields, "filter");

            allocate(nrfiles, nrfields, nrfilters);

            for (int i = 0; i < nrfiles; i++)
            {
                Node filenamenode = XMLHandler.getSubNodeByNr(filenode, "name", i);
                Node filemasknode = XMLHandler.getSubNodeByNr(filenode, "filemask", i);
                Node fileRequirednode = XMLHandler.getSubNodeByNr(filenode, "file_required", i);
                fileName[i] = XMLHandler.getNodeValue(filenamenode);
                fileMask[i] = XMLHandler.getNodeValue(filemasknode);
                fileRequired[i] = XMLHandler.getNodeValue(fileRequirednode);
            }

            fileType = XMLHandler.getTagValue(stepnode, "file", "type");
            zipped = YES.equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "zipped"));

            // Backward compatibility : just one filter
            if (XMLHandler.getTagValue(stepnode, "filter")!=null)
            {
                filter = new TextFileFilter[1];
                filter[0] = new TextFileFilter();
                
                filter[0].setFilterPosition( Const.toInt(XMLHandler.getTagValue(stepnode, "filter_position"), -1) );
                filter[0].setFilterString( XMLHandler.getTagValue(stepnode, "filter_string") );
                filter[0].setFilterLastLine( YES.equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "filter_is_last_line")) );
            }
            else
            {
                for (int i=0; i < nrfilters ; i++)
                {
                    Node fnode = XMLHandler.getSubNodeByNr(filtersNode, "filter", i);
                    filter[i] = new TextFileFilter();

                    filter[i].setFilterPosition( Const.toInt(XMLHandler.getTagValue(fnode, "filter_position"), -1) );
                    filter[i].setFilterString( XMLHandler.getTagValue(fnode, "filter_string") );
                    filter[i].setFilterLastLine( YES.equalsIgnoreCase(XMLHandler.getTagValue(fnode, "filter_is_last_line")) );
                }
            }

            for (int i = 0; i < nrfields; i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
                TextFileInputField field = new TextFileInputField();

                field.setName(XMLHandler.getTagValue(fnode, "name"));
                field.setType(Value.getType(XMLHandler.getTagValue(fnode, "type")));
                field.setFormat(XMLHandler.getTagValue(fnode, "format"));
                field.setCurrencySymbol(XMLHandler.getTagValue(fnode, "currency"));
                field.setDecimalSymbol(XMLHandler.getTagValue(fnode, "decimal"));
                field.setGroupSymbol(XMLHandler.getTagValue(fnode, "group"));
                field.setNullString(XMLHandler.getTagValue(fnode, "nullif"));
                field.setPosition(Const.toInt(XMLHandler.getTagValue(fnode, "position"), -1));
                field.setLength(Const.toInt(XMLHandler.getTagValue(fnode, "length"), -1));
                field.setPrecision(Const.toInt(XMLHandler.getTagValue(fnode, "precision"), -1));
                field.setTrimType(getTrimType(XMLHandler.getTagValue(fnode, "trim_type")));

                String srepeat = XMLHandler.getTagValue(fnode, "repeat");
                if (srepeat != null)
                    field.setRepeated(YES.equalsIgnoreCase(srepeat));
                else
                    field.setRepeated(false);

                inputFields[i] = field;
            }

            // Is there a limit on the number of rows we process?
            rowLimit = Const.toLong( XMLHandler.getTagValue(stepnode, "limit"), 0L);

            errorIgnored = YES.equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "error_ignored") );
            errorLineSkipped = YES.equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "error_line_skipped") );
            errorCountField = XMLHandler.getTagValue(stepnode, "error_count_field");
            errorFieldsField = XMLHandler.getTagValue(stepnode, "error_fields_field");
            errorTextField = XMLHandler.getTagValue(stepnode, "error_text_field");
            warningFilesDestinationDirectory = XMLHandler.getTagValue(stepnode, "bad_line_files_destination_directory");
            warningFilesExtension = XMLHandler.getTagValue(stepnode, "bad_line_files_extension");
            errorFilesDestinationDirectory = XMLHandler.getTagValue(stepnode, "error_line_files_destination_directory");
            errorFilesExtension = XMLHandler.getTagValue(stepnode, "error_line_files_extension"); 
            lineNumberFilesDestinationDirectory = XMLHandler.getTagValue(stepnode, "line_number_files_destination_directory");
            lineNumberFilesExtension = XMLHandler.getTagValue(stepnode, "line_number_files_extension");
            // Backward compatible
            dateFormatLenient = ! NO.equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "date_format_lenient"));
        }
        catch (Exception e)
        {
            throw new KettleXMLException("Unable to load step info from XML", e);
        }
    }

    public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters) throws KettleException
    {
        try
        {
            separator = rep.getStepAttributeString(id_step, "separator");
            enclosure = rep.getStepAttributeString(id_step, "enclosure");
            breakInEnclosureAllowed = rep.getStepAttributeBoolean(id_step, "enclosure_breaks");
            escapeCharacter = rep.getStepAttributeString(id_step, "escapechar");
            header = rep.getStepAttributeBoolean(id_step, "header");
            nrHeaderLines = (int)rep.getStepAttributeInteger(id_step, "nr_headerlines");
            footer = rep.getStepAttributeBoolean(id_step, "footer");
            nrFooterLines = (int)rep.getStepAttributeInteger(id_step, "nr_footerlines");
            lineWrapped = rep.getStepAttributeBoolean(id_step, "line_wrapped");
            nrWraps = (int)rep.getStepAttributeInteger(id_step, "nr_wraps");
            layoutPaged = rep.getStepAttributeBoolean(id_step, "layout_paged");
            nrLinesPerPage = (int)rep.getStepAttributeInteger(id_step, "nr_lines_per_page");
            nrLinesDocHeader = (int)rep.getStepAttributeInteger(id_step, "nr_lines_doc_header");
            noEmptyLines = rep.getStepAttributeBoolean(id_step, "noempty");
            includeFilename = rep.getStepAttributeBoolean(id_step, "include");
            filenameField = rep.getStepAttributeString(id_step, "include_field");

            includeRowNumber = rep.getStepAttributeBoolean(id_step, "rownum");
            rowNumberField = rep.getStepAttributeString(id_step, "rownum_field");
            fileFormat = rep.getStepAttributeString(id_step, "format");
            encoding = rep.getStepAttributeString(id_step, "encoding");

            rowLimit = (int) rep.getStepAttributeInteger(id_step, "limit");

            int nrfiles = rep.countNrStepAttributes(id_step, "file_name");
            int nrfields = rep.countNrStepAttributes(id_step, "field_name");
            int nrfilters = rep.countNrStepAttributes(id_step, "filter_string");

            allocate(nrfiles, nrfields, nrfilters);

            for (int i = 0; i < nrfiles; i++)
            {
                fileName[i] = rep.getStepAttributeString(id_step, i, "file_name");
                fileMask[i] = rep.getStepAttributeString(id_step, i, "file_mask");
                fileRequired[i] = rep.getStepAttributeString(id_step, i, "file_required");
                if(!YES.equalsIgnoreCase(fileRequired[i]))
                	fileRequired[i] = NO;
            }
            fileType = rep.getStepAttributeString(id_step, "file_type");
            zipped = rep.getStepAttributeBoolean(id_step, "file_zipped");

            for (int i = 0; i < nrfilters; i++)
            {
                filter[i] = new TextFileFilter();
                filter[i].setFilterPosition( (int) rep.getStepAttributeInteger(id_step, i, "filter_position") );
                filter[i].setFilterString( rep.getStepAttributeString(id_step, i, "filter_string") );
                filter[i].setFilterLastLine( rep.getStepAttributeBoolean(id_step, i, "filter_is_last_line") );
            }
            
            for (int i = 0; i < nrfields; i++)
            {
                TextFileInputField field = new TextFileInputField();

                field.setName(rep.getStepAttributeString(id_step, i, "field_name"));
                field.setType(Value.getType(rep.getStepAttributeString(id_step, i, "field_type")));
                field.setFormat(rep.getStepAttributeString(id_step, i, "field_format"));
                field.setCurrencySymbol(rep.getStepAttributeString(id_step, i, "field_currency"));
                field.setDecimalSymbol(rep.getStepAttributeString(id_step, i, "field_decimal"));
                field.setGroupSymbol(rep.getStepAttributeString(id_step, i, "field_group"));
                field.setNullString(rep.getStepAttributeString(id_step, i, "field_nullif"));
                field.setPosition((int) rep.getStepAttributeInteger(id_step, i, "field_position"));
                field.setLength((int) rep.getStepAttributeInteger(id_step, i, "field_length"));
                field.setPrecision((int) rep.getStepAttributeInteger(id_step, i, "field_precision"));
                field.setTrimType(getTrimType(rep.getStepAttributeString(id_step, i, "field_trim_type")));
                field.setRepeated(rep.getStepAttributeBoolean(id_step, i, "field_repeat"));

                inputFields[i] = field;
            }
            
            errorIgnored = rep.getStepAttributeBoolean(id_step, "error_ignored");
            errorLineSkipped = rep.getStepAttributeBoolean(id_step, "error_line_skipped");
            errorCountField = rep.getStepAttributeString(id_step, "error_count_field");
            errorFieldsField = rep.getStepAttributeString(id_step, "error_fields_field");
            errorTextField = rep.getStepAttributeString(id_step, "error_text_field");
            
            warningFilesDestinationDirectory = rep.getStepAttributeString(id_step, "bad_line_files_dest_dir");
            warningFilesExtension = rep.getStepAttributeString(id_step, "bad_line_files_ext");
            errorFilesDestinationDirectory = rep.getStepAttributeString(id_step, "error_line_files_dest_dir");
            errorFilesExtension = rep.getStepAttributeString(id_step, "error_line_files_ext"); 
            lineNumberFilesDestinationDirectory = rep.getStepAttributeString(id_step, "line_number_files_dest_dir");
            lineNumberFilesExtension = rep.getStepAttributeString(id_step, "line_number_files_ext");
            dateFormatLenient = rep.getStepAttributeBoolean(id_step, 0, "date_format_lenient", true);
        }
        catch (Exception e)
        {
            throw new KettleException("Unexpected error reading step information from the repository", e);
        }
    }

    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "separator", separator);
            rep.saveStepAttribute(id_transformation, id_step, "enclosure", enclosure);
            rep.saveStepAttribute(id_transformation, id_step, "enclosure_breaks", breakInEnclosureAllowed);
            rep.saveStepAttribute(id_transformation, id_step, "escapechar", escapeCharacter);
            rep.saveStepAttribute(id_transformation, id_step, "header", header);
            rep.saveStepAttribute(id_transformation, id_step, "nr_headerlines", nrHeaderLines);
            rep.saveStepAttribute(id_transformation, id_step, "footer", footer);
            rep.saveStepAttribute(id_transformation, id_step, "nr_footerlines", nrFooterLines);
            rep.saveStepAttribute(id_transformation, id_step, "line_wrapped", lineWrapped);
            rep.saveStepAttribute(id_transformation, id_step, "nr_wraps", nrWraps);
            rep.saveStepAttribute(id_transformation, id_step, "layout_paged", layoutPaged);
            rep.saveStepAttribute(id_transformation, id_step, "nr_lines_per_page", nrLinesPerPage);
            rep.saveStepAttribute(id_transformation, id_step, "nr_lines_doc_header", nrLinesDocHeader);

            rep.saveStepAttribute(id_transformation, id_step, "noempty", noEmptyLines);
            rep.saveStepAttribute(id_transformation, id_step, "include", includeFilename);
            rep.saveStepAttribute(id_transformation, id_step, "include_field", filenameField);
            rep.saveStepAttribute(id_transformation, id_step, "rownum", includeRowNumber);
            rep.saveStepAttribute(id_transformation, id_step, "rownum_field", rowNumberField);
            rep.saveStepAttribute(id_transformation, id_step, "format", fileFormat);
            rep.saveStepAttribute(id_transformation, id_step, "encoding", encoding);

            rep.saveStepAttribute(id_transformation, id_step, "limit", rowLimit);

            for (int i = 0; i < fileName.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "file_name", fileName[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "file_mask", fileMask[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "file_required", fileRequired[i]);
            }
            rep.saveStepAttribute(id_transformation, id_step, "file_type", fileType);
            rep.saveStepAttribute(id_transformation, id_step, "file_zipped", zipped);

            for (int i = 0; i < filter.length ; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "filter_position", filter[i].getFilterPosition());
                rep.saveStepAttribute(id_transformation, id_step, i, "filter_string", filter[i].getFilterString());
                rep.saveStepAttribute(id_transformation, id_step, i, "filter_is_last_line", filter[i].isFilterLastLine());
            }
            
            for (int i = 0; i < inputFields.length; i++)
            {
                TextFileInputField field = inputFields[i];

                rep.saveStepAttribute(id_transformation, id_step, i, "field_name", field.getName());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_type", field.getTypeDesc());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_format", field.getFormat());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_currency", field.getCurrencySymbol());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal", field.getDecimalSymbol());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_group", field.getGroupSymbol());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_nullif", field.getNullString());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_position", field.getPosition());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_length", field.getLength());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", field.getPrecision());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_trim_type", field.getTrimTypeDesc());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_repeat", field.isRepeated());
            }
            
            rep.saveStepAttribute(id_transformation, id_step, "error_ignored", errorIgnored);
            rep.saveStepAttribute(id_transformation, id_step, "error_line_skipped", errorLineSkipped);
            rep.saveStepAttribute(id_transformation, id_step, "error_count_field", errorCountField);
            rep.saveStepAttribute(id_transformation, id_step, "error_fields_field", errorFieldsField);
            rep.saveStepAttribute(id_transformation, id_step, "error_text_field", errorTextField);
            
            rep.saveStepAttribute(id_transformation, id_step, "bad_line_files_dest_dir", warningFilesDestinationDirectory);
            rep.saveStepAttribute(id_transformation, id_step, "bad_line_files_ext", warningFilesExtension);
            rep.saveStepAttribute(id_transformation, id_step, "error_line_files_dest_dir", errorFilesDestinationDirectory);
            rep.saveStepAttribute(id_transformation, id_step, "error_line_files_ext", errorFilesExtension); 
            rep.saveStepAttribute(id_transformation, id_step, "line_number_files_dest_dir", lineNumberFilesDestinationDirectory);
            rep.saveStepAttribute(id_transformation, id_step, "line_number_files_ext", lineNumberFilesExtension);
            rep.saveStepAttribute(id_transformation, id_step, "date_format_lenient", dateFormatLenient);            
        }
        catch (Exception e)
        {
            throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
        }
    }

    public final static int getTrimType(String tt)
    {
        if (tt == null) return 0;

        for (int i = 0; i < trimTypeDesc.length; i++)
        {
            if (trimTypeDesc[i].equalsIgnoreCase(tt)) return i;
        }
        return 0;
    }

    public final static String getTrimTypeDesc(int i)
    {
        if (i < 0 || i >= trimTypeDesc.length) return trimTypeDesc[0];
        return trimTypeDesc[i];
    }

    public String[] getFilePaths()
    {
    	List fileList = getTextFileList().getFiles();
		String[] filePaths = new String[fileList.size()];
    	for (int i = 0; i < filePaths.length; i++) {
			filePaths[i] = ((File) fileList.get(i)).getPath();
		}
    	return filePaths;
    }
    
    public TextFileList getTextFileList()
    {
    	TextFileList textFileList = new TextFileList();

        // Replace possible environment variables...
        final String realfile[] = Const.replEnv(fileName);
        final String realmask[] = Const.replEnv(fileMask);

        for (int i = 0; i < realfile.length; i++)
        {
            final String onefile = realfile[i];
            final String onemask = realmask[i];
            final boolean onerequired = YES.equalsIgnoreCase(fileRequired[i]);

            // System.out.println("Checking file ["+onefile+"] mask ["+onemask+"]");

            if (onemask != null && onemask.length() > 0) // A directory & a wildcard
            {
                File file = new File(onefile);
                try
                {
                    String[] fileNames = file.list(new FilenameFilter()
                    {
                        public boolean accept(File dir, String name)
                        {
                            return Pattern.matches(onemask, name);
                        }
                    });

                    if (fileNames != null)
						for (int j = 0; j < fileNames.length; j++) {
							textFileList.addFile(new File(file, fileNames[j]));
						}
                }
                catch (Exception e)
                {
                    //do othing
                	e.printStackTrace();
                }
            }
            else
            // A normal file...
            {
            	File file = new File(onefile);
				if (file.exists()) {
					if (file.canRead()) {
						if (file.isFile())
							textFileList.addFile(file);
					} else if (onerequired)
						textFileList.addNonAccessibleFile(file);
				} else if (onerequired)
					textFileList.addNonExistantFile(file);
            }
        }
        
        // Sort the list: quicksort
        textFileList.sortFiles();
        
        // OK, return the list in filelist...
//        files = (String[]) filelist.toArray(new String[filelist.size()]);

        return textFileList;
    }

    public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
    {
        CheckResult cr;

        // See if we get input...
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "This step is not expecting nor reading any input", stepinfo);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Not receiving any input from other steps.", stepinfo);
            remarks.add(cr);
        }

        TextFileList textFileList = getTextFileList();
        if (textFileList.nrOfFiles() == 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No files can be found to read.", stepinfo);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "This step is reading " + textFileList.nrOfFiles() + " files.", stepinfo);
            remarks.add(cr);
        }
    }

    public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
    {
        return new TextFileInputDialog(shell, info, transMeta, name);
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new TextFileInput(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new TextFileInputData();
    }

    /**
     * @return Returns the escapeCharacter.
     */
    public String getEscapeCharacter()
    {
        return escapeCharacter;
    }

    /**
     * @param escapeCharacter The escapeCharacter to set.
     */
    public void setEscapeCharacter(String escapeCharacter)
    {
        this.escapeCharacter = escapeCharacter;
    }

    public String getErrorCountField()
    {
        return errorCountField;
    }

    public void setErrorCountField(String errorCountField)
    {
        this.errorCountField = errorCountField;
    }

    public String getErrorFieldsField()
    {
        return errorFieldsField;
    }

    public void setErrorFieldsField(String errorFieldsField)
    {
        this.errorFieldsField = errorFieldsField;
    }

    public boolean isErrorIgnored()
    {
        return errorIgnored;
    }

    public void setErrorIgnored(boolean errorIgnored)
    {
        this.errorIgnored = errorIgnored;
    }

    public String getErrorTextField()
    {
        return errorTextField;
    }

    public void setErrorTextField(String errorTextField)
    {
        this.errorTextField = errorTextField;
    }

    /**
     * @return Returns the lineWrapped.
     */
    public boolean isLineWrapped()
    {
        return lineWrapped;
    }

    /**
     * @param lineWrapped The lineWrapped to set.
     */
    public void setLineWrapped(boolean lineWrapped)
    {
        this.lineWrapped = lineWrapped;
    }

    /**
     * @return Returns the nrFooterLines.
     */
    public int getNrFooterLines()
    {
        return nrFooterLines;
    }

    /**
     * @param nrFooterLines The nrFooterLines to set.
     */
    public void setNrFooterLines(int nrFooterLines)
    {
        this.nrFooterLines = nrFooterLines;
    }

    /**
     * @return Returns the nrHeaderLines.
     */
    public int getNrHeaderLines()
    {
        return nrHeaderLines;
    }

    /**
     * @param nrHeaderLines The nrHeaderLines to set.
     */
    public void setNrHeaderLines(int nrHeaderLines)
    {
        this.nrHeaderLines = nrHeaderLines;
    }

    /**
     * @return Returns the nrWraps.
     */
    public int getNrWraps()
    {
        return nrWraps;
    }

    /**
     * @param nrWraps The nrWraps to set.
     */
    public void setNrWraps(int nrWraps)
    {
        this.nrWraps = nrWraps;
    }

    /**
     * @return Returns the layoutPaged.
     */
    public boolean isLayoutPaged()
    {
        return layoutPaged;
    }

    /**
     * @param layoutPaged The layoutPaged to set.
     */
    public void setLayoutPaged(boolean layoutPaged)
    {
        this.layoutPaged = layoutPaged;
    }

    /**
     * @return Returns the nrLinesPerPage.
     */
    public int getNrLinesPerPage()
    {
        return nrLinesPerPage;
    }

    /**
     * @param nrLinesPerPage The nrLinesPerPage to set.
     */
    public void setNrLinesPerPage(int nrLinesPerPage)
    {
        this.nrLinesPerPage = nrLinesPerPage;
    }

    /**
     * @return Returns the nrLinesDocHeader.
     */
    public int getNrLinesDocHeader()
    {
        return nrLinesDocHeader;
    }

    /**
     * @param nrLinesDocHeader The nrLinesDocHeader to set.
     */
    public void setNrLinesDocHeader(int nrLinesDocHeader)
    {
        this.nrLinesDocHeader = nrLinesDocHeader;
    }

	public String getWarningFilesDestinationDirectory() {
		return warningFilesDestinationDirectory;
	}

	public void setWarningFilesDestinationDirectory(
			String warningFilesDestinationDirectory) {
		this.warningFilesDestinationDirectory = warningFilesDestinationDirectory;
	}

	public String getWarningFilesExtension() {
		return warningFilesExtension;
	}

	public void setWarningFilesExtension(String warningFilesExtension) {
		this.warningFilesExtension = warningFilesExtension;
	}

	public String getLineNumberFilesDestinationDirectory() {
		return lineNumberFilesDestinationDirectory;
	}

	public void setLineNumberFilesDestinationDirectory(
			String lineNumberFilesDestinationDirectory) {
		this.lineNumberFilesDestinationDirectory = lineNumberFilesDestinationDirectory;
	}

	public String getLineNumberFilesExtension() {
		return lineNumberFilesExtension;
	}

	public void setLineNumberFilesExtension(String lineNumberFilesExtension) {
		this.lineNumberFilesExtension = lineNumberFilesExtension;
	}

	public String getErrorLineFilesDestinationDirectory() {
		return errorFilesDestinationDirectory;
	}

	public void setErrorLineFilesDestinationDirectory(
			String errorLineFilesDestinationDirectory) {
		this.errorFilesDestinationDirectory = errorLineFilesDestinationDirectory;
	} 
	
	public String getErrorLineFilesExtension() {
		return errorFilesExtension;
	}

	public void setErrorLineFilesExtension(String errorLineFilesExtension) {
		this.errorFilesExtension = errorLineFilesExtension;
	}

	public boolean isDateFormatLenient() {
		return dateFormatLenient;
	}

	public void setDateFormatLenient(boolean dateFormatLenient) {
		this.dateFormatLenient = dateFormatLenient;
	}

	public boolean isErrorLineSkipped() {
		return errorLineSkipped;
	}

	public void setErrorLineSkipped(boolean errorLineSkipped) {
		this.errorLineSkipped = errorLineSkipped;
	}

	

}
