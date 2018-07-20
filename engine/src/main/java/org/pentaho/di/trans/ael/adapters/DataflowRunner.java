/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
package org.pentaho.di.trans.ael.adapters;

import com.google.common.base.Stopwatch;
import org.json.simple.JSONObject;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by ccaspanello on 6/18/18.
 */
public class DataflowRunner {

  private static final Logger LOG = LoggerFactory.getLogger( DataflowRunner.class );

  private LogChannelInterface logger;
  private String runner;
  private File applicationJar;

  public DataflowRunner( LogChannelInterface logger, String runner, File applicationJar ) {
    this.logger = logger;
    this.runner = runner;
    this.applicationJar = applicationJar;
  }

  public void run( String ktrFile, Map<String, String> env, Map<String, String> params ) {
    try {
      Stopwatch sw = Stopwatch.createStarted();
      checkApplicationExists();

      List<String> commands = buildCommands( ktrFile, env, params );

      ProcessBuilder processBuilder = new ProcessBuilder( commands );
      processBuilder.directory( applicationJar.getParentFile() );

      Process proc = processBuilder.start();
      ConsoleRedirector errorGobbler = new ConsoleRedirector( proc.getErrorStream(), Optional.ofNullable( logger ) );
      ConsoleRedirector outputGobbler = new ConsoleRedirector( proc.getInputStream(), Optional.ofNullable( logger ) );
      errorGobbler.start();
      outputGobbler.start();
      proc.waitFor();
      sw.stop();

      logger.logBasic( "Elapsed Time: " +  sw.toString() );

    } catch ( Exception e ) {
      // TODO Investigate why throwing an error does no good
      logger.logError( "Unexpected error running transformation.", e );
    }
  }

  private List<String> buildCommands( String ktrFile, Map<String, String> env, Map<String, String> params ) {
    // Create Command
    List<String> commands = new ArrayList<>();
    commands.add( "java" );
    commands.add( "-jar" );
    commands.add( applicationJar.getName() );
    commands.add( "--transformation=" + ktrFile );
    commands.add( "--runner=" + runner );

    // Add Environment Variables
    if ( !env.isEmpty() ) {
      JSONObject jsonObject = new JSONObject();
      env.keySet().stream().forEach( key -> jsonObject.put( key, env.get( key ) ) );
      commands.add( "--environment=" + jsonObject.toJSONString() );
    }

    // Add Parameters
    if ( !params.isEmpty() ) {
      JSONObject jsonObject = new JSONObject();
      params.keySet().stream().forEach( key -> jsonObject.put( key, params.get( key ) ) );
      commands.add( "--parameters=" + jsonObject.toJSONString() );
    }
    return commands;
  }

  private void checkApplicationExists() {
    if ( !applicationJar.exists() ) {
      throw new RuntimeException( "Application cannot be found." );
    }
  }

  private static class ConsoleRedirector extends Thread {
    private InputStream is;
    private Optional<LogChannelInterface> logger;

    public ConsoleRedirector( InputStream inputStream, Optional<LogChannelInterface> logger ) {
      this.is = inputStream;
      this.logger = logger;
    }

    public void run() {
      try {
        InputStreamReader isr = new InputStreamReader( is );
        BufferedReader br = new BufferedReader( isr );
        String line = null;
        while ( ( line = br.readLine() ) != null ) {
          if ( logger.isPresent() ) {
            logger.get().logBasic( line );
          } else {
            System.out.println( line );
          }
        }
      } catch ( IOException ioe ) {
        ioe.printStackTrace();
      }
    }
  }
}
