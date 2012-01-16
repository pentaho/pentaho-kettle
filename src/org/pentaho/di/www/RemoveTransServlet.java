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

package org.pentaho.di.www;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;

public class RemoveTransServlet extends BaseHttpServlet implements CarteServletInterface {
  private static Class<?> PKG = RemoveTransServlet.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$
  
  private static final long	serialVersionUID	= 6618979989596401783L;

  public static final String CONTEXT_PATH = "/kettle/removeTrans";

  public RemoveTransServlet() {
  }
  
  public RemoveTransServlet(TransformationMap transformationMap) {
    super(transformationMap);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH)) {
      return;
    }

    if (log.isDebug())
      logDebug(BaseMessages.getString(PKG, "TransStatusServlet.Log.RemoveTransRequested"));

    String transName = request.getParameter("name");
    String id = request.getParameter("id");
    boolean useXML = "Y".equalsIgnoreCase(request.getParameter("xml"));

    response.setStatus(HttpServletResponse.SC_OK);

    if (useXML) {
      response.setContentType("text/xml");
      response.setCharacterEncoding(Const.XML_ENCODING);
    } else {
      response.setContentType("text/html;charset=UTF-8");
    }

    response.setCharacterEncoding("UTF-8");
    
    PrintWriter out = response.getWriter();

    // ID is optional...
    //
    Trans trans;
    CarteObjectEntry entry;
    if (Const.isEmpty(id)) {
    	// get the first transformation that matches...
    	//
    	entry = getTransformationMap().getFirstCarteObjectEntry(transName);
    	if (entry==null) {
    		trans = null;
    	} else {
    		id = entry.getId();
    		trans = getTransformationMap().getTransformation(entry);
    	}
    } else {
    	// Take the ID into account!
    	//
    	entry = new CarteObjectEntry(transName, id);
    	trans = getTransformationMap().getTransformation(entry);
    }
    
    if (trans != null) {
      
      CentralLogStore.discardLines(trans.getLogChannelId(), true);
      getTransformationMap().removeTransformation(entry);
      
      if (useXML) {
        response.setContentType("text/xml");
        response.setCharacterEncoding(Const.XML_ENCODING);
        out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
        out.print(WebResult.OK.getXML());
      } else {
        response.setContentType("text/html;charset=UTF-8");

        out.println("<HTML>");
        out.println("<HEAD>");
        out.println("<TITLE>" + BaseMessages.getString(PKG, "RemoveTransServlet.TransRemoved") + "</TITLE>");
        out.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        out.println("</HEAD>");
        out.println("<BODY>");
        out.println("<H3>" + BaseMessages.getString(PKG, "RemoveTransServlet.TheTransWasRemoved", transName, id) + "</H3>");
        out.print("<a href=\"" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "\">" + BaseMessages.getString(PKG, "TransStatusServlet.BackToStatusPage") + "</a><br>");
        out.println("<p>");
        out.println("</BODY>");
        out.println("</HTML>");
      }
    } else {
      if (useXML) {
        out.println(new WebResult(WebResult.STRING_ERROR, BaseMessages.getString(PKG, "TransStatusServlet.Log.CoundNotFindSpecTrans", transName)));
      } else {
        out.println("<H1>" + BaseMessages.getString(PKG, "RemoveTransServlet.TransRemoved.Log.CoundNotFindTrans", transName, id) + "</H1>");
        out.println("<a href=\"" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "\">" + BaseMessages.getString(PKG, "TransStatusServlet.BackToStatusPage") + "</a><p>");
      }
    }
  }

  public String toString() {
    return "Remove transformation servlet";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
