/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.ivwloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * User: Dzmitry Stsiapanau Date: 11/20/13 Time: 2:00 PM
 */
public class VWLoadMocker {

  public static void main( String[] args ) {

    System.out.println( "Start VWLOADMOCKER" );
    int bufferSize = Integer.decode( args[0] == null ? "5000" : args[0] );
    int errorsAllowed = Integer.decode( args[1] == null ? "0" : args[1] );
    String errorFileName = args[2] == null ? "/tmp/error_test.txt" : args[2];

    VWLoadMocker vwload = new VWLoadMocker( bufferSize );
    vwload.setErrorsAllowed( errorsAllowed );
    vwload.setErrorFileName( errorFileName );

    int exitStatus = 0;

    Scanner sc = new Scanner( System.in );
    try {
      FileWriter out = new FileWriter( "/tmp/test.txt" );
      StringBuilder cmd = new StringBuilder();
      String line = null;
      while ( ( line = sc.nextLine() ) != null && !line.contains( "\\q" ) ) {
        cmd.append( line );
        out.write( line + '\n' + '\r' );
        out.flush();
        if ( line.contains( "\\g" ) ) {
          exitStatus += vwload.execute( cmd.toString() );
          cmd.setLength( 0 );
        }
      }
      out.flush();
      out.close();
    } catch ( IOException e ) {
      e.printStackTrace();
    }

    System.exit( exitStatus );
  }

  private int bufferSize;

  private int errorsAllowed;
  private String delimeter = "\\|";
  private String lineSeparator = System.getProperty( "line.separator" );

  private String errorFileName;
  private int processed;
  private int loaded;
  private int errors;

  public VWLoadMocker( int bufferSize ) {
    this.bufferSize = bufferSize;
  }

  public void setErrorsAllowed( int errorsAllowed ) {
    this.errorsAllowed = errorsAllowed;
  }

  public void setErrorFileName( String errorFileName ) {
    this.errorFileName = errorFileName;
  }

  private int execute( String cmd ) {
    BufferedReader rafR = null;
    try {
      System.out.println( "Start executing command " + cmd );
      int start = cmd.indexOf( "FROM " );
      String fifo;
      if ( start > 0 ) {
        start = cmd.indexOf( '\'', start );
        int end = cmd.indexOf( '\'', start + 1 );
        fifo = cmd.substring( ++start, end );
      } else {
        return 1;
      }
      processed = 0;
      loaded = 0;
      errors = 0;
      boolean notStopOnError = ( errorsAllowed == 0 );

      // RandomAccessFile raf = new RandomAccessFile( fifo, "rws" );
      File raf = new File( fifo );
      rafR = new BufferedReader( new FileReader( raf ), bufferSize );

      System.out.println( "Start reading from file " + fifo );
      if ( !raf.exists() ) {
        System.out.println( "Break fifo not exist" );
        return status();
      }
      while ( true ) {
        String data = rafR.readLine();
        if ( data == null ) {
          System.out.println( "Break fifo end" );
          break;
        }
        System.out.println( "Readed from fifo " + data );
        processed++;

        String[] columns = data.trim().split( delimeter );
        Long col = Long.valueOf( columns[0] );
        if ( col >= 10 ) {
          errors++;
          writeError( data );
        } else {
          loaded++;
          writeSuccess( data );
        }

        if ( !notStopOnError && errors > errorsAllowed ) {
          System.out.println( "Break error count" );
          break;
        }
      }
      return status();
    } catch ( FileNotFoundException e ) {
      System.err.println( e );
    } catch ( IOException e ) {
      System.err.println( e );
    } finally {
      try {
        rafR.close();
      } catch ( IOException e ) {
        System.err.println( e );
      }
    }
    return 1;
  }

  private int status() {
    int result = 0;
    if ( errors > errorsAllowed ) {
      loaded = 0;
      result = 1;
    }
    System.out.println( "processed " + processed + " records, loaded " + loaded + " records, " + errors + " errors" );
    return result;
  }

  private void writeSuccess( String data ) {
    System.out.println( "Successfully loaded ..... " + data );
  }

  private void writeError( String data ) throws IOException {
    System.err.println( "Error in loading ..... " + data );
    if ( errorFileName != null ) {
      new FileWriter( errorFileName, true ).write( data + lineSeparator );
    }
  }
}
