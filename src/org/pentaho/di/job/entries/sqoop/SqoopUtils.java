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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapred.JobTracker;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.*;

/**
 * Collection of utility methods used to support integration with Apache Sqoop.
 */
public class SqoopUtils {
  /**
   * Prefix to append before an argument's name when building up a list of command-line arguments, e.g. "--"
   */
  public static final String ARG_PREFIX = "--";

  public static Logger findLogger(String logName) {
    Log log = LogFactory.getLog(logName);
    if (log instanceof org.apache.commons.logging.impl.Log4JLogger) {
      Logger logger = ((org.apache.commons.logging.impl.Log4JLogger) log).getLogger();
      if (logger == null) {
        throw new IllegalArgumentException("Logger does not exist for log: " + logName);
      }
      return logger;
    } else if (log == null) {
      throw new IllegalArgumentException("Unknown log name: " + logName);
    } else {
      throw new IllegalArgumentException("Unsupported logging type: " + log.getClass());
    }
  }

  @SuppressWarnings("deprecation")
  public static void attachAppenderTo(Appender appender, LogLevel logLevel, Map<String, Level> logLevelCache, String... logNames) {
    for (String logName : logNames) {
      Logger logger = findLogger(logName);
      logger.addAppender(appender);
      // Update logger level to match our logging level
      Level level = org.pentaho.di.core.logging.KettleLogChannelAppender.LOG_LEVEL_MAP.get(logLevel);
      if (level != null) {
        // Cache the original level so we can reset it when we're done
        logLevelCache.put(logger.getName(), logger.getLevel());
        logger.setLevel(level);
      }
    }
  }

  public static void removeAppenderFrom(Appender appender, Map<String, Level> logLevelCache, String... logNames) {
    for (String logName : logNames) {
      Logger logger = findLogger(logName);
      logger.removeAppender(appender);
      // Reset logger level if it was changed
      if (logLevelCache.containsKey(logger.getName())) {
        logger.setLevel(logLevelCache.get(logger.getName()));
        logLevelCache.remove(logger.getName());
      }
    }
    appender.close();
  }

  /**
   * Configure a {@link SqoopConfig}'s Namenode and Jobtracker connection information based off a Hadoop Configuration's
   * settings. These properties are parsed from {@code fs.default.name} and {@code mapred.job.tracker} properties.
   *
   * @param config Sqoop configuration to update
   * @param c      Hadoop configuration to parse connection information from
   */
  public static void configureConnectionInformation(SqoopConfig config, Configuration c) {
    URI namenode = FileSystem.getDefaultUri(c);
    if (namenode != null) {
      config.setNamenodeHost(namenode.getHost());
      if (namenode.getPort() != -1) {
        config.setNamenodePort(String.valueOf(namenode.getPort()));
      }
    }

    if (!"local".equals(c.get("mapred.job.tracker", "local"))) {
      InetSocketAddress jobtracker = JobTracker.getAddress(c);
      config.setJobtrackerHost(jobtracker.getHostName());
      config.setJobtrackerPort(String.valueOf(jobtracker.getPort()));
    }
  }

  /**
   * Generate a list of command line arguments and their values for arguments that require them.
   *
   * @param config        Sqoop configuration to build a list of command line arguments from
   * @param variableSpace Variable space to look up argument values from. May be {@code null}
   * @return All the command line arguments for this configuration object
   */
  public static List<String> getCommandLineArgs(SqoopConfig config, VariableSpace variableSpace) {
    List<String> args = new ArrayList<String>();

    appendArguments(args, SqoopUtils.findAllArguments(config), variableSpace);

    return args;
  }

  /**
   * Add all {@link ArgumentWrapper}s to a list of arguments
   *
   * @param args          Arguments to append to
   * @param arguments     Arguments to append
   * @param variableSpace Variable space to look up argument values from. May be {@code null}.
   */
  protected static void appendArguments(List<String> args, Set<? extends ArgumentWrapper> arguments, VariableSpace variableSpace) {
    for (ArgumentWrapper ai : arguments) {
      appendArgument(args, ai, variableSpace);
    }
  }

