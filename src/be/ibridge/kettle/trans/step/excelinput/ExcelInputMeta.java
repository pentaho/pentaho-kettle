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

package be.ibridge.kettle.trans.step.excelinput;

import java.util.ArrayList;
import java.util.Hashtable;

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
import be.ibridge.kettle.trans.step.fileinput.FileInputList;

public class ExcelInputMeta extends BaseStepMeta implements StepMetaInterface
{
	private static final String NO = "N";

	private static final String YES = "Y";
	public final static int TYPE_TRIM_NONE  = 0;
	public final static int TYPE_TRIM_LEFT  = 1;
	public final static int TYPE_TRIM_RIGHT = 2;
	public final static int TYPE_TRIM_BOTH  = 3;
	
    public final static String type_trim_code[] = { "none", "left", "right", "both" };
  
	public final static String type_trim_desc[] = {
      Messages.getString("ExcelInputMeta.TrimType.None"),
      Messages.getString("ExcelInputMeta.TrimType.Left"),
      Messages.getString("ExcelInputMeta.TrimType.Right"),
      Messages.getString("ExcelInputMeta.TrimType.Both")
    };

	public  static final String STRING_SEPARATOR = " \t --> ";

	/**
	 * The filenames to load or directory in case a filemask was set.
	 */
	private  String  fileName[];
	
	/**
	 * The regular expression to use (null means: no mask)
	 */
	private  String  fileMask[];
	
	/** Array of boolean values as string, indicating if a file is required. */
	private  String  fileRequired[];

	/**
	 * The fieldname that holds the name of the file
	 */
	private String       fileField;

	/**
	 * The names of the sheets to load.
	 * Null means: all sheets...
	 */
	private String       sheetName[];

	/**
	 * The row-nr where we start processing.
	 */
	private int          startRow[];

	/**
	 * The column-nr where we start processing.
	 */
	private int          startColumn[];

	/**
	 * The fieldname that holds the name of the sheet
	 */
	private String       sheetField;

	/**
	 * The cell-range starts with a header-row
	 */
	private  boolean startsWithHeader;
	
	/**
	 * Stop reading when you hit an empty row.
	 */
	private  boolean stopOnEmpty;
	
	/**
	 * Avoid empty rows in the result.
	 */
	private boolean ignoreEmptyRows;
	
	/**
	 * The fieldname containing the row number.
	 * An empty (null) value means that no row number is included in the output.
	 * This is the rownumber of all written rows (not the row in the sheet).
	 */
	private  String  rowNumberField;

	/**
	 * The fieldname containing the sheet row number.
	 * An empty (null) value means that no sheet row number is included in the output.
	 * Sheet row number is the row number in the sheet.
	 */
	private  String  sheetRowNumberField;

	/**
	 * The maximum number of rows that this step writes to the next step.
	 */
	private  long    rowLimit;

	/**
	 * The fields to read in the range.
	 * Note: the number of columns in the range has to match field.length
	 */
	private  ExcelInputField field[];
	
    /** Strict types : will generate erros */
    private boolean strictTypes;
	
	/** Ignore error : turn into warnings */
    private boolean errorIgnored;
    
    /** If error line are skipped, you can replay without introducing doubles.*/
	private boolean errorLineSkipped;
	
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
	
    /** Are we accepting filenames in input rows?  */
    private boolean acceptingFilenames;
    
    /** The field in which the filename is placed */
    private String  acceptingField;

    /** The stepname to accept filenames from */
    private String  acceptingStepName;

    /** The step to accept filenames from */
    private StepMeta acceptingStep;
    
    /** The encoding to use for reading: null or empty string means system default encoding */
    private String encoding;

	public ExcelInputMeta()
	{
		super(); // allocate BaseStepMeta
	}

    /**
     * @return Returns the fieldLength.
     */
    public ExcelInputField[] getField()
    {
        return field;
    }

    /**
     * @param fields The excel input fields to set.
     */
    public void setField(ExcelInputField[] fields)
    {
        this.field = fields;
    }
    
    /**
     * @return Returns the fileField.
     */
    public String getFileField()
    {
        return fileField;
    }
    
