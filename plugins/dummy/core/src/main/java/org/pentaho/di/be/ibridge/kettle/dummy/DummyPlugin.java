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

package org.pentaho.di.be.ibridge.kettle.dummy;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;




/*
 * Created on 2-jun-2003
 *
 */

public class DummyPlugin extends BaseStep implements StepInterface {
  private DummyPluginData data;
  private DummyPluginMeta meta;

  public DummyPlugin( StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis ) {
    super( s, stepDataInterface, c, t, dis );
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (DummyPluginMeta) smi;
    data = (DummyPluginData) sdi;

    Object[] r = getRow();    // get row, blocks when needed!
    if ( r == null ) { // no more input to be expected...
      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this );
    }

    Object extraValue = meta.getValue().getValueData();

    Object[] outputRow = RowDataUtil.addValueData( r, data.outputRowMeta.size() - 1, extraValue );

    putRow( data.outputRowMeta, outputRow );     // copy row to possible alternate rowset(s).

    if ( checkFeedback( getLinesRead() ) ) {
      logBasic( "Linenr " + getLinesRead() );  // Some basic logging every 5000 rows.
    }

    return true;
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (DummyPluginMeta) smi;
    data = (DummyPluginData) sdi;

    return super.init( smi, sdi );
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (DummyPluginMeta) smi;
    data = (DummyPluginData) sdi;

    super.dispose( smi, sdi );
  }

  //
  // Run is were the action happens!
  public void run() {
    logBasic( "Starting to run..." );
    try {
      while ( processRow( meta, data ) && !isStopped() ) {
        // Process rows
      }
    } catch ( Exception e ) {
      logError( "Unexpected error : " + e.toString() );
      logError( Const.getStackTracker( e ) );
      setErrors( 1 );
      stopAll();
    } finally {
      dispose( meta, data );
      logBasic( "Finished, processing " + getLinesRead() + " rows" );
      markStop();
    }
  }
}
