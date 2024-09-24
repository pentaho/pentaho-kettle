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

package org.pentaho.di.trans.steps.script;

import java.util.List;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Interface to make the real ScriptValueMod and ScriptValueModDummy similar.
 *
 * @author Sven Boden
 */
public interface ScriptInterface extends StepInterface {
  boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException;

  void addRowListener( RowListener rowListener );

  void dispose( StepMetaInterface sii, StepDataInterface sdi );

  long getErrors();

  List<RowSet> getInputRowSets();

  long getLinesInput();

  long getLinesOutput();

  long getLinesRead();

  long getLinesUpdated();

  long getLinesWritten();

  long getLinesRejected();

  List<RowSet> getOutputRowSets();

  String getPartitionID();

  Object[] getRow() throws KettleException;

  List<RowListener> getRowListeners();

  String getStepID();

  String getStepname();

  boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface );

  boolean isAlive();

  boolean isPartitioned();

  boolean isStopped();

  void markStart();

  void markStop();

  void putRow( RowMetaInterface rowMeta, Object[] row ) throws KettleException;

  void removeRowListener( RowListener rowListener );

  void run();

  void setErrors( long errors );

  void setOutputDone();

  void setPartitionID( String partitionID );

  void start();

  void stopAll();

  void stopRunning( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) throws KettleException;

  void cleanup();

  void pauseRunning();

  void resumeRunning();
}
