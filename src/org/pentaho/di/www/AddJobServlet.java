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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Appender;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobListener;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.StepLoader;



public class AddJobServlet extends HttpServlet
{
    private static final long serialVersionUID = -6850701762586992604L;
    private static LogWriter log = LogWriter.getInstance();
    
    public static final String CONTEXT_PATH = "/kettle/addJob";
    
    private JobMap jobMap;
	private SocketRepository	socketRepository;
    
    public AddJobServlet(JobMap jobMap, SocketRepository socketRepository)
    {
        this.jobMap = jobMap;
        this.socketRepository = socketRepository;
    }
    
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!request.getRequestURI().equals(CONTEXT_PATH+"/")) return;

        if (log.isDebug()) log.logDebug(toString(), "Addition of job requested");

        boolean useXML = "Y".equalsIgnoreCase( request.getParameter("xml") );
        
        PrintWriter out = response.getWriter();
        BufferedReader in = request.getReader(); // read from the client
        if (log.isDetailed()) log.logDetailed(toString(), "Encoding: "+request.getCharacterEncoding());
        
        if (useXML)
        {
            response.setContentType("text/xml");
            out.print(XMLHandler.getXMLHeader());
        }
        else
        {
            response.setContentType("text/html");
            out.println("<HTML>");
            out.println("<HEAD><TITLE>Add job</TITLE></HEAD>");
            out.println("<BODY>");
        }

        response.setStatus(HttpServletResponse.SC_OK);

        try
        {
            // First read the complete transformation in memory from the request
            int c;
            StringBuffer xml = new StringBuffer();
            while ( (c=in.read())!=-1)
            {
                xml.append((char)c);
            }
            
            // Parse the XML, create a job configuration
            //
            // System.out.println(xml);
            //
            JobConfiguration jobConfiguration = JobConfiguration.fromXML(xml.toString());
            JobMeta jobMeta = jobConfiguration.getJobMeta();
            JobExecutionConfiguration jobExecutionConfiguration = jobConfiguration.getJobExecutionConfiguration();
            log.setLogLevel(jobExecutionConfiguration.getLogLevel());
            jobMeta.setArguments(jobExecutionConfiguration.getArgumentStrings());
            jobMeta.injectVariables(jobExecutionConfiguration.getVariables());
            
            // Also copy the parameters over...
            //
            Map<String, String> params = jobExecutionConfiguration.getParams();
            for (String param : params.keySet()) {
            	String value = params.get(param);
            	jobMeta.setParameterValue(param, value);
            }
            
            // If there was a repository, we know about it at this point in time.
            //
            final Repository repository = jobConfiguration.getJobExecutionConfiguration().getRepository();

            // Create the transformation and store in the list...
            //
            final Job job = new Job(LogWriter.getInstance(), StepLoader.getInstance(), repository, jobMeta);
            
            job.setSocketRepository(socketRepository);
            
            Job oldOne = jobMap.getJob(job.getJobname());
            if ( oldOne!=null)
            {
            	if (oldOne.isActive() || (oldOne.isInitialized() && !oldOne.isFinished())) {
            		throw new Exception("A job with the same name exists and is not idle."+Const.CR+"Please stop this job first.");
            	}
            }

        	// Remove the old log appender to avoid memory leaks!
        	//
        	Appender appender = jobMap.getAppender(job.getJobname());
        	if (appender!=null) {
        		log.removeAppender(appender);
        		appender.close();
        	}

            // Setting variables
            //
            job.initializeVariablesFrom(null);
            job.getJobMeta().setInternalKettleVariables(job);
            job.injectVariables(jobConfiguration.getJobExecutionConfiguration().getVariables());
            
            jobMap.addJob(jobMeta.getName(), job, jobConfiguration);
            
            // Make sure to disconnect from the repository when the job finishes.
            // 
            if (repository!=null) {
	            job.addJobListener(new JobListener() {
						public void jobFinished(Job job) {
							repository.disconnect();
						}
					}
	            );
            }

            // Add a listener at the end of the job for the logging!
            //
        	job.addJobListener(new JobListener() {
				public void jobFinished(Job job) {
					try {
					    if (job.isStopped())
					        job.endProcessing(Database.LOG_STATUS_STOP, job.getResult());
					    else
					        job.endProcessing(Database.LOG_STATUS_END, job.getResult());
					} catch(Exception e) {
						log.logError(toString(), "There was an error while logging the job result to the logging table", e);
					}
				}
			});

            String message;
            if (oldOne!=null)
            {
                message = "Job '"+job.getJobname()+"' was replaced in the list.";
            }
            else
            {
                message = "Job '"+job.getJobname()+"' was added to the list.";
            }
            // message+=" (session id = "+request.getSession(true).getId()+")";
            
            if (useXML)
            {
                out.println(new WebResult(WebResult.STRING_OK, message));
            }
            else
            {
                out.println("<H1>"+message+"</H1>");
                out.println("<p><a href=\"/kettle/jobStatus?name="+job.getJobname()+"\">Go to the job status page</a><p>");
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
    
    protected String[] getAllArgumentStrings(Map<String, String> arguments) {
        if (arguments==null || arguments.size()==0) return null;

        String[] argNames = arguments.keySet().toArray(new String[arguments.size()]);
        Arrays.sort(argNames);

        String[] values = new String[argNames.length];
        for (int i=0;i<argNames.length;i++) {
        	values[i] = arguments.get(argNames[i]);
        }

        return values;
    }
    
    public String toString()
    {
        return "Add Job";
    }

}
