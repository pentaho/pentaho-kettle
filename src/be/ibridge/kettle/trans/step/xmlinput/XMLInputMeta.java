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

package be.ibridge.kettle.trans.step.xmlinput;

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


public class XMLInputMeta extends BaseStepMeta implements StepMetaInterface
{
	public final static int TYPE_TRIM_NONE  = 0;
	public final static int TYPE_TRIM_LEFT  = 1;
	public final static int TYPE_TRIM_RIGHT = 2;
	public final static int TYPE_TRIM_BOTH  = 3;
	
	public final static String trimTypeDesc[] = { "none", "left", "right", "both" };
	
	
	
	
	/** Array of filenames */
	private  String  fileName[]; 

	/** Wildcard or filemask (regular expression) */
	private  String  fileMask[];
 	 
	/** Flag indicating that we should include the filename in the output */
	private  boolean includeFilename;
	
	/** The name of the field in the output containing the filename */
	private  String  filenameField;
	
	/** Flag indicating that a row number field should be included in the output */
	private  boolean includeRowNumber;
	
	/** The name of the field in the output containing the row number*/
	private  String  rowNumberField;
	
	/** The maximum number or lines to read */
	private  long  rowLimit;
	
	/** The fields to import... */
	private XMLInputField inputFields[];

    /** The position in the XML documents to start (elements)*/
    private String inputPosition[];
	
	public XMLInputMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	
	/**
     * @return Returns the input fields.
     */
    public XMLInputField[] getInputFields()
    {
        return inputFields;
    }
    
