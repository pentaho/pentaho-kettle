/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.di.job.entries.sqoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.sqoop.Sqoop;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.hadoop.jobconf.HadoopConfigurer;
import org.pentaho.hadoop.jobconf.HadoopConfigurerFactory;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all Sqoop job entries.
 */
public abstract class AbstractSqoopJobEntry<S extends SqoopConfig> extends JobEntryBase implements Cloneable, JobEntryInterface {
  /**
   * Configuration object for this job entry
   */
  private S sqoopConfig;

  /**
   * Log4j appender that redirects all Log4j logging to a Kettle {@link org.pentaho.di.core.logging.LogChannel}
   */
  private Appender sqoopToKettleAppender;
  /**
   * Logging proxy that redirects all {@link java.io.PrintStream} output to a Log4j logger.
   */
  private LoggingProxy stdErrProxy;

  /**
   * Logging categories to monitor and log within Kettle
   */
  private String[] LOGS_TO_MONITOR = new String[]{"org.apache.sqoop", "org.apache.hadoop"};
  /**
   * Cache for the levels of loggers we changed so we can revert them when we remove our appender
   */
  private Map<String, Level> logLevelCache = new HashMap<String, Level>();

  /**
   * Creates a new Sqoop job entry and loads creates a default {@link SqoopConfig} object via {@link #createSqoopConfig()}.
   */
  protected AbstractSqoopJobEntry() {
    sqoopConfig = createSqoopConfig();
  }

  /**
   * Build a configuration object that contains all configuration settings for this job entry. This will be configured by
   * {@link #createSqoopConfig} and is not intended to be used directly.
   *
   * @return a {@link SqoopConfig} object that contains all configuration settings for this job entry
   */
  protected abstract S buildSqoopConfig();

  /**
   * Declare the {@link Sqoop} tool used in this job entry.
   *
   * @return the name of the sqoop tool to use, e.g. "import"
   */
  protected abstract String getToolName();

  /**
   * @return a {@link SqoopConfig} that contains all configuration settings for this job entry
   */
  protected final S createSqoopConfig() {
    S config = buildSqoopConfig();
    SqoopUtils.configureConnectionInformation(config, new Configuration());
    return config;
  }

  /**
   * @return the Sqoop configuration object for this job entry
   */
  public S getSqoopConfig() {
    sqoopConfig.setJobEntryName(getName());
    return sqoopConfig;
  }

  /**
   * Set the Sqoop configuration object for this job entry
   *
   * @param sqoopConfig Sqoop configuration object for this job entry
   */
  public void setSqoopConfig(S sqoopConfig) {
    this.sqoopConfig = sqoopConfig;
    setName(sqoopConfig.getJobEntryName());
  }

  /**
   * @return {@code true} if this job entry yields a success or failure result
   */
  @Override
  public boolean evaluates() {
    return true;
  }

  /**
   * @return {@code true} if this job entry supports and unconditional hop from it
   */
  @Override
  public boolean isUnconditional() {
    return true;
  }

  /**
   * @return an portion of XML describing the current state of this job entry
   */
  @Override
  public String getXML() {
    StringBuffer buffer = new StringBuffer(1024);
    buffer.append(super.getXML());
    JobEntrySerializationHelper.write(sqoopConfig, 1, buffer);
    return buffer.toString();
  }

  /**
   * Set the state of this job entry from an XML document node containing a previous state.
   *
   * @param node
   * @param databaseMetas
   * @param slaveServers
   * @param repository
   * @throws KettleXMLException
   */
  @Override
  public void loadXML(Node node, List<DatabaseMeta> databaseMetas, List<SlaveServer> slaveServers, Repository repository) throws KettleXMLException {
    super.loadXML(node, databaseMetas, slaveServers);

    // Load into a new SqoopConfig since JobEntrySerializationHelper will obliterate the list of arguments
    S loaded = createSqoopConfig();
    JobEntrySerializationHelper.read(loaded, node);
    updateConfigFromSavedState(loaded);
  }

  @Override
  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    super.loadRep(rep, id_jobentry, databases, slaveServers);

