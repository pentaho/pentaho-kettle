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
 

package be.ibridge.kettle.trans.step.ldifinput;

import org.apache.commons.vfs.FileObject;
import netscape.ldap.util.LDIFAttributeContent;
import netscape.ldap.util.LDIF;
import netscape.ldap.util.LDIFRecord;
import netscape.ldap.util.LDIFContent;
import netscape.ldap.LDAPAttribute;
import java.util.Enumeration;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.vfs.KettleVFS;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.xmlinput.XMLInputField;

/**
 * Read all sorts of text files, convert them to rows and writes these to one or more output streams.
 * 
 * @author Samatar
 * @since 24-05-2007
 */
public class LDIFInput extends BaseStep implements StepInterface
{
	private LDIFInputMeta meta;
	private LDIFInputData data;
	
	public LDIFInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		data.rownr=0;
		for (int i=0;i<data.files.size();i++)
		{
	
			if ((meta.getRowLimit()>0 &&  data.rownr<meta.getRowLimit()) || meta.getRowLimit()==0) 
			{
		
				data.file = (FileObject) data.files.get(i);
		    	
				logBasic("-----------------------------------");
		    	
				logBasic(Messages.getString("LDIFInput.Log.OpeningFile", data.file.toString()));
		    	
				// Fetch files and process each one
				Processfile(data.file);	
				
				
				if (log.isDetailed()) logDetailed(Messages.getString("LDIFInput.Log.FileOpened", data.file.toString()));
		        		
	
			}
	    	
			//	Add this to the result file names...
			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname());
			resultFile.setComment(Messages.getString("LDIFInput.Log.FileAddedResult"));
			addResultFile(resultFile);
	    	
