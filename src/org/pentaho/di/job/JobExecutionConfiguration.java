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
package org.pentaho.di.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.w3c.dom.Node;


public class JobExecutionConfiguration implements Cloneable
{
    public static final String XML_TAG = "job_execution_configuration";
    
    private boolean executingLocally;
    
    private boolean executingRemotely;
    private SlaveServer remoteServer;
    
    private Map<String, String> arguments;
    private Map<String, String> params;
    private Map<String, String> variables;
    
    private Date     replayDate;
    private boolean  safeModeEnabled;
    private LogLevel logLevel;
    private boolean  clearingLog;
    
    private Result previousResult;
    private Repository repository;
    
    private boolean passingExport;
    
    private String startCopyName;
    private int startCopyNr;
    
    public JobExecutionConfiguration()
    {
    	executingLocally = true;
    	executingRemotely = false;
    	passingExport = false;
    	        
        arguments = new HashMap<String, String>();
        params = new HashMap<String, String>();
        variables = new HashMap<String, String>();
        
        logLevel = LogLevel.BASIC;
        
        clearingLog = true;
    }
    
    public Object clone()
    {
        try
        {
            JobExecutionConfiguration configuration = (JobExecutionConfiguration) super.clone();

            configuration.params = new HashMap<String, String>();
            configuration.params.putAll(params);                        
            
            configuration.arguments = new HashMap<String, String>();
            configuration.arguments.putAll(arguments);
            
            configuration.variables = new HashMap<String, String>();
            configuration.variables.putAll(variables);
            
            if (previousResult!=null) 
            {
            	configuration.previousResult = previousResult.clone();
            }
                        
            return configuration;
        }
        catch(CloneNotSupportedException e)
        {
            return null;
        }
    }

    /**
     * @return the arguments
     */
    public Map<String, String> getArguments()
    {
        return arguments;
    }

    /**
     * @param arguments the arguments to set
     */
    public void setArguments(Map<String, String> arguments)
    {
        this.arguments = arguments;
    }

    /**
     * @param params the parameters to set
     */
    public void setParams(Map<String, String> params)
    {
        this.params = params;
    }    
    
    /**
     * @return the parameters.
     */
    public Map<String, String> getParams()
    {
        return params;
    }
    
    /**
     * @param arguments the arguments to set
     */
    public void setArgumentStrings(String[] arguments)
    {
    	this.arguments = new HashMap<String, String>();
    	if (arguments!=null)
    	{
	    	for (int i=0;i<arguments.length;i++)
	    	{
	    		this.arguments.put("arg "+(i+1), arguments[i]);
	    	}
    	}
    }
    
    /**
     * @return the variables
     */
    public Map<String, String> getVariables()
    {
        return variables;
    }

    /**
     * @param variables the variables to set
     */
    public void setVariables(Map<String, String> variables)
    {
        this.variables = variables;
    }

    public void setVariables(VariableSpace space)
    {
    	this.variables = new HashMap<String, String>();
    	
    	for (String name : space.listVariables())
    	{
    		String value = space.getVariable(name);
    		this.variables.put(name, value);
    	}
    }
    
    /**
     * @return the remoteExecution
     */
    public boolean isExecutingRemotely()
    {
        return executingRemotely;
    }

    /**
     * @param remoteExecution the remoteExecution to set
     */
    public void setExecutingRemotely(boolean remoteExecution)
    {
        this.executingRemotely = remoteExecution;
    }

    /**
     * @return the localExecution
     */
    public boolean isExecutingLocally()
    {
        return executingLocally;
    }

    /**
     * @param localExecution the localExecution to set
     */
    public void setExecutingLocally(boolean localExecution)
    {
        this.executingLocally = localExecution;
    }

    /**
     * @return the remoteServer
     */
    public SlaveServer getRemoteServer()
    {
        return remoteServer;
    }

    /**
     * @param remoteServer the remoteServer to set
     */
    public void setRemoteServer(SlaveServer remoteServer)
    {
        this.remoteServer = remoteServer;
    }
    
