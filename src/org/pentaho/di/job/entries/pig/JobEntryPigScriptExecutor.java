/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
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

package org.pentaho.di.job.entries.pig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.backend.hadoop.datastorage.ConfigurationUtil;
import org.apache.pig.impl.util.PropertiesUtil;
import org.apache.pig.tools.grunt.GruntParser;
import org.apache.pig.tools.parameters.ParameterSubstitutionPreprocessor;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.Log4jFileAppender;
import org.pentaho.di.core.logging.Log4jKettleLayout;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

/**
 * Job entry that executes a Pig script either on a hadoop cluster or
 * locally.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
@JobEntry(id = "HadoopPigScriptExecutorPlugin", name = "Pig Script Executor", categoryDescription = "Hadoop", description = "Execute Pig Scripts in Hadoop", image = "HDE.png")
public class JobEntryPigScriptExecutor extends JobEntryBase implements Cloneable, JobEntryInterface {
  
  private static Class<?> PKG = JobEntryPigScriptExecutor.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$
  
  /** Hostname of the job tracker */
  protected String m_jobTrackerHostname = "";
  
  /** Port that the job tracker is listening on */
  protected String m_jobTrackerPort = "";
  
  /** Name node hostname */
  protected String m_hdfsHostname = "";
  
  /** Port that the name node is listening on */
  protected String m_hdfsPort = "";
  
  /** URL to the pig script to execute */
  protected String m_scriptFile = "";
  
  /** True if the job entry should block until the script has executed */
  protected boolean m_enableBlocking;
  
  /** True if the script should execute locally, rather than on a hadoop cluster */
  protected boolean m_localExecution;

  /** Parameters for the script */
  protected HashMap<String, String> m_params = new HashMap<String, String>(); 
  
  /** Default name node and job tracker ports */
  public static final String DEFAULT_HDFS_PORT = "9000";
  public static final String DEFAULT_JOBTRACKER_PORT = "9001";
  
  
  /**
   * An extended PrintWriter that sends output to Kettle's logging
   * 
   * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
   */
  class KettleLoggingPrintWriter extends PrintWriter {
    public KettleLoggingPrintWriter() {
      super(System.out);
    }
    
    public void println(String string) {
      logBasic(string);
    }
    
    public void println(Object obj) {
      println(obj.toString());
    }
    
    public void write(String string) {
      println(string);
    }
    
    public void print(String string) {
      println(string);
    }
    
    public void print(Object obj) {
      print(obj.toString());
    }
  }  
  
  /* (non-Javadoc)
   * @see org.pentaho.di.job.entry.JobEntryBase#getXML()
   */
  public String getXML() {
    StringBuffer retval = new StringBuffer();
    retval.append(super.getXML());
    retval.append("    ").append(XMLHandler.addTagValue("hdfs_hostname", m_hdfsHostname));
    retval.append("    ").append(XMLHandler.addTagValue("hdfs_port", m_hdfsPort));
    retval.append("    ").append(XMLHandler.addTagValue("jobtracker_hostname", m_jobTrackerHostname));
    retval.append("    ").append(XMLHandler.addTagValue("jobtracker_port", m_jobTrackerPort));
    retval.append("    ").append(XMLHandler.addTagValue("script_file", m_scriptFile));
    retval.append("    ").append(XMLHandler.addTagValue("enable_blocking", m_enableBlocking));
    retval.append("    ").append(XMLHandler.addTagValue("local_execution", m_localExecution));
    
    retval.append("    <script_parameters>").append(Const.CR);
    if (m_params != null) {
      for (String name : m_params.keySet()) {
        String value = m_params.get(name);
        if (!Const.isEmpty(name) && !Const.isEmpty(value)) {
          retval.append("      <parameter>").append(Const.CR);
          retval.append("        ").append(XMLHandler.addTagValue("name", name));
          retval.append("        ").append(XMLHandler.addTagValue("value", value));
          retval.append("      </parameter>").append(Const.CR);
        }
      }
    }
    retval.append("    </script_parameters>").append(Const.CR);
    
    return retval.toString();
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.di.job.entry.JobEntryInterface#loadXML(org.w3c.dom.Node, java.util.List, java.util.List, org.pentaho.di.repository.Repository)
   */
  public void loadXML(Node entrynode, List<DatabaseMeta> databases,
      List<SlaveServer> slaveServers, Repository repository) throws KettleXMLException {
    super.loadXML(entrynode, databases, slaveServers);
    
    m_hdfsHostname = XMLHandler.getTagValue(entrynode, "hdfs_hostname");
    m_hdfsPort = XMLHandler.getTagValue(entrynode, "hdfs_port");
    m_jobTrackerHostname = XMLHandler.getTagValue(entrynode, "jobtracker_hostname");
    m_jobTrackerPort = XMLHandler.getTagValue(entrynode, "jobtracker_port");
    m_scriptFile = XMLHandler.getTagValue(entrynode, "script_file");
    m_enableBlocking = XMLHandler.getTagValue(entrynode, "enable_blocking").equalsIgnoreCase("Y");
    m_localExecution = XMLHandler.getTagValue(entrynode, "local_execution").equalsIgnoreCase("Y");    
    
    // Script parameters
    m_params = new HashMap<String, String>();
    Node paramList = XMLHandler.getSubNode(entrynode, "script_parameters");
    if (paramList != null) {
      int numParams = XMLHandler.countNodes(paramList, "parameter");
      for (int i = 0; i < numParams; i++) {
        Node paramNode = XMLHandler.getSubNodeByNr(paramList, "parameter", i);
        String name = XMLHandler.getTagValue(paramNode, "name");
        String value = XMLHandler.getTagValue(paramNode, "value");
        m_params.put(name, value);
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.di.job.entry.JobEntryBase#loadRep(org.pentaho.di.repository.Repository, org.pentaho.di.repository.ObjectId, java.util.List, java.util.List)
   */
  public void loadRep(Repository rep, ObjectId id_jobentry, 
      List<DatabaseMeta> databases, List<SlaveServer> slaveServers) 
    throws KettleException {
    if (rep != null) {
      super.loadRep(rep, id_jobentry, databases, slaveServers);
      
      setHDFSHostname(rep.getJobEntryAttributeString(id_jobentry, "hdfs_hostname"));
      setHDFSPort(rep.getJobEntryAttributeString(id_jobentry, "hdfs_port"));
      setJobTrackerHostname(rep.getJobEntryAttributeString(id_jobentry, "jobtracker_hostname"));
      setJobTrackerPort(rep.getJobEntryAttributeString(id_jobentry, "jobtracker_port"));
      setScriptFilename(rep.getJobEntryAttributeString(id_jobentry, "script_file"));
      setEnableBlocking(rep.getJobEntryAttributeBoolean(id_jobentry, "enable_blocking"));
      setLocalExecution(rep.getJobEntryAttributeBoolean(id_jobentry, "local_execution"));
      
      // Script parameters
      m_params = new HashMap<String, String>();
      int numParams = rep.countNrJobEntryAttributes(id_jobentry, "param_name");
      if (numParams > 0) {
        for (int i = 0; i < numParams; i++) {
          String name = rep.getJobEntryAttributeString(id_jobentry, i, "param_name");
          String value = rep.getJobEntryAttributeString(id_jobentry, i, "param_value");
          m_params.put(name, value);
        }
      }
    } else {
      throw new KettleException("Unable to load from a repository. The repository is null.");
    }
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.di.job.entry.JobEntryBase#saveRep(org.pentaho.di.repository.Repository, org.pentaho.di.repository.ObjectId)
   */
  public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
    if (rep != null) {
      super.saveRep(rep, id_job);
      
      rep.saveJobEntryAttribute(id_job, getObjectId(), "hdfs_hostname", m_hdfsHostname);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "hdfs_port", m_hdfsPort);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "jobtracker_hostname", m_jobTrackerHostname);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "jobtracker_port", m_jobTrackerPort);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "script_file", m_scriptFile);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "enable_blocking", m_enableBlocking);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "local_execution", m_localExecution);
      
      if (m_params != null) {
        int i = 0;
        for (String name : m_params.keySet()) {
          String value = m_params.get(name);
          if (!Const.isEmpty(name) && !Const.isEmpty(value)) {
            rep.saveJobEntryAttribute(id_job, getObjectId(), i, "param_name", name);
            rep.saveJobEntryAttribute(id_job, getObjectId(), i, "param_value", value);
            i++;
          }
        }
      }
    } else {
      throw new KettleException("Unable to save to a repository. The repository is null.");
    }
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.di.job.entry.JobEntryBase#evaluates()
   */
  public boolean evaluates() {
    return true;
  }
  
  /**
   * Get the job tracker host name
   * 
   * @return the job tracker host name
   */
  public String getJobTrackerHostname() {
    return m_jobTrackerHostname;
  }
  
  /**
   * Set the job tracker host name
   * 
   * @param jt the job tracker host name
   */
  public void setJobTrackerHostname(String jt) {
    m_jobTrackerHostname = jt;
  }
  
  /**
   * Get the job tracker port
   * 
   * @return the job tracker port
   */
  public String getJobTrackerPort() {
    return m_jobTrackerPort;
  }
  
  /**
   * Set the job tracker port
   * 
   * @param jp the job tracker port
   */
  public void setJobTrackerPort(String jp) {
    m_jobTrackerPort = jp;
  }
  
  /**
   * Get the HDFS host name
   * 
   * @return the HDFS host name
   */
  public String getHDFSHostname() {
    return m_hdfsHostname;
  }
  
  /**
   * Set the HDFS host name
   * 
   * @param nameN the HDFS host name
   */
  public void setHDFSHostname(String nameN) {
    m_hdfsHostname = nameN;
  }
  
  /**
   * Get the HDFS port
   * 
   * @return the HDFS port
   */
  public String getHDFSPort() {
    return m_hdfsPort;
  }
  
  /**
   * Set the HDFS port
   * 
   * @param p the HDFS port
   */
  public void setHDFSPort(String p) {
    m_hdfsPort = p;
  }
  
  /**
   * Get whether the job entry will block until the script finishes
   * 
   * @return true if the job entry will block until the script finishes
   */
  public boolean getEnableBlocking() {
    return m_enableBlocking;
  }
  
  /**
   * Set whether the job will block until the script finishes
   * 
   * @param block true if the job entry is to block until the script finishes
   */
  public void setEnableBlocking(boolean block) {
    m_enableBlocking = block;
  }
  
  /**
   * Set whether the script is to be run locally rather than on a hadoop
   * cluster
   * 
   * @param l true if the script is to run locally
   */
  public void setLocalExecution(boolean l) {
    m_localExecution = l;
  }
  
  /**
   * Get whether the script is to run locally rather than on a hadoop
   * cluster
   * 
   * @return true if the script is to run locally
   */
  public boolean getLocalExecution() {
    return m_localExecution;
  }
  
  /**
   * Set the URL to the pig script to run
   * 
   * @param filename the URL to the pig script
   */
  public void setScriptFilename(String filename) {
    m_scriptFile = filename;
  }
  
  /**
   * Get the URL to the pig script to run
   * 
   * @return the URL to the pig script to run
   */
  public String getScriptFilename() {
    return m_scriptFile;
  }
  
  /**
   * Set the values of parameters to replace in the script
   * 
   * @param params a HashMap mapping parameter names to values
   */
  public void setScriptParameters(HashMap<String, String> params) {
    m_params = params;
  }
  
  /**
   * Get the values of parameters to replace in the script
   * 
   * @return a HashMap mapping parameter names to values
   */
  public HashMap<String, String> getScriptParameters() {
    return m_params;
  }
  

  /* (non-Javadoc)
   * @see org.pentaho.di.job.entry.JobEntryInterface#execute(org.pentaho.di.core.Result, int)
   */
  public Result execute(final Result result, int arg1) throws KettleException {
    
    // Set up an appender that will send all pig log messages to Kettle's log
    // via logBasic().
    KettleLoggingPrintWriter klps = new KettleLoggingPrintWriter();
    WriterAppender pigToKettleAppender = new WriterAppender(new Log4jKettleLayout(true), klps);
    
    Logger pigLogger = Logger.getLogger("org.apache.pig");
    Log4jFileAppender appender = null;
    String logFileName = "pdi-" + this.getName(); //$NON-NLS-1$
    LogWriter logWriter = LogWriter.getInstance();
    try {
      appender = LogWriter.createFileAppender(logFileName, true, false);
      logWriter.addAppender(appender);
      log.setLogLevel(parentJob.getLogLevel());
      if (pigLogger != null) {
        pigLogger.addAppender(pigToKettleAppender);
      }
    } catch (Exception e) {
      logError(BaseMessages.getString(PKG, "JobEntryPigScriptExecutor.FailedToOpenLogFile", logFileName, e.toString())); //$NON-NLS-1$
      logError(Const.getStackTracker(e));    
    }
    
    String fsProtocol = "hdfs://";
    try {
      // see if there is a specific file system protocol to use. If not, default to hdfs.      
      fsProtocol = System.getProperty("hadoop.filesystem.protocol", "hdfs://");
      if (!Const.isEmpty(fsProtocol)) {
        if (!fsProtocol.endsWith("://")) {
          fsProtocol += "://";
        }
      } else {
        fsProtocol = "hdfs://";
      }
      logBasic(BaseMessages.getString(PKG, 
          "JobEntryPigScriptExecutor.Message.HadoopFilesystem") + fsProtocol);
    } catch (Exception ex) {
      logError(BaseMessages.getString(PKG, 
          "JobEntryPigScriptExecutor.Error.UnableToAccessFSProperty"));
    }
    
    if (!m_localExecution && Const.isEmpty(m_hdfsHostname)) {
      throw new KettleException(BaseMessages.getString(PKG, 
          "JobEntryPigScriptExecutor.Error.NoHDFSHostSpecified"));
    }
    
    if (!m_localExecution && Const.isEmpty(m_jobTrackerHostname)) {
      throw new KettleException(BaseMessages.getString(PKG, 
          "JobEntryPigScriptExecutor.Error.NoJobTrackerHostSpecified"));
    }
    
    if (Const.isEmpty(m_scriptFile)) {
      throw new KettleException(BaseMessages.getString(PKG, 
          "JobEntryPigScriptExecutor.Error.NoPigScriptSpecified"));
    }

    try {
      URL scriptU = null;
      String scriptFileS = m_scriptFile;
      scriptFileS = environmentSubstitute(scriptFileS);
      if (scriptFileS.indexOf("://") == -1) {
        File scriptFile = new File(scriptFileS);
        scriptU = scriptFile.toURI().toURL();
      } else {
        scriptU = new URL(scriptFileS);
      }

      // configure for connection to hadoop
      Configuration conf = new Configuration(true);
      if (!m_localExecution) {
        String hdfsP = m_hdfsPort;
        if (Const.isEmpty(hdfsP)) {
          hdfsP = DEFAULT_HDFS_PORT; // default for name node
        }
        hdfsP = environmentSubstitute(hdfsP);
        
        String jobTP = m_jobTrackerPort;
        if (Const.isEmpty(jobTP)) {
          jobTP = DEFAULT_JOBTRACKER_PORT; // default for job tracker
        }
        jobTP = environmentSubstitute(jobTP);
        
        String hdfsHost = m_hdfsHostname;
        hdfsHost = environmentSubstitute(hdfsHost);
/*        if (m_hdfsHostname.toLowerCase().indexOf("hdfs://") < 0) {
          hdfsHost = "hdfs://" + hdfsHost;
        } */
        hdfsHost = fsProtocol + hdfsHost;
        
        if (!Const.isEmpty(hdfsP)) {
          hdfsHost += ":" + hdfsP;
        }
        
        conf.set("fs.default.name", hdfsHost);
        conf.set("mapred.job.tracker", m_jobTrackerHostname + ":" + jobTP);
      }
      
      Properties properties = new Properties();
      PropertiesUtil.loadDefaultProperties(properties);
      if (!m_localExecution) {
        properties.putAll(ConfigurationUtil.toProperties(conf));
      }
      

      // transform the map type to list type which can been accepted by ParameterSubstitutionPreprocessor                                           
      List<String> paramList = new ArrayList<String>();
      if (m_params != null){
        for (Map.Entry<String, String> entry : m_params.entrySet()) {
          String name = entry.getKey();
          name = environmentSubstitute(name); // do environment variable substitution
          String value = entry.getValue();
          value = environmentSubstitute(value); // do environment variable substitution
          paramList.add(name + "=" + value);
        }
      }
      
      final InputStream inStream = scriptU.openStream();
      // do parameter substitution                                                                                                                  
      ParameterSubstitutionPreprocessor psp = new ParameterSubstitutionPreprocessor(50);
      StringWriter writer = new StringWriter();
      psp.genSubstitutedFile(new BufferedReader(new InputStreamReader(inStream)),
                             writer,
                             paramList.size() > 0 ? paramList.toArray(new String[0]) : null, null);
      
      PigServer pigServer = 
        new PigServer((m_localExecution ? ExecType.LOCAL : ExecType.MAPREDUCE), 
            properties);
      final GruntParser grunt = new GruntParser(new StringReader(writer.toString()));
      grunt.setInteractive(false);
      grunt.setParams(pigServer);
      
      if (m_enableBlocking) {
        int[] executionStatus = null;
        executionStatus = grunt.parseStopOnError(false);
        logBasic(BaseMessages.getString(PKG, 
          "JobEntryPigScriptExecutor.JobCompletionStatus", 
          "" + executionStatus[0], "" + executionStatus[1]));
        
        if (executionStatus[1] > 0) {
          result.setStopped(true);
          result.setNrErrors(executionStatus[1]);
          result.setResult(false);
        }
        
        removeAppender(appender, pigToKettleAppender);
        if (appender != null) {
          ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_LOG, appender.getFile(), parentJob.getJobname(), getName());
          result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
        }
      } else {
        final Log4jFileAppender fa = appender;
        final WriterAppender ptk = pigToKettleAppender;
        Thread runThread = new Thread() {
          public void run() {
            try {
              int[] executionStatus = grunt.parseStopOnError(false);
              logBasic(BaseMessages.getString(PKG, 
                  "JobEntryPigScriptExecutor.JobCompletionStatus", 
                  "" + executionStatus[0], "" + executionStatus[1]));              
            } catch (Exception ex) {
              ex.printStackTrace();
              result.setStopped(true);
              result.setNrErrors(1);
              result.setResult(false);
            } finally {
              try {
                removeAppender(fa, ptk);
                if (fa != null) {
                  ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_LOG, fa.getFile(), parentJob.getJobname(), getName());
                  result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
                }
                inStream.close();                
              } catch (IOException e) {
                //e.printStackTrace();
              }
            }
          }
        };
        
        runThread.start();
      }            
    } catch (Exception ex) {
      ex.printStackTrace();
      result.setStopped(true);
      result.setNrErrors(1);
      result.setResult(false);
      logError(ex.getMessage(), ex); 
    }
        
    return result;
  }
  
  protected void removeAppender(Log4jFileAppender appender, 
      WriterAppender pigToKettleAppender) {
    
    // remove the file appender from kettle logging
    if (appender != null) {
      LogWriter.getInstance().removeAppender(appender);
      appender.close();      
    }
    
    Logger pigLogger = Logger.getLogger("org.apache.pig");
    if (pigLogger != null && pigToKettleAppender != null) {
      pigLogger.removeAppender(pigToKettleAppender);
      pigToKettleAppender.close();
    }    
  }  
}
