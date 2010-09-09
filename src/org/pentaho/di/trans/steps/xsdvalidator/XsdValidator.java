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
 

package org.pentaho.di.trans.steps.xsdvalidator;


import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.provider.http.HttpFileObject;
import org.apache.commons.vfs.provider.local.LocalFile;
import java.net.URL;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.xml.sax.SAXException;

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
	private static Class<?> PKG = XsdValidatorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private XsdValidatorMeta meta;
	private XsdValidatorData data;
	
	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	
	static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
		
		
	public XsdValidator(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(XsdValidatorMeta)smi;
		data=(XsdValidatorData)sdi;
		
		Object[] row = getRow();
		
		if (row==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		if (first)
		{
			first=false;
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			// Check if XML stream is given
			if (meta.getXMLStream()!=null)
			{
				// Try to get XML Field index
				data.xmlindex = getInputRowMeta().indexOfValue(meta.getXMLStream());
				// Let's check the Field
				if (data.xmlindex<0)
				{
					// The field is unreachable !
					logError(BaseMessages.getString(PKG, "XsdValidator.Log.ErrorFindingField")+ "[" + meta.getXMLStream()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleStepException(BaseMessages.getString(PKG, "XsdValidator.Exception.CouldnotFindField",meta.getXMLStream())); //$NON-NLS-1$ //$NON-NLS-2$
				}
					
					
				// Let's check that Result Field is given
				if (meta.getResultfieldname() == null )
				{
					//	Result field is missing !
					logError(BaseMessages.getString(PKG, "XsdValidator.Log.ErrorResultFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleStepException(BaseMessages.getString(PKG, "XsdValidator.Exception.ErrorResultFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
				}
					
				// Is XSD file is provided?
				if (meta.getXSDSource().equals(meta.SPECIFY_FILENAME))
				{
					if(meta.getXSDFilename()==null)
					{
						logError(BaseMessages.getString(PKG, "XsdValidator.Log.ErrorXSDFileMissing")); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleStepException(BaseMessages.getString(PKG, "XsdValidator.Exception.ErrorXSDFileMissing")); //$NON-NLS-1$ //$NON-NLS-2$
					}
					else
					{
						// Is XSD file exists ?
						FileObject xsdfile=null;
						try
						{
							xsdfile = KettleVFS.getFileObject(environmentSubstitute(meta.getXSDFilename()), getTransMeta());
						    if(!xsdfile.exists())
						    {
						    	logError(BaseMessages.getString(PKG, "XsdValidator.Log.Error.XSDFileNotExists"));
								throw new KettleStepException(BaseMessages.getString(PKG, "XsdValidator.Exception.XSDFileNotExists"));
						    }
							
						}
						catch (Exception e)
						{
							logError(BaseMessages.getString(PKG, "XsdValidator.Log.Error.GettingXSDFile"));
							throw new KettleStepException(BaseMessages.getString(PKG, "XsdValidator.Exception.GettingXSDFile"));
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
						logError(BaseMessages.getString(PKG, "XsdValidator.Log.Error.XSDFieldMissing"));
						throw new KettleStepException(BaseMessages.getString(PKG, "XsdValidator.Exception.XSDFieldMissing"));
					}
					else
					{
						// Let's check if the XSD field exist
						// Try to get XML Field index
						data.xsdindex = getInputRowMeta().indexOfValue(meta.getXSDDefinedField());
							
						if (data.xsdindex<0)
						{
							// The field is unreachable !
							logError(BaseMessages.getString(PKG, "XsdValidator.Log.ErrorFindingXSDField",meta.getXSDDefinedField())); //$NON-NLS-1$ //$NON-NLS-2$
							throw new KettleStepException(BaseMessages.getString(PKG, "XsdValidator.Exception.ErrorFindingXSDField",meta.getXSDDefinedField())); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
					
					
			}
			else
			{
				// XML stream field is missing !
				logError(BaseMessages.getString(PKG, "XsdValidator.Log.Error.XmlStreamFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
				throw new KettleStepException(BaseMessages.getString(PKG, "XsdValidator.Exception.XmlStreamFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
			
		boolean sendToErrorRow=false;
	    String errorMessage = null;
	    
		try
		{
	
			// Get the XML field value
			String XMLFieldvalue= getInputRowMeta().getString(row,data.xmlindex);			
			
			boolean isvalid =false;
			
			// XSD filename
			String xsdfilename= null;
			
			if (meta.getXSDSource().equals(meta.SPECIFY_FILENAME))
			{
				xsdfilename= environmentSubstitute(meta.getXSDFilename());
			}
			else if (meta.getXSDSource().equals(meta.SPECIFY_FIELDNAME))
			{
				// Get the XSD field value
				xsdfilename= getInputRowMeta().getString(row,data.xsdindex);
			}
				
				
			// Get XSD filename
			FileObject xsdfile = null;		
	        String validationmsg=null;
			try 
			{
						
				SchemaFactory factoryXSDValidator = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				
			    xsdfile = KettleVFS.getFileObject(xsdfilename, getTransMeta());
			    		    					
				//	Get XML stream		
				Source sourceXML = new StreamSource(new StringReader(XMLFieldvalue));
				
				if(meta.getXMLSourceFile())
				{
				    
					// We deal with XML file
					// Get XML File
					File xmlfileValidator = new File(XMLFieldvalue);
					if (!xmlfileValidator.exists()) 
					{
						logError(BaseMessages.getString(PKG, "XsdValidator.Log.Error.XMLfileMissing",XMLFieldvalue)); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleStepException(BaseMessages.getString(PKG, "XsdValidator.Exception.XMLfileMissing",XMLFieldvalue)); //$NON-NLS-1$ //$NON-NLS-2$
					}
					sourceXML = new StreamSource(xmlfileValidator);
				}
				
				//  create the schema 
                Schema SchematXSD = null;
                if (xsdfile instanceof LocalFile) {
                    SchematXSD = factoryXSDValidator.newSchema(new File(KettleVFS.getFilename(xsdfile)));
                }
                else if (xsdfile instanceof HttpFileObject) {
                    SchematXSD = factoryXSDValidator.newSchema(new URL(KettleVFS.getFilename(xsdfile)));
                }
                else {
                     //  we should not get here as anything entered in that does not look like
                     //  a url should be made a FileObject.
                     throw new KettleStepException(BaseMessages.getString(PKG, "XsdValidator.Exception.CannotCreateSchema", xsdfile.getClass().getName()));
                }
                                
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
			}
			catch (IOException ex) 
			{
				validationmsg=ex.getMessage();
			}
			finally
			{
				try 
				{
				    if ( xsdfile != null )   	xsdfile.close();
					
			    }
				catch ( IOException e ) { }			
			}
			
			Object[] outputRowData =null;
			Object[] outputRowData2=null;
			
			if(meta.getOutputStringField())
			{
				// Output type=String
				if(isvalid)
					outputRowData =RowDataUtil.addValueData(row, getInputRowMeta().size(),environmentSubstitute(meta.getIfXmlValid()));
				else
					outputRowData =RowDataUtil.addValueData(row, getInputRowMeta().size(),environmentSubstitute(meta.getIfXmlInvalid()));
			}else{
				outputRowData =RowDataUtil.addValueData(row, getInputRowMeta().size(),isvalid);
			}
			
			if(meta.useAddValidationMessage())
				outputRowData2 =RowDataUtil.addValueData(outputRowData, getInputRowMeta().size()+1,validationmsg);
			else
				outputRowData2=outputRowData;
			
			if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "XsdValidator.Log.ReadRow") + " " +  getInputRowMeta().getString(row)); 
			
	        //	add new values to the row.
	        putRow(data.outputRowMeta, outputRowData2);  // copy row to output rowset(s);
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
                putError(getInputRowMeta(), row, 1, errorMessage, null, "XSD001");  
            }
			else
			{
	            logError(BaseMessages.getString(PKG, "XsdValidator.ErrorProcesing" + " : "+ e.getMessage()));
	            throw new KettleStepException(BaseMessages.getString(PKG, "XsdValidator.ErrorProcesing"), e);
			}
		}
		
		return true;
	
	}
		
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(XsdValidatorMeta)smi;
		data=(XsdValidatorData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (XsdValidatorMeta) smi;
		data = (XsdValidatorData) sdi;

		super.dispose(smi, sdi);
	}

}