    public void getUsedVariables(JobMeta jobMeta)
    {
        Properties sp = new Properties();
        VariableSpace space = Variables.getADefaultVariableSpace();
        
        String keys[] = space.listVariables();
        for ( int i=0; i<keys.length; i++ )
        {
            sp.put(keys[i], space.getVariable(keys[i]));
        }
 
        List<String> vars = jobMeta.getUsedVariables();
        if (vars!=null && vars.size()>0)
        {
        	HashMap<String, String> newVariables = new HashMap<String, String>();
        	
            for (int i=0;i<vars.size();i++) 
            {
                String varname = (String)vars.get(i);
                if (!varname.startsWith(Const.INTERNAL_VARIABLE_PREFIX))
                {
                	newVariables.put(varname, Const.NVL(variables.get(varname), sp.getProperty(varname, "")));
                }
            }
            // variables.clear();
            variables.putAll(newVariables);
        }
    }
    
    /**
     * @return the replayDate
     */
    public Date getReplayDate()
    {
        return replayDate;
    }

    /**
     * @param replayDate the replayDate to set
     */
    public void setReplayDate(Date replayDate)
    {
        this.replayDate = replayDate;
    }

    /**
     * @return the usingSafeMode
     */
    public boolean isSafeModeEnabled()
    {
        return safeModeEnabled;
    }

    /**
     * @param usingSafeMode the usingSafeMode to set
     */
    public void setSafeModeEnabled(boolean usingSafeMode)
    {
        this.safeModeEnabled = usingSafeMode;
    }

    /**
     * @return the logLevel
     */
    public LogLevel getLogLevel()
    {
        return logLevel;
    }

    /**
     * @param logLevel the logLevel to set
     */
    public void setLogLevel(LogLevel logLevel)
    {
        this.logLevel = logLevel;
    }
    
    public String getXML() throws IOException
    {
        StringBuffer xml = new StringBuffer(160);
        
        xml.append("  <"+XML_TAG+">").append(Const.CR);
        
        xml.append("    ").append(XMLHandler.addTagValue("exec_local", executingLocally));
        
        xml.append("    ").append(XMLHandler.addTagValue("exec_remote", executingRemotely));
        if (remoteServer!=null)
        {
            xml.append("    ").append(remoteServer.getXML()).append(Const.CR);
        }
        xml.append("    ").append(XMLHandler.addTagValue("pass_export", passingExport));

        // Serialize the parameters...
        //
        xml.append("    <parameters>").append(Const.CR);
        List<String> paramNames = new ArrayList<String>(params.keySet());
        Collections.sort(paramNames);
        for (String name : paramNames) {
        	String value = params.get(name);
        	xml.append("    <parameter>");
        	xml.append(XMLHandler.addTagValue("name", name, false));
        	xml.append(XMLHandler.addTagValue("value", value, false));
        	xml.append("</parameter>").append(Const.CR);
        }
        xml.append("    </parameters>").append(Const.CR);           
        
        // Serialize the variables...
        //
        xml.append("    <variables>").append(Const.CR);
        List<String> variableNames = new ArrayList<String>(variables.keySet());
        Collections.sort(variableNames);
        for (String name : variableNames) {
        	String value = variables.get(name);
        	xml.append("    <variable>");
        	xml.append(XMLHandler.addTagValue("name", name, false));
        	xml.append(XMLHandler.addTagValue("value", value, false));
        	xml.append("</variable>").append(Const.CR);
        }
        xml.append("    </variables>").append(Const.CR);

        // Serialize the variables...
        //
        xml.append("    <arguments>").append(Const.CR);
        List<String> argumentNames = new ArrayList<String>(arguments.keySet());
        Collections.sort(argumentNames);
        for (String name : argumentNames) {
        	String value = arguments.get(name);
        	xml.append("    <argument>");
        	xml.append(XMLHandler.addTagValue("name", name, false));
        	xml.append(XMLHandler.addTagValue("value", value, false));
        	xml.append("</argument>").append(Const.CR);
        }
        xml.append("    </arguments>").append(Const.CR);

        xml.append("    ").append(XMLHandler.addTagValue("replay_date", replayDate));
        xml.append("    ").append(XMLHandler.addTagValue("safe_mode", safeModeEnabled));
        xml.append("    ").append(XMLHandler.addTagValue("log_level", logLevel.getCode()));
        xml.append("    ").append(XMLHandler.addTagValue("clear_log", clearingLog));

        xml.append("    ").append(XMLHandler.addTagValue("start_copy_name", startCopyName));
        xml.append("    ").append(XMLHandler.addTagValue("start_copy_nr", startCopyNr));

        // The source rows...
        //
        if (previousResult!=null)
        {
        	xml.append(previousResult.getXML());
        }
        
        // Send the repository name and user to the remote site...
        //
        if (repository!=null)
        {
            xml.append(XMLHandler.openTag("repository"));
            xml.append(XMLHandler.addTagValue("name", repository.getName()));
            xml.append(XMLHandler.addTagValue("login", repository.getUserInfo().getLogin()));
            xml.append(XMLHandler.addTagValue("password", Encr.encryptPassword(repository.getUserInfo().getPassword())));
            xml.append(XMLHandler.closeTag("repository"));
        }
        
        xml.append("</"+XML_TAG+">").append(Const.CR);
        return xml.toString();
    }
    
