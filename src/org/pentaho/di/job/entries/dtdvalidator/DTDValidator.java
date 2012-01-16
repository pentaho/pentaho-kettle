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

package org.pentaho.di.job.entries.dtdvalidator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class DTDValidator {
	private static Class<?> PKG = JobEntryDTDValidator.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String xmlfilename;
	private String xsdfilename;
	private boolean interndtd;
	private String errormessage;
	private int errorscount;
	
	private LogChannelInterface log;
	
	public DTDValidator(LogChannelInterface log) {
		this.log=log;
		this.xmlfilename=null;
		this.xsdfilename=null;
		this.interndtd=false;
		this.errormessage=null;
		this.errorscount=0;
	}
	
	public void setXMLFilename(String xmlfilename) {
		this.xmlfilename=xmlfilename;
	}
	public String getXMLFilename() {
		return this.xmlfilename;
	}
	public void setDTDFilename(String xsdfilename) {
		this.xsdfilename=xsdfilename;
	}
	public String getDTDFilename() {
		return this.xsdfilename;
	}
	public void setInternDTD(boolean value) {
		this.interndtd=value;
	}
	public boolean isInternDTD() {
		return this.interndtd;
	}
	
	private void setErrorMessage(String value) {
		this.errormessage=value;
	}
	public String getErrorMessage() {
		return this.errormessage;
	}
	public int getNrErrors() {
		return this.errorscount;
	}
	private void setNrErrors(int value) {
		this.errorscount=value;
	}
	public boolean validate() {
		
		boolean retval=false;
		
		FileObject xmlfile = null;
		FileObject DTDfile = null;

		ByteArrayInputStream ba=null;
		try {
			if (xmlfilename!=null &&  ((getDTDFilename()!=null && !isInternDTD()) || (isInternDTD()))   ) {
				xmlfile = KettleVFS.getFileObject(getXMLFilename());
				
				if (xmlfile.exists())  {	
					
					URL xmlFile = new File(KettleVFS.getFilename(xmlfile)).toURI().toURL();
					StringBuffer xmlStringbuffer = new StringBuffer("");
					
					BufferedReader xmlBufferedReader=null;
					InputStreamReader is=null;
					try  {
						// open XML File
						is=new InputStreamReader(xmlFile.openStream());
						xmlBufferedReader = new BufferedReader(is);
					
						char[] buffertXML = new char[1024];
						int LenXML = -1;
						while ((LenXML = xmlBufferedReader.read(buffertXML)) != -1)
							xmlStringbuffer.append(buffertXML, 0,LenXML);
					}finally {
						if(is!=null) is.close();
						if(xmlBufferedReader!=null) xmlBufferedReader.close();
					}
					
					
					// Prepare parsing ...
					DocumentBuilderFactory DocBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder DocBuilder = DocBuilderFactory.newDocumentBuilder();

					// Let's try to get XML document encoding

					DocBuilderFactory.setValidating(false);
					ba=new ByteArrayInputStream(xmlStringbuffer.toString().getBytes("UTF-8"));
					Document xmlDocDTD = DocBuilder.parse(ba);
					if(ba!=null) ba.close();
					
					String encoding = null;
					if (xmlDocDTD.getXmlEncoding() == null)  {
						encoding = "UTF-8";
					} else {
						encoding = xmlDocDTD.getXmlEncoding();
					}
				
					int xmlStartDTD = xmlStringbuffer.indexOf("<!DOCTYPE");
					 
					if (isInternDTD()) {
						// DTD find in the XML document
						if (xmlStartDTD != -1) {
							log.logBasic(BaseMessages.getString(PKG, "JobEntryDTDValidator.ERRORDTDFound.Label", getXMLFilename()));
						} else {
							setErrorMessage(BaseMessages.getString(PKG, "JobEntryDTDValidator.ERRORDTDNotFound.Label", getXMLFilename()));
						}
						
					} else {
						// DTD in external document
						// If we find an intern declaration, we remove it
						DTDfile = KettleVFS.getFileObject(getDTDFilename());
						
						if (DTDfile.exists()) {
							if (xmlStartDTD != -1) {
								int EndDTD = xmlStringbuffer.indexOf(">",xmlStartDTD);
								//String DocTypeDTD = xmlStringbuffer.substring(xmlStartDTD, EndDTD + 1);
								xmlStringbuffer.replace(xmlStartDTD,EndDTD + 1, "");
							}
													
							String xmlRootnodeDTD = xmlDocDTD.getDocumentElement().getNodeName();
								
							String RefDTD = "<?xml version='"
								+ xmlDocDTD.getXmlVersion() + "' encoding='"
								+ encoding + "'?>\n<!DOCTYPE " + xmlRootnodeDTD
								+ " SYSTEM '" + KettleVFS.getFilename(DTDfile) + "'>\n";
	
							int xmloffsetDTD = xmlStringbuffer.indexOf("<"+ xmlRootnodeDTD);
							xmlStringbuffer.replace(0, xmloffsetDTD,RefDTD);
						} else {
							log.logError(BaseMessages.getString(PKG, "JobEntryDTDValidator.ERRORDTDFileNotExists.Subject"), BaseMessages.getString(PKG, "JobEntryDTDValidator.ERRORDTDFileNotExists.Msg",getDTDFilename()));
						}
					}
						
					if ((isInternDTD() && xmlStartDTD == -1 || (!isInternDTD() && !DTDfile.exists()))) {
					} else {
						
						// Let's parse now ...
						MyErrorHandler error = new MyErrorHandler();
						DocBuilderFactory.setValidating(true);
						DocBuilder = DocBuilderFactory.newDocumentBuilder();
						DocBuilder.setErrorHandler(error);

						ba=	new ByteArrayInputStream(xmlStringbuffer.toString().getBytes(encoding));
						xmlDocDTD = DocBuilder.parse(ba);
									
						if(error.errorMessage==null) {
							log.logBasic(BaseMessages.getString(PKG, "JobEntryDTDValidator.DTDValidatorOK.Subject"),
									BaseMessages.getString(PKG, "JobEntryDTDValidator.DTDValidatorOK.Label",		
											getXMLFilename()));
							
							// Everything is OK
							retval=true;
						}else {
							// Invalid DTD
							setNrErrors(error.nrErrors);
							setErrorMessage(BaseMessages.getString(PKG, "JobEntryDTDValidator.DTDValidatorKO",	getXMLFilename(), error.nrErrors, error.errorMessage));
						}
					}
					
				} else {
					if(	!xmlfile.exists()) {
						setErrorMessage(BaseMessages.getString(PKG, "JobEntryDTDValidator.FileDoesNotExist.Label",	getXMLFilename()));
					}
				}
			} else {
				setErrorMessage(BaseMessages.getString(PKG, "JobEntryDTDValidator.AllFilesNotNull.Label"));
			}
		} catch ( Exception e ) {
			setErrorMessage(BaseMessages.getString(PKG, "JobEntryDTDValidator.ErrorDTDValidator.Label",		
							getXMLFilename(),getDTDFilename(), e.getMessage()));
		} finally {
			try  {
			    if ( xmlfile != null ) xmlfile.close();
			    if ( DTDfile != null ) DTDfile.close();
			    if(ba!=null) ba.close();	
		    } catch ( IOException e ) {};			
		}
		return retval;
	}
	
	private static class MyErrorHandler implements ErrorHandler {
		 String errorMessage = null;
		 int error = -1;
		 int nrErrors=0;
	      public void warning(SAXParseException e) throws SAXException {
	    	  error=0;
	    	  allErrors(e);
	      }
	      public void error(SAXParseException e) throws SAXException {
	    	  error=1;
	    	  allErrors(e);
	      }
	      public void fatalError(SAXParseException e) throws SAXException {
	    	  error=2;
	    	  allErrors(e);
	      }
	      private void allErrors (SAXParseException e) {
	    	  nrErrors++;
	    	  if(errorMessage==null) errorMessage="";
	    	  errorMessage+=  Const.CR + Const.CR + "Error Nr." + nrErrors + " (" ;
	  		  switch(error) {
					case 0 : errorMessage+="Warning"; break;
					case 1 : errorMessage+="Error"; break;
					case 2  : errorMessage+="FatalError"; break;
					default: break;
				}
	  		  errorMessage+=")" +  Const.CR
	          + "              Public ID: "+e.getPublicId() + Const.CR
	          + "              System ID: "+e.getSystemId() + Const.CR
	          + "              Line number: "+e.getLineNumber() + Const.CR
	          + "              Column number: "+e.getColumnNumber() + Const.CR
	          + "              Message: "+e.getMessage();
	      }
	   }

}
