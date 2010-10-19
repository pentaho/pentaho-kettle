/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.job.entries.job;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.Log4jFileAppender;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.www.SlaveServerJobStatus;
import org.w3c.dom.Node;

/**
 * Recursive definition of a Job. This step means that an entire Job has to be
 * executed. It can be the same Job, but just make sure that you don't get an
 * endless loop. Provide an escape routine using JobEval.
 * 
 * @author Matt
 * @since 01-10-2003, Rewritten on 18-06-2004
 * 
 */
public class JobEntryJob extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryJob.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private String               filename;
  private String               jobname;
  private String               directory;
  private ObjectId             jobObjectId;
  private ObjectLocationSpecificationMethod specificationMethod;

  public String                arguments[];
  public boolean               argFromPrevious;
  public boolean               paramsFromPrevious;
  public boolean               execPerRow;

  public String                parameters[];
  public String                parameterFieldNames[];
  public String                parameterValues[];

  public boolean               setLogfile;
  public String                logfile, logext;
  public boolean               addDate, addTime;
  public LogLevel              logFileLevel;

  public boolean               parallel;
  private String               directoryPath;
  public boolean               setAppendLogfile;
  public boolean               createParentFolder;

  public boolean               waitingToFinish      = true;
  public boolean               followingAbortRemotely;

  private String               remoteSlaveServerName;
  public boolean               passingAllParameters = true;

  private boolean              passingExport;

  public static final LogLevel DEFAULT_LOG_LEVEL    = LogLevel.NOTHING;

  private Job                  job;

  public JobEntryJob(String name) {
    super(name, "");
  }

  public JobEntryJob() {
    this("");
    clear();
  }

  public Object clone() {
    JobEntryJob je = (JobEntryJob) super.clone();
    return je;
  }

  public void setFileName(String n) {
    filename = n;
  }

  /**
   * @deprecated use getFilename() instead.
   * @return the filename
   */
  public String getFileName() {
    return filename;
  }

  public String getFilename() {
    return filename;
  }

  public String getRealFilename() {
    return environmentSubstitute(getFilename());
  }

  public void setJobName(String jobname) {
    this.jobname = jobname;
  }

  public String getJobName() {
    return jobname;
  }

  public String getDirectory() {
    return directory;
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }

  public boolean isPassingExport() {
    return passingExport;
  }

  public void setPassingExport(boolean passingExport) {
    this.passingExport = passingExport;
  }

  public String getLogFilename() {
    String retval = "";
    if (setLogfile) {
      retval+=logfile==null?"":logfile;
      Calendar cal = Calendar.getInstance();
      if (addDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        retval += "_" + sdf.format(cal.getTime());
      }
      if (addTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        retval += "_" + sdf.format(cal.getTime());
      }
      if (logext != null && logext.length() > 0) {
        retval += "." + logext;
      }
    }
    return retval;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(200);

    retval.append(super.getXML());

    // specificationMethod
    //
    retval.append("      ").append(XMLHandler.addTagValue("specification_method", specificationMethod == null ? null : specificationMethod.getCode()));
    retval.append("      ").append(XMLHandler.addTagValue("job_object_id", jobObjectId == null ? null : jobObjectId.toString()));
    retval.append("      ").append(XMLHandler.addTagValue("filename", filename));
    retval.append("      ").append(XMLHandler.addTagValue("jobname", jobname));
    
    if (directory != null) {
      retval.append("      ").append(XMLHandler.addTagValue("directory", directory));
    } else if (directoryPath != null) {
      retval.append("      ").append(XMLHandler.addTagValue("directory", directoryPath));
    }
    retval.append("      ").append(XMLHandler.addTagValue("arg_from_previous", argFromPrevious));
    retval.append("      ").append(XMLHandler.addTagValue("params_from_previous", paramsFromPrevious));
    retval.append("      ").append(XMLHandler.addTagValue("exec_per_row", execPerRow));
    retval.append("      ").append(XMLHandler.addTagValue("set_logfile", setLogfile));
    retval.append("      ").append(XMLHandler.addTagValue("logfile", logfile));
    retval.append("      ").append(XMLHandler.addTagValue("logext", logext));
    retval.append("      ").append(XMLHandler.addTagValue("add_date", addDate));
    retval.append("      ").append(XMLHandler.addTagValue("add_time", addTime));
    retval.append("      ").append(XMLHandler.addTagValue("loglevel", logFileLevel != null ? logFileLevel.getCode() : DEFAULT_LOG_LEVEL.getCode()));
    retval.append("      ").append(XMLHandler.addTagValue("slave_server_name", remoteSlaveServerName));
    retval.append("      ").append(XMLHandler.addTagValue("wait_until_finished", waitingToFinish));
    retval.append("      ").append(XMLHandler.addTagValue("follow_abort_remote", followingAbortRemotely));
    retval.append("      ").append(XMLHandler.addTagValue("create_parent_folder", createParentFolder));
    retval.append("      ").append(XMLHandler.addTagValue("pass_export", passingExport));

    if (arguments != null) {
      for (int i = 0; i < arguments.length; i++) {
        // This is a very very bad way of making an XML file, don't use it (or
        // copy it). Sven Boden
        retval.append("      ").append(XMLHandler.addTagValue("argument" + i, arguments[i]));
      }
    }

    if (parameters != null) {
      retval.append("      ").append(XMLHandler.openTag("parameters"));

      retval.append("        ").append(XMLHandler.addTagValue("pass_all_parameters", passingAllParameters));

      for (int i = 0; i < parameters.length; i++) {
        // This is a better way of making the XML file than the arguments.
        retval.append("            ").append(XMLHandler.openTag("parameter"));

        retval.append("            ").append(XMLHandler.addTagValue("name", parameters[i]));
        retval.append("            ").append(XMLHandler.addTagValue("stream_name", parameterFieldNames[i]));
        retval.append("            ").append(XMLHandler.addTagValue("value", parameterValues[i]));

        retval.append("            ").append(XMLHandler.closeTag("parameter"));
      }
      retval.append("      ").append(XMLHandler.closeTag("parameters"));
    }
    retval.append("      ").append(XMLHandler.addTagValue("set_append_logfile", setAppendLogfile));

    return retval.toString();
  }

  private void checkObjectLocationSpecificationMethod() {
    if (specificationMethod==null) {
      // Backward compatibility
      //
      // Default = Filename
      //
      specificationMethod=ObjectLocationSpecificationMethod.FILENAME;
      
      if (!Const.isEmpty(filename)) {
        specificationMethod=ObjectLocationSpecificationMethod.FILENAME;
      } else if (jobObjectId!=null) {
        specificationMethod=ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
      } else if (!Const.isEmpty(jobname)) {
        specificationMethod=ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
      }
    }
  }
  
  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException {
    try {
      super.loadXML(entrynode, databases, slaveServers);

      String method = XMLHandler.getTagValue(entrynode, "specification_method");
      specificationMethod = ObjectLocationSpecificationMethod.getSpecificationMethodByCode(method);
      String jobId = XMLHandler.getTagValue(entrynode, "job_object_id");
      jobObjectId = Const.isEmpty(jobId) ? null : new StringObjectId(jobId);
      filename = XMLHandler.getTagValue(entrynode, "filename");
      jobname = XMLHandler.getTagValue(entrynode, "jobname");
      
      // Backward compatibility check for object specification
      //
      checkObjectLocationSpecificationMethod();
      
      argFromPrevious = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "arg_from_previous"));
      paramsFromPrevious = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "params_from_previous"));
      execPerRow = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "exec_per_row"));
      setLogfile = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "set_logfile"));
      addDate = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "add_date"));
      addTime = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "add_time"));
      logfile = XMLHandler.getTagValue(entrynode, "logfile");
      logext = XMLHandler.getTagValue(entrynode, "logext");
      logFileLevel = LogLevel.getLogLevelForCode(XMLHandler.getTagValue(entrynode, "loglevel"));
      setAppendLogfile = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "set_append_logfile"));
      remoteSlaveServerName = XMLHandler.getTagValue(entrynode, "slave_server_name");
      passingExport = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "pass_export"));
      directory = XMLHandler.getTagValue(entrynode, "directory");
      createParentFolder = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "create_parent_folder"));

      String wait = XMLHandler.getTagValue(entrynode, "wait_until_finished");
      if (Const.isEmpty(wait))
        waitingToFinish = true;
      else
        waitingToFinish = "Y".equalsIgnoreCase(wait);

      followingAbortRemotely = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "follow_abort_remote"));

      // How many arguments?
      int argnr = 0;
      while (XMLHandler.getTagValue(entrynode, "argument" + argnr) != null)
        argnr++;
      arguments = new String[argnr];

      // Read them all... This is a very BAD way to do it by the way. Sven
      // Boden.
      for (int a = 0; a < argnr; a++) {
        arguments[a] = XMLHandler.getTagValue(entrynode, "argument" + a);
      }

      Node parametersNode = XMLHandler.getSubNode(entrynode, "parameters"); //$NON-NLS-1$

      String passAll = XMLHandler.getTagValue(parametersNode, "pass_all_parameters");
      passingAllParameters = Const.isEmpty(passAll) || "Y".equalsIgnoreCase(passAll);

      int nrParameters = XMLHandler.countNodes(parametersNode, "parameter"); //$NON-NLS-1$

      parameters = new String[nrParameters];
      parameterFieldNames = new String[nrParameters];
      parameterValues = new String[nrParameters];

      for (int i = 0; i < nrParameters; i++) {
        Node knode = XMLHandler.getSubNodeByNr(parametersNode, "parameter", i); //$NON-NLS-1$

        parameters[i] = XMLHandler.getTagValue(knode, "name"); //$NON-NLS-1$
        parameterFieldNames[i] = XMLHandler.getTagValue(knode, "stream_name"); //$NON-NLS-1$
        parameterValues[i] = XMLHandler.getTagValue(knode, "value"); //$NON-NLS-1$
      }
    } catch (KettleXMLException xe) {
      throw new KettleXMLException("Unable to load 'job' job entry from XML node", xe);
    }
  }

  /**
   * Load the jobentry from repository
   */
  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    try {
      String method = rep.getJobEntryAttributeString(id_jobentry, "specification_method");
      specificationMethod = ObjectLocationSpecificationMethod.getSpecificationMethodByCode(method);
      String jobId = rep.getJobEntryAttributeString(id_jobentry, "job_object_id");
      jobObjectId = Const.isEmpty(jobId) ? null : new StringObjectId(jobId);
      jobname = rep.getJobEntryAttributeString(id_jobentry, "name");
      directory = rep.getJobEntryAttributeString(id_jobentry, "dir_path");
      filename = rep.getJobEntryAttributeString(id_jobentry, "file_name");
      
      // Backward compatibility check for object specification
      //
      checkObjectLocationSpecificationMethod();
      
      argFromPrevious = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous");
      paramsFromPrevious = rep.getJobEntryAttributeBoolean(id_jobentry, "params_from_previous");
      execPerRow = rep.getJobEntryAttributeBoolean(id_jobentry, "exec_per_row");
      setLogfile = rep.getJobEntryAttributeBoolean(id_jobentry, "set_logfile");
      addDate = rep.getJobEntryAttributeBoolean(id_jobentry, "add_date");
      addTime = rep.getJobEntryAttributeBoolean(id_jobentry, "add_time");
      logfile = rep.getJobEntryAttributeString(id_jobentry, "logfile");
      logext = rep.getJobEntryAttributeString(id_jobentry, "logext");
      logFileLevel = LogLevel.getLogLevelForCode(rep.getJobEntryAttributeString(id_jobentry, "loglevel"));
      setAppendLogfile = rep.getJobEntryAttributeBoolean(id_jobentry, "set_append_logfile");
      remoteSlaveServerName = rep.getJobEntryAttributeString(id_jobentry, "slave_server_name");
      passingExport = rep.getJobEntryAttributeBoolean(id_jobentry, "pass_export");
      waitingToFinish = rep.getJobEntryAttributeBoolean(id_jobentry, "wait_until_finished", true);
      followingAbortRemotely = rep.getJobEntryAttributeBoolean(id_jobentry, "follow_abort_remote");
      createParentFolder = rep.getJobEntryAttributeBoolean(id_jobentry, "create_parent_folder");

      // How many arguments?
      int argnr = rep.countNrJobEntryAttributes(id_jobentry, "argument");
      arguments = new String[argnr];

      // Read all arguments ...
      for (int a = 0; a < argnr; a++) {
        arguments[a] = rep.getJobEntryAttributeString(id_jobentry, a, "argument");
      }

      // How many arguments?
      int parameternr = rep.countNrJobEntryAttributes(id_jobentry, "parameter_name");
      parameters = new String[parameternr];
      parameterFieldNames = new String[parameternr];
      parameterValues = new String[parameternr];

      // Read all parameters ...
      for (int a = 0; a < parameternr; a++) {
        parameters[a] = rep.getJobEntryAttributeString(id_jobentry, a, "parameter_name");
        parameterFieldNames[a] = rep.getJobEntryAttributeString(id_jobentry, a, "parameter_stream_name");
        parameterValues[a] = rep.getJobEntryAttributeString(id_jobentry, a, "parameter_value");
      }

      passingAllParameters = rep.getJobEntryAttributeBoolean(id_jobentry, "pass_all_parameters", true);

    } catch (KettleDatabaseException dbe) {
      throw new KettleException("Unable to load job entry of type 'job' from the repository with id_jobentry=" + id_jobentry, dbe);
    }
  }

  // Save the attributes of this job entry
  //
  public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
    try {
      rep.saveJobEntryAttribute(id_job, getObjectId(), "specification_method", specificationMethod==null ? null : specificationMethod.getCode());
      rep.saveJobEntryAttribute(id_job, getObjectId(), "job_object_id", jobObjectId==null ? null : jobObjectId.toString());
      rep.saveJobEntryAttribute(id_job, getObjectId(), "name", getJobName());
      rep.saveJobEntryAttribute(id_job, getObjectId(), "dir_path", getDirectory() != null ? getDirectory() : "");
      rep.saveJobEntryAttribute(id_job, getObjectId(), "file_name", filename);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "arg_from_previous", argFromPrevious);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "params_from_previous", paramsFromPrevious);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "exec_per_row", execPerRow);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "set_logfile", setLogfile);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "add_date", addDate);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "add_time", addTime);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "logfile", logfile);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "logext", logext);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "set_append_logfile", setAppendLogfile);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "loglevel", logFileLevel != null ? logFileLevel.getCode() : JobEntryJob.DEFAULT_LOG_LEVEL.getCode());
      rep.saveJobEntryAttribute(id_job, getObjectId(), "slave_server_name", remoteSlaveServerName);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "pass_export", passingExport);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "wait_until_finished", waitingToFinish);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "follow_abort_remote", followingAbortRemotely);
      rep.saveJobEntryAttribute(id_job, getObjectId(), "create_parent_folder", createParentFolder);

      // save the arguments...
      if (arguments != null) {
        for (int i = 0; i < arguments.length; i++) {
          rep.saveJobEntryAttribute(id_job, getObjectId(), i, "argument", arguments[i]);
        }
      }

      // save the parameters...
      if (parameters != null) {
        for (int i = 0; i < parameters.length; i++) {
          rep.saveJobEntryAttribute(id_job, getObjectId(), i, "parameter_name", parameters[i]);
          rep.saveJobEntryAttribute(id_job, getObjectId(), i, "parameter_stream_name", Const.NVL(parameterFieldNames[i], ""));
          rep.saveJobEntryAttribute(id_job, getObjectId(), i, "parameter_value", Const.NVL(parameterValues[i], ""));
        }
      }

      rep.saveJobEntryAttribute(id_job, getObjectId(), "pass_all_parameters", passingAllParameters);
    } catch (KettleDatabaseException dbe) {
      throw new KettleException("Unable to save job entry of type job to the repository with id_job=" + id_job, dbe);
    }
  }

  public Result execute(Result result, int nr) throws KettleException {
    result.setEntryNr(nr);

    Log4jFileAppender appender = null;
    LogLevel jobLogLevel = parentJob.getLogLevel();
    if (setLogfile) {
      String realLogFilename = environmentSubstitute(getLogFilename());
      // We need to check here the log filename
      // if we do not have one, we must fail
      if(Const.isEmpty(realLogFilename)) {
          logError(BaseMessages.getString(PKG, "JobJob.Exception.LogFilenameMissing"));
          result.setNrErrors(1);
          result.setResult(false);
          return result;
      }

      // create parent folder?
      if (!createParentFolder(realLogFilename)) {
        result.setNrErrors(1);
        result.setResult(false);
        return result;
      }
      try {
        appender = LogWriter.createFileAppender(realLogFilename, true, setAppendLogfile);
      } catch (KettleException e) {
        logError("Unable to open file appender for file [" + getLogFilename() + "] : " + e.toString());
        logError(Const.getStackTracker(e));
        result.setNrErrors(1);
        result.setResult(false);
        return result;
      }
      LogWriter.getInstance().addAppender(appender);
      jobLogLevel = logFileLevel;
    }

    // Figure out the remote slave server...
    //
    SlaveServer remoteSlaveServer = null;
    if (!Const.isEmpty(remoteSlaveServerName)) {
      String realRemoteSlaveServerName = environmentSubstitute(remoteSlaveServerName);
      remoteSlaveServer = parentJob.getJobMeta().findSlaveServer(realRemoteSlaveServerName);
      if (remoteSlaveServer == null) {
        throw new KettleException(BaseMessages.getString(PKG, "JobJob.Exception.UnableToFindRemoteSlaveServer", realRemoteSlaveServerName));
      }
    }
    try {
      // First load the job, outside of the loop...
      if (parentJob.getJobMeta() != null) {
        // reset the internal variables again.
        // Maybe we should split up the variables even more like in UNIX shells.
        // The internal variables need to be reset to be able use them properly
        // in 2 sequential sub jobs.
        parentJob.getJobMeta().setInternalKettleVariables();
      }

      // Explain what we are loading...
      //
      switch(specificationMethod) {
      case REPOSITORY_BY_NAME:
        if (log.isDetailed()) {
          logDetailed("Loading job from repository : [" + directory + " : " + environmentSubstitute(jobname) + "]");
        }
        break;
      case FILENAME:
        if (log.isDetailed()) {
          logDetailed("Loading job from XML file : [" + environmentSubstitute(filename) + "]");
        }
        break;
      case REPOSITORY_BY_REFERENCE: 
        if (log.isDetailed()) {
          logDetailed("Loading job from repository by reference : [" + jobObjectId + "]");
        }
        break;
      }

      JobMeta jobMeta = getJobMeta(rep, this);
      
      // Verify that we loaded something, complain if we did not...
      //
      if (jobMeta == null) {
        throw new KettleException("Unable to load the job: please specify the name and repository directory OR a filename");
      }

      verifyRecursiveExecution(parentJob, jobMeta);


      int iteration = 0;
      String args1[] = arguments;
      // no arguments? Check the parent jobs arguments
      if (args1 == null || args1.length == 0) 
      {
        args1 = parentJob.getJobMeta().getArguments();
      }

      copyVariablesFrom(parentJob);
      setParentVariableSpace(parentJob);

      //
      // For the moment only do variable translation at the start of a job, not
      // for every input row (if that would be switched on)
      //
      String args[] = null;
      if (args1 != null) {
        args = new String[args1.length];
        for (int idx = 0; idx < args1.length; idx++) {
          args[idx] = environmentSubstitute(args1[idx]);
        }
      }

      NamedParams namedParam = new NamedParamsDefault();
      if (parameters != null) {
        for (int idx = 0; idx < parameters.length; idx++) {
          if (!Const.isEmpty(parameters[idx])) {

            // We have a parameter
            try {
              namedParam.addParameterDefinition(parameters[idx], "", "Job entry runtime");
            } catch (DuplicateParamException e) {
              logError("Duplicate parameter definition for " + parameters[idx]);
            }

            if (Const.isEmpty(Const.trim(parameterFieldNames[idx]))) {
              namedParam.setParameterValue(parameters[idx], Const.NVL(environmentSubstitute(parameterValues[idx]), ""));
            } else {
              // something filled in, in the field column but we have no
              // incoming stream. yet.
              namedParam.setParameterValue(parameters[idx], "");
            }
          }
        }
      }

      RowMetaAndData resultRow = null;
      boolean first = true;
      List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>(result.getRows());

      while ((first && !execPerRow) || (execPerRow && rows != null && iteration < rows.size() && result.getNrErrors() == 0)) {
        if (execPerRow) {
          result.getRows().clear();
        }
        first = false;
        if (rows != null && execPerRow) {
          resultRow = (RowMetaAndData) rows.get(iteration);
        } else {
          resultRow = null;
        }

        Result oneResult = new Result();

        List<RowMetaAndData> sourceRows = null;

        if (execPerRow) // Execute for each input row
        {
          if (argFromPrevious) // Copy the input row to the (command line)
                               // arguments
          {
            args = null;
            if (resultRow != null) {
              args = new String[resultRow.size()];
              for (int i = 0; i < resultRow.size(); i++) {
                args[i] = resultRow.getString(i, null);
              }
            }
          } else {
            // Just pass a single row
            List<RowMetaAndData> newList = new ArrayList<RowMetaAndData>();
            newList.add(resultRow);
            sourceRows = newList;
          }

          if (paramsFromPrevious) { // Copy the input the parameters

            if (parameters != null) {
              for (int idx = 0; idx < parameters.length; idx++) {
                if (!Const.isEmpty(parameters[idx])) {
                  // We have a parameter
                  if (Const.isEmpty(Const.trim(parameterFieldNames[idx]))) {
                    namedParam.setParameterValue(parameters[idx], Const.NVL(environmentSubstitute(parameterValues[idx]), ""));
                  } else {
                    String fieldValue = "";

                    if (resultRow != null) {
                      fieldValue = resultRow.getString(parameterFieldNames[idx], "");
                    }
                    // Get the value from the input stream
                    namedParam.setParameterValue(parameters[idx], Const.NVL(fieldValue, ""));
                  }
                }
              }
            }
          }
        } else {
          if (argFromPrevious) {
            // Only put the first Row on the arguments
            args = null;
            if (resultRow != null) {
              args = new String[resultRow.size()];
              for (int i = 0; i < resultRow.size(); i++) {
                args[i] = resultRow.getString(i, null);
              }
            }
          } else {
            // Keep it as it was...
            sourceRows = result.getRows();
          }

          if (paramsFromPrevious) { // Copy the input the parameters

            if (parameters != null) {
              for (int idx = 0; idx < parameters.length; idx++) {
                if (!Const.isEmpty(parameters[idx])) {
                  // We have a parameter
                  if (Const.isEmpty(Const.trim(parameterFieldNames[idx]))) {
                    namedParam.setParameterValue(parameters[idx], Const.NVL(environmentSubstitute(parameterValues[idx]), ""));
                  } else {
                    String fieldValue = "";

                    if (resultRow != null) {
                      fieldValue = resultRow.getString(parameterFieldNames[idx], "");
                    }
                    // Get the value from the input stream
                    namedParam.setParameterValue(parameters[idx], Const.NVL(fieldValue, ""));
                  }
                }
              }
            }
          }
        }

        if (remoteSlaveServer == null) {
          // Local execution...
          //

          // Create a new job
          // 
          job = new Job(rep, jobMeta, this);
          job.setParentJob(parentJob);
          job.setLogLevel(jobLogLevel);
          job.shareVariablesWith(this);
          job.setInternalKettleVariables(this);
          job.copyParametersFrom(jobMeta);
          job.setInteractive(parentJob.isInteractive());
          if (job.isInteractive()) {
            job.getJobEntryListeners().addAll(parentJob.getJobEntryListeners());
          }

          // Pass the socket repository all around.
          //
          job.setSocketRepository(parentJob.getSocketRepository());

          // Set the parameters calculated above on this instance.
          //
          job.clearParameters();
          String[] parameterNames = job.listParameters();
          for (int idx = 0; idx < parameterNames.length; idx++) {
            // Grab the parameter value set in the job entry
            //
            String thisValue = namedParam.getParameterValue(parameterNames[idx]);
            if (!Const.isEmpty(thisValue)) {
              // Set the value as specified by the user in the job entry
              //
              job.setParameterValue(parameterNames[idx], thisValue);
            } else {
              // See if the parameter had a value set in the parent job...
              // This value should pass down to the sub-job if that's what we
              // opted to do.
              //
              if (isPassingAllParameters()) {
                String parentValue = parentJob.getParameterValue(parameterNames[idx]);
                if (!Const.isEmpty(parentValue)) {
                  job.setParameterValue(parameterNames[idx], parentValue);
                }
              }
            }
          }
          job.activateParameters();

          // Set the source rows we calculated above...
          //
          job.setSourceRows(sourceRows);

          // Don't forget the logging...
          job.beginProcessing();

          // Link the job with the sub-job
          parentJob.getJobTracker().addJobTracker(job.getJobTracker());

          // Link both ways!
          job.getJobTracker().setParentJobTracker(parentJob.getJobTracker());

          if (parentJob.getJobMeta().isBatchIdPassed()) {
            job.setPassedBatchId(parentJob.getBatchId());
          }

          job.getJobMeta().setArguments(args);
          JobEntryJobRunner runner = new JobEntryJobRunner( job, result, nr, log);
          Thread jobRunnerThread = new Thread(runner);
          jobRunnerThread.setName( Const.NVL(job.getJobMeta().getName(), job.getJobMeta().getFilename()) );
          jobRunnerThread.start();
          
          //job.start();
          
          while (!runner.isFinished() && !parentJob.isStopped())
          {
              try { Thread.sleep(0,1);}
              catch(InterruptedException e) { }
          }

          // if the parent-job was stopped, stop the sub-job too...
          if (parentJob.isStopped())
          {
              job.stopAll();
              runner.waitUntilFinished(); // Wait until finished!
          }
          
          oneResult = runner.getResult();
          
        } else {
          
          // Make sure we can parameterize the slave server connection
          //
          remoteSlaveServer.shareVariablesWith(this);

          // Remote execution...
          //
          JobExecutionConfiguration jobExecutionConfiguration = new JobExecutionConfiguration();
          jobExecutionConfiguration.setPreviousResult(result.clone());
          jobExecutionConfiguration.getPreviousResult().setRows(sourceRows);
          jobExecutionConfiguration.setArgumentStrings(args);
          jobExecutionConfiguration.setVariables(this);
          jobExecutionConfiguration.setRemoteServer(remoteSlaveServer);
          jobExecutionConfiguration.setRepository(rep);
          jobExecutionConfiguration.setLogLevel(jobLogLevel);
          jobExecutionConfiguration.setPassingExport(passingExport);

          // Send the XML over to the slave server
          // Also start the job over there...
          //
          String carteObjectId = null;
          try {
            carteObjectId = Job.sendToSlaveServer(jobMeta, jobExecutionConfiguration, rep);
          } catch (KettleException e) {
            // Perhaps the job exists on the remote server, carte is down, etc.
            // This is an abort situation, stop the parent job...
            // We want this in case we are running in parallel. The other job
            // entries can stop running now.
            // 
            parentJob.stopAll();

            // Pass the exception along
            // 
            throw e;
          }

          // Now start the monitoring...
          //
          SlaveServerJobStatus jobStatus = null;
          while (!parentJob.isStopped() && waitingToFinish) {
            try {
              jobStatus = remoteSlaveServer.getJobStatus(jobMeta.getName(), carteObjectId, 0);
              if (jobStatus.getResult() != null) {
                // The job is finished, get the result...
                //
                oneResult = jobStatus.getResult();
                break;
              }
            } catch (Exception e1) {
              logError("Unable to contact slave server [" + remoteSlaveServer + "] to verify the status of job [" + jobMeta.getName() + "]");
              oneResult.setNrErrors(1L);
              break; // Stop looking too, chances are too low the server will
                     // come back on-line
            }

            try {
              Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
            ; // sleep for 10 seconds
          }

          if (!waitingToFinish) {
            // Since the job was posted successfully, the result is true...
            //
            oneResult = new Result();
            oneResult.setResult(true);
          }

          if (parentJob.isStopped()) {
            try {
              // See if we have a status and if we need to stop the remote
              // execution here...
              // 
              if (jobStatus == null || jobStatus.isRunning()) {
                // Try a remote abort ...
                //
                remoteSlaveServer.stopJob(jobMeta.getName(), carteObjectId);
              }
            } catch (Exception e1) {
              logError("Unable to contact slave server [" + remoteSlaveServer + "] to stop job [" + jobMeta.getName() + "]");
              oneResult.setNrErrors(1L);
              break; // Stop looking too, chances are too low the server will
                     // come back on-line
            }
          }

        }

        if (iteration == 0) {
          result.clear();
        }
        
        result.add(oneResult);
        
        // if one of them fails (in the loop), increase the number of errors
        //
        if (oneResult.getResult() == false) 
        {
          result.setNrErrors(result.getNrErrors() + 1);
        }

        iteration++;
      }

    } catch (KettleException ke) {
      logError("Error running job entry 'job' : " + ke.toString());
      logError(Const.getStackTracker(ke));

      result.setResult(false);
      result.setNrErrors(1L);
    }

    if (setLogfile) {
      if (appender != null) {
        LogWriter.getInstance().removeAppender(appender);
        appender.close();

        ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_LOG, appender.getFile(), parentJob.getJobname(), getName());
        result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
      }
    }

    if (result.getNrErrors() > 0) {
      result.setResult(false);
    } else {
      result.setResult(true);
    }

    return result;
  }

  private boolean createParentFolder(String filename) {
    // Check for parent folder
    FileObject parentfolder = null;
    boolean resultat = true;
    try {
      // Get parent folder
      parentfolder = KettleVFS.getFileObject(filename, this).getParent();
      if (!parentfolder.exists()) {
        if (createParentFolder) {
          if (log.isDebug())
            logDebug(BaseMessages.getString(PKG, "JobJob.Log.ParentLogFolderNotExist", parentfolder.getName().toString()));
          parentfolder.createFolder();
          if (log.isDebug())
            logDebug(BaseMessages.getString(PKG, "JobJob.Log.ParentLogFolderCreated", parentfolder.getName().toString()));
        } else {
          log.logError(BaseMessages.getString(PKG, "JobJob.Log.ParentLogFolderNotExist", parentfolder.getName().toString()));
          resultat = false;
        }
      } else {
        if (log.isDebug())
          logDebug(BaseMessages.getString(PKG, "JobJob.Log.ParentLogFolderExists", parentfolder.getName().toString()));
      }
    } catch (Exception e) {
      resultat = false;
      log.logError(BaseMessages.getString(PKG, "JobJob.Error.ChekingParentLogFolderTitle"), BaseMessages.getString(PKG, "JobJob.Error.ChekingParentLogFolder", parentfolder.getName().toString()), e);
    } finally {
      if (parentfolder != null) {
        try {
          parentfolder.close();
          parentfolder = null;
        } catch (Exception ex) {
        }
        ;
      }
    }

    return resultat;
  }

  /**
   * Make sure that we are not loading jobs recursively...
   * 
   * @param parentJobMeta
   *          the parent job metadata
   * @param jobMeta
   *          the job metadata
   * @throws KettleException
   *           in case both jobs are loaded from the same source
   */
  private void verifyRecursiveExecution(Job parentJob, JobMeta jobMeta) throws KettleException {

    if (parentJob == null)
      return; // OK!

    JobMeta parentJobMeta = parentJob.getJobMeta();

    if (parentJobMeta.getName() == null && jobMeta.getName() != null)
      return; // OK
    if (parentJobMeta.getName() != null && jobMeta.getName() == null)
      return; // OK as well.

    // Not from the repository? just verify the filename
    //
    if (jobMeta.getFilename() != null && jobMeta.getFilename().equals(parentJobMeta.getFilename())) {
      throw new KettleException(BaseMessages.getString(PKG, "JobJobError.Recursive", jobMeta.getFilename()));
    }

    // Different directories: OK
    if (parentJobMeta.getRepositoryDirectory() == null && jobMeta.getRepositoryDirectory() != null)
      return;
    if (parentJobMeta.getRepositoryDirectory() != null && jobMeta.getRepositoryDirectory() == null)
      return;
    if (jobMeta.getRepositoryDirectory().getObjectId() != parentJobMeta.getRepositoryDirectory().getObjectId())
      return;

    // Same names, same directories : loaded from same location in the
    // repository:
    // --> recursive loading taking place!
    //
    if (parentJobMeta.getName().equals(jobMeta.getName())) {
      throw new KettleException(BaseMessages.getString(PKG, "JobJobError.Recursive", jobMeta.getFilename()));
    }

    // Also compare with the grand-parent (if there is any)
    verifyRecursiveExecution(parentJob.getParentJob(), jobMeta);
  }

  public void clear() {
    super.clear();

    specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    jobname = null;
    filename = null;
    directory = null;
    arguments = null;
    argFromPrevious = false;
    addDate = false;
    addTime = false;
    logfile = null;
    logext = null;
    setLogfile = false;
    setAppendLogfile = false;
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return true;
  }

  public List<SQLStatement> getSQLStatements(Repository repository) throws KettleException {
    return getSQLStatements(repository, null);
  }

  public List<SQLStatement> getSQLStatements(Repository repository, VariableSpace space) throws KettleException {
    this.copyVariablesFrom(space);
    JobMeta jobMeta = getJobMeta(repository, space);
    return jobMeta.getSQLStatements(repository, null);
  }

  public JobMeta getJobMeta(Repository rep, VariableSpace space) throws KettleException {
    try {
      switch(specificationMethod) {
      case FILENAME:
        return new JobMeta((space != null ? space.environmentSubstitute(getFilename()) : getFilename()), rep, null);
      case REPOSITORY_BY_NAME:
        if (rep != null) {
          String realDirectory = environmentSubstitute(getDirectory());
          RepositoryDirectoryInterface repositoryDirectory = rep.loadRepositoryDirectoryTree().findDirectory(realDirectory);
          if (repositoryDirectory==null) {
            throw new KettleException("Unable to find repository directory ["+Const.NVL(realDirectory, "")+"]");
          }
          return rep.loadJob((space != null ? space.environmentSubstitute(getJobName()) : getJobName()), repositoryDirectory, null, null); // reads
        } else {
          throw new KettleException("Could not execute job specified in a repository since we're not connected to one");
        }
      case REPOSITORY_BY_REFERENCE:
        if (rep != null) {
          // Load the last version...
          //
          return rep.loadJob(jobObjectId, null);
        } else {
          throw new KettleException("Could not execute job specified in a repository since we're not connected to one");
        }
      default: 
        throw new KettleException("The specified object location specification method '"+specificationMethod+"' is not yet supported in this job entry.");
      }
    } catch (Exception e) {
      throw new KettleException("Unexpected error during job metadata load", e);
    }

  }

  /**
   * @return Returns the runEveryResultRow.
   */
  public boolean isExecPerRow() {
    return execPerRow;
  }

  /**
   * @param runEveryResultRow
   *          The runEveryResultRow to set.
   */
  public void setExecPerRow(boolean runEveryResultRow) {
    this.execPerRow = runEveryResultRow;
  }

  public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
    if (!Const.isEmpty(filename)) {
      String realFileName = jobMeta.environmentSubstitute(filename);
      ResourceReference reference = new ResourceReference(this);
      reference.getEntries().add(new ResourceEntry(realFileName, ResourceType.ACTIONFILE));
      references.add(reference);
    }
    return references;
  }

  /**
   * We're going to load the transformation meta data referenced here. Then
   * we're going to give it a new filename, modify that filename in this
   * entries. The parent caller will have made a copy of it, so it should be OK
   * to do so.
   */
  public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface namingInterface, Repository repository) throws KettleException {
    // Try to load the transformation from repository or file.
    // Modify this recursively too...
    //
    // AGAIN: there is no need to clone this job entry because the caller is
    // responsible for this.
    //
    // First load the job meta data...
    //
    copyVariablesFrom(space); // To make sure variables are available.
    JobMeta jobMeta = getJobMeta(repository, space);

    // Also go down into the job and export the files there. (going down
    // recursively)
    //
    String proposedNewFilename = jobMeta.exportResources(jobMeta, definitions, namingInterface, repository);

    // To get a relative path to it, we inject
    // ${Internal.Job.Filename.Directory}
    //
    String newFilename = "${" + Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY + "}/" + proposedNewFilename;

    // Set the filename in the job
    //
    jobMeta.setFilename(newFilename);

    // exports always reside in the root directory, in case we want to turn this
    // into a file repository...
    //
    jobMeta.setRepositoryDirectory(new RepositoryDirectory());

    // change it in the job entry
    //
    filename = newFilename;

    return proposedNewFilename;
  }

  @Override
  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta) {
    if (setLogfile) {
      andValidator().validate(this, "logfile", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
    }

    if (null != directory) {
      // if from repo
      andValidator().validate(this, "directory", remarks, putValidators(notNullValidator())); //$NON-NLS-1$
      andValidator().validate(this, "jobName", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
    } else {
      // else from xml file
      andValidator().validate(this, "filename", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
    }
  }

  public static void main(String[] args) {
    List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>();
    new JobEntryJob().check(remarks, null);
    System.out.printf("Remarks: %s\n", remarks);
  }

  protected String getLogfile() {
    return logfile;
  }

  /**
   * @return the remote slave server name
   */
  public String getRemoteSlaveServerName() {
    return remoteSlaveServerName;
  }

  /**
   * @param remoteSlaveServerName
   *          the remoteSlaveServer to set
   */
  public void setRemoteSlaveServerName(String remoteSlaveServerName) {
    this.remoteSlaveServerName = remoteSlaveServerName;
  }

  /**
   * @return the waitingToFinish
   */
  public boolean isWaitingToFinish() {
    return waitingToFinish;
  }

  /**
   * @param waitingToFinish
   *          the waitingToFinish to set
   */
  public void setWaitingToFinish(boolean waitingToFinish) {
    this.waitingToFinish = waitingToFinish;
  }

  /**
   * @return the followingAbortRemotely
   */
  public boolean isFollowingAbortRemotely() {
    return followingAbortRemotely;
  }

  /**
   * @param followingAbortRemotely
   *          the followingAbortRemotely to set
   */
  public void setFollowingAbortRemotely(boolean followingAbortRemotely) {
    this.followingAbortRemotely = followingAbortRemotely;
  }

  /**
   * @return the passingAllParameters
   */
  public boolean isPassingAllParameters() {
    return passingAllParameters;
  }

  /**
   * @param passingAllParameters
   *          the passingAllParameters to set
   */
  public void setPassingAllParameters(boolean passingAllParameters) {
    this.passingAllParameters = passingAllParameters;
  }

  public Job getJob() {
    return job;
  }

  /**
   * @return the jobObjectId
   */
  public ObjectId getJobObjectId() {
    return jobObjectId;
  }

  /**
   * @param jobObjectId the jobObjectId to set
   */
  public void setJobObjectId(ObjectId jobObjectId) {
    this.jobObjectId = jobObjectId;
  }

  /**
   * @return the specificationMethod
   */
  public ObjectLocationSpecificationMethod getSpecificationMethod() {
    return specificationMethod;
  }

  /**
   * @param specificationMethod the specificationMethod to set
   */
  public void setSpecificationMethod(ObjectLocationSpecificationMethod specificationMethod) {
    this.specificationMethod = specificationMethod;
  }

}