    /**
     * @param fileField The fileField to set.
     */
    public void setFileField(String fileField)
    {
        this.fileField = fileField;
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
     * @return Returns the ignoreEmptyRows.
     */
    public boolean ignoreEmptyRows()
    {
        return ignoreEmptyRows;
    }
    
    /**
     * @param ignoreEmptyRows The ignoreEmptyRows to set.
     */
    public void setIgnoreEmptyRows(boolean ignoreEmptyRows)
    {
        this.ignoreEmptyRows = ignoreEmptyRows;
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
     * @return Returns the sheetRowNumberField.
     */
    public String getSheetRowNumberField()
    {
        return sheetRowNumberField;
    }
    
    /**
     * @param rowNumberField The rowNumberField to set.
     */
    public void setSheetRowNumberField(String rowNumberField)
    {
        this.sheetRowNumberField = rowNumberField;
    }

    /**
     * @return Returns the sheetField.
     */
    public String getSheetField()
    {
        return sheetField;
    }
    
    /**
     * @param sheetField The sheetField to set.
     */
    public void setSheetField(String sheetField)
    {
        this.sheetField = sheetField;
    }
    
    /**
     * @return Returns the sheetName.
     */
    public String[] getSheetName()
    {
        return sheetName;
    }
    
    /**
     * @param sheetName The sheetName to set.
     */
    public void setSheetName(String[] sheetName)
    {
        this.sheetName = sheetName;
    }
    
    /**
     * @return Returns the startColumn.
     */
    public int[] getStartColumn()
    {
        return startColumn;
    }
    
    /**
     * @param startColumn The startColumn to set.
     */
    public void setStartColumn(int[] startColumn)
    {
        this.startColumn = startColumn;
    }
    
    /**
     * @return Returns the startRow.
     */
    public int[] getStartRow()
    {
        return startRow;
    }
    
    /**
     * @param startRow The startRow to set.
     */
    public void setStartRow(int[] startRow)
    {
        this.startRow = startRow;
    }
    
    /**
     * @return Returns the startsWithHeader.
     */
    public boolean startsWithHeader()
    {
        return startsWithHeader;
    }
    
    /**
     * @param startsWithHeader The startsWithHeader to set.
     */
    public void setStartsWithHeader(boolean startsWithHeader)
    {
        this.startsWithHeader = startsWithHeader;
    }
    
    /**
     * @return Returns the stopOnEmpty.
     */
    public boolean stopOnEmpty()
    {
        return stopOnEmpty;
    }
    
    /**
     * @param stopOnEmpty The stopOnEmpty to set.
     */
    public void setStopOnEmpty(boolean stopOnEmpty)
    {
        this.stopOnEmpty = stopOnEmpty;
    }

	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		ExcelInputMeta retval = (ExcelInputMeta)super.clone();
		
		int nrfiles  = fileName.length;
		int nrsheets = sheetName.length;
		int nrfields = field.length;

		retval.allocate(nrfiles, nrsheets, nrfields);

		for (int i=0;i<nrfields;i++)
		{
			retval.field[i] = (ExcelInputField) field[i].clone();
		}

		for (int i=0;i<nrfiles;i++)
		{
			retval.fileName[i] = fileName[i];
			retval.fileMask[i] = fileMask[i];
		}

		for (int i=0;i<nrsheets;i++)
		{
			retval.sheetName[i] = sheetName[i];
		}

		return retval;
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			startsWithHeader          = YES.equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "header"));
			String nempty   = XMLHandler.getTagValue(stepnode, "noempty");
			ignoreEmptyRows         = YES.equalsIgnoreCase(nempty) || nempty==null;
			String soempty  = XMLHandler.getTagValue(stepnode, "stoponempty");
			stopOnEmpty     = YES.equalsIgnoreCase(soempty) || nempty==null;
			sheetRowNumberField = XMLHandler.getTagValue(stepnode, "sheetrownumfield");
			rowNumberField    = XMLHandler.getTagValue(stepnode, "rownum_field");
			rowNumberField    = XMLHandler.getTagValue(stepnode, "rownumfield");			
			rowLimit           = Const.toLong(XMLHandler.getTagValue(stepnode, "limit"), 0);
            encoding         = XMLHandler.getTagValue(stepnode, "encoding");
			sheetField      = XMLHandler.getTagValue(stepnode, "sheetfield");
			fileField       = XMLHandler.getTagValue(stepnode, "filefield");

