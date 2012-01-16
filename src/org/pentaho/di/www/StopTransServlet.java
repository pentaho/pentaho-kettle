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
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;

public class StopTransServlet extends BaseHttpServlet implements CarteServletInterface {
  private static Class<?> PKG = StopTransServlet.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private static final long serialVersionUID = 3634806745372015720L;
  public static final String CONTEXT_PATH = "/kettle/stopTrans";

  public StopTransServlet() {
  }
  
  public StopTransServlet(TransformationMap transformationMap) {
    super(transformationMap);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH)) {
      return;
    }

    if (log.isDebug())
      logDebug(BaseMessages.getString(PKG, "StopTransServlet.StopOfTransRequested"));

    String transName = request.getParameter("name");
    String id = request.getParameter("id");
    boolean useXML = "Y".equalsIgnoreCase(request.getParameter("xml"));

    PrintWriter out = response.getWriter();
    try {
      if (useXML) {
        response.setContentType("text/xml");
        response.setCharacterEncoding(Const.XML_ENCODING);
        out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
      } else {
        response.setContentType("text/html;charset=UTF-8");
        out.println("<HTML>");
        out.println("<HEAD>");
        out.println("<TITLE>" + BaseMessages.getString(PKG, "StopTransServlet.StopTrans") + "</TITLE>");
        out.println("<META http-equiv=\"Refresh\" content=\"2;url=" + convertContextPath(GetTransStatusServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(transName, "UTF-8")
            + "\">");
        out.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        out.println("</HEAD>");
        out.println("<BODY>");
      }

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
        trans.stopAll();

        String message = BaseMessages.getString(PKG, "StopTransServlet.TransSopRequested", transName);

        if (useXML) {
          out.println(new WebResult(WebResult.STRING_OK, message, id).getXML());
        } else {
          out.println("<H1>" + message + "</H1>");
          out.println("<a href=\"" + convertContextPath(GetTransStatusServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(transName, "UTF-8") + "&id="+id+"\">"
              + BaseMessages.getString(PKG, "TransStatusServlet.BackToTransStatusPage") + "</a><p>");
        }
      } else {
        String message = BaseMessages.getString(PKG, "StopTransServlet.CanNotFindTrans", transName);

        if (useXML) {
          out.println(new WebResult(WebResult.STRING_ERROR, message, id).getXML());
        } else {
          out.println("<H1>" + message + "</H1>");
          out.println("<a href=\"" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "\">" + BaseMessages.getString(PKG, "TransStatusServlet.BackToStatusPage") + "</a><p>");
        }
      }
    } catch (Exception ex) {
      if (useXML) {
        out.println(new WebResult(WebResult.STRING_ERROR, Const.getStackTracker(ex)).getXML());
      } else {
        out.println("<p>");
        out.println("<pre>");
        ex.printStackTrace(out);
        out.println("</pre>");
      }
    }

    if (!useXML) {
      out.println("<p>");
      out.println("</BODY>");
      out.println("</HTML>");
    }
  }

  public String toString() {
    return "Stop transformation";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