  /**
   * Append this argument to a list of arguments if it has a value or if it's a flag.
   *
   * @param args List of arguments to append to
   */
  protected static void appendArgument(List<String> args, ArgumentWrapper arg, VariableSpace variableSpace) {
    String value = arg.getValue();
    if (variableSpace != null) {
      value = variableSpace.environmentSubstitute(value);
    }
    if (arg.isFlag() && Boolean.parseBoolean(value)) {
      args.add(ARG_PREFIX + arg.getName());
    } else if (!arg.isFlag() && value != null) {
      args.add(ARG_PREFIX + arg.getName());
      args.add(value);
    }
  }

  /**
   * Find all fields annotated with {@link CommandLineArgument} in the class provided. All arguments must have valid
   * JavaBeans-style getter and setter methods in the object.
   *
   * @param o Object to look for arguments in
   * @return Ordered set of arguments representing all {@link CommandLineArgument}-annotated fields in {@code o}
   */
  public static Set<? extends ArgumentWrapper> findAllArguments(Object o) {
    Set<ArgumentWrapper> arguments = new LinkedHashSet<ArgumentWrapper>();

    Class<?> aClass = o.getClass();
    while (aClass != null) {
      for (Field f : aClass.getDeclaredFields()) {
        if (f.isAnnotationPresent(CommandLineArgument.class)) {
          CommandLineArgument anno = f.getAnnotation(CommandLineArgument.class);
          String fieldName = f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
          Method getter = findMethod(aClass, fieldName, null, "get", "is");
          Method setter = findMethod(aClass, fieldName, new Class<?>[]{f.getType()}, "set");
          arguments.add(new ArgumentWrapper(anno.name(), getDisplayName(anno), anno.flag(), o, getter, setter));
        }
      }
      aClass = aClass.getSuperclass();
    }

    return arguments;
  }

  /**
   * Determine the display name for the command line argument.
   *
   * @param anno Command line argument
   * @return {@link org.pentaho.di.job.entries.sqoop.CommandLineArgument#displayName()} or, if not set, {@link org.pentaho.di.job.entries.sqoop.CommandLineArgument#name()}
   */
  public static String getDisplayName(CommandLineArgument anno) {
    return StringUtil.isEmpty(anno.displayName()) ? anno.name() : anno.displayName();
  }

  /**
   * Finds a method in the given class or any super class with the name {@code prefix + methodName} that accepts 0 parameters.
   *
   * @param aClass         Class to search for method in
   * @param methodName     Camelcase'd method name to search for with any of the provided prefixes
   * @param parameterTypes The parameter types the method signature must match.
   * @param prefixes       Prefixes to prepend to {@code methodName} when searching for method names, e.g. "get", "is"
   * @return The first method found to match the format {@code prefix + methodName}
   */
  public static Method findMethod(Class<?> aClass, String methodName, Class<?>[] parameterTypes, String... prefixes) {
    for (String prefix : prefixes) {
      try {
        return aClass.getDeclaredMethod(prefix + methodName, parameterTypes);
      } catch (NoSuchMethodException ex) {
        // ignore, continue searching prefixes
      }
    }
    // If no method found with any prefixes search the super class
    aClass = aClass.getSuperclass();
    return aClass == null ? null : findMethod(aClass, methodName, parameterTypes, prefixes);
  }

  /**
   * @return {@code true} if {@link Boolean#parseBoolean(String)} returns {@code true} for {@link #isBlockingExecution()}
   */
  /**
   * Determine if the string equates to {@link Boolean#TRUE} after performing a variable substitution.
   *
   * @param s             String-encoded boolean value or variable expression
   * @param variableSpace Context for variables so we can substitute {@code s}
   * @return the value returned by {@link Boolean#parseBoolean(String) Boolean.parseBoolean(s)} after substitution
   */
  public static boolean asBoolean(String s, VariableSpace variableSpace) {
    String value = variableSpace.environmentSubstitute(s);
    return Boolean.parseBoolean(value);
  }

  /**
   * Parse the string as a {@link Long} after variable substitution.
   *
   * @param s             String-encoded {@link Long} value or variable expression that should resolve to a {@link Long} value
   * @param variableSpace Context for variables so we can substitute {@code s}
   * @return the value returned by {@link Long#parseLong(String, int) Long.parseLong(s, 10)} after substitution
   */
  public static Long asLong(String s, VariableSpace variableSpace) {
    String value = variableSpace.environmentSubstitute(s);
    return value == null ? null : Long.valueOf(value, 10);
  }
}
