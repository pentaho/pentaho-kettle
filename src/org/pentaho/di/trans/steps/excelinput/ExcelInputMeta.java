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


/* 
 * 
 * Created on 4-apr-2003
 * 
 */

package org.pentaho.di.trans.steps.excelinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceNamingInterface.FileNamingType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/**
 * Meta data for the Excel step.
 */
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
    
    /** The add filenames to result filenames flag */
    private boolean isaddresult;

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

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
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
			retval.fileName[i]     = fileName[i];
			retval.fileMask[i]     = fileMask[i];
			retval.fileRequired[i] = fileRequired[i];
		}

		for (int i=0;i<nrsheets;i++)
		{
			retval.sheetName[i] = sheetName[i];
      retval.startColumn[i] = startColumn[i];
      retval.startRow[i] = startRow[i];
    }

		return retval;
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			startsWithHeader    = YES.equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "header"));
			String nempty       = XMLHandler.getTagValue(stepnode, "noempty");
			ignoreEmptyRows     = YES.equalsIgnoreCase(nempty) || nempty==null;
			String soempty      = XMLHandler.getTagValue(stepnode, "stoponempty");
			stopOnEmpty         = YES.equalsIgnoreCase(soempty) || nempty==null;
			sheetRowNumberField = XMLHandler.getTagValue(stepnode, "sheetrownumfield");
			rowNumberField      = XMLHandler.getTagValue(stepnode, "rownum_field");
			rowNumberField      = XMLHandler.getTagValue(stepnode, "rownumfield");			
			rowLimit            = Const.toLong(XMLHandler.getTagValue(stepnode, "limit"), 0);
            encoding            = XMLHandler.getTagValue(stepnode, "encoding");
            String addToResult=XMLHandler.getTagValue(stepnode,  "add_to_result_filenames");
			if(Const.isEmpty(addToResult)) 
				isaddresult = true;
			else
				isaddresult = "Y".equalsIgnoreCase(addToResult);
            sheetField          = XMLHandler.getTagValue(stepnode, "sheetfield");
			fileField           = XMLHandler.getTagValue(stepnode, "filefield");

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
				field[i].setType( ValueMeta.getType(XMLHandler.getTagValue(fnode, "type")) );
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
		fileName      = new String[nrfiles];
		fileMask      = new String[nrfiles];
		fileRequired  = new String[nrfiles];
		
		sheetName     = new String[nrsheets];
		startRow      = new int   [nrsheets];
		startColumn   = new int   [nrsheets];
		
		field         = new ExcelInputField[nrfields];
	}
	
	public void setDefault()
	{
		startsWithHeader     = true;
		ignoreEmptyRows    = true;
		rowNumberField = "";
		sheetRowNumberField = "";
		isaddresult=true;
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
			field[i].setType( ValueMetaInterface.TYPE_NUMBER );
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
    
	public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		for (int i=0;i<field.length;i++)
		{
			int type=field[i].getType();
			if (type==ValueMetaInterface.TYPE_NONE) type=ValueMetaInterface.TYPE_STRING;
            ValueMetaInterface v=new ValueMeta(field[i].getName(), type);
			v.setLength(field[i].getLength());
            v.setPrecision(field[i].getPrecision());
			v.setOrigin(name);
            v.setConversionMask(field[i].getFormat());
            v.setDecimalSymbol(field[i].getDecimalSymbol());
            v.setGroupingSymbol(field[i].getGroupSymbol());
            v.setCurrencySymbol(field[i].getCurrencySymbol());
			row.addValueMeta(v);
		}
		if (fileField!=null && fileField.length()>0)
		{
			ValueMetaInterface v = new ValueMeta(fileField, ValueMetaInterface.TYPE_STRING);
			v.setLength(250);
            v.setPrecision(-1);
			v.setOrigin(name);
			row.addValueMeta(v);
		}
		if (sheetField!=null && sheetField.length()>0)
		{
            ValueMetaInterface v = new ValueMeta(sheetField, ValueMetaInterface.TYPE_STRING);
            v.setLength(250);
            v.setPrecision(-1);
			v.setOrigin(name);
			row.addValueMeta(v);
		}
		if (sheetRowNumberField!=null && sheetRowNumberField.length()>0)
		{
            ValueMetaInterface v = new ValueMeta(sheetRowNumberField, ValueMetaInterface.TYPE_INTEGER);
            v.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
			v.setOrigin(name);
			row.addValueMeta(v);
		}
		if (rowNumberField!=null && rowNumberField.length()>0)
		{
            ValueMetaInterface v = new ValueMeta(rowNumberField, ValueMetaInterface.TYPE_INTEGER);
            v.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
			v.setOrigin(name);
			row.addValueMeta(v);
		}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(1024);

		retval.append("    ").append(XMLHandler.addTagValue("header",          startsWithHeader));
		retval.append("    ").append(XMLHandler.addTagValue("noempty",         ignoreEmptyRows));
		retval.append("    ").append(XMLHandler.addTagValue("stoponempty",     stopOnEmpty));
		retval.append("    ").append(XMLHandler.addTagValue("filefield",       fileField));
		retval.append("    ").append(XMLHandler.addTagValue("sheetfield",      sheetField));
		retval.append("    ").append(XMLHandler.addTagValue("sheetrownumfield", sheetRowNumberField));		
		retval.append("    ").append(XMLHandler.addTagValue("rownumfield",     rowNumberField));
		retval.append("    ").append(XMLHandler.addTagValue("sheetfield",      sheetField));
		retval.append("    ").append(XMLHandler.addTagValue("filefield",       fileField));
		retval.append("    ").append(XMLHandler.addTagValue("limit",           rowLimit));
        retval.append("    ").append(XMLHandler.addTagValue("encoding",        encoding));
        retval.append("    "+XMLHandler.addTagValue("add_to_result_filenames",   isaddresult));

        retval.append("    ").append(XMLHandler.addTagValue("accept_filenames", acceptingFilenames));
        retval.append("    ").append(XMLHandler.addTagValue("accept_field", acceptingField));
        retval.append("    ").append(XMLHandler.addTagValue("accept_stepname", (acceptingStep!=null?acceptingStep.getName():"") ));

		/*
		 * Describe the files to read
		 */
		retval.append("    <file>").append(Const.CR);
		for (int i=0;i<fileName.length;i++)
		{
			retval.append("      ").append(XMLHandler.addTagValue("name",     fileName[i]));
			retval.append("      ").append(XMLHandler.addTagValue("filemask", fileMask[i]));
			retval.append("      ").append(XMLHandler.addTagValue("file_required", fileRequired[i]));
		}
		retval.append("    </file>").append(Const.CR);

		/*
		 * Describe the fields to read
		 */
		retval.append("    <fields>").append(Const.CR);
		for (int i=0;i<field.length;i++)
		{
			retval.append("      <field>").append(Const.CR);
			retval.append("        ").append(XMLHandler.addTagValue("name",      field[i].getName()) );
			retval.append("        ").append(XMLHandler.addTagValue("type",      field[i].getTypeDesc()) );
			retval.append("        ").append(XMLHandler.addTagValue("length",    field[i].getLength()) );
			retval.append("        ").append(XMLHandler.addTagValue("precision", field[i].getPrecision()));
			retval.append("        ").append(XMLHandler.addTagValue("trim_type", field[i].getTrimTypeCode() ) );
			retval.append("        ").append(XMLHandler.addTagValue("repeat",    field[i].isRepeated()) );

            retval.append("        ").append(XMLHandler.addTagValue("format", field[i].getFormat()));
            retval.append("        ").append(XMLHandler.addTagValue("currency", field[i].getCurrencySymbol()));
            retval.append("        ").append(XMLHandler.addTagValue("decimal", field[i].getDecimalSymbol()));
            retval.append("        ").append(XMLHandler.addTagValue("group", field[i].getGroupSymbol()));

			retval.append("      </field>").append(Const.CR);
		}
		retval.append("    </fields>").append(Const.CR);

		/*
		 * Describe the sheets to load... 
		 */
		retval.append("    <sheets>").append(Const.CR);
		for (int i=0;i<sheetName.length;i++)
		{
			retval.append("      <sheet>").append(Const.CR);
			retval.append("        ").append(XMLHandler.addTagValue("name",      sheetName[i]));
			retval.append("        ").append(XMLHandler.addTagValue("startrow",  startRow[i]));
			retval.append("        ").append(XMLHandler.addTagValue("startcol",  startColumn[i]));
			retval.append("        </sheet>").append(Const.CR);
		}
		retval.append("    </sheets>").append(Const.CR);
		
        // ERROR HANDLING
        retval.append("    ").append(XMLHandler.addTagValue("strict_types", strictTypes));
        retval.append("    ").append(XMLHandler.addTagValue("error_ignored", errorIgnored));
        retval.append("    ").append(XMLHandler.addTagValue("error_line_skipped", errorLineSkipped));
        
        retval.append("    ").append(XMLHandler.addTagValue("bad_line_files_destination_directory", warningFilesDestinationDirectory));
        retval.append("    ").append(XMLHandler.addTagValue("bad_line_files_extension", warningFilesExtension));
        retval.append("    ").append(XMLHandler.addTagValue("error_line_files_destination_directory", errorFilesDestinationDirectory));
        retval.append("    ").append(XMLHandler.addTagValue("error_line_files_extension", errorFilesExtension));
        retval.append("    ").append(XMLHandler.addTagValue("line_number_files_destination_directory", lineNumberFilesDestinationDirectory));
        retval.append("    ").append(XMLHandler.addTagValue("line_number_files_extension", lineNumberFilesExtension));
		
		return retval.toString();
	}
	
	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
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
            String addToResult=rep.getStepAttributeString (id_step, "add_to_result_filenames");
			if(Const.isEmpty(addToResult)) 
				isaddresult = true;
			else
				isaddresult =  rep.getStepAttributeBoolean(id_step, "add_to_result_filenames");
			
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
				fileName[i]     =      rep.getStepAttributeString (id_step, i, "file_name");
				fileMask[i]     =      rep.getStepAttributeString (id_step, i, "file_mask");
				fileRequired[i] = rep.getStepAttributeString(id_step, i, "file_required");
                if(!YES.equalsIgnoreCase(fileRequired[i]))
                	fileRequired[i] = NO;
			}

			for (int i=0;i<nrsheets;i++)
			{
				sheetName[i]   =      rep.getStepAttributeString (id_step, i, "sheet_name"      );
				startRow[i]    = (int)rep.getStepAttributeInteger(id_step, i, "sheet_startrow"  );
				startColumn[i] = (int)rep.getStepAttributeInteger(id_step, i, "sheet_startcol"  );
			}

			for (int i=0;i<nrfields;i++)
			{
				field[i] = new ExcelInputField();
				
				field[i].setName( rep.getStepAttributeString (id_step, i, "field_name") );
				field[i].setType( ValueMeta.getType( rep.getStepAttributeString (id_step, i, "field_type") ) );
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
            rep.saveStepAttribute(id_transformation, id_step, "add_to_result_filenames",    isaddresult);

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
		if (tt!=null) 
		{		
		    for (int i=0;i<type_trim_code.length;i++)
		    {
			    if (type_trim_code[i].equalsIgnoreCase(tt)) return i;
		    }
		}
		return 0;
	}
  
	public final static int getTrimTypeByDesc(String tt)
	{
		if (tt!=null)
		{		
		    for (int i=0;i<type_trim_desc.length;i++)
		    {
			    if (type_trim_desc[i].equalsIgnoreCase(tt)) return i;
		    }
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
	
	public String[] getFilePaths(VariableSpace space)
    {
    	return FileInputList.createFilePathList(space, fileName, fileMask, fileRequired);
    }
    
    public FileInputList getFileList(VariableSpace space)
    {
    	return FileInputList.createFileList(space, fileName, fileMask, fileRequired);
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

    public void searchInfoAndTargetSteps(List<StepMeta> steps)
    {
        acceptingStep = StepMeta.findStep(steps, acceptingStepName);
    }

    public String[] getInfoSteps()
    {
        return super.getInfoSteps();
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;

		// See if we get input...
		if (input.length>0)
		{		
			if ( !isAcceptingFilenames() )
			{
			    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("ExcelInputMeta.CheckResult.NoInputError"), stepMeta);
	  		    remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("ExcelInputMeta.CheckResult.AcceptFilenamesOk"), stepMeta);
			    remarks.add(cr);
			}
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("ExcelInputMeta.CheckResult.NoInputOk"), stepMeta);
			remarks.add(cr);
		}
		
		FileInputList fileList = getFileList(transMeta);
		if (fileList.nrOfFiles() == 0)
		{
			if ( ! isAcceptingFilenames() )
			{
			    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("ExcelInputMeta.CheckResult.ExpectedFilesError"), stepMeta);
   			    remarks.add(cr);
			}
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("ExcelInputMeta.CheckResult.ExpectedFilesOk", ""+fileList.nrOfFiles()), stepMeta);
			remarks.add(cr);
		}
	}
	
	public RowMetaInterface getEmptyFields()
	{
		RowMetaInterface row = new RowMeta();
		for (int i=0;i<field.length;i++)
		{
			ValueMetaInterface v = new ValueMeta(field[i].getName(), field[i].getType());
			row.addValueMeta(v);
		}
		
		return row;
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
     * @param isaddresult The isaddresult to set.
     */
    public void setAddResultFile(boolean isaddresult)
    {
        this.isaddresult = isaddresult;
    }
    
    /**
     *  @return Returns isaddresult.
     */
    public boolean isAddResultFile()
    {
        return isaddresult;
    }
    
    /**
     * Read all sheets if the sheet names are left blank. 
     * @return true if all sheets are read.
     */
    public boolean readAllSheets()
	{
		return Const.isEmpty(sheetName) || ( sheetName.length==1 && Const.isEmpty(sheetName[0]) );
	}
	
	/**
	 * Since the exported transformation that runs this will reside in a ZIP file, we can't reference files relatively.
	 * So what this does is turn the name of files into absolute paths OR it simply includes the resource in the ZIP file.
	 * For now, we'll simply turn it into an absolute path and pray that the file is on a shared drive or something like that.

	 * TODO: create options to configure this behavior 
	 */
	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface resourceNamingInterface, Repository repository) throws KettleException {
		try {
			// The object that we're modifying here is a copy of the original!
			// So let's change the filename from relative to absolute by grabbing the file object...
			// In case the name of the file comes from previous steps, forget about this!
			//
            List<FileObject> newFiles = new ArrayList<FileObject>();
			
			if (!acceptingFilenames) {
				FileInputList fileList = getFileList(space);
				if (fileList.getFiles().size()>0) {
					for (FileObject fileObject : fileList.getFiles()) {
						// From : ${Internal.Transformation.Filename.Directory}/../foo/bar.xls
						// To   : /home/matt/test/files/foo/bar.xls
						//
						// If the file doesn't exist, forget about this effort too!
						//
						if (fileObject.exists()) {
							// Convert to an absolute path and add it to the list.
							// 
							newFiles.add(fileObject);
						}
					}
					
					// Still here: set a new list of absolute filenames!
					//
					fileName = new String[newFiles.size()];
					fileMask = new String[newFiles.size()]; // all null since converted to absolute path.
					fileRequired = new String[newFiles.size()]; // all null, turn to "Y" :
					
					for (int i=0;i<newFiles.size();i++) {
					  FileObject fileObject = newFiles.get(i);
					  fileName[i] = resourceNamingInterface.nameResource(
					      fileObject.getName().getBaseName(), 
					      fileObject.getParent().getName().getPath(), 
					      space.toString(), 
					      FileNamingType.DATA_FILE);
					  fileRequired[i]="Y";
					}
				}
			}
			return null;
		} catch (Exception e) {
			throw new KettleException(e); //$NON-NLS-1$
		}
	}

}