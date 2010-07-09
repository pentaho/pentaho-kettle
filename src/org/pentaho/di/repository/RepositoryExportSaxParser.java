/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.repository;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.pentaho.di.core.xml.XMLHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;


public class RepositoryExportSaxParser extends DefaultHandler2 {

  public static final String STRING_REPOSITORY = "repository";
  public static final String STRING_TRANSFORMATIONS = "transformations";
  public static final String STRING_TRANSFORMATION = "transformation";
  public static final String STRING_JOBS = "jobs";
  public static final String STRING_JOB = "job";
  
  private SAXParserFactory factory;
  private SAXParser saxParser;
  
  private RepositoryElementReadListener repositoryElementReadListener;
  
  private StringBuffer xml;
  private boolean add;
  private String filename;
  private boolean cdata;
  
  RepositoryImportFeedbackInterface feedback;
  
  public RepositoryExportSaxParser(String filename, RepositoryImportFeedbackInterface feedback) throws Exception {
    this.filename = filename;
    this.feedback = feedback;
    this.xml = new StringBuffer(50000);
    this.add=false;
    this.cdata=false;
  }
  
  public void parse(RepositoryElementReadListener repositoryElementReadListener) throws Exception {
    this.repositoryElementReadListener = repositoryElementReadListener;
    
    this.factory = SAXParserFactory.newInstance();
    this.saxParser = this.factory.newSAXParser();
    this.saxParser.parse(filename, this);
  }
  
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (STRING_REPOSITORY.equals(qName) || STRING_TRANSFORMATIONS.equals(qName) || STRING_JOBS.equals(qName)) {
      add=false;
    } else {
      add=true;
    }
    
    if (add) {
    
      // A new job or transformation?
      //
      if (STRING_TRANSFORMATION.equals(qName) || STRING_JOB.equals(qName)) {
        xml.setLength(0);
      }

      xml.append(XMLHandler.openTag(qName));
    }
  }
  
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (add) {
      xml.append(XMLHandler.closeTag(qName));
    }
    
    if (STRING_TRANSFORMATION.equals(qName)) {
      if (!repositoryElementReadListener.transformationElementRead(xml.toString(), feedback)) {
        saxParser.reset();
      }
    }
    
    if (STRING_JOB.equals(qName)) {
      if (!repositoryElementReadListener.jobElementRead(xml.toString(), feedback)) {
        saxParser.reset();
      }
    }
  }

  public void startCDATA() throws SAXException {
    cdata=true;
  }
  
  public void endCDATA() throws SAXException {
    cdata=false;
  }
  
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (add) {
      
      String string = new String(ch, start, length);
      
      if (cdata) {
        xml.append(XMLHandler.buildCDATA(string)); 
      } else {
        XMLHandler.appendReplacedChars(xml, string);
      }
    }
  }  
  
  @Override
  public void fatalError(SAXParseException e) throws SAXException {
    repositoryElementReadListener.fatalXmlErrorEncountered(e);
  }
}
