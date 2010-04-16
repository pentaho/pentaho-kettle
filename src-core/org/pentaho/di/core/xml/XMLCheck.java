 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.core.xml;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettleException;
import org.xml.sax.helpers.DefaultHandler;


public class XMLCheck {

	 public static class XMLTreeHandler extends DefaultHandler {
		   
	 }
	 
    /**
     * Checks an xml file is well formed.
     * @param file The file to check 
     * @return true if the file is well formed.
     */
	 public static final boolean isXMLFileWellFormed(FileObject file)  throws KettleException {
		boolean retval=false;
		try{
			retval=isXMLWellFormed(file.getContent().getInputStream());
	    } catch (Exception e) {
	        throw new KettleException(e);
	    }
	  
	    return retval; 
	 }
	
    /**
     * Checks an xml string is well formed.
     * @param is inputstream 
     * @return true if the xml is well formed.
     */
	public static final boolean isXMLWellFormed(InputStream is) throws KettleException {
		boolean retval=false;
		try{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			XMLTreeHandler handler = new XMLTreeHandler();
	
			// Parse the input.
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(is,handler);
			retval= true;
	   } catch (Exception e) {
	       throw new KettleException(e);
	   }
	     return retval; 
	}
	
	
}
