/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.trans.steps.hadoopexit;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class HadoopExitData extends BaseStepData implements StepDataInterface {
  private RowMetaInterface outputRowMeta = null;

  private int inKeyOrdinal = -1;
  private int inValueOrdinal = -1;
  
  private final static int outKeyOrdinal = 0;
  private final static int outValueOrdinal = 1;
  
  public HadoopExitData() {
    super();
  }

  public void init(RowMetaInterface rowMeta, HadoopExitMeta stepMeta) throws KettleException {
    if (rowMeta != null) {
      outputRowMeta = new RowMeta();
      
      setInKeyOrdinal(rowMeta.indexOfValue(stepMeta.getOutKeyFieldname()));
      setInValueOrdinal(rowMeta.indexOfValue(stepMeta.getOutValueFieldname()));

      ValueMetaInterface keyMeta = rowMeta.searchValueMeta(stepMeta.getOutKeyFieldname()).clone();
      ValueMetaInterface valueMeta = rowMeta.searchValueMeta(stepMeta.getOutValueFieldname()).clone();
      
      keyMeta.setName("outKey");
      valueMeta.setName("outValue");
      
      outputRowMeta.addValueMeta(getOutKeyOrdinal(), keyMeta);
      outputRowMeta.addValueMeta(getOutValueOrdinal(), valueMeta);
    }
  }

  public RowMetaInterface getOutputRowMeta() {
    return outputRowMeta;
  }

  public void setInKeyOrdinal(int inKeyOrdinal) {
    this.inKeyOrdinal = inKeyOrdinal;
  }

  public int getInKeyOrdinal() {
    return inKeyOrdinal;
  }

  public void setInValueOrdinal(int inValueOrdinal) {
    this.inValueOrdinal = inValueOrdinal;
  }

  public int getInValueOrdinal() {
    return inValueOrdinal;
  }

  public static int getOutKeyOrdinal() {
    return outKeyOrdinal;
  }

  public static int getOutValueOrdinal() {
    return outValueOrdinal;
  }
}
