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

package org.pentaho.di.trans.steps.jsonoutput;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;



/**
 * This class knows how to handle the MetaData for the Json output step
 * 
 * @since 14-june-2010
 *
 */


public class JsonOutputMeta extends BaseStepMeta  implements StepMetaInterface
{
	private static Class<?> PKG = JsonOutputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	/** Operations type */
	private int operationType;
	
	/**
	 * The operations description
	 */
	public final static String operationTypeDesc[] = {
			BaseMessages.getString(PKG, "JsonOutputMeta.operationType.OutputValue"),
			BaseMessages.getString(PKG, "JsonOutputMeta.operationType.WriteToFile"),
			BaseMessages.getString(PKG, "JsonOutputMeta.operationType.Both")};
	
	/**
	 * The operations type codes
	 */
	public final static String operationTypeCode[] = { "outputvalue", "writetofile", "both" };

	public final static int OPERATION_TYPE_OUTPUT_VALUE = 0;

	public final static int OPERATION_TYPE_WRITE_TO_FILE = 1;

	public final static int OPERATION_TYPE_BOTH = 2;

    /** The encoding to use for reading: null or empty string means system default encoding */
    private String encoding;

    /** The name value containing the resulting Json fragment */
    private String outputValue;

    /** The name of the json bloc */
    private String jsonBloc;
    
    private String nrRowsInBloc;

    /* THE FIELD SPECIFICATIONS ... */
    
    /** The output fields */
    private  JsonOutputField outputFields[];

    private boolean	AddToResult;
 
    /** The base name of the output file */
	private  String fileName;
	
	/** The file extention in case of a generated filename */
	private  String  extension;

	/** Flag to indicate the we want to append to the end of an existing file (if it exists) */
    private  boolean fileAppended;

	/** Flag: add the stepnr in the filename */
    private  boolean stepNrInFilename;
	
	/** Flag: add the partition number in the filename */
    private  boolean partNrInFilename;
	
	/** Flag: add the date in the filename */
    private  boolean dateInFilename;
	
	/** Flag: add the time in the filename */
    private  boolean timeInFilename;
    
    /** Flag: create parent folder if needed */
    private boolean createparentfolder;
    
    private boolean DoNotOpenNewFileInit;
    
	

    public JsonOutputMeta()
    {
        super(); // allocate BaseStepMeta
    }

    
    public boolean isDoNotOpenNewFileInit()
    {
        return DoNotOpenNewFileInit;
    }
    public void setDoNotOpenNewFileInit(boolean DoNotOpenNewFileInit)
    {
        this.DoNotOpenNewFileInit = DoNotOpenNewFileInit;
    }
    /**
     * @return Returns the create parent folder flag.
     */
    public boolean isCreateParentFolder()
    {
    	return createparentfolder;
    }
    
    /**
     * @param createparentfolder The create parent folder flag to set.
     */
    public void setCreateParentFolder(boolean createparentfolder)
    {
    	this.createparentfolder=createparentfolder;
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
     * @return Returns the fileAppended.
     */
    public boolean isFileAppended()
    {
        return fileAppended;
    }
    
    /**
     * @param fileAppended The fileAppended to set.
     */
    public void setFileAppended(boolean fileAppended)
    {
        this.fileAppended = fileAppended;
    }
    
    /**
     * @return Returns the fileName.
     */
    public String getFileName()
    {
        return fileName;
    }
    /**
     * @return Returns the timeInFilename.
     */
    public boolean isTimeInFilename()
    {
        return timeInFilename;
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
     * @param timeInFilename The timeInFilename to set.
     */
    public void setTimeInFilename(boolean timeInFilename)
    {
        this.timeInFilename = timeInFilename;
    }

    
    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    /**
     * @return Returns the Add to result filesname flag.
     */
    public boolean AddToResult()
    {
        return AddToResult;
    }
    
    public int getOperationType() {
		return operationType;
	}
	public static int getOperationTypeByDesc(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < operationTypeDesc.length; i++) {
			if (operationTypeDesc[i].equalsIgnoreCase(tt))
				return i;
		}
		// If this fails, try to match using the code.
		return getOperationTypeByCode(tt);
	}
	public void setOperationType(int operationType) {
		this.operationType = operationType;
	}
	public static String getOperationTypeDesc(int i) {
		if (i < 0 || i >= operationTypeDesc.length)
			return operationTypeDesc[0];
		return operationTypeDesc[i];
	}
    private static int getOperationTypeByCode(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < operationTypeCode.length; i++) {
			if (operationTypeCode[i].equalsIgnoreCase(tt))
				return i;
		}
		return 0;
	}
    /**
     * @return Returns the outputFields.
     */
    public JsonOutputField[] getOutputFields()
    {
        return outputFields;
    }
    
