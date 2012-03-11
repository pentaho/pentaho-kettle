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
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;

public class StartTransServlet extends BaseHttpServlet implements CarteServletInterface {

	private static Class<?>		PKG					= StartTransServlet.class;	// for
																				// i18n
																				// purposes,
																				// needed
																				// by
																				// Translator2!!
																				// $NON-NLS-1$
	private static final long	serialVersionUID	= -5879200987669847357L;

	public static final String	CONTEXT_PATH		= "/kettle/startTrans";

	public StartTransServlet() {
	}

	public StartTransServlet(TransformationMap transformationMap) {
		super(transformationMap);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH)) {
			return;
		}

		if (log.isDebug())
			logDebug(BaseMessages.getString(PKG, "StartTransServlet.Log.StartTransRequested"));

		String transName = request.getParameter("name");
		String id = request.getParameter("id");
		if (StringUtils.isEmpty(transName)) {
			transName = "";
		}
		boolean useXML = "Y".equalsIgnoreCase(request.getParameter("xml"));

		response.setStatus(HttpServletResponse.SC_OK);

		PrintWriter out = response.getWriter();
		if (useXML) {
			response.setContentType("text/xml");
			response.setCharacterEncoding(Const.XML_ENCODING);
			out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
		} else {
		  response.setContentType("text/html;charset=UTF-8");
			out.println("<HTML>");
			out.println("<HEAD>");
			out.println("<TITLE>" + BaseMessages.getString(PKG, "StartTransServlet.Log.StartOfTrans") + "</TITLE>");
			out.println("<META http-equiv=\"Refresh\" content=\"2;url=" + convertContextPath(GetTransStatusServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(transName, "UTF-8") + "\">");
			out.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
			out.println("</HEAD>");
			out.println("<BODY>");
		}

		try {
			// ID is optional...
			//
			Trans trans;
			CarteObjectEntry entry;
			if (Const.isEmpty(id)) {
				// get the first transformation that matches...
				//
				entry = getTransformationMap().getFirstCarteObjectEntry(transName);
				if (entry == null) {
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
				
	            // Discard old log lines from old transformation runs
	            //
	            CentralLogStore.discardLines(trans.getLogChannelId(), true);
				
				String carteObjectId = UUID.randomUUID().toString();
				SimpleLoggingObject servletLoggingObject = new SimpleLoggingObject(CONTEXT_PATH, LoggingObjectType.CARTE, null);
				servletLoggingObject.setContainerObjectId(carteObjectId);
				servletLoggingObject.setLogLevel(trans.getLogLevel());
				trans.setParent(servletLoggingObject);
				
				executeTrans(trans);

				String message = BaseMessages.getString(PKG, "StartTransServlet.Log.TransStarted", transName);
				if (useXML) {
					out.println(new WebResult(WebResult.STRING_OK, message).getXML());
				} else {

					out.println("<H1>" + message + "</H1>");
					out.println("<a href=\"" + convertContextPath(GetTransStatusServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(transName, "UTF-8") + "&id="+id+"\">" + BaseMessages.getString(PKG, "TransStatusServlet.BackToStatusPage") + "</a><p>");
				}
			} else {
				String message = BaseMessages.getString(PKG, "TransStatusServlet.Log.CoundNotFindSpecTrans", transName);
				if (useXML) {
					out.println(new WebResult(WebResult.STRING_ERROR, message, id));
				} else {
					out.println("<H1>" + message + "</H1>");
					out.println("<a href=\"" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "\">" + BaseMessages.getString(PKG, "TransStatusServlet.BackToStatusPage") + "</a><p>");
				}
			}
		} catch (Exception ex) {
			if (useXML) {
				out.println(new WebResult(WebResult.STRING_ERROR, BaseMessages.getString(PKG, "StartTransServlet.Error.UnexpectedError", Const.CR + Const.getStackTracker(ex))));
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
		return "Start transformation";
	}

	public String getService() {
		return CONTEXT_PATH + " (" + toString() + ")";
	}

	protected void executeTrans(Trans trans) throws KettleException { 
	   trans.execute(null);
	} 
}
