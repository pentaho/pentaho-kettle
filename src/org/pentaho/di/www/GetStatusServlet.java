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
import java.net.URLEncoder;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;

public class GetStatusServlet extends HttpServlet
{
    private static final long serialVersionUID = 3634806745372015720L;
    
    public static final String CONTEXT_PATH = "/kettle/status";
    
    private static LogWriter log = LogWriter.getInstance();
    
    private TransformationMap transformationMap;

	private JobMap jobMap;
    
    public GetStatusServlet(TransformationMap transformationMap, JobMap jobMap)
    {
        this.transformationMap = transformationMap;
        this.jobMap = jobMap;
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!request.getContextPath().equals(CONTEXT_PATH)) return;
        
        if (log.isDebug()) log.logDebug(toString(), Messages.getString("GetStatusServlet.StatusRequested"));
        response.setStatus(HttpServletResponse.SC_OK);
        
        boolean useXML = "Y".equalsIgnoreCase( request.getParameter("xml") );

        if (useXML)
        {
            response.setContentType("text/xml");
            response.setCharacterEncoding(Const.XML_ENCODING);
        }
        else
        {
            response.setContentType("text/html");
        }
        
        PrintStream out = new PrintStream(response.getOutputStream());
        
        String[] transNames = transformationMap.getTransformationNames();
        String[] jobNames = jobMap.getJobNames();
        
        if (useXML)
        {
            out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
            SlaveServerStatus serverStatus = new SlaveServerStatus();
            serverStatus.setStatusDescription("Online");
            
            Arrays.sort(transNames);

            for (int i=0;i<transNames.length;i++)
            {
                String name   = transNames[i]; 
                Trans  trans  = transformationMap.getTransformation(name);
                String status = trans.getStatus();
               
                SlaveServerTransStatus sstatus = new SlaveServerTransStatus(name, status);
                sstatus.setPaused(trans.isPaused());
                serverStatus.getTransStatusList().add(sstatus);
            }

            Arrays.sort(jobNames);

            for (int i=0;i<jobNames.length;i++)
            {
                String name   = jobNames[i]; 
                Job job       = jobMap.getJob(name);
                String status = job.getStatus();
                
                serverStatus.getJobStatusList().add( new SlaveServerJobStatus(name, status) );
            }

            out.println(serverStatus.getXML());
        }
        else
        {    
            out.println("<HTML>");
            out.println("<HEAD><TITLE>" + Messages.getString("GetStatusServlet.KettleSlaveServerStatus") + "</TITLE></HEAD>");
            out.println("<BODY>");
            out.println("<H1>" + Messages.getString("GetStatusServlet.TopStatus") + "</H1>");
    
    
            try
            {
                out.println("<table border=\"1\">");
                out.print("<tr> <th>" + Messages.getString("GetStatusServlet.TransName") + "</th> <th>" + Messages.getString("GetStatusServlet.Status") + "</th> <th>" + Messages.getString("GetStatusServlet.LastLogDate") + "</th> </tr>");

                for (int i=0;i<transNames.length;i++)
                {
                    String name   = transNames[i]; 
                    Trans  trans  = transformationMap.getTransformation(name);
                    String status = trans.getStatus();
                    
                    out.print("<tr>");
                    out.print("<td><a href=\"/kettle/transStatus?name="+URLEncoder.encode(name, "UTF-8")+"\">"+name+"</a></td>");
                    out.print("<td>"+status+"</td>");
                    out.print("<td>"+( trans.getLogDate()==null ? "-" : XMLHandler.date2string( trans.getLogDate() ))+"</td>");
                    out.print("</tr>");
                }
                out.print("</table><p>");
                
                out.println("<table border=\"1\">");
                out.print("<tr> <th>" + Messages.getString("GetStatusServlet.JobName") + "</th> <th>" + Messages.getString("GetStatusServlet.Status") + "</th> <th>" + Messages.getString("GetStatusServlet.LastLogDate") + "</th> </tr>");

                for (int i=0;i<jobNames.length;i++)
                {
                    String name   = jobNames[i]; 
                    Job job       = jobMap.getJob(name);
                    String status = job.getStatus();
                    
                    out.print("<tr>");
                    out.print("<td><a href=\"/kettle/jobStatus?name="+URLEncoder.encode(name, "UTF-8")+"\">"+name+"</a></td>");
                    out.print("<td>"+status+"</td>");
                    out.print("<td>"+( job.getLogDate()==null ? "-" : XMLHandler.date2string( job.getLogDate() ))+"</td>");
                    out.print("</tr>");
                }
                out.print("</table>");

            }
            catch (Exception ex)
            {
                out.println("<p>");
                out.println("<pre>");
                ex.printStackTrace(out);
                out.println("</pre>");
            }
    
            out.println("<p>");
            out.println("</BODY>");
            out.println("</HTML>");
        }        
    }

    public String toString()
    {
        return "Status Handler";
    }
}
