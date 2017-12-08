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

package org.pentaho.di.trans.streaming.common;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepStatus;
import org.pentaho.di.trans.streaming.api.StreamSource;
import org.pentaho.di.trans.streaming.api.StreamWindow;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BaseStreamStep<I> extends BaseStep {

  protected StreamWindow<I, Result> window;
  protected StreamSource<I> source;

  public BaseStreamStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
                         TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }


  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    Preconditions.checkArgument( first,
      "Streaming steps should only have processRow called once." );

    bufferStream().forEach( result -> {
        if ( result.getNrErrors() > 0 ) {
          stopAll();
        } else {
          putRows( result.getRows() );
        }
      }
    );
    return false;
  }

  protected Iterable<Result> bufferStream() {
    return window.buffer( source.rows() );
  }

  @Override
  public void stopRunning( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface )
    throws KettleException {
    source.close();
    super.stopRunning( stepMetaInterface, stepDataInterface );
  }

  @Override public void resumeRunning() {
    source.resume();
    super.resumeRunning();
  }

  @Override public Collection<StepStatus> subStatuses() {
    return Collections.emptyList();
  }

  @Override public void pauseRunning() {
    source.pause();
    super.pauseRunning();
  }

  private void putRows( List<RowMetaAndData> rows ) {
    rows.forEach( row -> {
      try {
        putRow( row.getRowMeta(), row.getData() );
      } catch ( KettleStepException e ) {
        Throwables.propagate( e );
      }
    } );
  }


}
