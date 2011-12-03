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
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class HadoopExit extends BaseStep implements StepInterface {
  private static Class<?> PKG = HadoopExit.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private HadoopExitMeta meta;
  private HadoopExitData data;
  
  public HadoopExit(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }
  
  public boolean runtimeInit() throws KettleException {
    data.init(getInputRowMeta().clone(), meta);
    return true;
  }

  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
    meta = (HadoopExitMeta) smi;
    data = (HadoopExitData) sdi;
    
    Object[] r = getRow();
    if(first) {
        if(!runtimeInit()) {
        return false;
      }
      first = false;
    }
    
    if (r == null) // no more input to be expected...
    {
      setOutputDone();
      return false;
    }

    Object[] outputRow = new Object[2];
    outputRow[HadoopExitData.getOutKeyOrdinal()] = r[data.getInKeyOrdinal()];
    outputRow[HadoopExitData.getOutValueOrdinal()] = r[data.getInValueOrdinal()];
    
    putRow(data.getOutputRowMeta(), outputRow);
    
    return true;
  }

}
