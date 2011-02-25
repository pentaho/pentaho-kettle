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
 

package org.pentaho.di.trans.steps.xslt;

import java.io.FileInputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Executes a XSL Transform on the values in the input stream. 
 * 
 * @author Samatar
 * @since 15-Oct-2007
 *
 */
public class Xslt extends BaseStep implements StepInterface
{
	private static Class<?> PKG = XsltMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private XsltMeta meta;
	private XsltData data;
	
	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";	
	static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	
	public Xslt(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(XsltMeta)smi;
		data=(XsltData)sdi;
		
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
			
			// Check if The result field is given
			if (Const.isEmpty(meta.getResultfieldname()))
			{
				// Result Field is missing !
				logError(BaseMessages.getString(PKG, "Xslt.Log.ErrorResultFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
				throw new KettleStepException(BaseMessages.getString(PKG, "Xslt.Exception.ErrorResultFieldMissing")); 
			}
		
			// Check if The XML field is given
			if (Const.isEmpty(meta.getFieldname()))
			{
				// Result Field is missing !
				logError(BaseMessages.getString(PKG, "Xslt.Exception.ErrorXMLFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
				throw new KettleStepException(BaseMessages.getString(PKG, "Xslt.Exception.ErrorXMLFieldMissing")); 
			}
			
			// Try to get XML Field index
			data.fieldposition = getInputRowMeta().indexOfValue(meta.getFieldname());
			// Let's check the Field
			if (data.fieldposition<0)
			{
				// The field is unreachable !
				logError(BaseMessages.getString(PKG, "Xslt.Log.ErrorFindingField")+ "[" + meta.getFieldname()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
				throw new KettleStepException(BaseMessages.getString(PKG, "Xslt.Exception.CouldnotFindField",meta.getFieldname())); //$NON-NLS-1$ //$NON-NLS-2$
			}
				
			// Check if the XSL Filename is contained in a column
			if (meta.useXSLFileFieldUse())
			{
				if (Const.isEmpty(meta.getXSLFileField()))
				{
					// The field is missing
					//	Result field is missing !
					logError(BaseMessages.getString(PKG, "Xslt.Log.ErrorXSLFileFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleStepException(BaseMessages.getString(PKG, "Xslt.Exception.ErrorXSLFileFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			
				// Try to get Field index
				data.fielxslfiledposition = getInputRowMeta().indexOfValue(meta.getXSLFileField());
			
				//  Let's check the Field
				if (data.fielxslfiledposition<0)
				{
					//	 The field is unreachable !
					logError(BaseMessages.getString(PKG, "Xslt.Log.ErrorXSLFileFieldFinding")+ "[" + meta.getXSLFileField()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleStepException(BaseMessages.getString(PKG, "Xslt.Exception.ErrorXSLFileFieldFinding",meta.getXSLFileField())); //$NON-NLS-1$ //$NON-NLS-2$
				}	
				
			}else
			{
				if(Const.isEmpty(meta.getXslFilename()))
				{
					logError(BaseMessages.getString(PKG, "Xslt.Log.ErrorXSLFile")); 
					throw new KettleStepException(BaseMessages.getString(PKG, "Xslt.Exception.ErrorXSLFile"));
				}
				
				// Check if XSL File exists!
				data.xslfilename = environmentSubstitute(meta.getXslFilename());
				java.io.File file=new java.io.File(data.xslfilename);
				if(!file.exists())
				{
					logError(BaseMessages.getString(PKG, "Xslt.Log.ErrorXSLFileNotExists",data.xslfilename)); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleStepException(BaseMessages.getString(PKG, "Xslt.Exception.ErrorXSLFileNotExists",data.xslfilename)); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if(!file.isFile())
				{
					logError(BaseMessages.getString(PKG, "Xslt.Log.ErrorXSLNotAFile",data.xslfilename)); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleStepException(BaseMessages.getString(PKG, "Xslt.Exception.ErrorXSLNotAFile",data.xslfilename)); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}	
		
		// Get the field value
		String Fieldvalue= getInputRowMeta().getString(row,data.fieldposition);	
		
		String xmlString=null;

		if (meta.useXSLFileFieldUse())
		{
			// Get the value	
			data.xslfilename= getInputRowMeta().getString(row,data.fielxslfiledposition);	
			if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "Xslt.Log.XslfileNameFromFied",data.xslfilename,meta.getXSLFileField()));			
		}

		boolean sendToErrorRow=false;
	    String errorMessage = null;
		try {			
			if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "Xslt.Log.Filexsl") + data.xslfilename);
			TransformerFactory factory = TransformerFactory.newInstance();
			
			if (meta.getXSLFactory().equals("SAXON"))
			{
				// Set the TransformerFactory to the SAXON implementation.
				factory = (TransformerFactory) new net.sf.saxon.TransformerFactoryImpl(); 
				
			}
			
      FileInputStream xslInputStream = new FileInputStream(data.xslfilename);
      try {
  			// Use the factory to create a template containing the xsl file
  			Templates template = factory.newTemplates(new StreamSource(	xslInputStream ) );
  			// Use the template to create a transformer
  			Transformer xformer = template.newTransformer();
  			Source source = new StreamSource(new StringReader(Fieldvalue));			
  		    StreamResult resultat = new StreamResult(new StringWriter());	   
  			xformer.transform(source, resultat);
  			xmlString = resultat.getWriter().toString();	
  			if(log.isDetailed()) 
  			{
  				logDetailed(BaseMessages.getString(PKG, "Xslt.Log.FileResult"));
  				logDetailed(xmlString);		
  			}
  			Object[] outputRowData =RowDataUtil.addValueData(row, getInputRowMeta().size(),xmlString);
  			
  			if (log.isRowLevel()) { logRowlevel(BaseMessages.getString(PKG, "Xslt.Log.ReadRow") + " " +  getInputRowMeta().getString(row)); } 
  			//	add new values to the row.
  	    putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);
      } finally {
        BaseStep.closeQuietly(xslInputStream);
      }
		} 
		catch (Exception e) {
			if (getStepMeta().isDoingErrorHandling())
	        {
	           sendToErrorRow = true;
	           errorMessage = e.getMessage();
	        }
	            
			if (sendToErrorRow) 
            {
                // Simply add this row to the error row
                putError(getInputRowMeta(), row, 1, errorMessage, meta.getResultfieldname(), "XSLT01");  
           
            }
			else
			{
	            logError(BaseMessages.getString(PKG, "Xslt.ErrorProcesing" + " : "+ e.getMessage()));
	            throw new KettleStepException(BaseMessages.getString(PKG, "Xslt.ErrorProcesing"), e);
			}
		}
   
		return true;	
	}
		
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(XsltMeta)smi;
		data=(XsltData)sdi;		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}

}