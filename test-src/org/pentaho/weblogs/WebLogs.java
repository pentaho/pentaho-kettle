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
package org.pentaho.weblogs;
/**
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

/**
 * This is an example Hadoop Map/Reduce application.
 * It reads the text input files, breaks each line into words
 * and counts them. The output is a locally sorted list of words and the
 * count of how often they occurred.
 *
 * To run: bin/hadoop jar build/hadoop-examples.jar wordcount
 *            [-m <i>maps</i>] [-r <i>reduces</i>] <i>in-dir</i> <i>out-dir</i>
 */
public class WebLogs extends Configured implements Tool {
    
    private static final String input = "./junit/weblogs/input/access.log";
    private static final String outputFolder = "./junit/weblogs/output";
    
    static int printUsage() {
        System.out.println("Weblogs [-m <maps>] [-r <reduces>] <input> <output>");
        ToolRunner.printGenericCommandUsage(System.out);
        return -1;
    }

  /**
   * The main driver for word count map/reduce program.
   * Invoke this method to submit the map/reduce job.
   * @throws IOException When there is communication problems with the
   *                     job tracker.
   */
  public int run(String[] args) throws Exception {
           
    JobConf conf = new JobConf(getConf(), WebLogs.class);
    conf.setJobName("wordcount");
    conf.set("debug", "true");
    conf.setWorkingDirectory(new Path("./"));
    FileInputFormat.setInputPaths(conf, new Path(args[0]));
    FileOutputFormat.setOutputPath(conf, new Path(args[1]));

    //  these are set so the job is run in the same
    //  JVM as the debugger - we are not submitting 
    //  to MR Node.
    conf.set("mapred.job.tracker", "local");
    conf.set("fs.default.name", "local");
    
    //  The mapper, reducer and combiner classes.
    File jar = new File("./dist/pdi-hadoop-plugin-ee-TRUNK-SNAPSHOT.jar");
    URLClassLoader loader = new URLClassLoader(new URL[] { jar.toURI().toURL() });
    conf.setMapperClass((Class<? extends Mapper>) loader.loadClass("org.pentaho.hadoop.mapreduce.GenericTransMap"));
    //conf.setCombinerClass((Class<? extends Reducer>) loader.loadClass("org.pentaho.hadoop.mapreduce.GenericTransReduce"));
    conf.setReducerClass((Class<? extends Reducer>) loader.loadClass("org.pentaho.hadoop.mapreduce.GenericTransReduce"));

    TransExecutionConfiguration transExecConfig = new TransExecutionConfiguration();
    
    TransMeta mapperTransMeta = new TransMeta("./samples/jobs/hadoop/weblogs-mapper.ktr");
    TransConfiguration mapperTransConfig = new TransConfiguration(mapperTransMeta, transExecConfig);
    conf.set("transformation-map-xml", mapperTransConfig.getXML());
  
    TransMeta reducerTransMeta = new TransMeta("./samples/jobs/hadoop/weblogs-reducer.ktr");
    TransConfiguration reducerTransConfig = new TransConfiguration(reducerTransMeta, transExecConfig);
    conf.set("transformation-reduce-xml", reducerTransConfig.getXML());
    
    // transformation data interface
    conf.set("transformation-map-input-stepname", "Injector");
    conf.set("transformation-map-output-stepname", "Output");
    conf.set("transformation-reduce-input-stepname", "Injector");
    conf.set("transformation-reduce-output-stepname", "Output");
    conf.setOutputKeyClass(Text.class);
    conf.setOutputValueClass(Text.class);
    

    FileInputFormat.setInputPaths(conf, new Path(args[0]));
    FileOutputFormat.setOutputPath(conf, new Path(args[1]));

    List<String> other_args = new ArrayList<String>();
    for(int i=0; i < args.length; ++i) {
      try {
        if ("-m".equals(args[i])) {
          conf.setNumMapTasks(Integer.parseInt(args[++i]));
        } else if ("-r".equals(args[i])) {
          conf.setNumReduceTasks(Integer.parseInt(args[++i]));
        } else {
          other_args.add(args[i]);
        }
      } catch (NumberFormatException except) {
        System.out.println("ERROR: Integer expected instead of " + args[i]);
        return printUsage();
      } catch (ArrayIndexOutOfBoundsException except) {
        System.out.println("ERROR: Required parameter missing from " +
                           args[i-1]);
        return printUsage();
      }
    }
    // Make sure there are exactly 2 parameters left.
    if (other_args.size() != 2) {
      System.out.println("ERROR: Wrong number of parameters: " +
                         other_args.size() + " instead of 2.");
      return printUsage();
    }
    FileInputFormat.setInputPaths(conf, other_args.get(0));
    FileOutputFormat.setOutputPath(conf, new Path(other_args.get(1)));

    JobClient.runJob(conf);
    return 0;
  }

  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new Configuration(), new WebLogs(), args);
    System.exit(res);
  }
}


