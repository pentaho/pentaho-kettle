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
	
	String xslfilename= null;
	int fieldposition=0; 
	int fielxslfiledposition=0;
	
		
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
			
			logBasic("" + meta.getXslFilename());
			
			// Check if the XSL Filename is given
		
				if(Const.isEmpty(meta.getXslFilename()))
				{
					logError(Messages.getString("Xslt.Log.ErrorXSLFile")); 
					throw new KettleStepException(Messages.getString("Xslt.Exception.ErrorXSLFile"));
				}
				xslfilename = meta.getRealXslFilename();
				
				// Check if the XSL Filename is contained in a column
				if (meta.useXSLFileFieldUse())
				{
					if (Const.isEmpty(meta.getXSLFileField()))
					{
						// The field is missing
						//	Result field is missing !
						logError(Messages.getString("Xslt.Log.ErrorXSLFileFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleStepException(Messages.getString("Xslt.Exception.ErrorXSLFileFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
					}
					
					// Try to get Field index
					fielxslfiledposition =r.searchValueIndex(meta.getXSLFileField());
					
					
					//  Let's check the Field
					if (fielxslfiledposition<0)
					{
						//	 The field is unreachable !
						logError(Messages.getString("Xslt.Log.ErrorXSLFileFieldFinding")+ "[" + meta.getXSLFileField()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleStepException(Messages.getString("Xslt.Exception.ErrorXSLFileFieldFinding",meta.getXSLFileField())); //$NON-NLS-1$ //$NON-NLS-2$
					}						
										
			}			
				
		}		

		// Get the field value
		Value value = r.getValue(fieldposition);
		String Fieldvalue= value.getString();
		
		String xmlString=null;

		if (meta.useXSLFileFieldUse())
		{
			// Get the value
			Value valuexsl = r.getValue(fielxslfiledposition);
			xslfilename= valuexsl.getString();				
			if (log.isDetailed()) logDetailed(Messages.getString("Xslt.Log.XslfileNameFromFied",xslfilename,meta.getXSLFileField()));			
		}
		else
		{		
				xslfilename = meta.getRealXslFilename();
		}
		try {			
			logDetailed(Messages.getString("Xslt.Log.Filexsl") + xslfilename);
			TransformerFactory factory = TransformerFactory.newInstance();
			// Use the factory to create a template containing the xsl file
			Templates template = factory.newTemplates(new StreamSource(	new FileInputStream(xslfilename)));//"C:\\workspace\\Workflow\\fichiers\\GenerateFile.xsl")));
			// Use the template to create a transformer
			Transformer xformer = template.newTransformer();
			Source source = new StreamSource(new StringReader(Fieldvalue));			
		    StreamResult resultat = new StreamResult(new StringWriter());	   
			xformer.transform(source, resultat);
			xmlString = resultat.getWriter().toString();			
			logDetailed(Messages.getString("Xslt.Log.FileResult"));
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
