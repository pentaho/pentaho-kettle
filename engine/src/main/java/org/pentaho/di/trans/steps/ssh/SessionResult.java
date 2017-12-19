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

package org.pentaho.di.trans.steps.ssh;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.Utils;

import com.trilead.ssh2.Session;

public class SessionResult {

  private String stdout;
  private String stderr;
  private boolean stderrortype;

  public SessionResult( Session session ) throws KettleException {
    readStd( session );
  }

  private void setStdErr( String value ) {
    this.stderr = value;
    if ( !Utils.isEmpty( getStdErr() ) ) {
      setStdTypeErr( true );
    }
  }

  public String getStdErr() {
    return this.stderr;
  }

  public String getStd() {
    return getStdOut() + getStdErr();
  }

  private void setStdOut( String value ) {
    this.stdout = value;
  }

  public String getStdOut() {
    return this.stdout;
  }

  private void setStdTypeErr( boolean value ) {
    this.stderrortype = value;
  }

  public boolean isStdTypeErr() {
    return this.stderrortype;
  }

  private void readStd( Session session ) throws KettleException {
    InputStream isOut = null;
    InputStream isErr = null;
    try {
      isOut = session.getStdout();
      isErr = session.getStderr();

      setStdOut( readInputStream( isOut ) );
      setStdErr( readInputStream( isErr ) );

    } catch ( Exception e ) {
      throw new KettleException( e );
    } finally {
      try {
        if ( isOut != null ) {
          isOut.close();
        }
        if ( isErr != null ) {
          isErr.close();
        }
      } catch ( Exception e ) { /* Ignore */
      }
    }

  }

  private String readInputStream( InputStream std ) throws KettleException {
    BufferedReader br = null;
    try {
      br = new BufferedReader( new InputStreamReader( std ) );

      String line = "";
      StringBuilder stringStdout = new StringBuilder();

      if ( ( line = br.readLine() ) != null ) {
        stringStdout.append( line );
      }
      while ( ( line = br.readLine() ) != null ) {
        stringStdout.append( "\n" + line );
      }

      return stringStdout.toString();
    } catch ( Exception e ) {
      throw new KettleException( e );
    } finally {
      try {
        if ( br != null ) {
          br.close();
        }
      } catch ( Exception e ) { /* Ignore */
      }
    }
  }
}
