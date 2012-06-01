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
package org.pentaho.hadoop.mapreduce.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.JobTracker.State;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSubmitMapReduceJob {

  private static FileSystemManager fsManager;

  private static String hostname = "hadoop-vm1";
  private static String hdfsPort = "9000";
  private static String trackerPort = "9001";
  private static String username = "username";
  private static String password = "password";

  public static String buildHDFSURL(String path) {
    // hdfs://myusername:mypassword@somehost/pub/downloads/somefile.tgz
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    if (username != null && !"".equals(username)) {
      return "hdfs://" + username + ":" + password + "@" + hostname + ":" + hdfsPort + path;
    }
    return "hdfs://" + hostname + ":" + hdfsPort + path;
  }

  @BeforeClass
  public static void beforeClass() throws IOException {
    fsManager = VFS.getManager();
    Properties settings = new Properties();
    settings.load(TestSubmitMapReduceJob.class.getResourceAsStream("/test-settings.properties"));
    hostname = settings.getProperty("hostname", hostname);
    hdfsPort = settings.getProperty("hdfsPort", hdfsPort);
    trackerPort = settings.getProperty("trackerPort", trackerPort);
    username = settings.getProperty("username", username);
    password = settings.getProperty("password", password);

    // file management
    // first delete any existing resources that will conflict
    FileObject file = fsManager.resolveFile(buildHDFSURL("/junit/wordcount/output"));
    file.delete(new FileSelector() {
      public boolean includeFile(FileSelectInfo arg0) throws Exception {
        return true;
      }

      public boolean traverseDescendents(FileSelectInfo arg0) throws Exception {
        return true;
      }
    });
  }

  @Test
  public void submitJob() throws Exception {

    String[] args = { "hdfs://" + hostname + ":" + hdfsPort + "/junit/wordcount/input", "hdfs://" + hostname + ":" + hdfsPort + "/junit/wordcount/output" };

    JobConf conf = new JobConf();
    conf.setJobName("wordcount");

    conf.setOutputKeyClass(Text.class);
    conf.setOutputValueClass(IntWritable.class);

    File jar = new File("./test-res/pentaho-mapreduce-sample.jar");

    URLClassLoader loader = new URLClassLoader(new URL[] { jar.toURI().toURL() });

    conf.setMapperClass((Class<? extends Mapper>) loader.loadClass("org.pentaho.hadoop.mapreduce.sample.MRWordCount$Map"));
    conf.setCombinerClass((Class<? extends Reducer>) loader.loadClass("org.pentaho.hadoop.mapreduce.sample.MRWordCount$Reduce"));
    conf.setReducerClass((Class<? extends Reducer>) loader.loadClass("org.pentaho.hadoop.mapreduce.sample.MRWordCount$Reduce"));

    conf.setInputFormat(TextInputFormat.class);
    conf.setOutputFormat(TextOutputFormat.class);

    FileInputFormat.setInputPaths(conf, new Path(args[0]));
    FileOutputFormat.setOutputPath(conf, new Path(args[1]));

    conf.set("fs.default.name", "hdfs://" + hostname + ":" + hdfsPort);
    conf.set("mapred.job.tracker", hostname + ":" + trackerPort);

    conf.setJarByClass(loader.loadClass("org.pentaho.hadoop.mapreduce.sample.MRWordCount"));
    conf.setWorkingDirectory(new Path("/tmp/wordcount"));

    JobClient jobClient = new JobClient(conf);
    ClusterStatus status = jobClient.getClusterStatus();
    assertEquals(State.RUNNING, status.getJobTrackerState());

    RunningJob runningJob = jobClient.submitJob(conf);
    System.out.print("Running " + runningJob.getJobName() + "");
    while (!runningJob.isComplete()) {
      System.out.print(".");
      Thread.sleep(500);
    }
    System.out.println();
    System.out.println("Finished " + runningJob.getJobName() + ".");

    FileObject file = fsManager.resolveFile(buildHDFSURL("/junit/wordcount/output/part-00000"));
    String output = IOUtils.toString(file.getContent().getInputStream());
    assertEquals("Bye\t1\nGoodbye\t1\nHadoop\t2\nHello\t2\nWorld\t2\n", output);
  }

}
