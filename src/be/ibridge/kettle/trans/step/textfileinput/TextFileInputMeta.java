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
    public final static int    TYPE_TRIM_NONE  = 0;

    public final static int    TYPE_TRIM_LEFT  = 1;

    public final static int    TYPE_TRIM_RIGHT = 2;

    public final static int    TYPE_TRIM_BOTH  = 3;

    public final static String trimTypeDesc[]  = { "none", "left", "right", "both" };

    /** Array of filenames */
    private String             fileName[];

    /** Wildcard or filemask (regular expression) */
    private String             fileMask[];

    /** Type of file: CSV or fixed */
    private String             fileType;

    /** String used to separated field (;) */
    private String             separator;

    /** String used to enclose separated fields (") */
    private String             enclosure;

    /** Escape character used to escape the enclosure String (\) */
    private String             escapeCharacter;

    /** Flag indicating that the file contains one header line that should be skipped. */
    private boolean            header;

    /** Flag indicating that the file contains one footer line that should be skipped. */
    private boolean            footer;

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

    /** Flag indicating that we want to filter the input based on the occurance of a string on a certain position */
    private boolean            filter;

    /** The position on which the filter is taking place */
    private int                filterPosition;

    /** The string to filter on */
    private String             filterString;

    // The fields to import...
    private TextFileInputField inputFields[];

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
     * @param fileMask The fileMask to set.
     */
    public void setFileMask(String[] fileMask)
    {
        this.fileMask = fileMask;
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
     * @return Returns the filter.
     */
    public boolean hasFilter()
    {
        return filter;
    }

    /**
     * @param filter The filter to set.
     */
    public void setFilter(boolean filter)
    {
        this.filter = filter;
    }

    /**
     * @return Returns the filterPosition.
     */
    public int getFilterPosition()
    {
        return filterPosition;
    }

    /**
     * @param filterPosition The filterPosition to set.
     */
    public void setFilterPosition(int filterPosition)
    {
        this.filterPosition = filterPosition;
    }

    /**
     * @return Returns the filterString.
     */
    public String getFilterString()
    {
        return filterString;
    }

    /**
     * @param filterString The filterString to set.
     */
    public void setFilterString(String filterString)
    {
        this.filterString = filterString;
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

        retval.allocate(nrfiles, nrfields);

        for (int i = 0; i < nrfields; i++)
        {
            retval.inputFields[i] = (TextFileInputField) inputFields[i].clone();
        }

        return retval;
    }

    public void allocate(int nrfiles, int nrfields)
    {
        fileName = new String[nrfiles];
        fileMask = new String[nrfiles];

        inputFields = new TextFileInputField[nrfields];
    }

    public void setDefault()
    {
        separator = ";";
        enclosure = "\"";
        header = true;
        footer = false;
        zipped = false;
        noEmptyLines = true;
        fileFormat = "DOS";
        fileType = "CSV";
        includeFilename = false;
        filenameField = "";
        includeRowNumber = false;
        rowNumberField = "";

        filter = false;
        filterPosition = -1;
        filterString = "";

        int nrfiles = 0;
        int nrfields = 0;

        allocate(nrfiles, nrfields);

        for (int i = 0; i < nrfiles; i++)
        {
            fileName[i] = "filename" + (i + 1);
            fileMask[i] = "";
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
        retval += "    " + XMLHandler.addTagValue("escapechar", escapeCharacter);
        retval += "    " + XMLHandler.addTagValue("header", header);
        retval += "    " + XMLHandler.addTagValue("footer", footer);
        retval += "    " + XMLHandler.addTagValue("noempty", noEmptyLines);
        retval += "    " + XMLHandler.addTagValue("include", includeFilename);
        retval += "    " + XMLHandler.addTagValue("include_field", filenameField);
        retval += "    " + XMLHandler.addTagValue("rownum", includeRowNumber);
        retval += "    " + XMLHandler.addTagValue("rownum_field", rowNumberField);
        retval += "    " + XMLHandler.addTagValue("format", fileFormat);
        retval += "    " + XMLHandler.addTagValue("filter", filter);
        retval += "    " + XMLHandler.addTagValue("filter_position", filterPosition);
        retval += "    " + XMLHandler.addTagValue("filter_string", filterString);
        retval += "    <file>" + Const.CR;
        for (int i = 0; i < fileName.length; i++)
        {
            retval += "      " + XMLHandler.addTagValue("name", fileName[i]);
            retval += "      " + XMLHandler.addTagValue("filemask", fileMask[i]);
        }
        retval += "      " + XMLHandler.addTagValue("type", fileType);
        retval += "      " + XMLHandler.addTagValue("zipped", zipped);
        retval += "      </file>" + Const.CR;
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

        return retval;
    }

    private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {
            int nrfiles, nrfields;
            String lim;

            separator = XMLHandler.getTagValue(stepnode, "separator");
            enclosure = XMLHandler.getTagValue(stepnode, "enclosure");
            escapeCharacter = XMLHandler.getTagValue(stepnode, "escapechar");
            header = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "header"));
            footer = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "footer"));
            String nempty = XMLHandler.getTagValue(stepnode, "noempty");
            noEmptyLines = "Y".equalsIgnoreCase(nempty) || nempty == null;
            includeFilename = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include"));
            filenameField = XMLHandler.getTagValue(stepnode, "include_field");
            includeRowNumber = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "rownum"));
            rowNumberField = XMLHandler.getTagValue(stepnode, "rownum_field");
            fileFormat = XMLHandler.getTagValue(stepnode, "format");

            filter = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "filter"));
            filterPosition = Const.toInt(XMLHandler.getTagValue(stepnode, "filter_position"), -1);
            filterString = XMLHandler.getTagValue(stepnode, "filter_string");

            Node filenode = XMLHandler.getSubNode(stepnode, "file");
            Node fields = XMLHandler.getSubNode(stepnode, "fields");
            nrfiles = XMLHandler.countNodes(filenode, "name");
            nrfields = XMLHandler.countNodes(fields, "field");

            allocate(nrfiles, nrfields);

            for (int i = 0; i < nrfiles; i++)
            {
                Node filenamenode = XMLHandler.getSubNodeByNr(filenode, "name", i);
                Node filemasknode = XMLHandler.getSubNodeByNr(filenode, "filemask", i);
                fileName[i] = XMLHandler.getNodeValue(filenamenode);
                fileMask[i] = XMLHandler.getNodeValue(filemasknode);
            }

            fileType = XMLHandler.getTagValue(stepnode, "file", "type");
            zipped = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "zipped"));

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
                    field.setRepeated("Y".equalsIgnoreCase(srepeat));
                else
                    field.setRepeated(false);

                inputFields[i] = field;
            }

            // Is there a limit on the number of rows we process?
            lim = XMLHandler.getTagValue(stepnode, "limit");
            rowLimit = Const.toLong(lim, 0L);
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
            escapeCharacter = rep.getStepAttributeString(id_step, "escapechar");
            header = rep.getStepAttributeBoolean(id_step, "header");
            footer = rep.getStepAttributeBoolean(id_step, "footer");
            noEmptyLines = rep.getStepAttributeBoolean(id_step, "noempty");
            includeFilename = rep.getStepAttributeBoolean(id_step, "include");
            filenameField = rep.getStepAttributeString(id_step, "include_field");

            includeRowNumber = rep.getStepAttributeBoolean(id_step, "rownum");
            rowNumberField = rep.getStepAttributeString(id_step, "rownum_field");
            fileFormat = rep.getStepAttributeString(id_step, "format");
            filter = rep.getStepAttributeBoolean(id_step, "filter");
            filterPosition = (int) rep.getStepAttributeInteger(id_step, "filter_position");
            filterString = rep.getStepAttributeString(id_step, "filter_string");
            rowLimit = (int) rep.getStepAttributeInteger(id_step, "limit");

            int nrfiles = rep.countNrStepAttributes(id_step, "file_name");
            int nrfields = rep.countNrStepAttributes(id_step, "field_name");

            allocate(nrfiles, nrfields);

            for (int i = 0; i < nrfiles; i++)
            {
                fileName[i] = rep.getStepAttributeString(id_step, i, "file_name");
                fileMask[i] = rep.getStepAttributeString(id_step, i, "file_mask");
            }
            fileType = rep.getStepAttributeString(id_step, "file_type");
            zipped = rep.getStepAttributeBoolean(id_step, "file_zipped");

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
            rep.saveStepAttribute(id_transformation, id_step, "escapechar", escapeCharacter);
            rep.saveStepAttribute(id_transformation, id_step, "header", header);
            rep.saveStepAttribute(id_transformation, id_step, "footer", footer);
            rep.saveStepAttribute(id_transformation, id_step, "noempty", noEmptyLines);
            rep.saveStepAttribute(id_transformation, id_step, "include", includeFilename);
            rep.saveStepAttribute(id_transformation, id_step, "include_field", filenameField);
            rep.saveStepAttribute(id_transformation, id_step, "rownum", includeRowNumber);
            rep.saveStepAttribute(id_transformation, id_step, "rownum_field", rowNumberField);
            rep.saveStepAttribute(id_transformation, id_step, "format", fileFormat);
            rep.saveStepAttribute(id_transformation, id_step, "filter", filter);
            rep.saveStepAttribute(id_transformation, id_step, "filter_position", filterPosition);
            rep.saveStepAttribute(id_transformation, id_step, "filter_string", filterString);
            rep.saveStepAttribute(id_transformation, id_step, "limit", rowLimit);

            for (int i = 0; i < fileName.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "file_name", fileName[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "file_mask", fileMask[i]);
            }
            rep.saveStepAttribute(id_transformation, id_step, "file_type", fileType);
            rep.saveStepAttribute(id_transformation, id_step, "file_zipped", zipped);

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

    public String[] getFiles()
    {
        String files[] = null;

        // Replace possible environment variables...
        final String realfile[] = Const.replEnv(fileName);
        final String realmask[] = Const.replEnv(fileMask);

        ArrayList filelist = new ArrayList();

        for (int i = 0; i < realfile.length; i++)
        {
            final String onefile = realfile[i];
            final String onemask = realmask[i];

            // System.out.println("Checking file ["+onefile+"] mask ["+onemask+"]");

            if (onemask != null && onemask.length() > 0) // A directory & a wildcard
            {
                File file = new File(onefile);
                try
                {
                    files = file.list(new FilenameFilter()
                    {
                        public boolean accept(File dir, String name)
                        {
                            return Pattern.matches(onemask, name);
                        }
                    });

                    for (int j = 0; j < files.length; j++)
                    {
                        if (!onefile.endsWith(Const.FILE_SEPARATOR))
                        {
                            files[j] = onefile + Const.FILE_SEPARATOR + files[j];
                        }
                        else
                        {
                            files[j] = onefile + files[j];
                        }
                    }
                }
                catch (Exception e)
                {
                    files = null;
                }
            }
            else
            // A normal file...
            {
                // Check if it exists...
                File file = new File(onefile);
                if (file.exists() && file.isFile() && file.canRead())
                {
                    files = new String[] { onefile };
                }
                else
                // File is not accessible to us.
                {
                    files = null;
                }
            }

            // System.out.println(" --> found "+(files==null?0:files.length)+" files");

            // Add to our list...
            if (files != null) for (int x = 0; x < files.length; x++)
            {
                filelist.add(files[x]);
            }
        }
        // OK, return the list in filelist...
        files = (String[]) filelist.toArray(new String[filelist.size()]);

        return files;
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

        String files[] = getFiles();
        if (files == null || files.length == 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No files can be found to read.", stepinfo);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "This step is reading " + files.length + " files.", stepinfo);
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
}
