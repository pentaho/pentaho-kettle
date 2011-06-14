/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.www;

import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.xml.XMLHandler;

/**
 * This servlet allows a client (TransSplitter in our case) to ask for a port number.<br>
 * This port number will be allocated in such a way that the port number is unique for a given hostname.<br>
 * This in turn will ensure that all the slaves will use valid port numbers, even if multiple slaves run on the same host.
 * 
 * @author matt
 * 
 */
public class NextSequenceValueServlet extends BaseHttpServlet implements CarteServletInterface {
  private static final long serialVersionUID = 3634806745372015720L;

  public static final String CONTEXT_PATH = "/kettle/nextSequence";

  public static final String PARAM_NAME = "name";
  public static final String PARAM_INCREMENT = "increment";

  public static final String XML_TAG = "seq";
  public static final String XML_TAG_VALUE = "value";
  public static final String XML_TAG_INCREMENT = "increment";
  public static final String XML_TAG_ERROR = "error";

  public NextSequenceValueServlet() {
  }

  public NextSequenceValueServlet(TransformationMap transformationMap) {
    super(transformationMap);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH))
      return;

    if (log.isDebug()) logDebug(toString());
    
    String name = request.getParameter(PARAM_NAME);
    long increment = Const.toLong(request.getParameter(PARAM_INCREMENT), 10000);
    
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("text/xml");
    response.setCharacterEncoding(Const.XML_ENCODING);
    
    PrintStream out = new PrintStream(response.getOutputStream());
    out.println(XMLHandler.getXMLHeader(Const.XML_ENCODING));
    out.println(XMLHandler.openTag(XML_TAG));

    try {
    
      SlaveSequence slaveSequence = getTransformationMap().getSlaveSequence(name);
      if (slaveSequence==null && getTransformationMap().isAutomaticSlaveSequenceCreationAllowed()) {
        slaveSequence = getTransformationMap().createSlaveSequence(name);
      }
      if (slaveSequence==null) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        out.println(XMLHandler.addTagValue(XML_TAG_ERROR, "Slave sequence '"+name+"' could not be found."));
      } else {
          LoggingObjectInterface loggingObject = new SimpleLoggingObject("Carte", LoggingObjectType.CARTE, null);
          long nextValue = slaveSequence.getNextValue(loggingObject, increment);
          out.println(XMLHandler.addTagValue(XML_TAG_VALUE, nextValue));
          out.println(XMLHandler.addTagValue(XML_TAG_INCREMENT, increment));
      }

    } catch(Exception e) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      out.println(XMLHandler.addTagValue(XML_TAG_ERROR, "Error retrieving next value from slave sequence: "+Const.getStackTracker(e)));
    }


    out.println(XMLHandler.closeTag(XML_TAG));
  }

  public String toString() {
    return "Retrieve the next value of slave server sequence requested.";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
