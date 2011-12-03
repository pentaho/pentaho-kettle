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
package org.pentaho.hadoop.mapreduce;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

public class MRUtil {

  
  public static Trans getTrans(String transXml) throws KettleException {
    Trans trans = null;
    if (!KettleEnvironment.isInitialized()) {
      // Additionally load plugins from:
      //   $HADOOP_HOME/plugins
      //   $HADOOP_PDI_PLUGIN_FOLDER
      String hadoopPdiPluginPaths = System.getenv("HADOOP_HOME") + "/plugins"; //$NON-NLS-1$ //$NON-NLS-2$
      String hadoopPdiPluginFolder = System.getenv("HADOOP_PDI_PLUGIN_FOLDER"); //$NON-NLS-1$
      if (!StringUtils.isEmpty(hadoopPdiPluginFolder)) {
        hadoopPdiPluginPaths += "," + hadoopPdiPluginFolder; //$NON-NLS-1$
      }
      hadoopPdiPluginPaths = Const.DEFAULT_PLUGIN_BASE_FOLDERS + "," + hadoopPdiPluginPaths; //$NON-NLS-1$
      System.setProperty(Const.PLUGIN_BASE_FOLDERS_PROP, hadoopPdiPluginPaths);
      System.out.format("Loading PDI plugins from: '%s'\n", hadoopPdiPluginPaths); //$NON-NLS-1$
      KettleEnvironment.init();
    }
    TransConfiguration transConfiguration = TransConfiguration.fromXML(transXml);
    TransMeta transMeta = transConfiguration.getTransMeta();
    String carteObjectId = UUID.randomUUID().toString();
    SimpleLoggingObject servletLoggingObject = new SimpleLoggingObject("HADOOP_MAPPER", LoggingObjectType.CARTE, null); //$NON-NLS-1$
    servletLoggingObject.setContainerObjectId(carteObjectId);
    TransExecutionConfiguration executionConfiguration = transConfiguration.getTransExecutionConfiguration();
    servletLoggingObject.setLogLevel(executionConfiguration.getLogLevel());
    trans = new Trans(transMeta, servletLoggingObject);

    return trans;
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

}
