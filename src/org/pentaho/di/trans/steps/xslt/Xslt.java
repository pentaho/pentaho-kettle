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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.core.Const;
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
			if (meta.useXSLField())
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
				
			}else {
				if(Const.isEmpty(meta.getXslFilename())) {
					logError(BaseMessages.getString(PKG, "Xslt.Log.ErrorXSLFile")); 
					throw new KettleStepException(BaseMessages.getString(PKG, "Xslt.Exception.ErrorXSLFile"));
				}
				
				// Check if XSL File exists!
				data.xslfilename = environmentSubstitute(meta.getXslFilename());
				FileObject file=null;
				try {
					file=KettleVFS.getFileObject(data.xslfilename);
					if(!file.exists())
					{
						logError(BaseMessages.getString(PKG, "Xslt.Log.ErrorXSLFileNotExists",data.xslfilename)); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleStepException(BaseMessages.getString(PKG, "Xslt.Exception.ErrorXSLFileNotExists",data.xslfilename)); //$NON-NLS-1$ //$NON-NLS-2$
					}
					if(file.getType()!=FileType.FILE)
					{
						logError(BaseMessages.getString(PKG, "Xslt.Log.ErrorXSLNotAFile",data.xslfilename)); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleStepException(BaseMessages.getString(PKG, "Xslt.Exception.ErrorXSLNotAFile",data.xslfilename)); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}catch(Exception e) {
					throw new KettleStepException(e);
				} finally {
					try {
						if(file!=null) file.close();
					}catch(Exception e){};
				}
			}
			
			// Check output parameters
			int nrOutputProps= meta.getOutputPropertyName()==null?0:meta.getOutputPropertyName().length;
			if(nrOutputProps>0) {
				data.outputProperties= new Properties();
				for(int i=0; i<nrOutputProps; i++) {
					data.outputProperties.put(meta.getOutputPropertyName()[i], environmentSubstitute(meta.getOutputPropertyValue()[i]));
				}
				data.setOutputProperties=true;
			}
			

			// Check parameters
			data.nrParams= meta.getParameterField()==null?0:meta.getParameterField().length;
			if(data.nrParams>0) {
				data.indexOfParams = new int[data.nrParams];
				data.nameOfParams = new String[data.nrParams];
				for(int i=0; i<data.nrParams; i++) {
					String name = environmentSubstitute(meta.getParameterName()[i]);
					String field =  environmentSubstitute(meta.getParameterField()[i]);
					if(Const.isEmpty(field)) {
						throw new KettleStepException(BaseMessages.getString(PKG, "Xslt.Exception.ParameterFieldMissing", name, i));
					}
					data.indexOfParams[i]=getInputRowMeta().indexOfValue(field);
					if(data.indexOfParams[i]<0) {
						throw new KettleStepException(BaseMessages.getString(PKG, "Xslt.Exception.ParameterFieldNotFound", name));
					}
					data.nameOfParams[i]=name;
				}
				data.useParameters=true;
			}
			
			data.factory = TransformerFactory.newInstance();
			
			if (meta.getXSLFactory().equals("SAXON")){
				// Set the TransformerFactory to the SAXON implementation.
				data.factory = new net.sf.saxon.TransformerFactoryImpl(); 
			}
		}// end if first	
		
		// Get the field value
		String xmlValue= getInputRowMeta().getString(row,data.fieldposition);	

		if (meta.useXSLField()) {
			// Get the value	
			data.xslfilename= getInputRowMeta().getString(row,data.fielxslfiledposition);	
			if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "Xslt.Log.XslfileNameFromFied",data.xslfilename,meta.getXSLFileField()));			
		}


		try {		
			
			if(log.isDetailed()) {
				if(meta.isXSLFieldIsAFile()) logDetailed(BaseMessages.getString(PKG, "Xslt.Log.Filexsl") + data.xslfilename);
				else logDetailed(BaseMessages.getString(PKG, "Xslt.Log.XslStream", data.xslfilename));
			}
			
			// Get the template from the cache
	  		Transformer transformer = data.getTemplate(data.xslfilename, meta.isXSLFieldIsAFile());
	  	
	  		// Do we need to set output properties?
	  	 	if(data.setOutputProperties) {
	  	 		transformer.setOutputProperties(data.outputProperties);
	  	 	}
	  	
	  		// Do we need to pass parameters?
	  		if(data.useParameters) {
	  			for(int i=0; i<data.nrParams; i++) {
	  				transformer.setParameter(data.nameOfParams[i],  row[data.indexOfParams[i]]);	
	  			}
	  		}
	  		
	  		Source source = new StreamSource(new StringReader(xmlValue));	
	  		// Prepare output stream
	  	    StreamResult result = new StreamResult(new StringWriter());	
	  	    // transform xml source
	  	  transformer.transform(source, result);
			
			String xmlString = result.getWriter().toString();	
	  		if(log.isDetailed()) {
	  			logDetailed(BaseMessages.getString(PKG, "Xslt.Log.FileResult"));
	  			logDetailed(xmlString);		
	  		}
	  		Object[] outputRowData =RowDataUtil.addValueData(row, getInputRowMeta().size(),xmlString);
	  			
	  		if (log.isRowLevel()) { logRowlevel(BaseMessages.getString(PKG, "Xslt.Log.ReadRow") + " " +  getInputRowMeta().getString(row)); } 
	  		
	  		//	add new values to the row.
	  		putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);
	  		
		} catch (Exception e) {
			
			boolean sendToErrorRow=false;
		    String errorMessage = null;
		    
			if (getStepMeta().isDoingErrorHandling()) {
	           sendToErrorRow = true;
	           errorMessage = e.getMessage();
	        }
	            
			if (sendToErrorRow)  {
                // Simply add this row to the error row
                putError(getInputRowMeta(), row, 1, errorMessage, meta.getResultfieldname(), "XSLT01");  
            } else {
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
    
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (XsltMeta)smi;
        data = (XsltData)sdi;
        data.dispose();
        super.dispose(smi, sdi);
    }
}