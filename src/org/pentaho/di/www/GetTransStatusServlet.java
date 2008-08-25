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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.Log4jStringAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepStatus;



public class GetTransStatusServlet extends HttpServlet
{
    private static final long serialVersionUID = 3634806745372015720L;
    public static final String CONTEXT_PATH = "/kettle/transStatus";
    
    private static LogWriter log = LogWriter.getInstance();
    private TransformationMap transformationMap;
    
    public GetTransStatusServlet(TransformationMap transformationMap)
    {
        this.transformationMap = transformationMap;
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!request.getContextPath().equals(CONTEXT_PATH)) return;
        
        if (log.isDebug()) log.logDebug(toString(),  Messages.getString("TransStatusServlet.Log.TransStatusRequested"));

        String transName = request.getParameter("name");
        boolean useXML = "Y".equalsIgnoreCase( request.getParameter("xml") );

        response.setStatus(HttpServletResponse.SC_OK);

        if (useXML)
        {
            response.setContentType("text/xml");
            response.setCharacterEncoding(Const.XML_ENCODING);
        }
        else
        {
            response.setContentType("text/html");
        }
        
        PrintWriter out = response.getWriter();

        Trans  trans  = transformationMap.getTransformation(transName);
        
        if (trans!=null)
        {
            String status = trans.getStatus();
    
            if (useXML)
            {
                response.setContentType("text/xml");
                response.setCharacterEncoding(Const.XML_ENCODING);
                out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
                
                SlaveServerTransStatus transStatus = new SlaveServerTransStatus(transName, status);
    
                for (int i = 0; i < trans.nrSteps(); i++)
                {
                    BaseStep baseStep = trans.getRunThread(i);
                    if ( (baseStep.isAlive()) || baseStep.getStatus()!=StepDataInterface.STATUS_EMPTY)
                    {
                        StepStatus stepStatus = new StepStatus(baseStep);
                        transStatus.getStepStatusList().add(stepStatus);
                    }
                }
                
                Log4jStringAppender appender = (Log4jStringAppender) transformationMap.getAppender(transName);
                if (appender!=null)
                {
                    // The log can be quite large at times, we are going to put a base64 encoding around a compressed stream
                    // of bytes to handle this one.
                    
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    GZIPOutputStream gzos = new GZIPOutputStream(baos);
                    gzos.write( appender.getBuffer().toString().getBytes() );
                    gzos.close();
                    
                    String loggingString = new String(Base64.encodeBase64(baos.toByteArray()));
                    transStatus.setLoggingString( loggingString );
                }
                
                // Also set the result object...
                //
                transStatus.setResult( trans.getResult() );
                
                // Is the transformation paused?
                //
                transStatus.setPaused( trans.isPaused() );
                
                // Send the result back as XML
                //
                out.println(transStatus.getXML());
            }
            else
            {
                response.setContentType("text/html");

                out.println("<HTML>");
                out.println("<HEAD>");
                out.println("<TITLE>" + Messages.getString("TransStatusServlet.KettleTransStatus")  + "</TITLE>");
                out.println("<META http-equiv=\"Refresh\" content=\"10;url=/kettle/transStatus?name="+URLEncoder.encode(transName, "UTF-8")+"\">");
                out.println("</HEAD>");
                out.println("<BODY>");
                out.println("<H1>" + Messages.getString("TransStatusServlet.TopTransStatus", transName) +"</H1>");
                
                
        
                try
                {
                    out.println("<table border=\"1\">");
                    out.print("<tr> <th>" + Messages.getString("TransStatusServlet.TransName") + "</th> <th>" + Messages.getString("TransStatusServlet.TransStatus") +  "</th> </tr>");
        
                    out.print("<tr>");
                    out.print("<td>"+transName+"</td>");
                    out.print("<td>"+status+"</td>");
                    out.print("</tr>");
                    out.print("</table>");
                    
                    out.print("<p>");
                    
                    if ( (trans.isFinished() && trans.isRunning()) || ( !trans.isRunning() && !trans.isPreparing() && !trans.isInitializing() ))
                    {
                        out.print("<a href=\"/kettle/startTrans?name="+URLEncoder.encode(transName, "UTF-8")+"\">" + Messages.getString("TransStatusServlet.StartTrans") +"</a>");
                        out.print("<p>");
                        out.print("<a href=\"/kettle/prepareExec?name="+URLEncoder.encode(transName, "UTF-8")+"\">" + Messages.getString("TransStatusServlet.PrepareTrans") +"</a><br>");
                        //out.print("<a href=\"/kettle/startExec?name="+URLEncoder.encode(transName, "UTF-8")+"\">" + Messages.getString("TransStatusServlet.StartTrans") + "</a><p>");
                    }
                    else
                    if (trans.isRunning())
                    {
                        out.print("<a href=\"/kettle/pauseTrans?name="+URLEncoder.encode(transName, "UTF-8")+"\">" + Messages.getString("PauseStatusServlet.PauseResumeTrans")  + "</a><br>");
                        out.print("<a href=\"/kettle/stopTrans?name="+URLEncoder.encode(transName, "UTF-8")+"\">" + Messages.getString("TransStatusServlet.StopTrans")  + "</a>");
                        out.print("<p>");
                    }
                    out.print("<a href=\"/kettle/cleanupTrans?name="+URLEncoder.encode(transName, "UTF-8")+"\">" + Messages.getString("TransStatusServlet.CleanupTrans") + "</a>");
                    out.print("<p>");
                    
                    out.println("<table border=\"1\">");
                    out.print("<tr> <th>" +Messages.getString("TransStatusServlet.Stepname") + "</th> <th>"  + Messages.getString("TransStatusServlet.CopyNr") + "</th> <th>" + Messages.getString("TransStatusServlet.Read") + "</th> <th>"+ Messages.getString("TransStatusServlet.Written") + "</th> <th>" +Messages.getString("TransStatusServlet.Input") + "</th> <th>" +Messages.getString("TransStatusServlet.Output") + "</th> " +
                            "<th>" + Messages.getString("TransStatusServlet.Updated") + "</th> <th>" +  Messages.getString("TransStatusServlet.Rejected") + "</th> <th>" +Messages.getString("TransStatusServlet.Errors")  + "</th> <th>" + Messages.getString("TransStatusServlet.Active")  + "</th> <th>" + Messages.getString("TransStatusServlet.Time") + "</th> " +
                            "<th>" + Messages.getString("TransStatusServlet.Speed")+"</th> <th>" + Messages.getString("TransStatusServlet.prinout") + "</th> </tr>");
                   
        
                    for (int i = 0; i < trans.nrSteps(); i++)
                    {
                        BaseStep baseStep = trans.getRunThread(i);
                        if ( (baseStep.isAlive()) || baseStep.getStatus()!=StepDataInterface.STATUS_EMPTY)
                        {
                            StepStatus stepStatus = new StepStatus(baseStep);
                            out.print(stepStatus.getHTMLTableRow());
                        }
                    }
                    out.println("</table>");
                    out.println("<p>");
                    
                    out.print("<a href=\"/kettle/transStatus/?name="+URLEncoder.encode(transName, "UTF-8")+"&xml=y\">" +  Messages.getString("TransStatusServlet.ShowAsXml")  +"</a><br>");
                    out.print("<a href=\"/kettle/status\">" + Messages.getString("TransStatusServlet.BackToStatusPage")  +"</a><br>");
                    out.print("<p><a href=\"/kettle/transStatus?name="+URLEncoder.encode(transName, "UTF-8")+"\">" +  Messages.getString("TransStatusServlet.Refresh")  + "</a>");
                    
                    // Put the logging below that.
                    Log4jStringAppender appender = (Log4jStringAppender) transformationMap.getAppender(transName);
                    if (appender!=null)
                    {
                        out.println("<p>");
                        /*
                        out.println("<pre>");
                        out.println(appender.getBuffer().toString());
                        out.println("</pre>");
                        */
                        out.println("<textarea id=\"translog\" cols=\"120\" rows=\"20\" wrap=\"off\" name=\"Transformation log\" readonly=\"readonly\">"+appender.getBuffer().toString()+"</textarea>");
                        
                        out.println("<script type=\"text/javascript\"> ");
                        out.println("  translog.scrollTop=translog.scrollHeight; ");
                        out.println("</script> ");
                        out.println("<p>");
                    }
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
        else
        {
            if (useXML)
            {
                out.println(new WebResult(WebResult.STRING_ERROR, Messages.getString("TransStatusServlet.Log.CoundNotFindSpecTrans",transName)));
            }
            else
            {
                out.println("<H1>" + Messages.getString("TransStatusServlet.Log.CoundNotFindTrans",transName)  + "</H1>");
                out.println("<a href=\"/kettle/status\">" + Messages.getString("TransStatusServlet.BackToStatusPage")+ "</a><p>");
                
            }
        }
    }

    public String toString()
    {
        return "Trans Status Handler";
    }
}
