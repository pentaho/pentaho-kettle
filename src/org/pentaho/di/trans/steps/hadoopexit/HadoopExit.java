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

package org.pentaho.di.trans.steps.hadoopexit;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class HadoopExit extends BaseStep implements StepInterface {
  private HadoopExitMeta meta;
  private HadoopExitData data;
  
  public HadoopExit(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }
  
  public boolean runtimeInit() throws KettleException {
    data.init(getInputRowMeta(), meta, this);
    return true;
  }

  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
    meta = (HadoopExitMeta) smi;
    data = (HadoopExitData) sdi;
    
    Object[] r = getRow();
    if (r == null) // no more input to be expected...
    {
      setOutputDone();
      return false;
    }

    if(first) {
      if(!runtimeInit()) {
        return false;
      }
      first = false;
    }

    Object[] outputRow = new Object[2];
    outputRow[HadoopExitData.getOutKeyOrdinal()] = r[data.getInKeyOrdinal()];
    outputRow[HadoopExitData.getOutValueOrdinal()] = r[data.getInValueOrdinal()];
    
    putRow(data.getOutputRowMeta(), outputRow);
    
    return true;
  }

}
