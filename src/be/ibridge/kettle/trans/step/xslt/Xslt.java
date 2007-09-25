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
 

package be.ibridge.kettle.trans.step.xslt;

import java.io.FileInputStream;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader; 
import java.io.StringWriter;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Executes a javascript on the values in the input stream. 
 * Selected calculated values can then be put on the output stream.
 * 
 * @author Matt
 * @since 5-apr-2003
 *
 */
public class Xslt extends BaseStep implements StepInterface
{
	private XsltMeta meta;
	private XsltData data;
	
	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";	
	static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
	
	String xsdfilename= null;
	int fieldposition=0; 
	int fielxsdfiledposition=0;
	
		
	public Xslt(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(XsltMeta)smi;
		data=(XsltData)sdi;
		
		Row r=getRow();       // Get row from input rowset & set row busy!
		
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}				
		if (first)
		{
			first=false;
			
			// Try to get Field index
			fieldposition =r.searchValueIndex(meta.getFieldname());		
			
			// Check if The result field is given
			if (meta.getResultfieldname()!=null)
			{
				// Let's check the Field
				if (fieldposition<0)
				{
					// The field is unreachable !
					logError(Messages.getString("Xslt.Log.ErrorFindingField")+ "[" + meta.getFieldname()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleStepException(Messages.getString("Xslt.Exception.CouldnotFindField",meta.getFieldname())); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				// Let's check that Result Field is given
				if (meta.getRealResultfieldname() == null )
				{
					//	Result field is missing !
					logError(Messages.getString("Xslt.Log.ErrorResultFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleStepException(Messages.getString("Xslt.Exception.ErrorResultFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			else
			{
				// Result Field is missing !
				logError(Messages.getString("Xslt.Log.ErrorMatcherMissing")); //$NON-NLS-1$ //$NON-NLS-2$
				throw new KettleStepException(Messages.getString("Xslt.Exception.ErrorMatcherMissing")); 
			}
			
			logBasic("" + meta.getXsdFilename());
			
			// Check if the XSD Filename is given
			if (!meta.useInternXSD())	
			{
				if(Const.isEmpty(meta.getXsdFilename()))
				{
					logError(Messages.getString("Xslt.Log.ErrorXSDFile")); 
					throw new KettleStepException(Messages.getString("Xslt.Exception.ErrorXSDFile"));
				}
				xsdfilename = meta.getRealXsdFilename();
				
				// Check if the XSD Filename is contained in a column
				if (meta.useXSDFileFieldUse())
				{
					if (Const.isEmpty(meta.getXSDFileField()))
					{
						// The field is missing
						//	Result field is missing !
						logError(Messages.getString("Xslt.Log.ErrorXSDFileFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleStepException(Messages.getString("Xslt.Exception.ErrorXSDFileFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
					}
					
					// Try to get Field index
					fielxsdfiledposition =r.searchValueIndex(meta.getXSDFileField());
					
					
					//  Let's check the Field
					if (fielxsdfiledposition<0)
					{
						//	 The field is unreachable !
						logError(Messages.getString("Xslt.Log.ErrorXSDFileFieldFinding")+ "[" + meta.getXSDFileField()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleStepException(Messages.getString("Xslt.Exception.ErrorXSDFileFieldFinding",meta.getXSDFileField())); //$NON-NLS-1$ //$NON-NLS-2$
					}						
				}						
			}			
			// Check Output Field format
			if (meta.getResultfieldFormat().equals("String"))
				{
					if(meta.getXsdValideText()==null)
					{
						logError(Messages.getString("Xslt.Log.ErrorXSDValide")); 
						throw new KettleStepException(Messages.getString("Xslt.Exception.ErrorXSDValide"));						
					}
					
					if(meta.getXsdNoValideText()==null)
					{
						logError(Messages.getString("Xslt.Log.ErrorXSDNoValide")); 
						throw new KettleStepException(Messages.getString("Xslt.Exception.ErrorXSDNoValide")); 						
					}
				}
			// Check XSD Invalid message Field		
		      if (meta.useInvalidMsg() && Const.isEmpty(meta.getInvalidMsgField()))
		       {	
	    		  	logError(Messages.getString("XsltMeta.Log.NoXSDInvalidMessageField")); 
	    		  	throw new KettleStepException(Messages.getString("XsltMeta.Exception.NoXSDInvalidMessageField"));   				
		       }			
		}		

		// Get the field value
		Value value = r.getValue(fieldposition);
		String Fieldvalue= value.getString();
		
		String xmlString=null;

		if (meta.useXSDFileFieldUse())
		{
			// Get the value
			Value valuexsd = r.getValue(fielxsdfiledposition);
			xsdfilename= valuexsd.getString();				
			if (log.isDetailed()) logDetailed("XSD Filename [" + xsdfilename + "] extracted from field [" +  meta.getXSDFileField() + "]");			
		}
		else
		{		
				xsdfilename = meta.getRealXsdFilename();
		}
		try {			
			logDetailed(Messages.getString("XsltMeta.Log.Filexsl") + xsdfilename);
			TransformerFactory factory = TransformerFactory.newInstance();
			// Use the factory to create a template containing the xsl file
			Templates template = factory.newTemplates(new StreamSource(	new FileInputStream(xsdfilename)));//"C:\\workspace\\Workflow\\fichiers\\GenerateFile.xsl")));
			// Use the template to create a transformer
			Transformer xformer = template.newTransformer();
			Source source = new StreamSource(new StringReader(Fieldvalue));			
		    StreamResult resultat = new StreamResult(new StringWriter());	   
			xformer.transform(source, resultat);
			xmlString = resultat.getWriter().toString();			
			logDetailed(Messages.getString("XsltMeta.Log.FileResult"));
			logDetailed(xmlString);		
		} 
		catch (Exception e) {
			// TODO: handle exception
			logError("ERROR : " +  e);
		}
		Value fn=null;
		
		fn=new Value(meta.getRealResultfieldname(),xmlString); // build a value!	
		r.addValue(fn);		
		if (log.isRowLevel()) logRowlevel(Messages.getString("Xslt.Log.ReadRow") + " " +  r.toString()); 		
        putRow(r);       // copy row to output rowset(s);       
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

	// Run is were the action happens!
	public void run()
	{		
		try
		{
			logBasic(Messages.getString("Xslt.Log.StartingToRun")); //$NON-NLS-1$		
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("Xslt.Log.UnexpectedeError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Messages.getString("Xslt.Log.ErrorStackTrace")+Const.CR+Const.getStackTracker(e)); //$NON-NLS-1$
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