            acceptingFilenames = YES.equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "accept_filenames"));
            acceptingField = XMLHandler.getTagValue(stepnode, "accept_field");
            acceptingStepName = XMLHandler.getTagValue(stepnode, "accept_stepname");

			Node filenode   = XMLHandler.getSubNode(stepnode, "file");
			Node sheetsnode = XMLHandler.getSubNode(stepnode, "sheets");
			Node fields     = XMLHandler.getSubNode(stepnode, "fields");
			int nrfiles   = XMLHandler.countNodes(filenode,   "name");
			int nrsheets  = XMLHandler.countNodes(sheetsnode, "sheet");
			int nrfields  = XMLHandler.countNodes(fields,     "field");
	
			allocate(nrfiles, nrsheets, nrfields);		
	
			for (int i=0;i<nrfiles;i++)
			{
				Node filenamenode = XMLHandler.getSubNodeByNr(filenode, "name", i); 
				Node filemasknode = XMLHandler.getSubNodeByNr(filenode, "filemask", i); 
				Node fileRequirednode = XMLHandler.getSubNodeByNr(filenode, "file_required", i);
				fileName[i] = XMLHandler.getNodeValue(filenamenode);
				fileMask[i] = XMLHandler.getNodeValue(filemasknode);
				fileRequired[i] = XMLHandler.getNodeValue(fileRequirednode);
			}
			
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				field[i] = new ExcelInputField();
				
				field[i].setName( XMLHandler.getTagValue(fnode, "name") );
				field[i].setType( Value.getType(XMLHandler.getTagValue(fnode, "type")) );
				field[i].setLength( Const.toInt(XMLHandler.getTagValue(fnode, "length"), -1) );
				field[i].setPrecision( Const.toInt(XMLHandler.getTagValue(fnode, "precision"), -1) );
				String srepeat      = XMLHandler.getTagValue(fnode, "repeat");
				field[i].setTrimType( getTrimTypeByCode(XMLHandler.getTagValue(fnode, "trim_type")) );
				
				if (srepeat!=null) field[i].setRepeated( YES.equalsIgnoreCase(srepeat) ); 
				else               field[i].setRepeated( false );
				
                field[i].setFormat(XMLHandler.getTagValue(fnode, "format"));
                field[i].setCurrencySymbol(XMLHandler.getTagValue(fnode, "currency"));
                field[i].setDecimalSymbol(XMLHandler.getTagValue(fnode, "decimal"));
                field[i].setGroupSymbol(XMLHandler.getTagValue(fnode, "group"));

			}
			
			for (int i=0;i<nrsheets;i++)
			{
				Node snode = XMLHandler.getSubNodeByNr(sheetsnode, "sheet", i);
				
				sheetName[i] = XMLHandler.getTagValue(snode, "name");
				startRow[i]  = Const.toInt(XMLHandler.getTagValue(snode, "startrow"), 0);
				startColumn[i]  = Const.toInt(XMLHandler.getTagValue(snode, "startcol"), 0);
			}
			
            strictTypes = YES.equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "strict_types") );
            errorIgnored = YES.equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "error_ignored") );
            errorLineSkipped = YES.equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "error_line_skipped") );
            warningFilesDestinationDirectory = XMLHandler.getTagValue(stepnode, "bad_line_files_destination_directory");
            warningFilesExtension = XMLHandler.getTagValue(stepnode, "bad_line_files_extension");
            errorFilesDestinationDirectory = XMLHandler.getTagValue(stepnode, "error_line_files_destination_directory");
            errorFilesExtension = XMLHandler.getTagValue(stepnode, "error_line_files_extension");
            lineNumberFilesDestinationDirectory = XMLHandler.getTagValue(stepnode, "line_number_files_destination_directory");
            lineNumberFilesExtension = XMLHandler.getTagValue(stepnode, "line_number_files_extension");
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to read step information from XML", e);
		}
	}
	
	public void allocate(int nrfiles, int nrsheets, int nrfields)
	{
		fileName   = new String [nrfiles];
		fileMask   = new String [nrfiles];
		fileRequired = new String[nrfiles];
		
		sheetName  = new String [nrsheets];
		startRow   = new int    [nrsheets];
		startColumn   = new int    [nrsheets];
		
		field = new ExcelInputField[nrfields];
	}
	
	public void setDefault()
	{
		startsWithHeader     = true;
		ignoreEmptyRows    = true;
		rowNumberField = "";
		sheetRowNumberField = "";
		
		int nrfiles=0;
		int nrfields=0;
		int nrsheets=0;

		allocate(nrfiles, nrsheets, nrfields);	
		
		for (int i=0;i<nrfiles;i++) 
		{
			fileName[i]="filename"+(i+1);
			fileMask[i]="";
			fileRequired[i] = NO;
		}
		
		for (int i=0;i<nrfields;i++)
		{
			field[i] = new ExcelInputField();
			field[i].setName( "field"+i );				
			field[i].setType( Value.VALUE_TYPE_NUMBER );
			field[i].setLength( 9 );
			field[i].setPrecision( 2 );
			field[i].setTrimType( TYPE_TRIM_NONE );
			field[i].setRepeated( false );
		}
			
		rowLimit=0L;
		
		strictTypes = false;
		errorIgnored = false;
		errorLineSkipped = false;
		warningFilesDestinationDirectory = null;
        warningFilesExtension = "warning";
        errorFilesDestinationDirectory = null;
        errorFilesExtension = "error";
        lineNumberFilesDestinationDirectory = null;
        lineNumberFilesExtension = "line";
	}
	
	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...
		
		int i;
		for (i=0;i<field.length;i++)
		{
			int type=field[i].getType();
			if (type==Value.VALUE_TYPE_NONE) type=Value.VALUE_TYPE_STRING;
			Value v=new Value(field[i].getName(), type);
			v.setLength(field[i].getLength(), field[i].getPrecision());
			v.setOrigin(name);
			row.addValue(v);
		}
		if (fileField!=null && fileField.length()>0)
		{
			Value v = new Value(fileField, Value.VALUE_TYPE_STRING);
			v.setLength(250, -1);
			v.setOrigin(name);
			row.addValue(v);
		}
		if (sheetField!=null && sheetField.length()>0)
		{
			Value v = new Value(sheetField, Value.VALUE_TYPE_STRING);
			v.setLength(250, -1);
			v.setOrigin(name);
			row.addValue(v);
		}
		if (sheetRowNumberField!=null && sheetRowNumberField.length()>0)
		{
			Value v = new Value(sheetRowNumberField, Value.VALUE_TYPE_NUMBER);
			v.setLength(7, 0);
			v.setOrigin(name);
			row.addValue(v);
		}
		if (rowNumberField!=null && rowNumberField.length()>0)
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
        StringBuffer retval = new StringBuffer();

		retval.append("    "+XMLHandler.addTagValue("header",          startsWithHeader));
		retval.append("    "+XMLHandler.addTagValue("noempty",         ignoreEmptyRows));
		retval.append("    "+XMLHandler.addTagValue("stoponempty",     stopOnEmpty));
		retval.append("    "+XMLHandler.addTagValue("filefield",       fileField));
		retval.append("    "+XMLHandler.addTagValue("sheetfield",      sheetField));
		retval.append("    "+XMLHandler.addTagValue("sheetrownumfield", sheetRowNumberField));		
		retval.append("    "+XMLHandler.addTagValue("rownumfield",     rowNumberField));
		retval.append("    "+XMLHandler.addTagValue("sheetfield",      sheetField));
		retval.append("    "+XMLHandler.addTagValue("filefield",       fileField));
		retval.append("    "+XMLHandler.addTagValue("limit",           rowLimit));
        retval.append("    "+XMLHandler.addTagValue("encoding",        encoding));

        retval.append("    " + XMLHandler.addTagValue("accept_filenames", acceptingFilenames));
        retval.append("    " + XMLHandler.addTagValue("accept_field", acceptingField));
        retval.append("    " + XMLHandler.addTagValue("accept_stepname", (acceptingStep!=null?acceptingStep.getName():"") ));

		/*
		 * Describe the files to read
		 */
		retval.append("    <file>"+Const.CR);
		for (int i=0;i<fileName.length;i++)
		{
			retval.append("      "+XMLHandler.addTagValue("name",     fileName[i]));
			retval.append("      "+XMLHandler.addTagValue("filemask", fileMask[i]));
			retval.append("      "+XMLHandler.addTagValue("file_required", fileRequired[i]));
		}
		retval.append("      </file>"+Const.CR);

		/*
		 * Describe the fields to read
		 */
		retval.append("    <fields>"+Const.CR);
		for (int i=0;i<field.length;i++)
		{
			retval.append("      <field>"+Const.CR);
			retval.append("        "+XMLHandler.addTagValue("name",      field[i].getName()) );
			retval.append("        "+XMLHandler.addTagValue("type",      field[i].getTypeDesc()) );
			retval.append("        "+XMLHandler.addTagValue("length",    field[i].getLength()) );
			retval.append("        "+XMLHandler.addTagValue("precision", field[i].getPrecision()));
			retval.append("        "+XMLHandler.addTagValue("trim_type", field[i].getTrimTypeCode() ) );
			retval.append("        "+XMLHandler.addTagValue("repeat",    field[i].isRepeated()) );

            retval.append("        " + XMLHandler.addTagValue("format", field[i].getFormat()));
            retval.append("        " + XMLHandler.addTagValue("currency", field[i].getCurrencySymbol()));
            retval.append("        " + XMLHandler.addTagValue("decimal", field[i].getDecimalSymbol()));
            retval.append("        " + XMLHandler.addTagValue("group", field[i].getGroupSymbol()));

			retval.append("        </field>"+Const.CR);
		}
		retval.append("      </fields>"+Const.CR);

		/*
		 * Describe the sheets to load... 
		 */
		retval.append("    <sheets>"+Const.CR);
		for (int i=0;i<sheetName.length;i++)
		{
			retval.append("      <sheet>"+Const.CR);
			retval.append("        "+XMLHandler.addTagValue("name",      sheetName[i]));
			retval.append("        "+XMLHandler.addTagValue("startrow",  startRow[i]));
			retval.append("        "+XMLHandler.addTagValue("startcol",  startColumn[i]));
			retval.append("        </sheet>"+Const.CR);
		}
		retval.append("      </sheets>"+Const.CR);
		
        // ERROR HANDLING
        retval.append("    " + XMLHandler.addTagValue("strict_types", strictTypes));
        retval.append("    " + XMLHandler.addTagValue("error_ignored", errorIgnored));
        retval.append("    " + XMLHandler.addTagValue("error_line_skipped", errorLineSkipped));
        
        retval.append("    " + XMLHandler.addTagValue("bad_line_files_destination_directory", warningFilesDestinationDirectory));
        retval.append("    " + XMLHandler.addTagValue("bad_line_files_extension", warningFilesExtension));
        retval.append("    " + XMLHandler.addTagValue("error_line_files_destination_directory", errorFilesDestinationDirectory));
        retval.append("    " + XMLHandler.addTagValue("error_line_files_extension", errorFilesExtension));
        retval.append("    " + XMLHandler.addTagValue("line_number_files_destination_directory", lineNumberFilesDestinationDirectory));
        retval.append("    " + XMLHandler.addTagValue("line_number_files_extension", lineNumberFilesExtension));
		
		return retval.toString();
	}
	
	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			startsWithHeader  =      rep.getStepAttributeBoolean(id_step, "header");
			ignoreEmptyRows   =      rep.getStepAttributeBoolean(id_step, "noempty");  
			stopOnEmpty       =      rep.getStepAttributeBoolean(id_step, "stoponempty");  
			fileField         =      rep.getStepAttributeString (id_step, "filefield");
			sheetField        =      rep.getStepAttributeString (id_step, "sheetfield");
			sheetRowNumberField =    rep.getStepAttributeString (id_step, "sheetrownumfield");
			rowNumberField    =      rep.getStepAttributeString (id_step, "rownumfield");
			rowLimit          = (int)rep.getStepAttributeInteger(id_step, "limit");
            encoding          =      rep.getStepAttributeString (id_step, "encoding");

            acceptingFilenames = rep.getStepAttributeBoolean(id_step, "accept_filenames");
            acceptingField     = rep.getStepAttributeString (id_step, "accept_field");
            acceptingStepName  = rep.getStepAttributeString (id_step, "accept_stepname");

			int nrfiles     = rep.countNrStepAttributes(id_step, "file_name");
			int nrsheets    = rep.countNrStepAttributes(id_step, "sheet_name");
			int nrfields    = rep.countNrStepAttributes(id_step, "field_name");
			
			allocate(nrfiles, nrsheets, nrfields);

            // System.out.println("Counted "+nrfiles+" files to read and "+nrsheets+" sheets, "+nrfields+" fields.");
			for (int i=0;i<nrfiles;i++)
			{
				fileName[i] =      rep.getStepAttributeString (id_step, i, "file_name"    );
				fileMask[i] =      rep.getStepAttributeString (id_step, i, "file_mask"    );
				fileRequired[i] = rep.getStepAttributeString(id_step, i, "file_required");
                if(!YES.equalsIgnoreCase(fileRequired[i]))
                	fileRequired[i] = NO;
			}

			for (int i=0;i<nrsheets;i++)
			{
				sheetName[i] =      rep.getStepAttributeString (id_step, i, "sheet_name"      );
				startRow[i]  = (int)rep.getStepAttributeInteger(id_step, i, "sheet_startrow"  );
				startColumn[i]  = (int)rep.getStepAttributeInteger(id_step, i, "sheet_startcol"  );
			}

			for (int i=0;i<nrfields;i++)
			{
				field[i] = new ExcelInputField();
				
				field[i].setName( rep.getStepAttributeString (id_step, i, "field_name") );
				field[i].setType( Value.getType( rep.getStepAttributeString (id_step, i, "field_type") ) );
				field[i].setLength( (int)rep.getStepAttributeInteger(id_step, i, "field_length") );
				field[i].setPrecision( (int)rep.getStepAttributeInteger(id_step, i, "field_precision") );
				field[i].setTrimType( getTrimTypeByCode(   rep.getStepAttributeString (id_step, i, "field_trim_type") ) );
				field[i].setRepeated( rep.getStepAttributeBoolean(id_step, i, "field_repeat") );
				
                field[i].setFormat(rep.getStepAttributeString(id_step, i, "field_format"));
                field[i].setCurrencySymbol(rep.getStepAttributeString(id_step, i, "field_currency"));
                field[i].setDecimalSymbol(rep.getStepAttributeString(id_step, i, "field_decimal"));
                field[i].setGroupSymbol(rep.getStepAttributeString(id_step, i, "field_group"));
			}		
			
            strictTypes = rep.getStepAttributeBoolean(id_step, 0, "strict_types", false);
			errorIgnored = rep.getStepAttributeBoolean(id_step, 0, "error_ignored", false);
            errorLineSkipped = rep.getStepAttributeBoolean(id_step, 0, "error_line_skipped", false);
            
            warningFilesDestinationDirectory = rep.getStepAttributeString(id_step, "bad_line_files_dest_dir");
            warningFilesExtension = rep.getStepAttributeString(id_step, "bad_line_files_ext");
            errorFilesDestinationDirectory = rep.getStepAttributeString(id_step, "error_line_files_dest_dir");
            errorFilesExtension = rep.getStepAttributeString(id_step, "error_line_files_ext");
            lineNumberFilesDestinationDirectory = rep.getStepAttributeString(id_step, "line_number_files_dest_dir");
            lineNumberFilesExtension = rep.getStepAttributeString(id_step, "line_number_files_ext");
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "header",          startsWithHeader);
			rep.saveStepAttribute(id_transformation, id_step, "noempty",         ignoreEmptyRows);
			rep.saveStepAttribute(id_transformation, id_step, "stoponempty",     stopOnEmpty);
			rep.saveStepAttribute(id_transformation, id_step, "filefield",       fileField);
			rep.saveStepAttribute(id_transformation, id_step, "sheetfield",      sheetField);
			rep.saveStepAttribute(id_transformation, id_step, "sheetrownumfield", sheetRowNumberField);
			rep.saveStepAttribute(id_transformation, id_step, "rownumfield",     rowNumberField);			
			rep.saveStepAttribute(id_transformation, id_step, "limit",           rowLimit);
            rep.saveStepAttribute(id_transformation, id_step, "encoding",        encoding);

            rep.saveStepAttribute(id_transformation, id_step, "accept_filenames", acceptingFilenames);
            rep.saveStepAttribute(id_transformation, id_step, "accept_field", acceptingField);
            rep.saveStepAttribute(id_transformation, id_step, "accept_stepname", (acceptingStep!=null?acceptingStep.getName():"") );

			for (int i=0;i<fileName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "file_name",     fileName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_mask",     fileMask[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_required", fileRequired[i]);
			}
	
			for (int i=0;i<sheetName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "sheet_name",      sheetName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "sheet_startrow",  startRow[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "sheet_startcol",  startColumn[i]);
			}
	
			for (int i=0;i<field.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      field[i].getName() );
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      field[i].getTypeDesc() );
				rep.saveStepAttribute(id_transformation, id_step, i, "field_length",    field[i].getLength() );
				rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", field[i].getPrecision() );
				rep.saveStepAttribute(id_transformation, id_step, i, "field_trim_type", field[i].getTrimTypeCode() );
				rep.saveStepAttribute(id_transformation, id_step, i, "field_repeat",    field[i].isRepeated());
				
                rep.saveStepAttribute(id_transformation, id_step, i, "field_format", field[i].getFormat());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_currency", field[i].getCurrencySymbol());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal", field[i].getDecimalSymbol());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_group", field[i].getGroupSymbol());

			}
			
            rep.saveStepAttribute(id_transformation, id_step, "strict_types", strictTypes);
			rep.saveStepAttribute(id_transformation, id_step, "error_ignored", errorIgnored);
            rep.saveStepAttribute(id_transformation, id_step, "error_line_skipped", errorLineSkipped);
            
            rep.saveStepAttribute(id_transformation, id_step, "bad_line_files_dest_dir", warningFilesDestinationDirectory);
            rep.saveStepAttribute(id_transformation, id_step, "bad_line_files_ext", warningFilesExtension);
            rep.saveStepAttribute(id_transformation, id_step, "error_line_files_dest_dir", errorFilesDestinationDirectory);
            rep.saveStepAttribute(id_transformation, id_step, "error_line_files_ext", errorFilesExtension);
            rep.saveStepAttribute(id_transformation, id_step, "line_number_files_dest_dir", lineNumberFilesDestinationDirectory);
            rep.saveStepAttribute(id_transformation, id_step, "line_number_files_ext", lineNumberFilesExtension);
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}

	}
	
  public final static int getTrimTypeByCode(String tt)
	{
		if (tt==null) return 0;
		
		for (int i=0;i<type_trim_code.length;i++)
		{
			if (type_trim_code[i].equalsIgnoreCase(tt)) return i;
		}
		return 0;
	}
  
	public final static int getTrimTypeByDesc(String tt)
	{
		if (tt==null) return 0;
		
		for (int i=0;i<type_trim_desc.length;i++)
		{
			if (type_trim_desc[i].equalsIgnoreCase(tt)) return i;
		}
		return 0;
	}

  public final static String getTrimTypeCode(int i)
	{
		if (i<0 || i>=type_trim_code.length) return type_trim_code[0];
		return type_trim_code[i];	
	}
  
	public final static String getTrimTypeDesc(int i)
	{
		if (i<0 || i>=type_trim_desc.length) return type_trim_desc[0];
		return type_trim_desc[i];	
	}
	
	public String[] getFilePaths()
    {
    	return FileInputList.createFilePathList(fileName, fileMask, fileRequired);
    }
    
    public FileInputList getFileList()
    {
    	return FileInputList.createFileList(fileName, fileMask, fileRequired);
    }
	
    public String getLookupStepname()
    {
        if (acceptingFilenames &&
            acceptingStep!=null && 
            !Const.isEmpty( acceptingStep.getName() )
           ) 
            return acceptingStep.getName();
        return null;
    }

    public void searchInfoAndTargetSteps(ArrayList steps)
    {
        acceptingStep = StepMeta.findStep(steps, acceptingStepName);
    }

    public String[] getInfoSteps()
    {
        if (acceptingFilenames && acceptingStep!=null)
        {
            return new String[] { acceptingStep.getName() };
        }
        return super.getInfoSteps();
    }

	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;

		// See if we get input...
		if (input.length>0)
		{		
			if ( !isAcceptingFilenames() )
			{
			    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("ExcelInputMeta.CheckResult.NoInputError"), stepinfo);
	  		    remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ExcelInputMeta.CheckResult.AcceptFilenamesOk"), stepinfo);
			    remarks.add(cr);
			}
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ExcelInputMeta.CheckResult.NoInputOk"), stepinfo);
			remarks.add(cr);
		}
		
		FileInputList fileList = getFileList();
		if (fileList.nrOfFiles() == 0)
		{
			if ( ! isAcceptingFilenames() )
			{
			    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("ExcelInputMeta.CheckResult.ExpectedFilesError"), stepinfo);
   			    remarks.add(cr);
			}
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ExcelInputMeta.CheckResult.ExpectedFilesOk", ""+fileList.nrOfFiles()), stepinfo);
			remarks.add(cr);
		}
	}
	
	public Row getEmptyFields()
	{
		Row row = new Row();
		for (int i=0;i<field.length;i++)
		{
			Value v = new Value(field[i].getName(), field[i].getType());
			row.addValue(v);
		}
		
		return row;
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new ExcelInputDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new ExcelInput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new ExcelInputData();
	}

	public String getWarningFilesDestinationDirectory() {
		return warningFilesDestinationDirectory;
	}

	public void setWarningFilesDestinationDirectory(
			String badLineFilesDestinationDirectory) {
		this.warningFilesDestinationDirectory = badLineFilesDestinationDirectory;
	}

	public String getBadLineFilesExtension() {
		return warningFilesExtension;
	}

	public void setBadLineFilesExtension(String badLineFilesExtension) {
		this.warningFilesExtension = badLineFilesExtension;
	}

	public boolean isErrorIgnored() {
		return errorIgnored;
	}

	public void setErrorIgnored(boolean errorIgnored) {
		this.errorIgnored = errorIgnored;
	}

	public String getErrorFilesDestinationDirectory() {
		return errorFilesDestinationDirectory;
	}

	public void setErrorFilesDestinationDirectory(
			String errorLineFilesDestinationDirectory) {
		this.errorFilesDestinationDirectory = errorLineFilesDestinationDirectory;
	}

	public String getErrorFilesExtension() {
		return errorFilesExtension;
	}

	public void setErrorFilesExtension(String errorLineFilesExtension) {
		this.errorFilesExtension = errorLineFilesExtension;
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

	public boolean isErrorLineSkipped() {
		return errorLineSkipped;
	}

	public void setErrorLineSkipped(boolean errorLineSkipped) {
		this.errorLineSkipped = errorLineSkipped;
	}
	
	public boolean isStrictTypes() {
		return strictTypes;
	}

	public void setStrictTypes(boolean strictTypes) {
		this.strictTypes = strictTypes;
	}

	public String[] getFileRequired() {
		return fileRequired;
	}

	public void setFileRequired(String[] fileRequired) {
		this.fileRequired = fileRequired;
	}

    /**
     * @return Returns the acceptingField.
     */
    public String getAcceptingField()
    {
        return acceptingField;
    }

    /**
     * @param acceptingField The acceptingField to set.
     */
    public void setAcceptingField(String acceptingField)
    {
        this.acceptingField = acceptingField;
    }

    /**
     * @return Returns the acceptingFilenames.
     */
    public boolean isAcceptingFilenames()
    {
        return acceptingFilenames;
    }

    /**
     * @param acceptingFilenames The acceptingFilenames to set.
     */
    public void setAcceptingFilenames(boolean acceptingFilenames)
    {
        this.acceptingFilenames = acceptingFilenames;
    }

    /**
     * @return Returns the acceptingStep.
     */
    public StepMeta getAcceptingStep()
    {
        return acceptingStep;
    }

    /**
     * @param acceptingStep The acceptingStep to set.
     */
    public void setAcceptingStep(StepMeta acceptingStep)
    {
        this.acceptingStep = acceptingStep;
    }

    /**
     * @return Returns the acceptingStepName.
     */
    public String getAcceptingStepName()
    {
        return acceptingStepName;
    }

    /**
     * @param acceptingStepName The acceptingStepName to set.
     */
    public void setAcceptingStepName(String acceptingStepName)
    {
        this.acceptingStepName = acceptingStepName;
    }
    
    public String[] getUsedLibraries()
    {
        return new String[] { "jxl.jar", };
    }

    /**
     * @return the encoding
     */
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }
    
    /**
     * Read all sheets if the sheet names are left blank. 
     * @return true if all sheets are read.
     */
    public boolean readAllSheets()
	{
		return Const.isEmpty(sheetName) || ( sheetName.length==1 && Const.isEmpty(sheetName[0]) );
	}
}