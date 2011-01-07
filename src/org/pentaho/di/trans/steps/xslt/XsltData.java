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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 24-jan-2005
 *
 */
public class XsltData extends BaseStepData implements StepDataInterface
{

	public RowMetaInterface outputRowMeta;
	public int fieldposition;
	public int fielxslfiledposition;
	public String xslfilename;
	
	public int fields_used[];
	
	public TransformerFactory factory;
	public HashMap<String, Transformer> transformers;
	
	public int nrParams;
	public int indexOfParams[];
	public String nameOfParams[];
	public boolean useParameters;
	
	public Properties outputProperties;
	public boolean setOutputProperties;
	
	/**
	 * 
	 */
	public XsltData()
	{
		super();
		fieldposition=-1;
		fielxslfiledposition=-1;
		xslfilename=null;
		transformers= new HashMap<String, Transformer>();
		useParameters=false;
		nrParams=0;
		setOutputProperties=false;
	}
	
	public Transformer getTemplate(String xslFilename, boolean isAfile) throws Exception {
		Transformer template = 	transformers.get(xslFilename);
		if(template!=null) {
			template.clearParameters();
			return template;
		}
		
		return createNewTemplate(xslFilename, isAfile);
	}
	
	private Transformer createNewTemplate(String xslSource, boolean isAfile) throws Exception{
	  	FileObject file= null;
	  	InputStream xslInputStream=null;
	  	Transformer transformer=null;
		try {
			if(isAfile) {
				file=KettleVFS.getFileObject(xslSource);
		  		xslInputStream = new FileInputStream(file.getName().getPathDecoded());
			}else {
				xslInputStream = new ByteArrayInputStream(xslSource.getBytes("UTF-8")); 
			}
	      	
	  		// Use the factory to create a template containing the xsl source
			transformer = factory.newTransformer(new StreamSource(xslInputStream ));
	  		// Add transformer to cache
			transformers.put(xslSource, transformer);
	  		
	  		return transformer;
	  	}finally {
	  		try {
	  			if(file!=null) file.close();
	  			if(xslInputStream!=null) xslInputStream.close();
	  		}catch(Exception e){};
	  	}
	}

	public void dispose() {
		transformers=null;
		factory=null;
		outputRowMeta=null;
		outputProperties=null;
	}
}
