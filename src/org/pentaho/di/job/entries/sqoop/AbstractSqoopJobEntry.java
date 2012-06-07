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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.HiveDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.AbstractJobEntry;
import org.pentaho.di.job.JobEntryUtils;
import org.pentaho.di.job.LoggingProxy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.hadoop.jobconf.HadoopConfigurer;
import org.pentaho.hadoop.jobconf.HadoopConfigurerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all Sqoop job entries.
 */
public abstract class AbstractSqoopJobEntry<S extends SqoopConfig> extends AbstractJobEntry<S> implements Cloneable, JobEntryInterface {

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
   * Build a configuration object that contains all configuration settings for this job entry. This will be configured by
   * {@link #createJobConfig} and is not intended to be used directly.
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
  protected final S createJobConfig() {
    S config = buildSqoopConfig();
    try {
      SqoopUtils.configureConnectionInformation(config, new Configuration());
    } catch (Exception ex) {
      // Error loading connection information from Hadoop Configuration. Just log the error and leave the configuration as is.
      logError(BaseMessages.getString(AbstractSqoopJobEntry.class, "ErrorLoadingHadoopConnectionInformation"), ex);
    }
    return config;
  }

  /**
   * Attach a log appender to all Loggers used by Sqoop so we can redirect the output to Kettle's logging
   * facilities.
   */
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

  /**
   * Remove our log appender from all loggers used by Sqoop.
   */
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

  /**
   * Validate any configuration option we use directly that could be invalid at runtime.
   *
   * @param config Configuration to validate
   * @return List of warning messages for any invalid configuration options we use directly in this job entry.
   */
  public List<String> getValidationWarnings(SqoopConfig config) {
    List<String> warnings = new ArrayList<String>();

    if (StringUtil.isEmpty(config.getConnect())) {
      warnings.add(BaseMessages.getString(AbstractSqoopJobEntry.class, "ValidationError.Connect.Message", config.getConnect()));
    }

    try {
      JobEntryUtils.asLong(config.getBlockingPollingInterval(), variables);
    } catch (NumberFormatException ex) {
      warnings.add(BaseMessages.getString(AbstractSqoopJobEntry.class, "ValidationError.BlockingPollingInterval.Message", config.getBlockingPollingInterval()));
    }

    return warnings;
  }

  /**
   * Determine if the configuration provide is valid. This will validate all options in one pass.
   *
   * @param config Configuration to validate
   * @return {@code true} if the configuration contains valid values for all options we use directly in this job entry.
   */
  @Override
  public boolean isValid(SqoopConfig config) {
    List<String> warnings = getValidationWarnings(config);
    for (String warning : warnings) {
      logError(warning);
    }
    return warnings.isEmpty();
  }

  /**
   * Handle any clean up required when our execution thread encounters an unexpected {@link Exception}.
   *
   * @param t         Thread that encountered the uncaught exception
   * @param e         Exception that was encountered
   * @param jobResult Job result for the execution that spawned the thread
   */
  protected void handleUncaughtThreadException(Thread t, Throwable e, Result jobResult) {
    logError(BaseMessages.getString(AbstractSqoopJobEntry.class, "ErrorRunningSqoopTool"), e);
    removeLoggingAppenders();
    setJobResultFailed(jobResult);
  }

  /**
   * @return the Hadoop configuration object for this Sqoop execution
   */
  protected Configuration getHadoopConfiguration() {
    return new Configuration();
  }

  @Override
  protected Runnable getExecutionRunnable(final Result jobResult) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        executeSqoop(getJobConfig(), getHadoopConfiguration(), jobResult);
      }
    };
    return runnable;
  }

  /**
   * Executes Sqoop using the provided configuration objects. The {@code jobResult} will accurately reflect the completed
   * execution state when finished.
   *
   * @param config       Sqoop configuration settings
   * @param hadoopConfig Hadoop configuration settings. This will be additionally configured using {@link #configure(org.apache.hadoop.conf.Configuration)}.
   * @param jobResult    Result to update based on feedback from the Sqoop tool
   */
  protected void executeSqoop(SqoopConfig config, Configuration hadoopConfig, Result jobResult) {
    attachLoggingAppenders();
    try {
      configure(hadoopConfig);
      List<String> args = SqoopUtils.getCommandLineArgs(config, getVariables());
      args.add(0, getToolName()); // push the tool command-line argument on the top of the args list
      int result = Sqoop.runTool(args.toArray(new String[args.size()]), hadoopConfig);
      if (result != 0) {
        setJobResultFailed(jobResult);
      }
    } catch (Exception ex) {
      logError(BaseMessages.getString(AbstractSqoopJobEntry.class, "ErrorRunningSqoopTool"), ex);
      setJobResultFailed(jobResult);
    } finally {
      removeLoggingAppenders();
    }
  }

  /**
   * Configure the Hadoop environment
   * TODO Move this to HadoopConfigurerFactory
   *
   * @param conf
   * @throws org.pentaho.di.core.exception.KettleException
   *
   */
  public void configure(Configuration conf) throws KettleException {
    try {
      HadoopConfigurer configurer = HadoopConfigurerFactory.getConfigurer("generic"); // TODO The default should be handled by the factory
      List<String> messages = new ArrayList<String>();

      SqoopConfig sqoopConfig = getJobConfig();
      configurer.configure(sqoopConfig.getNamenodeHost(), sqoopConfig.getNamenodePort(),
        sqoopConfig.getJobtrackerHost(), sqoopConfig.getJobtrackerPort(),
        conf, messages);
      for (String m : messages) {
        logBasic(m);
      }
    } catch (Exception e) {
      throw new KettleException(BaseMessages.getString(AbstractSqoopJobEntry.class, "ErrorConfiguringHadoopEnvironment"), e);
    }
  }

  /**
   * Determine if a database type is supported.
   *
   * @param databaseType Database type to check for compatibility
   * @return {@code true} if this database is supported for this tool
   */
  public boolean isDatabaseSupported(Class<? extends DatabaseInterface> databaseType) {
    // For now all database types are supported
    return true;
  }
}
