/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.trans.steps.palo.diminput;

/*
 *   This file is part of PaloKettlePlugin.
 *
 *   PaloKettlePlugin is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PaloKettlePlugin is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with PaloKettlePlugin.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   Copyright 2008 Stratebi Business Solutions, S.L.
 */

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.palo.core.PaloHelper;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 *
 */
public class PaloDimInput extends BaseStep implements StepInterface {
  private PaloDimInputMeta meta;
  private PaloDimInputData data;

  public PaloDimInput(final StepMeta stepMeta, final StepDataInterface stepDataInterface, final int copyNr, final TransMeta transMeta, final Trans trans) {
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }

  public boolean processRow(final StepMetaInterface smi, final StepDataInterface sdi) throws KettleException {
    this.logBasic("Getting Dimension Row Meta.");

    final RowMetaInterface rowMeta = data.helper.getDimensionRowMeta(meta.getDimension(), meta.getLevels(), meta.getBaseElementsOnly());

    this.logBasic("Getting Dimension Rows.");
    final List<Object[]> rows = data.helper.getDimensionRows(meta.getDimension(), rowMeta, meta.getBaseElementsOnly(), new PaloHelper.Listener() {
      public void resume() {

      }

      public void stop() {

      }

      public void cancel() {

      }

      public boolean getStop() {
        return false;
      }

      public void prepareElements(int numElements) {

      }

      public boolean getCancel() {
        return false;
      }

      public void oneMoreElement(Object element) {

      }
    }

    );

    for (Object[] row : rows) {
      putRow(rowMeta, row);
      incrementLinesInput();
    }
    this.logBasic("Process Ended.");
    setOutputDone();
    return false;
  }

  public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
    meta = (PaloDimInputMeta) smi;
    data = (PaloDimInputData) sdi;

    if (super.init(smi, sdi)) {
      try {
        this.logDebug("Meta Levels: " + meta.getLevels().size());
        data.helper = new PaloHelper(meta.getDatabaseMeta());
        data.helper.connect();
        return true;
      } catch (Exception e) {
        logError("An error occurred, processing will be stopped: " + e.getMessage());
        setErrors(1);
        stopAll();
      }
    }

    return false;
  }

  public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
    data.helper.disconnect();
    super.dispose(smi, sdi);
  }
}
