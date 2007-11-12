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
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransMeta;



public class AddTransServlet extends HttpServlet
{
    private static final long serialVersionUID = -6850701762586992604L;
    private static LogWriter log = LogWriter.getInstance();
    
    public static final String CONTEXT_PATH = "/kettle/addTrans";
    
    private TransformationMap transformationMap;
    
    public AddTransServlet(TransformationMap transformationMap)
    {
        this.transformationMap = transformationMap;
    }
    
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!request.getRequestURI().equals(CONTEXT_PATH+"/")) return;

        if (log.isDebug()) log.logDebug(toString(), "Addition of transformation requested");

        boolean useXML = "Y".equalsIgnoreCase( request.getParameter("xml") );
        
        PrintWriter out = response.getWriter();
        InputStream is = request.getInputStream(); // read from the client

        if (useXML)
        {
            response.setContentType("text/xml");
            out.print(XMLHandler.getXMLHeader());
        }
        else
        {
            response.setContentType("text/html");
            out.println("<HTML>");
            out.println("<HEAD><TITLE>Add transformation</TITLE></HEAD>");
            out.println("<BODY>");
        }

        response.setStatus(HttpServletResponse.SC_OK);

        try
        {
            // First read the complete transformation in memory from the inputStream
            int c;
            StringBuffer xml = new StringBuffer();
            while ( (c=is.read())!=-1)
            {
                xml.append((char)c);
            }
            
            // Parse the XML, create a transformation configuration
            //
            TransConfiguration transConfiguration = TransConfiguration.fromXML(xml.toString());
            TransMeta transMeta = transConfiguration.getTransMeta();
            transMeta.injectVariables(transConfiguration.getTransExecutionConfiguration().getVariables());
            
            // If there was a repository, we know about it at this point in time.
            //
            final Repository repository = transConfiguration.getTransExecutionConfiguration().getRepository();
            
            // Create the transformation and store in the list...
            //
            final Trans trans = new Trans(transMeta);
            trans.setRepository(repository);
            
            Trans oldOne = transformationMap.getTransformation(trans.getName());
            if ( oldOne!=null && !oldOne.isFinished())
            {
                if ( oldOne.isRunning() || oldOne.isPreparing() || oldOne.isInitializing() )
                {
                    throw new Exception("A transformation with the same name exists and is not idle."+Const.CR+"Please stop the transformation first.");
                }
            }
            
            transformationMap.addTransformation(transMeta.getName(), trans, transConfiguration);

            if (repository!=null)
            {
	            // The repository connection is open: make sure we disconnect from the repository once we
	            // are done with this transformation.
	            //
	            new Thread(new Runnable() {
					public void run() 
					{
						while (!trans.isFinished())
						{
							try { Thread.sleep(250); } catch (InterruptedException e) { }
						}
						// Once we're done, we disconnect from the repository...
						//
						repository.disconnect();
					}
				
				}).start();
            }
            

            
            String message;
            if (oldOne!=null)
            {
                message = "Transformation '"+trans.getName()+"' was replaced in the list.";
            }
            else
            {
                message = "Transformation '"+trans.getName()+"' was added to the list.";
            }
            // message+=" (session id = "+request.getSession(true).getId()+")";
            
            if (useXML)
            {
                out.println(new WebResult(WebResult.STRING_OK, message));
            }
            else
            {
                out.println("<H1>"+message+"</H1>");
                out.println("<p><a href=\"/kettle/transStatus?name="+trans.getName()+"\">Go to the transformation status page</a><p>");
            }
        }
        catch (Exception ex)
        {
            if (useXML)
            {
                out.println(new WebResult(WebResult.STRING_ERROR, Const.getStackTracker(ex)));
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
        return "Add Transformation";
    }

}
