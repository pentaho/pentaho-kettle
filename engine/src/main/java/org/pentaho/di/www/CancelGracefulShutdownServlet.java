/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.CarteServlet;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintStream;

@CarteServlet( id = "CancelGracefulShutdownServlet", name = "CancelGracefulShutdownServlet" )
public class CancelGracefulShutdownServlet extends BaseHttpServlet implements CartePluginInterface {
    private static Class<?> PKG = CancelGracefulShutdownServlet.class;

    //    private static final long serialVersionUID = -5459379367791045161L;
    public static final String CONTEXT_PATH = "/kettle/cancelGracefulShutdown";
    public static final String REQUEST_ACCEPTED = "request_accepted";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH)) {
            return;
        }

        if (log.isDebug()) {
            logDebug(BaseMessages.getString(PKG, "cancelGracefulShutdown.shutdownRequest"));
        }

        response.setStatus(HttpServletResponse.SC_OK);
        boolean useXML = "Y".equalsIgnoreCase(request.getParameter("xml"));

        if (useXML) {
            response.setContentType("text/xml");
            response.setCharacterEncoding(Const.XML_ENCODING);
        } else {
            response.setContentType("text/html");
        }

        PrintStream out = new PrintStream(response.getOutputStream());
        final Carte carte = CarteSingleton.getCarte();

        if (useXML) {
            out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
            out.print(XMLHandler.addTagValue(REQUEST_ACCEPTED, carte != null));
        } else {
            out.println("<HTML>");
            out.println(
                    "<HEAD><TITLE>" + BaseMessages.getString(PKG, "cancelGracefulShutdown.shutdownRequest") + "</TITLE></HEAD>");
            out.println("<BODY>");
            out.println("<H1>" + BaseMessages.getString(PKG, "cancelGracefulShutdown.status.label") + "</H1>");
            out.println("<p>");
            if (carte != null) {
                if (!StopCarteServlet.isAcceptJobs()) {
                    logMinimal("is acceptJobs in cancel graceful" + StopCarteServlet.isAcceptJobs());
                    logMinimal(BaseMessages.getString(PKG, "cancelGracefulShutdown.shutdownRequest.status.ok"));
                    StopCarteServlet.setAcceptJobs(true);
                } else {
                    logMinimal("is acceptJobs in cancel graceful else " + StopCarteServlet.isAcceptJobs());
                }
                out.println("<br>");
                out.println(BaseMessages.getString(PKG, "cancelGracefulShutdown.shutdownRequest.status.ok"));
            } else {
                out.println(BaseMessages.getString(PKG, "cancelGracefulShutdown.shutdownRequest.status.notFound"));
            }
            out.println("</p>");
            out.println("</BODY>");
            out.println("</HTML>");
        }
        out.flush();
    }

    @Override
    public String getContextPath() {
        return CONTEXT_PATH;
    }

    @Override
    public String getService() {
        return CONTEXT_PATH + " (" + this + ")";
    }
}