    public JobExecutionConfiguration(Node trecNode) throws KettleException
    {
    	this();
    	
        executingLocally = "Y".equalsIgnoreCase(XMLHandler.getTagValue(trecNode, "exec_local"));

        executingRemotely = "Y".equalsIgnoreCase(XMLHandler.getTagValue(trecNode, "exec_remote"));
        Node remoteHostNode = XMLHandler.getSubNode(trecNode, SlaveServer.XML_TAG);
        if (remoteHostNode!=null)
        {
            remoteServer = new SlaveServer(remoteHostNode);
        }
        passingExport = "Y".equalsIgnoreCase(XMLHandler.getTagValue(trecNode, "pass_export"));

        // Read the variables...
        //
        Node varsNode = XMLHandler.getSubNode(trecNode, "variables");
        int nrVariables = XMLHandler.countNodes(varsNode, "variable");
        for (int i=0;i<nrVariables;i++) {
        	Node argNode = XMLHandler.getSubNodeByNr(varsNode, "variable", i);
        	String name = XMLHandler.getTagValue(argNode, "name");
        	String value = XMLHandler.getTagValue(argNode, "value");
        	if (!Const.isEmpty(name) && !Const.isEmpty(value)) {
        		variables.put(name, value);
        	}
        }
        
        // Read the arguments...
        //
        Node argsNode = XMLHandler.getSubNode(trecNode, "arguments");
        int nrArguments = XMLHandler.countNodes(argsNode, "argument");
        for (int i=0;i<nrArguments;i++) {
        	Node argNode = XMLHandler.getSubNodeByNr(argsNode, "argument", i);
        	String name = XMLHandler.getTagValue(argNode, "name");
        	String value = XMLHandler.getTagValue(argNode, "value");
        	if (!Const.isEmpty(name) && !Const.isEmpty(value)) {
        		arguments.put(name, value);
        	}
        }
        
        // Read the parameters...
        //
        Node parmsNode = XMLHandler.getSubNode(trecNode, "parameters");
        int nrParams = XMLHandler.countNodes(parmsNode, "parameter");
        for (int i=0;i<nrParams;i++) {
        	Node parmNode = XMLHandler.getSubNodeByNr(parmsNode, "parameter", i);
        	String name = XMLHandler.getTagValue(parmNode, "name");
        	String value = XMLHandler.getTagValue(parmNode, "value");
        	if (!Const.isEmpty(name) ) {
        		params.put(name, value);
        	}
        }      
        
        replayDate = XMLHandler.stringToDate( XMLHandler.getTagValue(trecNode, "replay_date") );
        safeModeEnabled = "Y".equalsIgnoreCase(XMLHandler.getTagValue(trecNode, "safe_mode"));
        logLevel = LogLevel.getLogLevelForCode( XMLHandler.getTagValue(trecNode, "log_level") );
        clearingLog = "Y".equalsIgnoreCase(XMLHandler.getTagValue(trecNode, "clear_log"));

        startCopyName = XMLHandler.getTagValue(trecNode, "start_copy_name");
        startCopyNr = Const.toInt(XMLHandler.getTagValue(trecNode, "start_copy_nr"), 0);
        
        Node resultNode = XMLHandler.getSubNode(trecNode, Result.XML_TAG);
        if (resultNode!=null)
        {
        	try {
				previousResult = new Result(resultNode);
			} catch (KettleException e) {
				throw new KettleException("Unable to hydrate previous result", e);
			}
        }
        
        // Try to get a handle to the repository from here...
        //
        Node repNode = XMLHandler.getSubNode(trecNode, "repository");
        if (repNode!=null)
        {
            String repositoryName = XMLHandler.getTagValue(repNode, "name");
            String username = XMLHandler.getTagValue(repNode, "login");
            String password = Encr.decryptPassword(XMLHandler.getTagValue(repNode, "password"));
            
            // Verify that the repository exists on the slave server...
            //
            RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
            try {
            	repositoriesMeta.readData();
            } catch(Exception e) {
            	throw new KettleException("Unable to get a list of repositories to locate repository '"+repositoryName+"'");
            }
        	RepositoryMeta repositoryMeta = repositoriesMeta.findRepository(repositoryName);
        	if (repositoryMeta==null)
        	{
        		throw new KettleException("I couldn't find the repository with name '"+repositoryName+"'");
        	}

//        	Repository rep = (Repository) PluginRegistry.getInstance().loadClass(RepositoryPluginType.class, repositoryMeta, PluginClassType.MainClassType);

          Repository rep = PluginRegistry.getInstance().loadClass(RepositoryPluginType.class, repositoryMeta, Repository.class);
        	rep.init(repositoryMeta);
			
			try {
				rep.connect(username, password);
			} catch(Exception e) {
				throw new KettleException("Unable to connect to the repository with name '"+repositoryName+"'");
			}
			
			// Confirmed access:
			//
			repository = rep;
        }

    }

    
    public String[] getArgumentStrings()
    {
        if (arguments==null || arguments.size()==0) return null;
        
        String[] argNames = arguments.keySet().toArray(new String[arguments.size()]);
        Arrays.sort(argNames);
        
        String[] values = new String[argNames.length];
        for (int i=0;i<argNames.length;i++) {
        	values[i] = arguments.get(argNames[i]);
        }
        
        return values;
    }

