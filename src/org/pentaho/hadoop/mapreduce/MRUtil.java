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

package org.pentaho.hadoop.mapreduce;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

public class MRUtil {
  /**
   * Path to the directory to load plugins from. This must be accessible from all TaskTracker nodes.
   */
  public static final String PROPERTY_PENTAHO_KETTLE_PLUGINS_DIR = "pentaho.kettle.plugins.dir";

  /**
   * Hadoop Configuration for setting KETTLE_HOME. See {@link Const.getKettleDirectory()} for usage.
   */
  public static final String PROPERTY_PENTAHO_KETTLE_HOME = "pentaho.kettle.home";
  
  public static Trans getTrans(final Configuration conf, final String transXml) throws KettleException {
    if (!KettleEnvironment.isInitialized()) {
      System.setProperty(Const.PLUGIN_BASE_FOLDERS_PROP, getPluginDirProperty(conf));
      final String kettleHome = conf.get(PROPERTY_PENTAHO_KETTLE_HOME);
      if (StringUtils.isEmpty(kettleHome)) {
        throw new KettleException(BaseMessages.getString(MRUtil.class, "Property.Missing", PROPERTY_PENTAHO_KETTLE_HOME));
      }
      System.setProperty("KETTLE_HOME", kettleHome);
        
      KettleEnvironment.init();
    }

    TransConfiguration transConfiguration = TransConfiguration.fromXML(transXml);
    TransMeta transMeta = transConfiguration.getTransMeta();
    String carteObjectId = UUID.randomUUID().toString();
    SimpleLoggingObject servletLoggingObject = new SimpleLoggingObject("HADOOP_MAPPER", LoggingObjectType.CARTE, null); //$NON-NLS-1$
    servletLoggingObject.setContainerObjectId(carteObjectId);
    TransExecutionConfiguration executionConfiguration = transConfiguration.getTransExecutionConfiguration();
    servletLoggingObject.setLogLevel(executionConfiguration.getLogLevel());
    return new Trans(transMeta, servletLoggingObject);
  }

  /**
   * Builds a comma-separated list of paths to load Kettle plugins from. To be used as the value for the System property
   * {@link Const.PLUGIN_BASE_FOLDERS_PROP}.
   *
   * @param conf Configuration to retrieve properties from
   * @return Comma-separated list of paths to look for Kettle plugins in
   */
  public static String getPluginDirProperty(final Configuration conf) throws KettleException {
    // Load plugins from the directory specified in the configuration
    String kettlePluginDir = conf.get(PROPERTY_PENTAHO_KETTLE_PLUGINS_DIR);
    
    if (StringUtils.isEmpty(kettlePluginDir)) {
      throw new KettleException(BaseMessages.getString(MRUtil.class, "Property.Missing", PROPERTY_PENTAHO_KETTLE_PLUGINS_DIR));
    }

    kettlePluginDir = Const.DEFAULT_PLUGIN_BASE_FOLDERS + "," + kettlePluginDir;
    return kettlePluginDir;
  }
  
  /**
   * Create a copy of {@code trans}
   */
  public static Trans recreateTrans(Trans trans) {
    return new Trans(trans.getTransMeta(), trans.getParent());
  }

  public static String getStackTrace(Throwable t) {
    StringWriter stringWritter = new StringWriter();
    PrintWriter printWritter = new PrintWriter(stringWritter, true);
    t.printStackTrace(printWritter);
    printWritter.flush();
    stringWritter.flush();
    return stringWritter.toString();
  }

  public static void logMessage(String message) {
    logMessage(Thread.currentThread().hashCode(), message);
  }

  public static void logMessage(Throwable t) {
    logMessage(Thread.currentThread().hashCode(), getStackTrace(t));
  }

  public static void logMessage(String message, Throwable t) {
    logMessage(Thread.currentThread().hashCode(), message);
    logMessage(Thread.currentThread().hashCode(), getStackTrace(t));
  }
  
  public static void logMessage(int id, String message) {
    logMessage(new Integer(id).toString(), message);
  }

  public static void logMessage(int id, Throwable t) {
    logMessage(new Integer(id).toString(), getStackTrace(t));
  }
  
  public static void logMessage(int id, String message, Throwable t) {
    logMessage(new Integer(id).toString(), message);
    logMessage(new Integer(id).toString(), getStackTrace(t));
  }

  
  public static void logMessage(String id, String message) {
    try {
      FileOutputStream fos = new FileOutputStream("/tmp/PDIMapReduce.log", true); //$NON-NLS-1$
      if (id != null) {
        fos.write((id + ": ").getBytes()); //$NON-NLS-1$
      }
      fos.write(message.getBytes());
      fos.write(System.getProperty("line.separator").getBytes()); //$NON-NLS-1$
      fos.close();
    } catch (Throwable t) {
    }
  }

  public static Class<?> getJavaClass(ValueMetaInterface vmi) {
    Class<?> metaClass = null;

    switch (vmi.getType()) {
    case ValueMeta.TYPE_BIGNUMBER: {
      metaClass = BigDecimal.class;
    }
      break;
    case ValueMeta.TYPE_BINARY: {
      metaClass = byte[].class;
    }
      break;
    case ValueMeta.TYPE_BOOLEAN: {
      metaClass = Boolean.class;
    }
      break;
    case ValueMeta.TYPE_DATE: {
      metaClass = Date.class;
    }
      break;
    case ValueMeta.TYPE_INTEGER: {
      metaClass = Long.class;
    }
      break;
    case ValueMeta.TYPE_NUMBER: {
      metaClass = Double.class;
    }
      break;
    case ValueMeta.TYPE_STRING: {
      metaClass = String.class;
    }
      break;
    }

    return metaClass;
  }
  
  public static Class getWritableForKettleType(ValueMetaInterface kettleType) {
    switch (kettleType.getType()) {
    case ValueMetaInterface.TYPE_STRING:
    case ValueMetaInterface.TYPE_BIGNUMBER:
    case ValueMetaInterface.TYPE_DATE:
      return Text.class;
    case ValueMetaInterface.TYPE_INTEGER:
      return LongWritable.class;
    case ValueMetaInterface.TYPE_NUMBER:
      return DoubleWritable.class;
    case ValueMetaInterface.TYPE_BOOLEAN:
      return BooleanWritable.class;
    case ValueMetaInterface.TYPE_BINARY:
      return BytesWritable.class;
    default:
        return Text.class;      
    }
  }
}