    /**
     * @param outputFields The outputFields to set.
     */
    public void setOutputFields(JsonOutputField[] outputFields)
    {
        this.outputFields = outputFields;
    }
    
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
        readData(stepnode);
    }

    public void allocate(int nrfields)
    {
        outputFields = new JsonOutputField[nrfields];
    }
    
    public Object clone()
    {
        JsonOutputMeta retval = (JsonOutputMeta)super.clone();
        int nrfields=outputFields.length;
        
        retval.allocate(nrfields);
        
        for (int i=0;i<nrfields;i++)
        {
            retval.outputFields[i] = (JsonOutputField) outputFields[i].clone();
        }
        
        return retval;
    }
    /**
     * @param AddToResult The Add file to result to set.
     */
    public void setAddToResult(boolean AddToResult)
    {
        this.AddToResult = AddToResult;
    }
    private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {
            encoding         = XMLHandler.getTagValue(stepnode, "encoding"); //$NON-NLS-1$
            outputValue      = XMLHandler.getTagValue(stepnode, "outputValue"); //$NON-NLS-1$
            jsonBloc    = XMLHandler.getTagValue(stepnode, "jsonBloc"); //$NON-NLS-1$
            nrRowsInBloc= XMLHandler.getTagValue(stepnode, "nrRowsInBloc"); //$NON-NLS-1$
            operationType = getOperationTypeByCode(Const.NVL(XMLHandler.getTagValue(stepnode,	"operation_type"), ""));
            
			encoding         = XMLHandler.getTagValue(stepnode, "encoding");
			AddToResult = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "AddToResult"));
			fileName  = XMLHandler.getTagValue(stepnode, "file", "name");
			createparentfolder ="Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "create_parent_folder"));
			extension = XMLHandler.getTagValue(stepnode, "file", "extention");
			fileAppended    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "append"));
			stepNrInFilename     = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "split"));
			partNrInFilename     = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "haspartno"));
			dateInFilename  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "add_date"));
			timeInFilename  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "add_time"));
			DoNotOpenNewFileInit    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "DoNotOpenNewFileInit"));
			
			
			Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
            int nrfields= XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
    
            allocate(nrfields);
            
            for (int i=0;i<nrfields;i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
            
                outputFields[i] = new JsonOutputField();
                outputFields[i].setFieldName( XMLHandler.getTagValue(fnode, "name") ); //$NON-NLS-1$
                outputFields[i].setElementName( XMLHandler.getTagValue(fnode, "element") ); //$NON-NLS-1$
            }
        }
        catch(Exception e)
        {
            throw new KettleXMLException("Unable to load step info from XML", e); //$NON-NLS-1$
        }
    }

    public void setDefault()
    {
        encoding         = Const.XML_ENCODING;
        outputValue        = "outputValue"; //$NON-NLS-1$
        jsonBloc         = "data"; //$NON-NLS-1$
        nrRowsInBloc= "1";
        operationType=OPERATION_TYPE_WRITE_TO_FILE;
        extension = "js";
        int nrfields=0;
        
        allocate(nrfields);
                    
        for (int i=0;i<nrfields;i++)
        {
            outputFields[i] = new JsonOutputField();
            outputFields[i].setFieldName( "field"+i ); //$NON-NLS-1$
            outputFields[i].setElementName( "field"+i ); //$NON-NLS-1$
        }
    }
    
    public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {
        
    	if(getOperationType()!=OPERATION_TYPE_WRITE_TO_FILE) {
    		ValueMetaInterface v=new ValueMeta(space.environmentSubstitute(this.getOutputValue()), ValueMetaInterface.TYPE_STRING);
    		v.setOrigin(name);
    		row.addValueMeta( v );
    	}
    }

    public String getXML()
    {
        StringBuffer retval=new StringBuffer(500);
        
        retval.append("    ").append(XMLHandler.addTagValue("encoding",  encoding)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("outputValue",  outputValue)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("jsonBloc",  jsonBloc)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("nrRowsInBloc",  nrRowsInBloc)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("operation_type",getOperationTypeCode(operationType)));
		retval.append("    ").append(XMLHandler.addTagValue("encoding",  encoding));
		retval.append("    ").append(XMLHandler.addTagValue("addtoresult",      AddToResult));
		retval.append("    <file>"+Const.CR);
		retval.append("      ").append(XMLHandler.addTagValue("name",       fileName));
		retval.append("      ").append(XMLHandler.addTagValue("extention",  extension));
		retval.append("      ").append(XMLHandler.addTagValue("append",     fileAppended));
		retval.append("      ").append(XMLHandler.addTagValue("split",      stepNrInFilename));
		retval.append("      ").append(XMLHandler.addTagValue("haspartno",  partNrInFilename));
		retval.append("      ").append(XMLHandler.addTagValue("add_date",   dateInFilename));
		retval.append("      ").append(XMLHandler.addTagValue("add_time",   timeInFilename));
		retval.append("      ").append(XMLHandler.addTagValue("create_parent_folder",   createparentfolder));
		retval.append("      ").append(XMLHandler.addTagValue("DoNotOpenNewFileInit",     DoNotOpenNewFileInit));
		retval.append("      </file>"+Const.CR);
        
        retval.append("    <fields>").append(Const.CR); //$NON-NLS-1$
        for (int i=0;i<outputFields.length;i++)
        {
            JsonOutputField field = outputFields[i];
            
            if (field.getFieldName()!=null && field.getFieldName().length()!=0)
            {
                retval.append("      <field>").append(Const.CR); //$NON-NLS-1$
                retval.append("        ").append(XMLHandler.addTagValue("name",      field.getFieldName())); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("element",   field.getElementName())); //$NON-NLS-1$ //$NON-NLS-2$retval.append("        </field>").append(Const.CR); //$NON-NLS-1$
                retval.append("    </field>"+Const.CR); //$NON-NLS-1$
            }
        }
        retval.append("    </fields>").append(Const.CR); //$NON-NLS-1$
        return retval.toString();
    }
    
    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
        try
        {
            encoding        =      rep.getStepAttributeString (id_step, "encoding"); //$NON-NLS-1$
            outputValue     =      rep.getStepAttributeString (id_step, "outputValue"); //$NON-NLS-1$
            jsonBloc   =      rep.getStepAttributeString (id_step, "jsonBloc"); //$NON-NLS-1$
            nrRowsInBloc   =      rep.getStepAttributeString (id_step, "nrRowsInBloc"); //$NON-NLS-1$
            
            operationType = getOperationTypeByCode(Const.NVL(rep.getStepAttributeString(id_step, "operation_type"), ""));
			encoding        =      rep.getStepAttributeString (id_step, "encoding");
			AddToResult     =      rep.getStepAttributeBoolean(id_step, "addtoresult"); 
			
			fileName        =      rep.getStepAttributeString (id_step, "file_name");    
			extension       =      rep.getStepAttributeString (id_step, "file_extention");
			fileAppended          =      rep.getStepAttributeBoolean(id_step, "file_append");
			stepNrInFilename      =      rep.getStepAttributeBoolean(id_step, "file_add_stepnr");
			partNrInFilename      =      rep.getStepAttributeBoolean(id_step, "file_add_partnr");
			dateInFilename        =      rep.getStepAttributeBoolean(id_step, "file_add_date");
			timeInFilename        =      rep.getStepAttributeBoolean(id_step, "file_add_time");
			createparentfolder        =      rep.getStepAttributeBoolean(id_step, "create_parent_folder");
			DoNotOpenNewFileInit          =      rep.getStepAttributeBoolean(id_step, "DoNotOpenNewFileInit");
			
			
			
            int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$
            
            allocate(nrfields);
            
            for (int i=0;i<nrfields;i++)
            {
                outputFields[i] = new JsonOutputField();

                outputFields[i].setFieldName(       rep.getStepAttributeString (id_step, i, "field_name") ); //$NON-NLS-1$
                outputFields[i].setElementName(     rep.getStepAttributeString (id_step, i, "field_element") ); //$NON-NLS-1$ 
            }       
        }
        catch(Exception e)
        {
            throw new KettleException("Unexpected error reading step information from the repository", e); //$NON-NLS-1$
        }
    }
	private static String getOperationTypeCode(int i) {
		if (i < 0 || i >= operationTypeCode.length)
			return operationTypeCode[0];
		return operationTypeCode[i];
	}
    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "encoding",           encoding); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "outputValue",          outputValue); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "jsonBloc", jsonBloc); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "nrRowsInBloc", nrRowsInBloc); //$NON-NLS-1$
            
            rep.saveStepAttribute(id_transformation, id_step, "operation_type", getOperationTypeCode(operationType));		
			rep.saveStepAttribute(id_transformation, id_step, "encoding",         encoding);
			rep.saveStepAttribute(id_transformation, id_step, "addtoresult",        AddToResult);
			
			rep.saveStepAttribute(id_transformation, id_step, "file_name",        fileName);
			rep.saveStepAttribute(id_transformation, id_step, "file_extention",   extension);
			rep.saveStepAttribute(id_transformation, id_step, "file_append",      fileAppended);
			rep.saveStepAttribute(id_transformation, id_step, "file_add_stepnr",  stepNrInFilename);
			rep.saveStepAttribute(id_transformation, id_step, "file_add_partnr",  partNrInFilename);
			rep.saveStepAttribute(id_transformation, id_step, "file_add_date",    dateInFilename);
			rep.saveStepAttribute(id_transformation, id_step, "file_add_time",    timeInFilename);
			rep.saveStepAttribute(id_transformation, id_step, "create_parent_folder",    createparentfolder);
			rep.saveStepAttribute(id_transformation, id_step, "DoNotOpenNewFileInit",      DoNotOpenNewFileInit);
			
            for (int i=0;i<outputFields.length;i++)
            {
                JsonOutputField field = outputFields[i];
                
                rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      field.getFieldName()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_element",   field.getElementName()); //$NON-NLS-1$
            }
        }
        catch(Exception e)
        {
            throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e); //$NON-NLS-1$
        }
    }
    public String[] getFiles(String fileName)
	{
		int copies=1;
		int splits=1;
		int parts=1;

		if (stepNrInFilename)
		{
			copies=3;
		}
		
		if (partNrInFilename)
		{
			parts=3;
		}
		
		
		int nr=copies*parts*splits;
		if (nr>1) nr++;
		
		String retval[]=new String[nr];
		
		int i=0;
		for (int copy=0;copy<copies;copy++)
		{
			for (int part=0;part<parts;part++)
			{
				for (int split=0;split<splits;split++)
				{
					retval[i]=buildFilename( fileName, copy, split);
					i++;
				}
			}
		}
		if (i<nr)
		{
			retval[i]="...";
		}
		
		return retval;
	}
    public String buildFilename(String fileName,int stepnr, int splitnr)
	{
		SimpleDateFormat daf     = new SimpleDateFormat();

		// Replace possible environment variables...
		String retval=fileName;
		

		Date now = new Date();
		
		if (dateInFilename)
		{
			daf.applyPattern("yyyMMdd");
			String d = daf.format(now);
			retval+="_"+d;
		}
		if (timeInFilename)
		{
			daf.applyPattern("HHmmss.SSS");
			String t = daf.format(now);
			retval+="_"+t;
		}
		if (stepNrInFilename)
		{
			retval+="_"+stepnr;
		}
		
		if (extension!=null && extension.length()!=0) 
		{
			retval+="."+extension;
		}
		
		return retval;
	}
    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) {

    	CheckResult cr;
    	if(getOperationType() != JsonOutputMeta.OPERATION_TYPE_WRITE_TO_FILE) {
    		// We need to have output field name
    		if(Const.isEmpty(transMeta.environmentSubstitute(getOutputValue()))) {
    			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "JsonOutput.Error.MissingOutputFieldName"), stepMeta);
    			remarks.add(cr);
    		}
    	}
		if(Const.isEmpty(transMeta.environmentSubstitute(getFileName()))) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "JsonOutput.Error.MissingTargetFilename"), stepMeta);
			remarks.add(cr);
		}
        // Check output fields
        if (prev!=null && prev.size()>0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "JsonOutputMeta.CheckResult.FieldsReceived", ""+prev.size()), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
            
            String  error_message="";
            boolean error_found=false;
            
            // Starting from selected fields in ...
            for (int i=0;i<outputFields.length;i++)
            {
                int idx = prev.indexOfValue(outputFields[i].getFieldName());
                if (idx<0)
                {
                    error_message+="\t\t"+outputFields[i].getFieldName()+Const.CR; //$NON-NLS-1$
                    error_found=true;
                } 
            }
            if (error_found) 
            {
                error_message=BaseMessages.getString(PKG, "JsonOutputMeta.CheckResult.FieldsNotFound", error_message); //$NON-NLS-1$
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
                remarks.add(cr);
            }
            else
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "JsonOutputMeta.CheckResult.AllFieldsFound"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
        }
        
        // See if we have input streams leading to this step!
        if (input.length>0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "JsonOutputMeta.CheckResult.ExpectedInputOk"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "JsonOutputMeta.CheckResult.ExpectedInputError"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
        
        cr = new CheckResult(CheckResult.TYPE_RESULT_COMMENT, BaseMessages.getString(PKG, "JsonOutputMeta.CheckResult.FilesNotChecked"), stepMeta); //$NON-NLS-1$
        remarks.add(cr);
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new JsonOutput(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new JsonOutputData();
    }



    public String getEncoding()
    {
        return encoding;
    }


    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }


    /**
     * @return Returns the jsonBloc.
     */
    public String getJsonBloc()
    {
        return jsonBloc;
    }

    /**
     * @param jsonBloc The root node to set.
     */
    public void setJsonBloc(String jsonBloc)
    {
        this.jsonBloc = jsonBloc;
    }
    
    /**
     * @return Returns the jsonBloc.
     */
    public String getNrRowsInBloc()
    {
        return nrRowsInBloc;
    }

    /**
     * @param jsonBloc The nrRowsInBloc.
     */
    public void setNrRowsInBloc(String nrRowsInBloc)
    {
        this.nrRowsInBloc = nrRowsInBloc;
    }
    public String getOutputValue() {
        return outputValue;
    }


    public void setOutputValue(String outputValue) {
        this.outputValue = outputValue;
    }
    
}
