/*******************************************************************************
 *
 * Pentaho Data Integration
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

package org.pentaho.di.trans.steps.rules;

import java.util.Arrays;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.rules.Rules.Column;

/**
 * This Transformation Step allows a user to execute a rule set against
 * an individual rule or a collection of rules.
 * 
 * Additional columns can be added to the output from the rules and these
 * (of course) can be used for routing if desired.
 * 
 * @author cboyden
 *
 */

public class RulesExecutor extends BaseStep implements StepInterface {
  // private static Class<?> PKG = Rules.class; // for i18n purposes

  private RulesExecutorMeta meta;

  private RulesExecutorData data;

  public RulesExecutor(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans) {
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }

  public boolean init(StepMetaInterface smi, StepDataInterface sdi) {

    meta = (RulesExecutorMeta) smi;
    data = (RulesExecutorData) sdi;

    if (super.init(smi, sdi)) {
      return true;
    }
    return false;
  }

  public boolean runtimeInit() throws KettleStepException {
    data.setOutputRowMeta(getInputRowMeta().clone());
    meta.getFields(data.getOutputRowMeta(), getStepname(), null, null, this);

    data.setRuleFilePath(meta.getRuleFile());
    data.setRuleString(meta.getRuleDefinition());

    data.initializeRules();
    data.initializeColumns(getInputRowMeta());

    return true;
  }

  public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
    meta = (RulesExecutorMeta) smi;
    data = (RulesExecutorData) sdi;

    super.dispose(smi, sdi);
  }

  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
    meta = (RulesExecutorMeta) smi;
    data = (RulesExecutorData) sdi;

    Object[] r = getRow(); // get row, set busy!
    if (r == null) // no more input to be expected...
    {
      data.shutdown();
      setOutputDone();
      return false;
    }

    if (first) {
      if (!runtimeInit()) {
        return false;
      }

      first = false;
    }

    // Load the column objects
    data.loadRow(r);

    data.execute();

    Object[] outputRow;
    int beginOutputRowFill = 0;

    String[] expectedResults = meta.getExpectedResultList();

    if (meta.isKeepInputFields()) {
      int inputRowSize = getInputRowMeta().size();
      outputRow = Arrays.copyOf(r, inputRowSize + expectedResults.length);
      beginOutputRowFill = inputRowSize;
    } else {
      outputRow = new Object[expectedResults.length];
    }

    Column result = null;
    for (int i = 0; i < expectedResults.length; i++) {
      result = (Column) data.fetchResult(expectedResults[i]);
      outputRow[i + beginOutputRowFill] = result == null ? null : result.getPayload();
    }

    putRow(data.getOutputRowMeta(), outputRow);

    return true;
  }
}