			// 	 Move file pointer ahead!
			data.filenr++;	
			
		}
		
		setOutputDone();  // signal end to receiver(s)
		return false;     // This is the end of this step.
   
	}
		
	
	private void Processfile(FileObject file)
	{
		
		try 
		{
			
			LDIF InputLDIF = new LDIF(KettleVFS.getFilename(file));
			
			for (LDIFRecord recordLDIF = InputLDIF.nextRecord(); recordLDIF != null; recordLDIF = InputLDIF.nextRecord()) 
			{
				// 	Get LDIF Content
				LDIFContent contentLDIF = recordLDIF.getContent();
				

				if (((meta.getRowLimit()>0 && data.rownr<meta.getRowLimit()) || meta.getRowLimit()==0)
						&& (contentLDIF.getType()== LDIFContent.ATTRIBUTE_CONTENT))  
				{
					
					// Create new row
					Row row = buildEmptyRow();	
					
					
					// Get only ATTRIBUTE_CONTENT
					
					LDIFAttributeContent attrContentLDIF = (LDIFAttributeContent) contentLDIF;
					LDAPAttribute[] attributes_LDIF = attrContentLDIF.getAttributes();
				
			
					// Execute for each Input field...
					for (int i=0;i<meta.getInputFields().length;i++)
					{
						LDIFInputField xmlInputField = meta.getInputFields()[i];
						// Get the Attribut to look for
						String AttributValue = xmlInputField.getRealAttribut();
					
						// OK, we have the string...
						Value v = row.getValue(i);
						v.setValue(GetValue(attributes_LDIF ,AttributValue));
						
					    // DO Trimming!
			            switch(xmlInputField.getTrimType())
			            {
			            case XMLInputField.TYPE_TRIM_LEFT  : v.ltrim(); break;
			            case XMLInputField.TYPE_TRIM_RIGHT : v.rtrim(); break;
			            case XMLInputField.TYPE_TRIM_BOTH  : v.trim(); break;
			            default: break;
			            }
			            
			            //DO CONVERSIONS...
			            switch(xmlInputField.getType())
			            {
			            case Value.VALUE_TYPE_STRING:
			                // System.out.println("Convert value to String :"+v);
			                break;
			            case Value.VALUE_TYPE_NUMBER:
			                // System.out.println("Convert value to Number :"+v);
			                if (xmlInputField.getFormat()!=null && xmlInputField.getFormat().length()>0)
			                {
			                    if (xmlInputField.getDecimalSymbol()!=null && xmlInputField.getDecimalSymbol().length()>0)
			                    {
			                        if (xmlInputField.getGroupSymbol()!=null && xmlInputField.getGroupSymbol().length()>0)
			                        {
			                            if (xmlInputField.getCurrencySymbol()!=null && xmlInputField.getCurrencySymbol().length()>0)
			                            {
			                                v.str2num(xmlInputField.getFormat(), xmlInputField.getDecimalSymbol(), xmlInputField.getGroupSymbol(), xmlInputField.getCurrencySymbol());
			                            }
			                            else
			                            {
			                                v.str2num(xmlInputField.getFormat(), xmlInputField.getDecimalSymbol(), xmlInputField.getGroupSymbol());
			                            }
			                        }
			                        else
			                        {
			                            v.str2num(xmlInputField.getFormat(), xmlInputField.getDecimalSymbol());
			                        }
			                    }
			                    else
			                    {
			                        v.str2num(xmlInputField.getFormat()); // just a format mask
			                   }
			                }
			                else
			                {
			                    v.str2num();
			                }
			                v.setLength(xmlInputField.getLength(), xmlInputField.getPrecision());
			                break;
			            case Value.VALUE_TYPE_INTEGER:
			                // System.out.println("Convert value to integer :"+v);
			                v.setValue(v.getInteger());
			                v.setLength(xmlInputField.getLength(), xmlInputField.getPrecision());
			                break;
			            case Value.VALUE_TYPE_BIGNUMBER:
			                // System.out.println("Convert value to BigNumber :"+v);
			                v.setValue(v.getBigNumber());
			                v.setLength(xmlInputField.getLength(), xmlInputField.getPrecision());
			                break;
			            case Value.VALUE_TYPE_DATE:
			                // System.out.println("Convert value to Date :"+v);

			                if (xmlInputField.getFormat()!=null && xmlInputField.getFormat().length()>0)
			                {
			                    v.str2dat(xmlInputField.getFormat());
			                }
			                else
			                {
			                    v.setValue(v.getDate());
			                }
			                break;
			            case Value.VALUE_TYPE_BOOLEAN:
			                v.setValue(v.getBoolean());
			                break;
			            default: break;
			            }
			            
			            // Do we need to repeat this field if it is null?
			            if (meta.getInputFields()[i].isRepeated())
			            {
			                if (v.isNull() && data.previousRow!=null)
			                {
			                    Value previous = data.previousRow.getValue(i);
			                    v.setValue(previous);
			                }
			            }
			 			
					}    // End of loop over fields...
			            
					   // See if we need to add the filename to the row...  
			        if (meta.includeFilename() && meta.getFilenameField()!=null && meta.getFilenameField().length()>0)
			        {
			            Value fn = new Value( meta.getFilenameField(), KettleVFS.getFilename(data.file));
			            row.addValue(fn);
			        }
			        
			        // See if we need to add the row number to the row...  
			        if (meta.includeRowNumber() && meta.getRowNumberField()!=null && meta.getRowNumberField().length()>0)
			        {
			            Value fn = new Value( meta.getRowNumberField(), data.rownr );
			            row.addValue(fn);
			        }
					   	
			        
					data.previousRow = new Row(row); // copy it to make sure the next step doesn't change it in between... 
					data.rownr++;
			        
		    		
					if (log.isRowLevel()) logRowlevel(Messages.getString("LDIFInput.Log.ReadRow", row.toString()));        
		            
					putRow(row);
				}
			}        
		} 
		catch(Exception e)
		{
			logError(Messages.getString("LDIFInput.Log.UnableToOpenFile", ""+data.filenr, data.file.toString(), e.toString()));
			stopAll();
			setErrors(1);
		}    
    		
		
	}
	
	private String GetValue(LDAPAttribute[] attributes_LDIF ,String AttributValue)
	{
		String Stringvalue=null;
		int i=0;
        
		for (int j = 0; j < attributes_LDIF.length; j++) 
		{
			LDAPAttribute attribute_DIF = attributes_LDIF[j];
			if (attribute_DIF.getName().equalsIgnoreCase(AttributValue))
			{

				Enumeration valuesLDIF = attribute_DIF.getStringValues();
				
				while (valuesLDIF.hasMoreElements()) 
				{
					String valueLDIF = (String) valuesLDIF.nextElement();
					if (i==0)
					{	
						Stringvalue=  valueLDIF;
					}
					else
					{
						Stringvalue= Stringvalue + "," + valueLDIF;	
						
					}
					i++;
				
				}
				
			}
			
		}
		
		return Stringvalue;
	}

	/**
	 * Build an empty row based on the meta-data...
	 * @return
	 */
	private Row buildEmptyRow()
	{
		Row row = new Row();
        
		LDIFInputField fields[] = meta.getInputFields();
		for (int i=0;i<fields.length;i++)
		{
			LDIFInputField field = fields[i];
            
			Value value = new Value(StringUtil.environmentSubstitute(field.getName()), field.getType());
			value.setLength(field.getLength(), field.getPrecision());
			value.setNull();
            
			row.addValue(value);
		}
        
		return row;
	}

	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(LDIFInputMeta)smi;
		data=(LDIFInputData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.files = meta.getFiles().getFiles();
			if (data.files==null || data.files.size()==0)
			{
				logError(Messages.getString("LDIFInput.Log.NoFiles"));
				return false;
			}
            
			data.rownr = 1L;
			
			return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(LDIFInputMeta)smi;
		data=(LDIFInputData)sdi;

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
			logBasic(Messages.getString("LDIFInput.Log.StartingRun"));		
			
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error : "+e.toString());
			logError(Const.getStackTracker(e));
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