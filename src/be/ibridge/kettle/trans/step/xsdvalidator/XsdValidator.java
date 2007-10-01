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
 

package be.ibridge.kettle.trans.step.xsdvalidator;


import java.io.IOException;
import org.xml.sax.SAXException;
import java.io.StringReader; 
import java.io.File; 
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.commons.vfs.FileObject;
import javax.xml.XMLConstants;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
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

/**
 * Executes a xsd validator on the values in the input stream. 
 * New fields were calculated values can then be put on the output stream.
 * 
 * @author Samatar
 * @since 14-08-2007
 *
 */
public class XsdValidator extends BaseStep implements StepInterface
{
	private XsdValidatorMeta meta;
	private XsdValidatorData data;
	
	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	
	static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
	
	private int xmlindex;
	private int xsdindex;
	
		
	public XsdValidator(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	


    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(XsdValidatorMeta)smi;
		data=(XsdValidatorData)sdi;
		
		Row r=getRow();       // Get row from input rowset & set row busy!
		
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		if (first)
		{
			first=false;
				
			// Try to get XML Field index
			xmlindex =r.searchValueIndex(meta.getXMLStream());			
				
			// Check if XML stream is given
			if (meta.getXMLStream()!=null)
			{
				// Let's check the Field
				if (xmlindex<0)
				{
					// The field is unreachable !
					logError(Messages.getString("XsdValidator.Log.ErrorFindingField")+ "[" + meta.getXMLStream()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleStepException(Messages.getString("XsdValidator.Exception.CouldnotFindField",meta.getXMLStream())); //$NON-NLS-1$ //$NON-NLS-2$
				}
					
					
				// Let's check that Result Field is given
				if (meta.getRealResultfieldname() == null )
				{
					//	Result field is missing !
					logError(Messages.getString("XsdValidator.Log.ErrorResultFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleStepException(Messages.getString("XsdValidator.Exception.ErrorResultFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
				}
					
				// Is XSD file is provided?
				if (meta.getXSDSource().equals(meta.SPECIFY_FILENAME))
				{
					if(meta.getXSDFilename()==null)
					{
							
						logError(Messages.getString("XsdValidator.Log.ErrorXSDFileMissing")); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleStepException(Messages.getString("XsdValidator.Exception.ErrorXSDFileMissing")); //$NON-NLS-1$ //$NON-NLS-2$
					}
					else
					{
						// Is XSD file exists ?
						FileObject xsdfile=null;
						try
						{
							xsdfile = KettleVFS.getFileObject(meta.getRealXSDFilename());
						    if(!xsdfile.exists())
						    {
						    	logError(Messages.getString("XsdValidator.Log.Error.XSDFileNotExists"));
								throw new KettleStepException(Messages.getString("XsdValidator.Exception.XSDFileNotExists"));
						    }
							
						}
						catch (Exception e)
						{
							logError(Messages.getString("XsdValidator.Log.Error.GettingXSDFile"));
							throw new KettleStepException(Messages.getString("XsdValidator.Exception.GettingXSDFile"));
						}
						finally
						{
							try 
							{
								   
							    if ( xsdfile != null )   	xsdfile.close();
									
						    }
							catch ( IOException e ) { }			
						}
					}
				}
					
				// Is XSD field is provided?
				if (meta.getXSDSource().equals(meta.SPECIFY_FIELDNAME))
				{
					if(meta.getXSDDefinedField()==null)
					{
						logError(Messages.getString("XsdValidator.Log.Error.XSDFieldMissing"));
						throw new KettleStepException(Messages.getString("XsdValidator.Exception.XSDFieldMissing"));
					}
					else
					{
						// Let's check if the XSD field exist
						// Try to get XML Field index
						xsdindex =r.searchValueIndex(meta.getXSDDefinedField());
							
						if (xsdindex<0)
						{
							// The field is unreachable !
							logError(Messages.getString("XsdValidator.Log.ErrorFindingXSDField",meta.getXSDDefinedField())); //$NON-NLS-1$ //$NON-NLS-2$
							throw new KettleStepException(Messages.getString("XsdValidator.Exception.ErrorFindingXSDField",meta.getXSDDefinedField())); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
					
					
			}
			else
			{
				// XML stream field is missing !
				logError(Messages.getString("XsdValidator.Log.Error.XmlStreamFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
				throw new KettleStepException(Messages.getString("XsdValidator.Exception.XmlStreamFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
			
		boolean sendToErrorRow=false;
	    String errorMessage = null;
	    
		try
		{
	
			// Get the XML field value
			Value xmlvalue = r.getValue(xmlindex);
			String XMLFieldvalue= xmlvalue.getString();
			
			
			boolean isvalid =false;
			
			// XSD filename
			String xsdfilename= null;
			
			if (meta.getXSDSource().equals(meta.SPECIFY_FILENAME))
			{
				xsdfilename= meta.getRealXSDFilename();
			}
			else if (meta.getXSDSource().equals(meta.SPECIFY_FIELDNAME))
			{
				// Get the XML field value
				Value xsdvalue = r.getValue(xsdindex);
				xsdfilename= xsdvalue.getString();
			}
				
				
			// Get XSD filename
			FileObject xsdfile = null;		
			String validationmsg=null;
			try 
			{
						
				SchemaFactory factoryXSDValidator = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				
			    xsdfile = KettleVFS.getFileObject(xsdfilename);
				File XSDFile = new File(KettleVFS.getFilename(xsdfile));
					
				//	Get XML stream		
				Source sourceXML = new StreamSource(new StringReader(XMLFieldvalue));
				
				if(meta.getXMLSourceFile())
				{
					
					// We deal with XML file
					// Get XML File
					File xmlfileValidator = new File(XMLFieldvalue);
					if (!xmlfileValidator.exists()) 
					{
						logError(Messages.getString("XsdValidator.Log.Error.XMLfileMissing",XMLFieldvalue)); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleStepException(Messages.getString("XsdValidator.Exception.XMLfileMissing",XMLFieldvalue)); //$NON-NLS-1$ //$NON-NLS-2$
					}
					sourceXML = new StreamSource(xmlfileValidator);
				}
				
				// Create XSD schema
				Schema SchematXSD = factoryXSDValidator.newSchema(XSDFile);
				
				if (meta.getXSDSource().equals(meta.NO_NEED))
				{
					// ---Some documents specify the schema they expect to be validated against, 
					// ---typically using xsi:noNamespaceSchemaLocation and/or xsi:schemaLocation attributes
					//---Schema SchematXSD = factoryXSDValidator.newSchema();
					SchematXSD = factoryXSDValidator.newSchema();
				}
				
				// Create XSDValidator
				Validator XSDValidator = SchematXSD.newValidator();
				// Validate XML / XSD		
				XSDValidator.validate(sourceXML);			
				
				isvalid=true;
				
					
			}
			catch (SAXException ex) 
			{
				validationmsg=ex.getMessage();
				logError("SAX Exception : " +  ex);
			}
			catch (IOException ex) 
			{
				validationmsg=ex.getMessage();
				logError("SAX Exception : " +  ex);
			}
			finally
			{
				try 
				{
				    if ( xsdfile != null )   	xsdfile.close();
					
			    }
				catch ( IOException e ) { }			
			}
			
	
			//	add new values to the row.
			Value fn=new Value(meta.getRealResultfieldname(),isvalid); // build a value!
			
			if(meta.getOutputStringField())
			{
				// Output type=String
				if(isvalid)
				{
					fn=new Value(meta.getRealResultfieldname(),meta.getifXMLValid());
				}
				else
				{
					fn=new Value(meta.getRealResultfieldname(),meta.getifXMLUnValid());
				}
			}
			
			// Add value to row
			r.addValue(fn);
			
			if(meta.useAddValidationMsg())
			{
				fn=new Value(StringUtil.environmentSubstitute(meta.getValidationMsgField()),validationmsg);		
				// Add value to row
				r.addValue(fn);
			}
			
			if (log.isRowLevel()) logRowlevel(Messages.getString("XsdValidator.Log.ReadRow") + " " +  r.toString()); 
			
	        putRow(r);       // copy row to output rowset(s);
		}
		catch(KettleException e)
		{
			if (getStepMeta().isDoingErrorHandling())
	        {
	           sendToErrorRow = true;
	           errorMessage = e.toString();
	        }
	            
			if (sendToErrorRow) 
            {
                // Simply add this row to the error row
                putError(r, 1L, errorMessage, null, "XSD001");
                r.setIgnore();   
            }
			else
			{
				logError(Messages.getString("XsdValidator.ErrorProcesing")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
		}
		
		return true;
	
	}
		
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(XsdValidatorMeta)smi;
		data=(XsdValidatorData)sdi;
		
		xmlindex=-1;
		xsdindex=-1;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}


	//
	// Run is were the action happens!
	public void run()
	{
		
		try
		{
			logBasic(Messages.getString("XsdValidator.Log.StartingToRun")); //$NON-NLS-1$
		
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("XsdValidator.Log.UnexpectedeError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Messages.getString("XsdValidator.Log.ErrorStackTrace")+Const.CR+Const.getStackTracker(e)); //$NON-NLS-1$
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
