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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;


public class StartExecutionTransServlet extends HttpServlet
{
    private static final long serialVersionUID = 3634806745372015720L;
    public static final String CONTEXT_PATH = "/kettle/startExec";
    private static LogWriter log = LogWriter.getInstance();
    private TransformationMap transformationMap;
    
    public StartExecutionTransServlet(TransformationMap transformationMap)
    {
        this.transformationMap = transformationMap;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // if (!request.getServletPath().equals(CONTEXT_PATH)) return;
        
        if (log.isDebug()) log.logDebug(toString(), "Start execution of transformation requested");
        response.setStatus(HttpServletResponse.SC_OK);

        String transName = request.getParameter("name");
        boolean useXML = "Y".equalsIgnoreCase( request.getParameter("xml") );

        PrintWriter out = response.getWriter();
        if (useXML)
        {
            response.setContentType("text/xml");
            out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
        }
        else
        {
            response.setContentType("text/html");
            out.println("<HTML>");
            out.println("<HEAD>");
            out.println("<TITLE>Prepare execution of transformation</TITLE>");
            out.println("<META http-equiv=\"Refresh\" content=\"2;url=/kettle/transStatus?name="+transName+"\">");
            out.println("</HEAD>");
            out.println("<BODY>");
        }
    
        try
        {            
            Trans trans = transformationMap.getTransformation(transName);
            if (trans!=null)
            {
                if (trans.isReadyToStart())
                {
                    trans.startThreads();
                    
                    if (useXML)
                    {
                        out.println(WebResult.OK.getXML());
                    }
                    else
                    {
                        out.println("<H1>Transformation '"+transName+"' has been executed.</H1>");
                        out.println("<a href=\"/kettle/transStatus?name="+transName+"\">Back to the transformation status page</a><p>");
                    }
                }
                else
                {
                    String message = "The specified transformation ["+transName+"] is not ready to be started. (Was not prepared for execution)";
                    if (useXML)
                    {
                        out.println(new WebResult(WebResult.STRING_ERROR, message));
                    }
                    else
                    {
                        out.println("<H1>"+message+"</H1>");
                        out.println("<a href=\"/kettle/status\">Back to the status page</a><p>");
                    }
                }
            }
            else
            {
                if (useXML)
                {
                    out.println(new WebResult(WebResult.STRING_ERROR, "The specified transformation ["+transName+"] could not be found"));
                }
                else
                {
                    out.println("<H1>Transformation '"+transName+"' could not be found.</H1>");
                    out.println("<a href=\"/kettle/status\">Back to the status page</a><p>");
                }
            }
        }
        catch (Exception ex)
        {
            if (useXML)
            {
                out.println(new WebResult(WebResult.STRING_ERROR, "Unexpected error during transformation execution preparation:"+Const.CR+Const.getStackTracker(ex)));
            }
            else
            {
                out.println("<p>");
                out.println("<pre>");
                ex.printStackTrace(out);
                out.println("</pre>");
            }
        }

        if (!useXML)
        {
            out.println("<p>");
            out.println("</BODY>");
            out.println("</HTML>");
        }
    }

    public String toString()
    {
        return "Start transformation";
    }
}