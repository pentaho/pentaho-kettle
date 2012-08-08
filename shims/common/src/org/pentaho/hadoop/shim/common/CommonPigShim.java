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

package org.pentaho.hadoop.shim.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.backend.hadoop.datastorage.ConfigurationUtil;
import org.apache.pig.impl.util.PropertiesUtil;
import org.apache.pig.tools.grunt.GruntParser;
import org.apache.pig.tools.parameters.ParameterSubstitutionPreprocessor;
import org.apache.pig.tools.parameters.ParseException;
import org.pentaho.hadoop.shim.ShimVersion;
import org.pentaho.hadoop.shim.api.Configuration;
import org.pentaho.hadoop.shim.spi.PigShim;

public class CommonPigShim implements PigShim {
  private static final String[] EMPTY_STRING_ARRAY = new String[0];

  @Override
  public ShimVersion getVersion() {
    return new ShimVersion(1, 0);
  }

  @Override
  public boolean isLocalExecutionSupported() {
    return true;
  }

  @Override
  public void configure(Properties properties, Configuration configuration) {
    PropertiesUtil.loadDefaultProperties(properties);
    if (configuration != null) {
      properties.putAll(ConfigurationUtil.toProperties(ShimUtils.asConfiguration(configuration)));
    }
  }

  @Override
  public String substituteParameters(URL pigScript, List<String> paramList) throws IOException, ParseException {
    final InputStream inStream = pigScript.openStream();
    StringWriter writer = new StringWriter();
    // do parameter substitution                                                                                                                  
    ParameterSubstitutionPreprocessor psp = new ParameterSubstitutionPreprocessor(50);
    psp.genSubstitutedFile(new BufferedReader(new InputStreamReader(inStream)),
                           writer,
                           paramList.size() > 0 ? paramList.toArray(EMPTY_STRING_ARRAY) : null, null);
    return writer.toString();
  }
  
  /**
   * Convert {@link ExecutionMode} to {@link ExecType}
   * 
   * @param mode Execution mode
   * @return Type of execution for mode
   */
  protected ExecType getExecType(ExecutionMode mode) {
    switch(mode) {
      case LOCAL:
        return ExecType.LOCAL;
      case MAPREDUCE:
        return ExecType.MAPREDUCE;
      default:
        throw new IllegalStateException("unknown execution mode: " + mode);
    }
  }
  
  @Override
  public int[] executeScript(String pigScript, ExecutionMode mode, Properties properties) throws IOException, org.apache.pig.tools.pigscript.parser.ParseException {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    try {
      PigServer pigServer = new PigServer(getExecType(mode), properties);
      GruntParser grunt = new GruntParser(new StringReader(pigScript));
      grunt.setInteractive(false);
      grunt.setParams(pigServer);
      return grunt.parseStopOnError(false);
    } finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }
}
