 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 /**********************************************************************
 **                                                                   **
 ** This Script has been modified for higher performance              **
 ** and more functionality in December-2006,                          **
 ** by proconis GmbH / Germany                                        **
 **                                                                   ** 
 ** http://www.proconis.de                                            **
 ** info@proconis.de                                                  **
 **                                                                   **
 **********************************************************************/
package org.pentaho.di.ui.trans.steps.script;

import java.io.InputStream;
import java.util.Hashtable;

import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ScriptHelp {

	private static Document dom;
	private static Hashtable<String, String> hatFunctionsList;
	
	
	public ScriptHelp(String strFileName) throws KettleXMLException {
		super();
		xparseXmlFile(strFileName);
		buildFunctionList();
		
	}
	public Hashtable<String, String> getFunctionList(){
		return hatFunctionsList;
	}
	
	private static void buildFunctionList(){
		hatFunctionsList = new Hashtable<String, String>();
		NodeList nlFunctions = dom.getElementsByTagName("jsFunction");
		for(int i=0;i<nlFunctions.getLength();i++){
			String strFunctionName = nlFunctions.item(i).getAttributes().getNamedItem("name").getNodeValue();
			Node elType = ((Element)nlFunctions.item(i)).getElementsByTagName("type").item(0);
			String strType = "";
			if(elType.hasChildNodes()) strType = elType.getFirstChild().getNodeValue();
			NodeList nlFunctionArgs=((Element)nlFunctions.item(i)).getElementsByTagName("argument");
			for(int j=0;j<nlFunctionArgs.getLength();j++){
				String strFunctionArgs=nlFunctionArgs.item(j).getFirstChild().getNodeValue();
				hatFunctionsList.put(strFunctionName +"("+strFunctionArgs+")", strType);
			}
			if(nlFunctionArgs.getLength()==0) hatFunctionsList.put(strFunctionName +"()", strType);
		}
	}
	
	public String getSample(String strFunctionName, String strFunctionNameWithArgs){
		String sRC="// Sorry, no Script available for "+ strFunctionNameWithArgs;
		
		NodeList nl = dom.getElementsByTagName("jsFunction");
		for(int i=0;i<nl.getLength();i++){
			if(nl.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(strFunctionName)){
				Node elSample = ((Element)nl.item(i)).getElementsByTagName("sample").item(0);
				if(elSample.hasChildNodes()) return(elSample.getFirstChild().getNodeValue());
			}
		}
		return sRC;
	}
	
	private static void xparseXmlFile(String strFileName) throws KettleXMLException{
        try
        {
            InputStream is = ScriptHelp.class.getResourceAsStream(strFileName);
            int c;
            StringBuffer buffer = new StringBuffer();
            while ( (c=is.read())!=-1 ) buffer.append((char)c);
            is.close();
            dom = XMLHandler.loadXMLString(buffer.toString());
        }
        catch(Exception e)
        {
            throw new KettleXMLException("Unable to read script values help file from file ["+strFileName+"]", e);
        }
	}	
}
