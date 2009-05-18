/* * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 

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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowSet;
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
        	if(target_field_id == -1) throw new KettleException(BaseMessages.getString(PKG, "XMLJoin.Exception.FieldNotFound", meta.getTargetXMLfield()));
        	
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
            			throw new KettleXMLException("XPath statement returned no reuslt [" + data.XPathStatement +"]");
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
	            	strOmitXMLHeader = "yes";
	            }
	            else{
	            	strOmitXMLHeader = "no";
	            }
	            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, strOmitXMLHeader);
	            serializer.setOutputProperty(OutputKeys.INDENT, "no");
	            StringWriter sw = new StringWriter();
	            StreamResult resultXML = new StreamResult(sw);
	            DOMSource source = new DOMSource(data.targetDOM);
	            serializer.transform(source, resultXML);
	            
	            String output = sw.toString();
	            int outputIndex = data.outputRowMeta.size()-1;
	            
	            //send the row to the next steps...
	        	putRow(data.outputRowMeta, RowDataUtil.addValueData(data.outputRowData, outputIndex, output));
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
	    	if(data.iSourceXMLField == -1) throw new KettleException(BaseMessages.getString(PKG, "XMLJoin.Exception.FieldNotFound", meta.getSourceXMLfield()));
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
	    	if(data.iCompareFieldID == -1) throw new KettleException(BaseMessages.getString(PKG, "XMLJoin.Exception.FieldNotFound", meta.getJoinCompareField()));  	
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
	    		String strXPathStatement = data.XPathStatement.replace("?", strCompareValue);
	  
	    		try{
	    			data.targetNode = (Node) xpath.evaluate(strXPathStatement, data.targetDOM, XPathConstants.NODE);
	    			if(data.targetNode == null){
            			throw new KettleXMLException("XPath statement returned no reuslt [" + strXPathStatement +"]");
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
    	   setSerializer(TransformerFactory.newInstance().newTransformer());
    	   if(meta.getEncoding()!=null) {
           	getSerializer().setOutputProperty(OutputKeys.ENCODING, meta.getEncoding());
           }
           
           if(meta.isOmitXMLHeader()) {
               getSerializer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
           }
           
    	   this.setSafeModeEnabled(false);
    	   // See if a main step is supplied: in that case move the corresponding rowset to position 0
			for (int i=0;i<inputRowSets.size();i++)
			{
			    RowSet rs = (RowSet) inputRowSets.get(i);
			    if (rs.getOriginStepName().equalsIgnoreCase(meta.getTargetXMLstep()))
			    {
			        // swap this one and position 0...
                   // That means, the main stream is always stream 0 --> easy!
                   //
			        RowSet zero = (RowSet)inputRowSets.get(0);
			        inputRowSets.set(0, rs);
			        inputRowSets.set(i, zero);
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
    
	//
	// Run is were the action happens!
	public void run()
	{		
    	//BaseStep.runStepThread(this, meta, data);
		try
        {
            logBasic("Starting to run...");
            while (processRow(meta, data) && !isStopped());
            
        }
        catch(Exception e)
        {
            logError("Unexpected error : ");
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
	

    private void setSerializer(Transformer serializer) {
        this.serializer = serializer;
    }

    private Transformer getSerializer() {
        return serializer;
    }   
}