    /**
     * @param inputFields The input fields to set.
     */
    public void setInputFields(XMLInputField[] inputFields)
    {
        this.inputFields = inputFields;
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
    
    
    
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		XMLInputMeta retval = (XMLInputMeta)super.clone();
		
		int nrfiles  = fileName.length;
		int nrfields = inputFields.length;

		retval.allocate(nrfiles, nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			retval.inputFields[i] = (XMLInputField)inputFields[i].clone();
		}
		
		return retval;
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			String lim;
			
			includeFilename         = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include"));
			filenameField   = XMLHandler.getTagValue(stepnode, "include_field");
			includeRowNumber          = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "rownum"));
			rowNumberField    = XMLHandler.getTagValue(stepnode, "rownum_field");
	
			Node filenode = XMLHandler.getSubNode(stepnode, "file");
			Node fields   = XMLHandler.getSubNode(stepnode, "fields");
			int nrfiles  = XMLHandler.countNodes(filenode, "name");
			int nrfields = XMLHandler.countNodes(fields, "field");
	
			allocate(nrfiles, nrfields);
			
			for (int i=0;i<nrfiles;i++)
			{
				Node filenamenode = XMLHandler.getSubNodeByNr(filenode, "name", i); 
				Node filemasknode = XMLHandler.getSubNodeByNr(filenode, "filemask", i); 
				fileName[i] = XMLHandler.getNodeValue(filenamenode);
				fileMask[i] = XMLHandler.getNodeValue(filemasknode);
			}
			
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				XMLInputField field = new XMLInputField();
				
				field.setName( XMLHandler.getTagValue(fnode, "name") );
				field.setType( Value.getType(XMLHandler.getTagValue(fnode, "type")) );
				field.setFormat( XMLHandler.getTagValue(fnode, "format") );
				field.setCurrencySymbol( XMLHandler.getTagValue(fnode, "currency") );
				field.setDecimalSymbol( XMLHandler.getTagValue(fnode, "decimal") );
				field.setGroupSymbol( XMLHandler.getTagValue(fnode, "group") );
				field.setLength( Const.toInt(XMLHandler.getTagValue(fnode, "length"), -1) );
				field.setPrecision( Const.toInt(XMLHandler.getTagValue(fnode, "precision"), -1) );
				field.setTrimType( getTrimType(XMLHandler.getTagValue(fnode, "trim_type")) );
				
				String srepeat = XMLHandler.getTagValue(fnode, "repeat");
				if (srepeat!=null) field.setRepeated( "Y".equalsIgnoreCase(srepeat) ); 
				else               field.setRepeated( false );
				
				inputFields[i] = field;
			}
			
			// Is there a limit on the number of rows we process?
			lim=XMLHandler.getTagValue(stepnode, "limit");
			rowLimit = Const.toLong(lim, 0L);
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public void allocate(int nrfiles, int nrfields)
	{
		fileName   = new String [nrfiles];
		fileMask   = new String [nrfiles];
		
		inputFields = new XMLInputField[nrfields];
	}
	
	public void setDefault()
	{
		includeFilename    = false;
		filenameField = "";
		includeRowNumber    = false;
		rowNumberField = "";
		
		int nrfiles=0;
		int nrfields=0;

		allocate(nrfiles, nrfields);	
		
		for (int i=0;i<nrfiles;i++) 
		{
			fileName[i]="filename"+(i+1);
			fileMask[i]="";
		}
		
		for (int i=0;i<nrfields;i++)
		{
		    inputFields[i] = new XMLInputField("field"+(i+1), null);
		}
			
		rowLimit=0L;
	}
	
	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...
		
		int i;
		for (i=0;i<inputFields.length;i++)
		{
		    XMLInputField field = inputFields[i];
		    
			int type=field.getType();
			if (type==Value.VALUE_TYPE_NONE) type=Value.VALUE_TYPE_STRING;
			Value v=new Value(field.getName(), type);
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
		String retval="";
		
		retval+="    "+XMLHandler.addTagValue("include",         includeFilename);
		retval+="    "+XMLHandler.addTagValue("include_field",   filenameField);
		retval+="    "+XMLHandler.addTagValue("rownum",          includeRowNumber);
		retval+="    "+XMLHandler.addTagValue("rownum_field",    rowNumberField);
		
        retval+="    <file>"+Const.CR;
		for (int i=0;i<fileName.length;i++)
		{
			retval+="      "+XMLHandler.addTagValue("name",     fileName[i]);
			retval+="      "+XMLHandler.addTagValue("filemask", fileMask[i]);
		}
		
		retval+="      </file>"+Const.CR;
		retval+="    <fields>"+Const.CR;
		
		for (int i=0;i<inputFields.length;i++)
		{
		    XMLInputField field = inputFields[i];
		    
			retval+="      <field>"+Const.CR;
			retval+="        "+XMLHandler.addTagValue("name",      field.getName());
			retval+="        "+XMLHandler.addTagValue("type",      field.getTypeDesc());
			retval+="        "+XMLHandler.addTagValue("format",    field.getFormat());
			retval+="        "+XMLHandler.addTagValue("currency",  field.getCurrencySymbol());
			retval+="        "+XMLHandler.addTagValue("decimal",   field.getDecimalSymbol());
			retval+="        "+XMLHandler.addTagValue("group",     field.getGroupSymbol());
			retval+="        "+XMLHandler.addTagValue("length",    field.getLength());
			retval+="        "+XMLHandler.addTagValue("precision", field.getPrecision());
			retval+="        "+XMLHandler.addTagValue("trim_type", field.getTrimTypeDesc());
			retval+="        "+XMLHandler.addTagValue("repeat",    field.isRepeated());
			retval+="        </field>"+Const.CR;
		}
		retval+="      </fields>"+Const.CR;
		retval+="    "+XMLHandler.addTagValue("limit", rowLimit);

		return retval;
	}
	
	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			includeFilename         =      rep.getStepAttributeBoolean(id_step, "include");  
			filenameField   =      rep.getStepAttributeString (id_step, "include_field");
	
			includeRowNumber          =      rep.getStepAttributeBoolean(id_step, "rownum");
			rowNumberField    =      rep.getStepAttributeString (id_step, "rownum_field");
			rowLimit           = (int)rep.getStepAttributeInteger(id_step, "limit");
	
			int nrfiles     = rep.countNrStepAttributes(id_step, "file_name");
			int nrfields    = rep.countNrStepAttributes(id_step, "field_name");
			
			allocate(nrfiles, nrfields);

			for (int i=0;i<nrfiles;i++)
			{
				fileName[i] =      rep.getStepAttributeString (id_step, i, "file_name"    );
				fileMask[i] =      rep.getStepAttributeString (id_step, i, "file_mask"    );
			}

			for (int i=0;i<nrfields;i++)
			{
			    XMLInputField field = new XMLInputField();
			    
				field.setName( rep.getStepAttributeString (id_step, i, "field_name") );
				field.setType( Value.getType( rep.getStepAttributeString (id_step, i, "field_type") ) );
				field.setFormat( rep.getStepAttributeString (id_step, i, "field_format") );
				field.setCurrencySymbol( rep.getStepAttributeString (id_step, i, "field_currency") );
				field.setDecimalSymbol( rep.getStepAttributeString (id_step, i, "field_decimal") );
				field.setGroupSymbol( rep.getStepAttributeString (id_step, i, "field_group") );
				field.setLength( (int)rep.getStepAttributeInteger(id_step, i, "field_length") );
				field.setPrecision( (int)rep.getStepAttributeInteger(id_step, i, "field_precision") );
				field.setTrimType( getTrimType( rep.getStepAttributeString (id_step, i, "field_trim_type") ));
				field.setRepeated( rep.getStepAttributeBoolean(id_step, i, "field_repeat") );
				
				inputFields[i] = field;
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
			rep.saveStepAttribute(id_transformation, id_step, "include",         includeFilename);
			rep.saveStepAttribute(id_transformation, id_step, "include_field",   filenameField);
			rep.saveStepAttribute(id_transformation, id_step, "rownum",          includeRowNumber);
			rep.saveStepAttribute(id_transformation, id_step, "rownum_field",    rowNumberField);
			rep.saveStepAttribute(id_transformation, id_step, "limit", rowLimit);
			
			for (int i=0;i<fileName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "file_name",     fileName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_mask",     fileMask[i]);
			}
			
			for (int i=0;i<inputFields.length;i++)
			{
			    XMLInputField field = inputFields[i];
			    
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      field.getName());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      field.getTypeDesc());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_format",    field.getFormat());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_currency",  field.getCurrencySymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal",   field.getDecimalSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_group",     field.getGroupSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_length",    field.getLength());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", field.getPrecision());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_trim_type", field.getTrimTypeDesc());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_repeat",    field.isRepeated());
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}
	
	public final static int getTrimType(String tt)
	{
		if (tt==null) return 0;
		
		for (int i=0;i<trimTypeDesc.length;i++)
		{
			if (trimTypeDesc[i].equalsIgnoreCase(tt)) return i;
		}
		return 0;
	}

	public final static String getTrimTypeDesc(int i)
	{
		if (i<0 || i>=trimTypeDesc.length) return trimTypeDesc[0];
		return trimTypeDesc[i];	
	}
	
	public String[] getFiles()
	{
		String files[]=null;
		
		// Replace possible environment variables...
		final String realfile[] = Const.replEnv(fileName);
		final String realmask[] = Const.replEnv(fileMask);
		
		ArrayList filelist = new ArrayList();
		
		for (int i=0;i<realfile.length;i++)
		{
			final String onefile = realfile[i];
			final String onemask = realmask[i];
			
			// System.out.println("Checking file ["+onefile+"] mask ["+onemask+"]");
			
			if (onemask!=null && onemask.length()>0) // A directory & a wildcard
			{
				File file = new File(onefile);
				try
				{
					files = file.list(new FilenameFilter() { public boolean accept(File dir, String name)
							{ return Pattern.matches(onemask, name); } } );
					
					for (int j = 0; j < files.length; j++)
					{
						if (!onefile.endsWith(Const.FILE_SEPARATOR))
						{
							files[j] = onefile+Const.FILE_SEPARATOR+files[j];
						}
						else
						{
							files[j] = onefile+files[j];
						}
					}
				}
				catch(Exception e)
				{
					files=null;
				}
			}
			else // A normal file...
			{
				// Check if it exists...
				File file = new File(onefile);
				if (file.exists() && file.isFile() && file.canRead() )
				{
					files = new String[] { onefile };
				}
				else // File is not accessible to us.
				{
					files = null;
				}
			}

			// System.out.println(" --> found "+(files==null?0:files.length)+" files");

			// Add to our list...
			if (files!=null)
			for (int x=0;x<files.length;x++)
			{				
				filelist.add(files[x]);
			}
		}
		// OK, return the list in filelist...
		files = (String[])filelist.toArray(new String[filelist.size()]);

		return files;
	}
	
	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;

		// See if we get input...
		if (input.length>0)
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
		if (files==null || files.length==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No files can be found to read.", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "This step is reading "+files.length+" files.", stepinfo);
			remarks.add(cr);
		}
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new XMLInputDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new XMLInput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new XMLInputData();
	}


    /**
     * @return Returns the inputPosition.
     */
    public String[] getInputPosition()
    {
        return inputPosition;
    }


    /**
     * @param inputPosition The inputPosition to set.
     */
    public void setInputPosition(String[] inputPosition)
    {
        this.inputPosition = inputPosition;
    }
}
