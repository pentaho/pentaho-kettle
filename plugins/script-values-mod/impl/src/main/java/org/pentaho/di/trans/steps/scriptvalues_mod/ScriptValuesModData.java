/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.scriptvalues_mod;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class ScriptValuesModData extends BaseStepData implements StepDataInterface {
  public Context cx;
  public Scriptable scope;
  public Script script;

  public int[] fields_used;
  public Value[] values_used;

  public RowMetaInterface outputRowMeta;
  public int[] replaceIndex;

  public ScriptValuesModData() {
    super();
    cx = null;
    fields_used = null;
  }

  public void check( int i ) {
    System.out.println( i );
  }
}
