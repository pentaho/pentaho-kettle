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

package org.pentaho.hadoop.shim.api;

import org.pentaho.hadoop.shim.api.fs.Path;

/**
 * A thin abstraction for {@link org.apache.hadoop.mapred.JobConf} (and
 * consequently {@link org.apache.hadoop.conf.Configuration}). Most of the methods
 * here are a direct wrapping for methods found in {@link org.apache.hadoop.mapred.JobConf}
 * and the documentation there should be considered when providing an implementation.
 * 
 * @author Jordan Ganoff (jganoff@pentaho.com)
 */
public interface Configuration {

  /**
   * Property for indicating a Pentaho MapReduce combiner should execute in
   * single threaded mode.
   */
  public static final String STRING_COMBINE_SINGLE_THREADED = "transformation-combine-single-threaded";

  /**
   * Property for indicating a Pentaho MapReduce reduce should execute in
   * single threaded mode.
   */
  public static final String STRING_REDUCE_SINGLE_THREADED = "transformation-reduce-single-threaded";

  /**
   * Sets the MapReduce job name.
   * 
   * @param jobName Name of job
   */
  void setJobName(String jobName);

  /**
   * Sets the property {@code name}'s value to {@code value}.
   * 
   * @param name Name of property
   * @param value Value of property
   */
  void set(String name, String value);

  /**
   * Look up the value of a property.
   * 
   * @param name Name of property
   * @return Value of the property named {@code name}
   */
  String get(String name);

  /**
   * Look up the value of a property optionally returning a default value if 
   * the property is not set.
   * 
   * @param name Name of property
   * @param defaultValue Value to return if the property is not set
   * @return Value of property named {@code name} or {@code defaultValue} if {@code name} is not set
   */
  String get(String name, String defaultValue);

  /**
   * Set the key class for the map output data.
   * 
   * @param c the map output key class
   */
  void setMapOutputKeyClass(Class<?> c);

  /**
   * Set the value class for the map output data.
   * 
   * @param c the map output value class
   */
  void setMapOutputValueClass(Class<?> c);

  void setMapperClass(Class<?> c);

  void setCombinerClass(Class<?> c);

  void setReducerClass(Class<?> c);

  void setOutputKeyClass(Class<?> c);

  void setOutputValueClass(Class<?> c);

  void setMapRunnerClass(Class<?> c);

  void setInputFormat(Class<?> inputFormat);

  void setOutputFormat(Class<?> outputFormat);

  void setInputPaths(Path... paths);

  void setOutputPath(Path path);

  void setJarByClass(Class<?> c);

  void setJar(String url);

  /**
   * Provide a hint to Hadoop for the number of map tasks to start for the
   * MapReduce job submitted with this configuration.
   * 
   * @param n the number of map tasks for this job
   */
  void setNumMapTasks(int n);

  /**
   * Sets the requisite number of reduce tasks for the MapReduce job submitted
   * with this configuration.
   * 
   * <p>If {@code n} is {@code zero} there will not be a
   * reduce (or sort/shuffle) phase and the output of the map tasks will be 
   * written directly to the distributed file system under the path specified
   * via {@link #setOutputPath(Path)</p>
   * 
   * @param n the number of reduce tasks required for this job
   */
  void setNumReduceTasks(int n);

  /** 
   * Set the array of string values for the <code>name</code> property as 
   * as comma delimited values.  
   * 
   * @param name property name.
   * @param values The values
   */
  void setStrings(String name, String... values);

  /**
   * Get the default file system URL as stored in this configuration.
   * 
   * @return the default URL if it was set, otherwise empty string
   */
  String getDefaultFileSystemURL();
}
