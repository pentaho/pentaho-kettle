/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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

package org.pentaho.di.job.entries.hadoopjobexecutor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.RunningJob;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.SerializationHelper;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

@JobEntry(id = "HadoopJobExecutorPlugin", name = "Hadoop Job Executor", categoryDescription = "Hadoop", description = "Execute Map/Reduce jobs in Hadoop", image = "HDE.png")
public class JobEntryHadoopJobExecutor extends JobEntryBase implements Cloneable, JobEntryInterface {

  private String hadoopJobName;

  private String jarUrl;

  private boolean isSimple;

  private String cmdLineArgs;

  private String outputKeyClass;
  private String outputValueClass;
  private String mapperClass;
  private String combinerClass;
  private String reducerClass;
  private String inputFormatClass;
  private String outputFormatClass;

  private String workingDirectory;

  private String hdfsHostname;
  private String hdfsPort;

  private String jobTrackerHostname;
  private String jobTrackerPort;

  private String inputPath;
  private String outputPath;

  private boolean blocking;

  private List<UserDefinedItem> userDefined;

  public class UserDefinedItem {
    private String name;
    private String value;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }

  public String getHadoopJobName() {
    return hadoopJobName;
  }

  public void setHadoopJobName(String hadoopJobName) {
    this.hadoopJobName = hadoopJobName;
  }
  
  public Result execute(Result result, int arg1) throws KettleException {

    try {
      if (isSimple) {
        List<Class<?>> classesWithMains = JarUtility.getClassesInJarWithMain(jarUrl);
        for (Class<?> clazz : classesWithMains) {
          try {
            Method mainMethod = clazz.getMethod("main", new Class[] { String[].class });
            Object[] args = new String[] {};
            if (cmdLineArgs != null) {
              args = cmdLineArgs.split(" ");
            }
            mainMethod.invoke(clazz, args);
          } catch (Throwable ignored) {
            // skip, try the next one
          }
        }

      } else {

        URL[] urls = new URL[] { new URL(jarUrl) };
        URLClassLoader loader = new URLClassLoader(urls);

        JobConf conf = new JobConf();
        conf.setJobName(hadoopJobName);

        conf.setOutputKeyClass(loader.loadClass(outputKeyClass));
        conf.setOutputValueClass(loader.loadClass(outputValueClass));

        Class<? extends Mapper> mapper = (Class<? extends Mapper>) loader.loadClass(mapperClass);
        conf.setMapperClass(mapper);
        Class<? extends Reducer> combiner = (Class<? extends Reducer>) loader.loadClass(combinerClass);
        conf.setCombinerClass(combiner);
        Class<? extends Reducer> reducer = (Class<? extends Reducer>) loader.loadClass(reducerClass);
        conf.setReducerClass(reducer);

        Class<? extends InputFormat> inputFormat = (Class<? extends InputFormat>) loader.loadClass(inputFormatClass);
        conf.setInputFormat(inputFormat);
        Class<? extends OutputFormat> outputFormat = (Class<? extends OutputFormat>) loader.loadClass(outputFormatClass);
        conf.setOutputFormat(outputFormat);

        // TODO: this could be a list of input paths apparently
        FileInputFormat.setInputPaths(conf, new Path(inputPath));
        FileOutputFormat.setOutputPath(conf, new Path(outputPath));

        String hdfsBaseUrl = "hdfs://" + hdfsHostname + ":" + hdfsPort;
        conf.set("fs.default.name", hdfsBaseUrl);
        conf.set("mapred.job.tracker", jobTrackerHostname + ":" + jobTrackerPort);

        // process user defined values
        for (UserDefinedItem item : userDefined) {
          conf.set(item.getName(), item.getValue());
        }

        conf.setWorkingDirectory(new Path(hdfsBaseUrl + workingDirectory));
        conf.setJar(jarUrl);

        JobClient jobClient = new JobClient(conf);
        RunningJob runningJob = jobClient.submitJob(conf);

        if (blocking) {
          try {
            while (!runningJob.isComplete()) {
              Thread.sleep(500);
            }
          } catch (InterruptedException ie) {
            log.logError(ie.getMessage(), ie);
          }
        }

      }
      return result;
    } catch (ClassNotFoundException cnfe) {
      throw new KettleException(cnfe);
    } catch (IOException ioe) {
      throw new KettleException(ioe);
    }
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException {
    SerializationHelper.read(this, entrynode);
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(1024);
    SerializationHelper.write(this, 1, retval);
    return retval.toString();
  }

  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    SerializationHelper.readJobRep(this, rep, id_jobentry, databases);
  }

  public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
    SerializationHelper.saveJobRep(this, rep, id_job, getObjectId());
  }

}
