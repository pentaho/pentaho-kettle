/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.xmljoin;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;


/**
 * Converts input rows to one or more XML files.
 * 
 * @author Matt
 * @since 14-jan-2006
 */
public class XMLJoin extends BaseStep implements StepInterface
{
	private static Class<?> PKG = XMLJoinMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private XMLJoinMeta meta;
    private XMLJoinData data;
    
    private Transformer serializer;
     
     
    public XMLJoin(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
   
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(XMLJoinMeta)smi;
        data=(XMLJoinData)sdi;
        
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        //if first row we do some initializing and process the first row of the target XML Step
        if (first) 
        {
        	first=false;
        	int target_field_id = -1;
        	XMLJoinMeta meta = (XMLJoinMeta) smi;
        	
        	//Get the two input row sets
        	data.TargetRowSet = findInputRowSet(meta.getTargetXMLstep());
            data.SourceRowSet = findInputRowSet(meta.getSourceXMLstep());
            
          //get the first line from the target row set
        	Object[] rTarget = getRowFrom(data.TargetRowSet);
        	
        	//get target xml
        	meta.getTargetXMLstep();
        	String[] target_field_names = data.TargetRowSet.getRowMeta().getFieldNames();
        	for(int i=0; i< target_field_names.length; i++){
        		if(meta.getTargetXMLfield().equals(target_field_names[i])){
        			target_field_id = i;
        		}
        	}        	
        	//Throw exception if target field has not been found
        	if(target_field_id == -1) throw new KettleException(BaseMessages.getString(PKG, "XMLJoin.Exception.FieldNotFound", meta.getTargetXMLfield())); //$NON-NLS-1$
        	
        	data.outputRowMeta = data.TargetRowSet.getRowMeta().clone();
        	meta.getFields(data.outputRowMeta, getStepname(), new RowMetaInterface[] { data.TargetRowSet.getRowMeta() }, null, this);
        	data.outputRowData = rTarget.clone();
        	
        	//get the target xml structure and create a DOM
        	String strTarget = (String)rTarget[target_field_id];    
        	// parse the XML as a W3C Document
        	
        	InputSource inputSource = new InputSource(new StringReader(strTarget));
        	
        	data.XPathStatement  = meta.getTargetXPath();
        	try{
        		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            	data.targetDOM = builder.parse(inputSource);
            	if(! meta.isComplexJoin()){
            		data.targetNode = (Node) xpath.evaluate(data.XPathStatement, data.targetDOM, XPathConstants.NODE);
            		if(data.targetNode == null){
            			throw new KettleXMLException("XPath statement returned no reuslt [" + data.XPathStatement +"]"); //$NON-NLS-1$ //$NON-NLS-2$
            		}
            	}
        	}
        	catch(Exception e){
        		throw new KettleXMLException(e);
        	}          	
        	
        }
        
        Object[]  rJoinSource = getRowFrom(data.SourceRowSet);    // This also waits for a row to be finished.
        // no more input to be expected... create the output row
        if (rJoinSource==null)  // no more input to be expected...
        {
        	//create string from xml tree
        	try{
	            String strOmitXMLHeader;
	            if(meta.isOmitXMLHeader()){
	            	strOmitXMLHeader = "yes"; //$NON-NLS-1$
	            }
	            else{
	            	strOmitXMLHeader = "no"; //$NON-NLS-1$
	            }
	            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, strOmitXMLHeader);
	            serializer.setOutputProperty(OutputKeys.INDENT, "no"); //$NON-NLS-1$
	            StringWriter sw = new StringWriter();
	            StreamResult resultXML = new StreamResult(sw);
	            DOMSource source = new DOMSource(data.targetDOM);
	            serializer.transform(source, resultXML);
	            
	            int outputIndex = data.outputRowMeta.size()-1;
	            
	            //send the row to the next steps...
	            putRow(data.outputRowMeta, RowDataUtil.addValueData(data.outputRowData, outputIndex, sw.toString()));
	            // finishing up
	            setOutputDone();
	            return false;
        	} catch (Exception e) {
        		throw new KettleException(e);
        	}

        }
        
