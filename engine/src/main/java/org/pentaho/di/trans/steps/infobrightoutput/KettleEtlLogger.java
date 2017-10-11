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

package org.pentaho.di.trans.steps.infobrightoutput;

import org.pentaho.di.trans.step.BaseStep;

import com.infobright.logging.EtlLogger;

/**
 * Adapter between Infobright EtlLogger and a Kettle BaseStep.
 *
 * @author geoffrey.falk@infobright.com
 */
public class KettleEtlLogger implements EtlLogger {

  private BaseStep step;

  public KettleEtlLogger( BaseStep step ) {
    this.step = step;
  }

  // @Override
  public void debug( String s ) {
    step.logDebug( s );
  }

  // @Override
  public void error( String s, Throwable cause ) {
    step.logError( s + ": " + cause.getMessage() );
  }

  // @Override
  public void error( String s ) {
    step.logError( s );
  }

  // @Override
  public void info( String s ) {
    step.logBasic( s );
  }

  // @Override
  public void trace( String s ) {
    step.logRowlevel( s );
  }

  // @Override
  public void warn( String s ) {
    step.logMinimal( s );
  }

  // @Override
  public void fatal( String s ) {
    step.logError( s ); // Kettle BaseStep does not have FATAL level
  }

}
