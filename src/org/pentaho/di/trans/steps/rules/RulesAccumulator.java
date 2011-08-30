/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.di.trans.steps.rules;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.rules.Rules.Row;

public class RulesAccumulator extends BaseStep implements StepInterface {
  // private static Class<?> PKG = Rules.class; // for i18n purposes

  private RulesAccumulatorMeta meta;

  private RulesAccumulatorData data;

  public RulesAccumulator(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans) {
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }

  public boolean init(StepMetaInterface smi, StepDataInterface sdi) {

    meta = (RulesAccumulatorMeta) smi;
    data = (RulesAccumulatorData) sdi;

    if (super.init(smi, sdi)) {
      return true;
    }
    return false;
  }

  public boolean runtimeInit() throws KettleStepException {
    try {
      data.setOutputRowMeta(getInputRowMeta().clone());
      meta.setKeepInputFields(false);
      meta.getFields(data.getOutputRowMeta(), getStepname(), null, null, this);

      data.setRuleFilePath(meta.getRuleFile());
      data.setRuleString(meta.getRuleDefinition());

      data.initializeRules();
      data.initializeInput(getInputRowMeta());

      return true;
    } catch (Exception e) {
      throw new KettleStepException(e);
    }
  }

  public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
    meta = (RulesAccumulatorMeta) smi;
    data = (RulesAccumulatorData) sdi;

    super.dispose(smi, sdi);
  }

  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
    try {
      meta = (RulesAccumulatorMeta) smi;
      data = (RulesAccumulatorData) sdi;

      Object[] r = getRow(); // get row, set busy!

      if (r == null) // no more input to be expected...
      {
        data.execute();

        Object[] outputRow;

        String[] expectedResults = meta.getExpectedResultList();

        for (Row resultRow : data.getResultRows()) {
          outputRow = new Object[expectedResults.length];
          for (String columnName : expectedResults) {
            outputRow[data.getOutputRowMeta().indexOfValue(columnName)] = resultRow.getColumn().get(columnName);
          }
          putRow(data.getOutputRowMeta(), outputRow);
        }

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

      // Store the row for processing
      data.loadRow(r);

      return true;
    } catch (Exception e) {
      throw new KettleException(e);
    }
  }
}