	/**
	 * @return the previousResult
	 */
	public Result getPreviousResult() {
		return previousResult;
	}

	/**
	 * @param previousResult the previousResult to set
	 */
	public void setPreviousResult(Result previousResult) {
		this.previousResult = previousResult;
	}

	/**
	 * @return the repository
	 */
	public Repository getRepository() {
		return repository;
	}

	/**
	 * @param repository the repository to set
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/**
	 * @return the clearingLog
	 */
	public boolean isClearingLog() {
		return clearingLog;
	}

	/**
	 * @param clearingLog the clearingLog to set
	 */
	public void setClearingLog(boolean clearingLog) {
		this.clearingLog = clearingLog;
	}

	/**
	 * @return the passingExport
	 */
	public boolean isPassingExport() {
		return passingExport;
	}

	/**
	 * @param passingExport the passingExport to set
	 */
	public void setPassingExport(boolean passingExport) {
		this.passingExport = passingExport;
	}

  /**
   * @return the startCopyName
   */
  public String getStartCopyName() {
    return startCopyName;
  }

  /**
   * @param startCopyName the startCopyName to set
   */
  public void setStartCopyName(String startCopyName) {
    this.startCopyName = startCopyName;
  }

  /**
   * @return the startCopyNr
   */
  public int getStartCopyNr() {
    return startCopyNr;
  }

  /**
   * @param startCopyNr the startCopyNr to set
   */
  public void setStartCopyNr(int startCopyNr) {
    this.startCopyNr = startCopyNr;
  }

}