    // Load the arguments and merge them with the existing ones
    S loaded = createSqoopConfig();
    JobEntrySerializationHelper.loadRep(loaded, rep, id_jobentry, databases, slaveServers);
    updateConfigFromSavedState(loaded);
  }

  private void updateConfigFromSavedState(S loaded) {
    // Clone the loaded config so we can re-initialize the arguments and re-apply their values
    S loadedClone = (S) loaded.clone();
    loaded.initArguments();
    loaded.copyArguments(loadedClone);
    setSqoopConfig(loaded);
  }

  @Override
  public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
    JobEntrySerializationHelper.saveRep(sqoopConfig, rep, id_job, getObjectId());
  }

  @SuppressWarnings("deprecation")
  public void attachLoggingAppenders() {
    sqoopToKettleAppender = new org.pentaho.di.core.logging.KettleLogChannelAppender(log);
    try {
      // Redirect all stderr logging to the first log to monitor so it shows up in the Kettle LogChannel
      Logger sqoopLogger = SqoopUtils.findLogger(LOGS_TO_MONITOR[0]);
      if (sqoopLogger != null) {
        stdErrProxy = new LoggingProxy(System.err, sqoopLogger, Level.ERROR);
        System.setErr(stdErrProxy);
      }
      SqoopUtils.attachAppenderTo(sqoopToKettleAppender, getLogLevel(), logLevelCache, LOGS_TO_MONITOR);
    } catch (Exception ex) {
      logError(BaseMessages.getString(AbstractSqoopJobEntry.class, "ErrorAttachingLogging"));
      logError(Const.getStackTracker(ex));

      // Attempt to clean up logging if we failed
      try {
        SqoopUtils.removeAppenderFrom(sqoopToKettleAppender, logLevelCache, LOGS_TO_MONITOR);
      } catch (Exception e) {
        // Ignore any exceptions while trying to clean up
      }
    }
  }

  public void removeLoggingAppenders() {
    try {
      if (sqoopToKettleAppender != null) {
        SqoopUtils.removeAppenderFrom(sqoopToKettleAppender, logLevelCache, LOGS_TO_MONITOR);
        sqoopToKettleAppender = null;
      }
      if (stdErrProxy != null) {
        System.setErr(stdErrProxy.getWrappedStream());
        stdErrProxy = null;
      }
    } catch (Exception ex) {
      logError(BaseMessages.getString(AbstractSqoopJobEntry.class, "ErrorDetachingLogging"));
      logError(Const.getStackTracker(ex));
    }
  }

  @Override
  public Result execute(Result result, int i) throws KettleException {
    System.setProperty(Sqoop.SQOOP_RETHROW_PROPERTY, "true"); // Make sure Sqoop throws errors instead of returning a status of 1

    final Result jobResult = result;
    result.setResult(true);

    Thread t = new Thread() {
      @Override
      public void run() {
        executeSqoop(getSqoopConfig(), getHadoopConfiguration(), jobResult);
      }
    };

    t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        handleUncaughtThreadException(t, e, jobResult);
      }
    });

    t.start();

    if (sqoopConfig.isBlockingExecution()) {
      while (!parentJob.isStopped() && t.isAlive()) {
        try {
          t.join(300);
        } catch (InterruptedException ex) {
          // ignore
          break;
        }
      }
      // If the parent job is stopped and the thread is still running make sure to interrupt it
      if (t.isAlive()) {
        t.interrupt();
      }
      // Wait for thread to die so we get the proper return status set in jobResult before returning
      try {
        t.join(10 * 1000); // Don't wait for more than 10 seconds in case the thread is really blocked
      } catch (InterruptedException e) {
        // ignore
      }
    }

    return result;
  }

  /**
   * Handle any clean up required when our execution thread encounters an unexpected {@link Exception}.
   *
   * @param t Thread that encountered the uncaught exception
   * @param e Exception that was encountered
   * @param jobResult Job result for the execution that spawned the thread
   */
  protected void handleUncaughtThreadException(Thread t, Throwable e, Result jobResult) {
    removeLoggingAppenders();
    setJobResultFailed(jobResult);
  }

  /**
   * @return the Hadoop configuration object for this Sqoop execution
   */
  protected Configuration getHadoopConfiguration() {
    return new Configuration();
  }

  /**
   * Executes Sqoop using the provided configuration objects. The {@code jobResult} will accurately reflect the completed
   * execution state when finished.
   *
   * @param config Sqoop configuration settings
   * @param hadoopConfig Hadoop configuration settings. This will be additionally configured using {@link #configure(org.apache.hadoop.conf.Configuration)}.
   * @param jobResult Result to update based on feedback from the Sqoop tool
   */
  protected void executeSqoop(SqoopConfig config, Configuration hadoopConfig, Result jobResult) {
    attachLoggingAppenders();
    try {
      configure(hadoopConfig);
      List<String> args = config.getCommandLineArgs();
      args.add(0, getToolName()); // push the tool command-line argument on the top of the args list
      int result = Sqoop.runTool(args.toArray(new String[args.size()]), hadoopConfig);
      if (result != 0) {
        setJobResultFailed(jobResult);
      }
    } catch (Exception ex) {
      logError(BaseMessages.getString(SqoopExportJobEntry.class, "ErrorRunningSqoopTool"), ex);
      setJobResultFailed(jobResult);
    } finally {
      removeLoggingAppenders();
    }
  }

  /**
   * Flag a job result as failed
   *
   * @param jobResult
   */
  protected void setJobResultFailed(Result jobResult) {
    jobResult.setNrErrors(1);
    jobResult.setResult(false);
  }

  /**
   * Configure the Hadoop environment
   * TODO Move this to HadoopConfigurerFactory
   *
   * @param conf
   * @throws org.pentaho.di.core.exception.KettleException
   *
   */
  protected void configure(Configuration conf) throws KettleException {
    try {
      HadoopConfigurer configurer = HadoopConfigurerFactory.getConfigurer("generic"); // TODO The default should be handled by the factory
      List<String> messages = new ArrayList<String>();

      configurer.configure(sqoopConfig.getNamenodeHost(), sqoopConfig.getNamenodePort(),
        sqoopConfig.getJobtrackerHost(), sqoopConfig.getJobtrackerPort(),
        conf, messages);
      for (String m : messages) {
        logBasic(m);
      }
    } catch (Exception e) {
      throw new KettleException(BaseMessages.getString(SqoopExportJobEntry.class, "ErrorConfiguringHadoopEnvironment"), e);
    }
  }

}