        if (data.iSourceXMLField == -1){
        	//assume failure
	        //get the column of the join xml set
	    	//get target xml
	    	String[] source_field_names = data.SourceRowSet.getRowMeta().getFieldNames();
	    	for(int i=0; i< source_field_names.length; i++){
	    		if(meta.getSourceXMLfield().equals(source_field_names[i])){
	    			data.iSourceXMLField = i;
	    		}
	    	}        	
	    	//Throw exception if source xml field has not been found
	    	if(data.iSourceXMLField == -1) throw new KettleException(BaseMessages.getString(PKG, "XMLJoin.Exception.FieldNotFound", meta.getSourceXMLfield())); //$NON-NLS-1$
        }
        
        if(meta.isComplexJoin() && data.iCompareFieldID == -1){
        	//get the column of the compare value
        	String[] source_field_names = data.SourceRowSet.getRowMeta().getFieldNames();
	    	for(int i=0; i< source_field_names.length; i++){
	    		if(meta.getJoinCompareField().equals(source_field_names[i])){
	    			data.iCompareFieldID= i;
	    		}
	    	}        	
	    	//Throw exception if source xml field has not been found
	    	if(data.iCompareFieldID == -1) throw new KettleException(BaseMessages.getString(PKG, "XMLJoin.Exception.FieldNotFound", meta.getJoinCompareField()));  	 //$NON-NLS-1$
        }
        
        //get XML tags to join
        Document joinDocument;
        
        if (rJoinSource != null){
	        String strJoinXML = (String) rJoinSource[data.iSourceXMLField];
	        
	        
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        try{
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        joinDocument = builder.parse(new InputSource(new StringReader(strJoinXML)));
	        }
	    	catch(Exception e){
	    		throw new KettleException(e);
	    	}
	        
	    	Node node = data.targetDOM.importNode(joinDocument.getDocumentElement(), true);
	    	
	    	if(meta.isComplexJoin()){
	    		String strCompareValue = rJoinSource[data.iCompareFieldID].toString();  
	    		String strXPathStatement = data.XPathStatement.replace("?", strCompareValue); //$NON-NLS-1$
	  
	    		try{
	    			data.targetNode = (Node) xpath.evaluate(strXPathStatement, data.targetDOM, XPathConstants.NODE);
	    			if(data.targetNode == null){
            			throw new KettleXMLException("XPath statement returned no reuslt [" + strXPathStatement +"]"); //$NON-NLS-1$ //$NON-NLS-2$
            		}else{
            			data.targetNode.appendChild(node);
            		}
	    		} catch (Exception e) {
	        		throw new KettleException(e);
	        	}
            }else{
	    		data.targetNode.appendChild(node);
	    	}
	        
        }
               
        return true;
        
    }
    
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(XMLJoinMeta)smi;
        data=(XMLJoinData)sdi;
        if(!super.init(smi, sdi))
            return false;
        
        

       try {
         if(meta.isOmitNullValues()) {
           setSerializer(TransformerFactory.newInstance().newTransformer(new StreamSource(XMLJoin.class.getClassLoader().getResourceAsStream("org/pentaho/di/trans/steps/xmljoin/RemoveNulls.xsl")))); //$NON-NLS-1$
         } else {
           setSerializer(TransformerFactory.newInstance().newTransformer());
         }
    	   if(meta.getEncoding()!=null) {
           	getSerializer().setOutputProperty(OutputKeys.ENCODING, meta.getEncoding());
           }
           
           if(meta.isOmitXMLHeader()) {
               getSerializer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); //$NON-NLS-1$
           }
           
    	    // See if a main step is supplied: in that case move the corresponding rowset to position 0
            // 
			for (int i=0;i<getInputRowSets().size();i++)
			{
			    BlockingRowSet rs = (BlockingRowSet) getInputRowSets().get(i);
			    if (rs.getOriginStepName().equalsIgnoreCase(meta.getTargetXMLstep()))
			    {
			        // swap this one and position 0...
                   // That means, the main stream is always stream 0 --> easy!
                   //
			        BlockingRowSet zero = (BlockingRowSet)getInputRowSets().get(0);
			        getInputRowSets().set(0, rs);
			        getInputRowSets().set(i, zero);
			    }
			}
        } catch (Exception e) {
            return false;
        }

        return true;
    }
    
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(XMLJoinMeta)smi;
        data=(XMLJoinData)sdi;
        
        super.dispose(smi, sdi);
        
    }    

    private void setSerializer(Transformer serializer) {
        this.serializer = serializer;
    }

    private Transformer getSerializer() {
        return serializer;
    }   
}