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

//TODO: add custom header & footer fields in different Tab on dialog.

package be.ibridge.kettle.trans.step.xmloutput;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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


/**
 * This class knows how to handle the MetaData for the XML output step
 * 
 * @since 14-jan-2006
 *
 */

public class XMLOutputMeta extends BaseStepMeta  implements StepMetaInterface
{
    /** The base name of the output file */
	private  String fileName;

	/** The file extention in case of a generated filename */
	private  String  extension;

	/** if this value is larger then 0, the text file is split up into parts of this number of lines */
    private  int    splitEvery;

	/** Flag: add the stepnr in the filename */
    private  boolean stepNrInFilename;
	
	/** Flag: add the date in the filename */
    private  boolean dateInFilename;
	
	/** Flag: add the time in the filename */
    private  boolean timeInFilename;
	
	/** Flag: put the destination file in a zip archive */
    private  boolean zipped;

    /** The encoding to use for reading: null or empty string means system default encoding */
    private String encoding;

    /** The name of the parent XML element */
    private String mainElement;

    /** The name of the repeating row XML element */
    private String repeatElement;

	/* THE FIELD SPECIFICATIONS ... */
	
	/** The output fields */
    private  XMLField outputFields[];


	public XMLOutputMeta()
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
     * @param fileName The fileName to set.
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
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
     * @param timeInFilename The timeInFilename to set.
     */
    public void setTimeInFilename(boolean timeInFilename)
    {
        this.timeInFilename = timeInFilename;
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



    /**
     * @return Returns the outputFields.
     */
    public XMLField[] getOutputFields()
    {
        return outputFields;
    }
    
    /**
     * @param outputFields The outputFields to set.
     */
    public void setOutputFields(XMLField[] outputFields)
    {
        this.outputFields = outputFields;
    }
	
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public void allocate(int nrfields)
	{
	    outputFields = new XMLField[nrfields];
	}
	
	public Object clone()
	{
		XMLOutputMeta retval = (XMLOutputMeta)super.clone();
		int nrfields=outputFields.length;
		
		retval.allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
            retval.outputFields[i] = (XMLField) outputFields[i].clone();
		}
		
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
            encoding         = XMLHandler.getTagValue(stepnode, "encoding");
            mainElement      = XMLHandler.getTagValue(stepnode, "xml_main_element");
            repeatElement    = XMLHandler.getTagValue(stepnode, "xml_repeat_element");

			fileName  = XMLHandler.getTagValue(stepnode, "file", "name");
			extension = XMLHandler.getTagValue(stepnode, "file", "extention");
			stepNrInFilename     = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "split"));
			dateInFilename  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "add_date"));
			timeInFilename  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "add_time"));
			zipped    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "zipped"));
			splitEvery=Const.toInt(XMLHandler.getTagValue(stepnode, "file", "splitevery"), 0);
			
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int nrfields= XMLHandler.countNodes(fields, "field");
	
			allocate(nrfields);
			
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
			
				outputFields[i] = new XMLField();
				outputFields[i].setFieldName( XMLHandler.getTagValue(fnode, "name") );
                outputFields[i].setElementName( XMLHandler.getTagValue(fnode, "element") );
				outputFields[i].setType( XMLHandler.getTagValue(fnode, "type") );
				outputFields[i].setFormat( XMLHandler.getTagValue(fnode, "format") );
				outputFields[i].setCurrencySymbol( XMLHandler.getTagValue(fnode, "currency") );
				outputFields[i].setDecimalSymbol( XMLHandler.getTagValue(fnode, "decimal") );
				outputFields[i].setGroupingSymbol( XMLHandler.getTagValue(fnode, "group") );
				outputFields[i].setNullString( XMLHandler.getTagValue(fnode, "nullif") );
				outputFields[i].setLength( Const.toInt(XMLHandler.getTagValue(fnode, "length"), -1) );
				outputFields[i].setPrecision( Const.toInt(XMLHandler.getTagValue(fnode, "precision"), -1) );
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
		fileName   = "file";
		extension  = "xml";
		stepNrInFilename = false;
		dateInFilename   = false;
		timeInFilename   = false;
		zipped           = false;
		splitEvery       = 0;
        encoding         = Const.XML_ENCODING;
        
        mainElement      = "Rows";
        repeatElement    = "Row";


		int nrfields=0;
		
		allocate(nrfields);
					
		for (int i=0;i<nrfields;i++)
		{
			outputFields[i] = new XMLField();

			outputFields[i].setFieldName( "field"+i );				
            outputFields[i].setElementName( "field"+i );              
			outputFields[i].setType( "Number" );
			outputFields[i].setFormat( " 0,000,000.00;-0,000,000.00" );
			outputFields[i].setCurrencySymbol( "" );
			outputFields[i].setDecimalSymbol( "," );
			outputFields[i].setGroupingSymbol(  "." );
			outputFields[i].setNullString( "" );
			outputFields[i].setLength( -1 );
			outputFields[i].setPrecision( -1 );
		}
	}
	
	public String[] getFiles()
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
				retval[i]=buildFilename(copy, split, false);
				i++;
			}
		}
		if (i<nr)
		{
			retval[i]="...";
		}
		
		return retval;
	}
	
	public String buildFilename(int stepnr, int splitnr, boolean ziparchive)
	{
		SimpleDateFormat daf     = new SimpleDateFormat();
		DecimalFormat df = new DecimalFormat("00000"); 
        
		// Replace possible environment variables...
		String retval=Const.replEnv( fileName );
		
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
			retval+="_"+df.format(splitnr+1);
		}
		
		if (zipped)
		{
			if (ziparchive)
			{
				retval+=".zip";
			}
			else
			{
				if (extension!=null && extension.length()!=0) 
				{
					retval+="."+extension;
				} 
			}
		}
		else
		{
			if (extension!=null && extension.length()!=0) 
			{
				retval+="."+extension;
			} 
		}
		return retval;
	}

	
	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...
		
		// No values are added to the row in this type of step
		// However, in case of Fixed length records, 
		// the field precisions and lengths are altered!
		
		for (int i=0;i<outputFields.length;i++)
		{
		    XMLField field = outputFields[i];
			Value v = row.searchValue(field.getFieldName());
			if (v!=null)
			{
				v.setLength(field.getLength(), field.getPrecision());
			}
		}

		return row;
	}

	public String getXML()
	{
		String retval="";
		int i;
		
        retval+="    "+XMLHandler.addTagValue("encoding",  encoding);
        retval+="    "+XMLHandler.addTagValue("xml_main_element",  mainElement);
        retval+="    "+XMLHandler.addTagValue("xml_repeat_element",  repeatElement);

		retval+="    <file>"+Const.CR;
		retval+="      "+XMLHandler.addTagValue("name",       fileName);
		retval+="      "+XMLHandler.addTagValue("extention",  extension);
		retval+="      "+XMLHandler.addTagValue("split",      stepNrInFilename);
		retval+="      "+XMLHandler.addTagValue("add_date",   dateInFilename);
		retval+="      "+XMLHandler.addTagValue("add_time",   timeInFilename);
		retval+="      "+XMLHandler.addTagValue("zipped",     zipped);
		retval+="      "+XMLHandler.addTagValue("splitevery", splitEvery);
		retval+="      </file>"+Const.CR;
		retval+="    <fields>"+Const.CR;
		for (i=0;i<outputFields.length;i++)
		{
		    XMLField field = outputFields[i];
		    
			if (field.getFieldName()!=null && field.getFieldName().length()!=0)
			{
				retval+="      <field>"+Const.CR;
				retval+="        "+XMLHandler.addTagValue("name",      field.getFieldName());
                retval+="        "+XMLHandler.addTagValue("element",   field.getElementName());
				retval+="        "+XMLHandler.addTagValue("type",      field.getType());
				retval+="        "+XMLHandler.addTagValue("format",    field.getFormat());
				retval+="        "+XMLHandler.addTagValue("currency",  field.getCurrencySymbol());
				retval+="        "+XMLHandler.addTagValue("decimal",   field.getDecimalSymbol());
				retval+="        "+XMLHandler.addTagValue("group",     field.getGroupingSymbol());
				retval+="        "+XMLHandler.addTagValue("nullif",    field.getNullString());
				retval+="        "+XMLHandler.addTagValue("length",    field.getLength());
				retval+="        "+XMLHandler.addTagValue("precision", field.getPrecision());
				retval+="        </field>"+Const.CR;
			}
		}
		retval+="      </fields>"+Const.CR;

		return retval;
	}
	
	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
            encoding        =      rep.getStepAttributeString (id_step, "encoding");
            mainElement     =      rep.getStepAttributeString (id_step, "xml_main_element");
            repeatElement   =      rep.getStepAttributeString (id_step, "xml_repeat_element");
            
			fileName        =      rep.getStepAttributeString (id_step, "file_name");  
			extension       =      rep.getStepAttributeString (id_step, "file_extention");
			splitEvery      = (int)rep.getStepAttributeInteger(id_step, "file_split");
			stepNrInFilename      =      rep.getStepAttributeBoolean(id_step, "file_add_stepnr");
			dateInFilename        =      rep.getStepAttributeBoolean(id_step, "file_add_date");
			timeInFilename        =      rep.getStepAttributeBoolean(id_step, "file_add_time");
			zipped          =      rep.getStepAttributeBoolean(id_step, "file_zipped");
	
			int nrfields = rep.countNrStepAttributes(id_step, "field_name");
			
			allocate(nrfields);
			
			for (int i=0;i<nrfields;i++)
			{
			    outputFields[i] = new XMLField();

			    outputFields[i].setFieldName(    	rep.getStepAttributeString (id_step, i, "field_name") );
                outputFields[i].setElementName(     rep.getStepAttributeString (id_step, i, "field_element") );
                outputFields[i].setType(            rep.getStepAttributeString (id_step, i, "field_type") );
                outputFields[i].setFormat(  		rep.getStepAttributeString (id_step, i, "field_format") );
			    outputFields[i].setCurrencySymbol(	rep.getStepAttributeString (id_step, i, "field_currency") );
			    outputFields[i].setDecimalSymbol(	rep.getStepAttributeString (id_step, i, "field_decimal") );
			    outputFields[i].setGroupingSymbol(	rep.getStepAttributeString (id_step, i, "field_group") );
			    outputFields[i].setNullString(		rep.getStepAttributeString (id_step, i, "field_nullif") );
			    outputFields[i].setLength(	   (int)rep.getStepAttributeInteger(id_step, i, "field_length") );
			    outputFields[i].setPrecision(  (int)rep.getStepAttributeInteger(id_step, i, "field_precision") );
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
            rep.saveStepAttribute(id_transformation, id_step, "encoding",           encoding);
            rep.saveStepAttribute(id_transformation, id_step, "xml_main_element",   mainElement);
            rep.saveStepAttribute(id_transformation, id_step, "xml_repeat_element", repeatElement);
			rep.saveStepAttribute(id_transformation, id_step, "file_name",          fileName);
			rep.saveStepAttribute(id_transformation, id_step, "file_extention",     extension);
			rep.saveStepAttribute(id_transformation, id_step, "file_split",         splitEvery);
			rep.saveStepAttribute(id_transformation, id_step, "file_add_stepnr",    stepNrInFilename);
			rep.saveStepAttribute(id_transformation, id_step, "file_add_date",      dateInFilename);
			rep.saveStepAttribute(id_transformation, id_step, "file_add_time",      timeInFilename);
			rep.saveStepAttribute(id_transformation, id_step, "file_zipped",        zipped);
			
			for (int i=0;i<outputFields.length;i++)
			{
			    XMLField field = outputFields[i];
			    
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      field.getFieldName());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_element",   field.getElementName());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      field.getTypeDesc());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_format",    field.getFormat());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_currency",  field.getCurrencySymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal",   field.getDecimalSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_group",     field.getGroupingSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_nullif",    field.getNullString());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_length",    field.getLength());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", field.getPrecision());
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}


	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		
		// Check output fields
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "+prev.size()+" fields", stepinfo);
			remarks.add(cr);
			
			String  error_message="";
			boolean error_found=false;
			
			// Starting from selected fields in ...
			for (int i=0;i<outputFields.length;i++)
			{
				int idx = prev.searchValueIndex(outputFields[i].getFieldName());
				if (idx<0)
				{
					error_message+="\t\t"+outputFields[i].getFieldName()+Const.CR;
					error_found=true;
				} 
			}
			if (error_found) 
			{
				error_message="Fields that were not found in input stream:"+Const.CR+Const.CR+error_message;
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All output fields are found in the input stream.", stepinfo);
				remarks.add(cr);
			}
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepinfo);
			remarks.add(cr);
		}
		
		cr = new CheckResult(CheckResult.TYPE_RESULT_COMMENT, "File specifications are not checked.", stepinfo);
		remarks.add(cr);
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new XMLOutputDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new XMLOutput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new XMLOutputData();
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
     * @return Returns the mainElement.
     */
    public String getMainElement()
    {
        return mainElement;
    }



    /**
     * @param mainElement The mainElement to set.
     */
    public void setMainElement(String mainElement)
    {
        this.mainElement = mainElement;
    }



    /**
     * @return Returns the repeatElement.
     */
    public String getRepeatElement()
    {
        return repeatElement;
    }



    /**
     * @param repeatElement The repeatElement to set.
     */
    public void setRepeatElement(String repeatElement)
    {
        this.repeatElement = repeatElement;
    }

}
