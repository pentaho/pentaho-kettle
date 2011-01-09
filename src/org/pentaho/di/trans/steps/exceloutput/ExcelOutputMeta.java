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

package org.pentaho.di.trans.steps.exceloutput;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


/**
 * Metadata of the Excel Output step.
 * @author Matt
 * @since on 6-sep-2006
 */

public class ExcelOutputMeta extends BaseStepMeta  implements StepMetaInterface
{
	private static Class<?> PKG = ExcelOutputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** The base name of the output file */
	private  String fileName;

	/** The file extention in case of a generated filename */
	private  String  extension;

	
	/** The password to protect the sheet */
	private  String  password;


	/** Add a header at the top of the file? */
    private  boolean headerEnabled;
	
	/** Add a footer at the bottom of the file? */
    private  boolean footerEnabled;
	
	/** if this value is larger then 0, the text file is split up into parts of this number of lines */
    private  int    splitEvery;

	/** Flag: add the stepnr in the filename */
    private  boolean stepNrInFilename;
	
	/** Flag: add the date in the filename */
    private  boolean dateInFilename;
    
	/** Flag: add the filenames to result filenames */
    private boolean addToResultFilenames;

	/** Flag: protect the sheet */
	private  boolean protectsheet;
	
	/** Flag: add the time in the filename */
    private  boolean timeInFilename;
    
	/** Flag: use a template */
    private  boolean templateEnabled;
    
    /** the excel template */
    private  String templateFileName;

	/** Flag: append when template */
    private  boolean templateAppend;

	/** the excel sheet name */
	private  String sheetname;
    
	/* THE FIELD SPECIFICATIONS ... */
	
	/** The output fields */
    private  ExcelField outputFields[];

    /** The encoding to use for reading: null or empty string means system default encoding */
    private String encoding;
    
    /** Calculated value ... */
    private  String newline;
    
	/** Flag : append workbook? */
    private  boolean append;
    
    /** Flag : Do not open new file when transformation start  */ 
    private boolean doNotOpenNewFileInit;
    
    /** Flag: create parent folder when necessary */
    private boolean createparentfolder;
    
    private boolean SpecifyFormat;
    
    private String date_time_format;
    
	/** Flag : auto size columns? */
    private  boolean autosizecolums;
    
    /** Flag : write null field values as blank Excel cells? */
    private  boolean nullIsBlank;

	public ExcelOutputMeta()
	{
		super(); // allocate BaseStepMeta
	}
	  /**
     * @return Returns the createparentfolder.
     */
    public boolean isCreateParentFolder()
    {
        return createparentfolder;
    }


    /**
     * @param createparentfolder The createparentfolder to set.
     */
    public void setCreateParentFolder(boolean createparentfolder)
    {
        this.createparentfolder = createparentfolder;
    }
	/**
     * @return Returns the dateInFilename.
     */
    public boolean isDateInFilename()
    {
        return dateInFilename;
    }

    /**
     * @param dateInFilename The dateInFilename to set.
     */
    public void setDateInFilename(boolean dateInFilename)
    {
        this.dateInFilename = dateInFilename;
    }

    /**
     * @return Returns the extension.
     */
    public String getExtension()
    {
        return extension;
    }

    /**
     * @param extension The extension to set.
     */
    public void setExtension(String extension)
    {
        this.extension = extension;
    }

    /**
     * @return Returns the fileName.
     */
    public String getFileName()
    {
        return fileName;
    }

	/**
	 * @return Returns the password.
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @return Returns the sheet name.
	 */
	public String getSheetname()
	{
		return sheetname;
	}

	/**
	 * @param sheetname The sheet name.
	 */
	public void setSheetname(String sheetname)
	{
		this.sheetname = sheetname;
	}

    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

	/**
	 * @param password teh passwoed to set.
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

    /**
     * @return Returns the footer.
     */
    public boolean isFooterEnabled()
    {
        return footerEnabled;
    }

    /**
     * @param footer The footer to set.
     */
    public void setFooterEnabled(boolean footer)
    {
        this.footerEnabled = footer;
    }
    /**
     * @return Returns the autosizecolums.
     */
    public boolean isAutoSizeColums()
    {
        return autosizecolums;
    }  
    
    /**
     * @param autosizecolums The autosizecolums to set.
     */
    public void setAutoSizeColums(boolean autosizecolums)
    {
        this.autosizecolums = autosizecolums;
    }

