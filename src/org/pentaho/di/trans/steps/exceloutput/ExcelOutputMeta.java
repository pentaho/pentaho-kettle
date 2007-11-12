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
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
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

	public ExcelOutputMeta()
	{
		super(); // allocate BaseStepMeta
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

            fileName  = XMLHandler.getTagValue(stepnode, "file", "name");
			extension = XMLHandler.getTagValue(stepnode, "file", "extention");
			stepNrInFilename     = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "split"));
			dateInFilename  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "add_date"));
			timeInFilename  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "add_time"));
			protectsheet = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "protect_sheet"));
			password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue(stepnode, "file", "password") );
			splitEvery=Const.toInt(XMLHandler.getTagValue(stepnode, "file", "splitevery"), 0);

			templateEnabled    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "template", "enabled"));
			templateAppend    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "template", "append"));
			templateFileName  = XMLHandler.getTagValue(stepnode, "template", "filename");
			sheetname  = XMLHandler.getTagValue(stepnode, "file", "sheetname");
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int nrfields= XMLHandler.countNodes(fields, "field");
	
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
		headerEnabled    = true;
		footerEnabled    = false;
		fileName         = "file";
		extension        = "xls";
		stepNrInFilename = false;
		dateInFilename   = false;
		timeInFilename   = false;
		protectsheet	 = false;
		splitEvery       = 0;
		templateEnabled  = false;
		templateAppend   = false;
		templateFileName = "template.xls";
		sheetname="Sheet1";	
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
		
		Date now = new Date();
		
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
		if (stepNrInFilename)
		{
			retval+="_"+stepnr;
		}
		if (splitEvery>0)
		{
			retval+="_"+splitnr;
		}
		
		if (extension!=null && extension.length()!=0) 
		{
			retval+="."+extension;
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
		StringBuffer retval=new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("header",    headerEnabled));
		retval.append("    "+XMLHandler.addTagValue("footer",    footerEnabled));
        retval.append("    "+XMLHandler.addTagValue("encoding",  encoding));

		retval.append("    <file>"+Const.CR);
		retval.append("      "+XMLHandler.addTagValue("name",       fileName));
		retval.append("      "+XMLHandler.addTagValue("extention",  extension));
		retval.append("      "+XMLHandler.addTagValue("split",      stepNrInFilename));
		retval.append("      "+XMLHandler.addTagValue("add_date",   dateInFilename));
		retval.append("      "+XMLHandler.addTagValue("add_time",   timeInFilename));
		retval.append("      "+XMLHandler.addTagValue("sheetname", sheetname));
		retval.append("      "+XMLHandler.addTagValue("protect_sheet",   protectsheet));
		retval.append("      "+XMLHandler.addTagValue("password",  Encr.encryptPasswordIfNotUsingVariables(password)));
		retval.append("      "+XMLHandler.addTagValue("splitevery", splitEvery));
		retval.append("      </file>"+Const.CR);
		
		retval.append("    <template>"+Const.CR);
		retval.append("      "+XMLHandler.addTagValue("enabled",  templateEnabled));
		retval.append("      "+XMLHandler.addTagValue("append",   templateAppend));
		retval.append("      "+XMLHandler.addTagValue("filename", templateFileName));
		retval.append("      </template>"+Const.CR);
		
		retval.append("    <fields>"+Const.CR);
		for (int i=0;i<outputFields.length;i++)
		{
		    ExcelField field = outputFields[i];
		    
			if (field.getName()!=null && field.getName().length()!=0)
			{
				retval.append("      <field>"+Const.CR);
				retval.append("        "+XMLHandler.addTagValue("name",      field.getName()));
				retval.append("        "+XMLHandler.addTagValue("type",      field.getTypeDesc()));
				retval.append("        "+XMLHandler.addTagValue("format",    field.getFormat()));
				retval.append("        </field>"+Const.CR);
			}
		}
		retval.append("      </fields>"+Const.CR);

		return retval.toString();
	}
	
	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			headerEnabled    =      rep.getStepAttributeBoolean(id_step, "header");
			footerEnabled    =      rep.getStepAttributeBoolean(id_step, "footer");   
            encoding         =      rep.getStepAttributeString (id_step, "encoding");
            
			fileName         =      rep.getStepAttributeString (id_step, "file_name");  
			extension        =      rep.getStepAttributeString (id_step, "file_extention");
			splitEvery       = (int)rep.getStepAttributeInteger(id_step, "file_split");
			stepNrInFilename =      rep.getStepAttributeBoolean(id_step, "file_add_stepnr");
			dateInFilename   =      rep.getStepAttributeBoolean(id_step, "file_add_date");
			timeInFilename   =      rep.getStepAttributeBoolean(id_step, "file_add_time");
			protectsheet     =      rep.getStepAttributeBoolean(id_step, "protect_sheet");
			password         = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString (id_step, "password") );

			templateEnabled       =      rep.getStepAttributeBoolean(id_step, "template_enabled");
			templateAppend        =      rep.getStepAttributeBoolean(id_step, "template_append");
			templateFileName      =      rep.getStepAttributeString (id_step, "template_filename");
			sheetname      =      rep.getStepAttributeString (id_step, "sheetname");
			int nrfields = rep.countNrStepAttributes(id_step, "field_name");
			
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

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "header",           headerEnabled);
			rep.saveStepAttribute(id_transformation, id_step, "footer",           footerEnabled);
            rep.saveStepAttribute(id_transformation, id_step, "encoding",         encoding);
			rep.saveStepAttribute(id_transformation, id_step, "file_name",        fileName);
			rep.saveStepAttribute(id_transformation, id_step, "file_extention",   extension);
			rep.saveStepAttribute(id_transformation, id_step, "file_split",       splitEvery);
			rep.saveStepAttribute(id_transformation, id_step, "file_add_stepnr",  stepNrInFilename);
			rep.saveStepAttribute(id_transformation, id_step, "file_add_date",    dateInFilename);
			rep.saveStepAttribute(id_transformation, id_step, "file_add_time",    timeInFilename);
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
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("ExcelOutputMeta.CheckResult.FieldsReceived", ""+prev.size()), stepMeta);
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
				error_message= Messages.getString("ExcelOutputMeta.CheckResult.FieldsNotFound", error_message);
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("ExcelOutputMeta.CheckResult.AllFieldsFound"), stepMeta);
				remarks.add(cr);
			}
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("ExcelOutputMeta.CheckResult.ExpectedInputOk"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("ExcelOutputMeta.CheckResult.ExpectedInputError"), stepMeta);
			remarks.add(cr);
		}
		
		cr = new CheckResult(CheckResultInterface.TYPE_RESULT_COMMENT, Messages.getString("ExcelOutputMeta.CheckResult.FilesNotChecked"), stepMeta);
		remarks.add(cr);
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
