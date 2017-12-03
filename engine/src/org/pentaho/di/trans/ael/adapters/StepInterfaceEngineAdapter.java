/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.model.Row;
import org.pentaho.di.engine.api.model.Rows;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Collections;

import static org.pentaho.di.engine.api.model.Rows.TYPE.OUT;

/**
 * Adapts AEL Operation events to the StepInterface.
 * This class will subscribe to engine events and translate them
 * to corresponding StepInterface updates.
 */
public class StepInterfaceEngineAdapter extends BaseStep {

  private final Operation operation;
  private final ExecutionContext executionContext;

  public StepInterfaceEngineAdapter( Operation op, ExecutionContext executionContext, StepMeta stepMeta,
                                     TransMeta transMeta, StepDataInterface dataInterface, Trans trans ) {
    super( stepMeta, dataInterface, 0, transMeta, trans );
    operation = op;
    this.executionContext = executionContext;
    setInputRowSets( Collections.emptyList() );
    setOutputRowSets( Collections.emptyList() );
    init();
  }

  @Override public void dispatch() {
    // No thanks. I'll take it from here.
  }

  private void init() {
    subscribeToMetrics();
    subscribeToStatus();
    subscribeToRows();
  }

  private void subscribeToRows() {
    executionContext.subscribe( operation, Rows.class, data -> {
      if ( data.getType().equals( OUT ) ) {
        data.stream().forEach( this::putRow );
      }
    } );
  }

  private void subscribeToStatus() {
    executionContext.subscribe( operation, Status.class, data -> {
      switch ( data ) {
        case RUNNING:
          StepInterfaceEngineAdapter.this.setRunning( true );
          break;
        case PAUSED:
          StepInterfaceEngineAdapter.this.setPaused( true );
          break;
        case STOPPED:
          StepInterfaceEngineAdapter.this.setStopped( true );
          break;
        case FAILED:
        case FINISHED:
          StepInterfaceEngineAdapter.this.setRunning( false );
          break;
      }

    } );
  }

  private void subscribeToMetrics() {
    executionContext.subscribe( operation, Metrics.class, data -> {
      StepInterfaceEngineAdapter.this.setLinesRead( data.getIn() );
      StepInterfaceEngineAdapter.this.setLinesWritten( data.getOut() );
    } );
  }

  /**
   * Writes a Row to all rowListeners
   **/
  private void putRow( Row row ) {
    try {
      if ( executionContext.getConversionManager() == null ) {
        // no way to convert this row to a RowMetaInterface.
        return;
      }
      for ( RowListener listener : rowListeners ) {
        listener.rowWrittenEvent( executionContext.getConversionManager().convert( row, RowMetaInterface.class ),
                row.getObjects() );
      }
    } catch ( KettleStepException e ) {
      // log that we were unable to convert row.
      // will probably want to throw when row conversion is more robust.  Right
      // now this only means preview will not be populated for the current Engine impl.
      logDebug( e.getMessage() );
    }
  }
}