    /**
     * @return Returns whether or not null values are written as blank cells.
     */
    public boolean isNullBlank()
    {
        return nullIsBlank;
    }  
    
    /**
     * @param setNullIsBlank The boolean indicating whether or not to write null values as blank cells
     */
    public void setNullIsBlank(boolean nullIsBlank)
    {
        this.nullIsBlank = nullIsBlank;
    }

    /**
     * @return Returns the header.
     */
    public boolean isHeaderEnabled()
    {
        return headerEnabled;
    }

    /**
     * @param header The header to set.
     */
    public void setHeaderEnabled(boolean header)
    {
        this.headerEnabled = header;
    }

    
    public boolean  isSpecifyFormat()
    {
    	return SpecifyFormat;
    }
    public void setSpecifyFormat(boolean SpecifyFormat)
    {
    	this.SpecifyFormat=SpecifyFormat;
    }
    public String getDateTimeFormat()
 	{
 		return date_time_format;
 	}
 	public void setDateTimeFormat(String date_time_format)
 	{
 		this.date_time_format=date_time_format;
 	}
    
    
    /**
     * @return Returns the newline.
     */
    public String getNewline()
    {
        return newline;
    }

    /**
     * @param newline The newline to set.
     */
    public void setNewline(String newline)
    {
        this.newline = newline;
    }

    /**
     * @return Returns the splitEvery.
     */
    public int getSplitEvery()
    {
        return splitEvery;
    }
    
    /**
     * @return Returns the add to result filesname.
     */
    public boolean isAddToResultFiles()
    {
    	return addToResultFilenames;
    }
    
    /**
     * @param addtoresultfilenamesin The addtoresultfilenames to set.
     */
    public void setAddToResultFiles(boolean addtoresultfilenamesin)
    {
        this.addToResultFilenames = addtoresultfilenamesin;
    }
    
    /**
     * @param splitEvery The splitEvery to set.
     */
    public void setSplitEvery(int splitEvery)
    {
        this.splitEvery = splitEvery;
    }

    /**
     * @return Returns the stepNrInFilename.
     */
    public boolean isStepNrInFilename()
    {
        return stepNrInFilename;
    }

    /**
     * @param stepNrInFilename The stepNrInFilename to set.
     */
    public void setStepNrInFilename(boolean stepNrInFilename)
    {
        this.stepNrInFilename = stepNrInFilename;
    }

    /**
     * @return Returns the timeInFilename.
     */
    public boolean isTimeInFilename()
    {
        return timeInFilename;
    }

	/**
	 * @return Returns the protectsheet.
	 */
	public boolean isSheetProtected()
	{
		return protectsheet;
	}

    /**
     * @param timeInFilename The timeInFilename to set.
     */
    public void setTimeInFilename(boolean timeInFilename)
    {
        this.timeInFilename = timeInFilename;
    }

	/**
	 * @param protectsheet the value to set.
	 */
	public void setProtectSheet(boolean protectsheet)
	{
		this.protectsheet = protectsheet;
	}

    /**
     * @return Returns the outputFields.
     */
    public ExcelField[] getOutputFields()
    {
        return outputFields;
    }
    
    /**
     * @param outputFields The outputFields to set.
     */
    public void setOutputFields(ExcelField[] outputFields)
    {
        this.outputFields = outputFields;
    }

    /**
     * @return The desired encoding of output file, null or empty if the default system encoding needs to be used. 
     */
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * @param encoding The desired encoding of output file, null or empty if the default system encoding needs to be used.
     */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }
    
	/**
	 * @return Returns the template.
	 */
	public boolean isTemplateEnabled() {
		return templateEnabled;
	}
	
	/**
	 * @param template The template to set.
	 */
	public void setTemplateEnabled(boolean template) {
		this.templateEnabled = template;
	}
	
	/**
	 * @return Returns the templateAppend.
	 */
	public boolean isTemplateAppend() {
		return templateAppend;
	}
	
	/**
	 * @param templateAppend The templateAppend to set.
	 */
	public void setTemplateAppend(boolean templateAppend) {
		this.templateAppend = templateAppend;
	}
	
	/**
	 * @return Returns the templateFileName.
	 */
	public String getTemplateFileName() {
		return templateFileName;
	}
	
	/**
	 * @param templateFileName The templateFileName to set.
	 */
	public void setTemplateFileName(String templateFileName) {
		this.templateFileName = templateFileName;
	}
	
	
    /**
     * @return Returns the "do not open new file at init" flag.
     */    
    public boolean isDoNotOpenNewFileInit()
    {
    	return doNotOpenNewFileInit;
    }

    /**
     * @param doNotOpenNewFileInit The "do not open new file at init" flag to set.
     */
    public void setDoNotOpenNewFileInit(boolean doNotOpenNewFileInit)
    {
    	this.doNotOpenNewFileInit=doNotOpenNewFileInit;
    }
	
    /**
     * @return Returns the append.
     */
    public boolean isAppend()
    {
        return append;
    }
    /**
     * @param append The append to set.
     */
    public void setAppend(boolean append)
    {
        this.append = append;
    }
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public void allocate(int nrfields)
	{
	    outputFields = new ExcelField[nrfields];
	}
	
	public Object clone()
	{
		ExcelOutputMeta retval = (ExcelOutputMeta)super.clone();
		int nrfields=outputFields.length;
		
		retval.allocate(nrfields);
		
        for (int i=0;i<nrfields;i++)
        {
            retval.outputFields[i] = (ExcelField) outputFields[i].clone();
        }

		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			
			headerEnabled    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "header"));
			footerEnabled    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "footer"));
			encoding         = XMLHandler.getTagValue(stepnode, "encoding");
			append    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "append"));
			String addToResult=XMLHandler.getTagValue(stepnode,  "add_to_result_filenames");
			if(Const.isEmpty(addToResult)) 
				addToResultFilenames = true;
			else
				addToResultFilenames = "Y".equalsIgnoreCase(addToResult);
		
            fileName             = XMLHandler.getTagValue(stepnode, "file", "name");
			extension            = XMLHandler.getTagValue(stepnode, "file", "extention");
			
			doNotOpenNewFileInit       = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "do_not_open_newfile_init"));
			createparentfolder = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "create_parent_folder"));
			
			stepNrInFilename     = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "split"));
			dateInFilename       = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "add_date"));
			timeInFilename       = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "add_time"));
			SpecifyFormat       = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "SpecifyFormat"));
			date_time_format         = XMLHandler.getTagValue(stepnode, "file","date_time_format");
			
			autosizecolums = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "autosizecolums"));
			nullIsBlank = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "nullisblank"));
            protectsheet = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "protect_sheet"));
			password     = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue(stepnode, "file", "password") );
			splitEvery   = Const.toInt(XMLHandler.getTagValue(stepnode, "file", "splitevery"), 0);
			
			templateEnabled   = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "template", "enabled"));
			templateAppend    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "template", "append"));
			templateFileName  = XMLHandler.getTagValue(stepnode, "template", "filename");
			sheetname         = XMLHandler.getTagValue(stepnode, "file", "sheetname");
			Node fields       = XMLHandler.getSubNode(stepnode, "fields");
			int nrfields      = XMLHandler.countNodes(fields, "field");
	
			allocate(nrfields);
			
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
			
				outputFields[i] = new ExcelField();
				outputFields[i].setName( XMLHandler.getTagValue(fnode, "name") );
				outputFields[i].setType( XMLHandler.getTagValue(fnode, "type") );
				outputFields[i].setFormat( XMLHandler.getTagValue(fnode, "format") );
			}
			
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	public String getNewLine(String fformat)
	{
		String nl=System.getProperty("line.separator");

		if (fformat!=null)
		{
			if (fformat.equalsIgnoreCase("DOS"))
			{
				nl="\r\n";
			}
			else
			if (fformat.equalsIgnoreCase("UNIX"))
			{
				nl="\n";
			}
		}
		
		return nl;
	}

	public void setDefault()
	{
		autosizecolums=false;
		headerEnabled    = true;
		footerEnabled    = false;
		fileName         = "file";
		extension        = "xls";
		doNotOpenNewFileInit=false;
		createparentfolder = false;
		stepNrInFilename = false;
		dateInFilename   = false;
		timeInFilename   = false;
		date_time_format  =null;
		SpecifyFormat	= false;
		addToResultFilenames=true;
		protectsheet	 = false;
		splitEvery       = 0;
		templateEnabled  = false;
		templateAppend   = false;
		templateFileName = "template.xls";
		sheetname="Sheet1";	
		append   		 = false;
		nullIsBlank      = false;
		int i, nrfields=0;
		allocate(nrfields);
					
		for (i=0;i<nrfields;i++)
		{
			outputFields[i] = new ExcelField();

			outputFields[i].setName( "field"+i );				
			outputFields[i].setType( "Number" );
			outputFields[i].setFormat( " 0,000,000.00;-0,000,000.00" );
		}
	}
	
	public String[] getFiles(VariableSpace space)
	{
		int copies=1;
		int splits=1;

		if (stepNrInFilename)
		{
			copies=3;
		}
		
		if (splitEvery!=0)
		{
			splits=3;
		}
		
		int nr=copies*splits;
		if (nr>1) nr++;
		
		String retval[]=new String[nr];
		
		int i=0;
		for (int copy=0;copy<copies;copy++)
		{
			for (int split=0;split<splits;split++)
			{
				retval[i]=buildFilename(space, copy, split);
				i++;
			}
		}
		if (i<nr)
		{
			retval[i]="...";
		}
		
		return retval;
	}
	
	public String buildFilename(VariableSpace space, int stepnr, int splitnr)
	{
		SimpleDateFormat daf     = new SimpleDateFormat();

		// Replace possible environment variables...
		String retval=space.environmentSubstitute( fileName );
		String realextension=space.environmentSubstitute( extension );
		
		Date now = new Date();
		
		if(SpecifyFormat && !Const.isEmpty(date_time_format))
		{
			daf.applyPattern(date_time_format);
			String dt = daf.format(now);
			retval+=dt;
		}else
		{
			if (dateInFilename)
			{
				daf.applyPattern("yyyMMdd");
				String d = daf.format(now);
				retval+="_"+d;
			}
			if (timeInFilename)
			{
				daf.applyPattern("HHmmss");
				String t = daf.format(now);
				retval+="_"+t;
			}
		}
		if (stepNrInFilename)
		{
			retval+="_"+stepnr;
		}
		if (splitEvery>0)
		{
			retval+="_"+splitnr;
		}
		
		if (realextension!=null && realextension.length()!=0) 
		{
			retval+="."+realextension;
		} 

        return retval;
	}

	
	public void getFields(RowMetaInterface  r, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
	{
		if (r==null) r=new RowMeta(); // give back values
		
		// No values are added to the row in this type of step
	}

	public String getXML()
	{
		StringBuffer retval=new StringBuffer(800);
		
		retval.append("    ").append(XMLHandler.addTagValue("header",    headerEnabled));
		retval.append("    ").append(XMLHandler.addTagValue("footer",    footerEnabled));
        retval.append("    ").append(XMLHandler.addTagValue("encoding",  encoding));
        retval.append("    "+XMLHandler.addTagValue("append",    append));
        retval.append("    "+XMLHandler.addTagValue("add_to_result_filenames",   addToResultFilenames));
        
		retval.append("    <file>").append(Const.CR);
		retval.append("      ").append(XMLHandler.addTagValue("name",       fileName));
		retval.append("      ").append(XMLHandler.addTagValue("extention",  extension));
		retval.append("      ").append(XMLHandler.addTagValue("do_not_open_newfile_init",   doNotOpenNewFileInit));
		retval.append("      ").append(XMLHandler.addTagValue("create_parent_folder",   createparentfolder));
		retval.append("      ").append(XMLHandler.addTagValue("split",      stepNrInFilename));
		retval.append("      ").append(XMLHandler.addTagValue("add_date",   dateInFilename));
		retval.append("      ").append(XMLHandler.addTagValue("add_time",   timeInFilename));
		retval.append("      ").append(XMLHandler.addTagValue("SpecifyFormat",   SpecifyFormat));
		retval.append("      ").append(XMLHandler.addTagValue("date_time_format",  date_time_format));
		retval.append("      ").append(XMLHandler.addTagValue("sheetname", sheetname));
		retval.append("      ").append(XMLHandler.addTagValue("autosizecolums",   autosizecolums));
		retval.append("      ").append(XMLHandler.addTagValue("nullisblank",   nullIsBlank));
        retval.append("      ").append(XMLHandler.addTagValue("protect_sheet",   protectsheet));
		retval.append("      ").append(XMLHandler.addTagValue("password",  Encr.encryptPasswordIfNotUsingVariables(password)));
		retval.append("      ").append(XMLHandler.addTagValue("splitevery", splitEvery));
		
		retval.append("      </file>").append(Const.CR);
		
		retval.append("    <template>").append(Const.CR);
		retval.append("      ").append(XMLHandler.addTagValue("enabled",  templateEnabled));
		retval.append("      ").append(XMLHandler.addTagValue("append",   templateAppend));
		retval.append("      ").append(XMLHandler.addTagValue("filename", templateFileName));
		retval.append("    </template>").append(Const.CR);
		
		retval.append("    <fields>").append(Const.CR);
		for (int i=0;i<outputFields.length;i++)
		{
		    ExcelField field = outputFields[i];
		    
			if (field.getName()!= null && field.getName().length()!= 0)
			{
				retval.append("      <field>").append(Const.CR);
				retval.append("        ").append(XMLHandler.addTagValue("name",      field.getName()));
				retval.append("        ").append(XMLHandler.addTagValue("type",      field.getTypeDesc()));
				retval.append("        ").append(XMLHandler.addTagValue("format",    field.getFormat()));
				retval.append("      </field>").append(Const.CR);
			}
		}
		retval.append("    </fields>").append(Const.CR);

		return retval.toString();
	}
	
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			headerEnabled    =      rep.getStepAttributeBoolean(id_step, "header");
			footerEnabled    =      rep.getStepAttributeBoolean(id_step, "footer");   
            encoding         =      rep.getStepAttributeString (id_step, "encoding");
            append   =      rep.getStepAttributeBoolean(id_step, "append");
            
            String addToResult=rep.getStepAttributeString (id_step, "add_to_result_filenames");
			if(Const.isEmpty(addToResult)) 
				addToResultFilenames = true;
			else
				addToResultFilenames =  rep.getStepAttributeBoolean(id_step, "add_to_result_filenames");
            
            
			fileName         =      rep.getStepAttributeString (id_step, "file_name");  
			extension        =      rep.getStepAttributeString (id_step, "file_extention");
			
			doNotOpenNewFileInit =      rep.getStepAttributeBoolean(id_step, "do_not_open_newfile_init");
			createparentfolder        =      rep.getStepAttributeBoolean(id_step, "create_parent_folder");
			splitEvery       = (int)rep.getStepAttributeInteger(id_step, "file_split");
			stepNrInFilename =      rep.getStepAttributeBoolean(id_step, "file_add_stepnr");
			dateInFilename   =      rep.getStepAttributeBoolean(id_step, "file_add_date");
			timeInFilename   =      rep.getStepAttributeBoolean(id_step, "file_add_time");
			SpecifyFormat   =      rep.getStepAttributeBoolean(id_step, "SpecifyFormat");
			date_time_format  =      rep.getStepAttributeString (id_step, "date_time_format");  
			
			autosizecolums        = rep.getStepAttributeBoolean(id_step, "autosizecolums");
			nullIsBlank           = rep.getStepAttributeBoolean(id_step, "nullisblank");
            protectsheet          = rep.getStepAttributeBoolean(id_step, "protect_sheet");
			password              = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString (id_step, "password") );

			templateEnabled       = rep.getStepAttributeBoolean(id_step, "template_enabled");
			templateAppend        = rep.getStepAttributeBoolean(id_step, "template_append");
			templateFileName      = rep.getStepAttributeString(id_step, "template_filename");
			sheetname             = rep.getStepAttributeString(id_step, "sheetname");
			int nrfields          = rep.countNrStepAttributes(id_step, "field_name");
			
			allocate(nrfields);
			
			for (int i=0;i<nrfields;i++)
			{
			    outputFields[i] = new ExcelField();

			    outputFields[i].setName(    		rep.getStepAttributeString (id_step, i, "field_name") );
			    outputFields[i].setType( 			rep.getStepAttributeString (id_step, i, "field_type") );
			    outputFields[i].setFormat(  		rep.getStepAttributeString (id_step, i, "field_format") );
			}
			
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "header",           headerEnabled);
			rep.saveStepAttribute(id_transformation, id_step, "footer",           footerEnabled);
            rep.saveStepAttribute(id_transformation, id_step, "encoding",         encoding);
            rep.saveStepAttribute(id_transformation, id_step, "append",           append);
            rep.saveStepAttribute(id_transformation, id_step, "add_to_result_filenames",    addToResultFilenames);
			rep.saveStepAttribute(id_transformation, id_step, "file_name",        fileName);
			rep.saveStepAttribute(id_transformation, id_step, "do_not_open_newfile_init",  doNotOpenNewFileInit);
			rep.saveStepAttribute(id_transformation, id_step, "create_parent_folder",    createparentfolder);
			rep.saveStepAttribute(id_transformation, id_step, "file_extention",   extension);
			rep.saveStepAttribute(id_transformation, id_step, "file_split",       splitEvery);
			rep.saveStepAttribute(id_transformation, id_step, "file_add_stepnr",  stepNrInFilename);
			rep.saveStepAttribute(id_transformation, id_step, "file_add_date",    dateInFilename);
			rep.saveStepAttribute(id_transformation, id_step, "file_add_time",    timeInFilename);
			rep.saveStepAttribute(id_transformation, id_step, "SpecifyFormat",    SpecifyFormat);
			rep.saveStepAttribute(id_transformation, id_step, "date_time_format",   date_time_format);
			
			rep.saveStepAttribute(id_transformation, id_step, "autosizecolums",    autosizecolums);
			rep.saveStepAttribute(id_transformation, id_step, "nullisblank",    nullIsBlank);
            rep.saveStepAttribute(id_transformation, id_step, "protect_sheet",    protectsheet);
			rep.saveStepAttribute(id_transformation, id_step, "password",  Encr.encryptPasswordIfNotUsingVariables(password) );
			rep.saveStepAttribute(id_transformation, id_step, "template_enabled",  templateEnabled);
			rep.saveStepAttribute(id_transformation, id_step, "template_append",   templateAppend);
			rep.saveStepAttribute(id_transformation, id_step, "template_filename", templateFileName);
			rep.saveStepAttribute(id_transformation, id_step, "sheetname", sheetname);
			for (int i=0;i<outputFields.length;i++)
			{
			    ExcelField field = outputFields[i];
			    
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      field.getName());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      field.getTypeDesc());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_format",    field.getFormat());
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}


	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		// Check output fields
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ExcelOutputMeta.CheckResult.FieldsReceived", ""+prev.size()), stepMeta);
			remarks.add(cr);
			
			String  error_message="";
			boolean error_found=false;
			
			// Starting from selected fields in ...
			for (int i=0;i<outputFields.length;i++)
			{
				int idx = prev.indexOfValue(outputFields[i].getName());
				if (idx<0)
				{
					error_message+="\t\t"+outputFields[i].getName()+Const.CR;
					error_found=true;
				} 
			}
			if (error_found) 
			{
				error_message= BaseMessages.getString(PKG, "ExcelOutputMeta.CheckResult.FieldsNotFound", error_message);
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ExcelOutputMeta.CheckResult.AllFieldsFound"), stepMeta);
				remarks.add(cr);
			}
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ExcelOutputMeta.CheckResult.ExpectedInputOk"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ExcelOutputMeta.CheckResult.ExpectedInputError"), stepMeta);
			remarks.add(cr);
		}
		
		cr = new CheckResult(CheckResultInterface.TYPE_RESULT_COMMENT, BaseMessages.getString(PKG, "ExcelOutputMeta.CheckResult.FilesNotChecked"), stepMeta);
		remarks.add(cr);
	}
	
	/**
	 * Since the exported transformation that runs this will reside in a ZIP file, we can't reference files relatively.
	 * So what this does is turn the name of the base path into an absolute path.
	 */
	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface resourceNamingInterface, Repository repository) throws KettleException {
		try {
			// The object that we're modifying here is a copy of the original!
			// So let's change the filename from relative to absolute by grabbing the file object...
			// 
			if (!Const.isEmpty(fileName)) {
				FileObject fileObject = KettleVFS.getFileObject(space.environmentSubstitute(fileName), space);
				fileName = resourceNamingInterface.nameResource(fileObject, space, true);
			}
			
			return null;
		} catch (Exception e) {
			throw new KettleException(e); //$NON-NLS-1$
		}
	}


	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new ExcelOutput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new ExcelOutputData();
	}

    public String[] getUsedLibraries()
    {
        return new String[] { "jxl.jar", };
    }
}