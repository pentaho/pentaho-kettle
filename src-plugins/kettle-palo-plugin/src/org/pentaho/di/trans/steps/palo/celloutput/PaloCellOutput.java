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
package org.pentaho.di.trans.steps.palo.celloutput;

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
 *   Copyright 2011 De Bortoli Wines Pty Limited (Australia)
 */

import org.pentaho.di.core.exception.KettleException;
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
public class PaloCellOutput extends BaseStep implements StepInterface {

  private PaloCellOutputMeta meta;
  private PaloCellOutputData data;
  
  public PaloCellOutput(final StepMeta stepMeta, final StepDataInterface stepDataInterface, final int copyNr, final TransMeta transMeta, final Trans trans) {
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }

  public final boolean processRow(final StepMetaInterface smi, final StepDataInterface sdi) throws KettleException {

    meta = (PaloCellOutputMeta) smi;
    data = (PaloCellOutputData) sdi;

    Object[] r = getRow(); // this also waits for a previous step to be
                           // finished.
    if (r == null) { // no more input to be expected...
      this.logDebug("No More Rows.");
      setOutputDone();
      return false;
    }
    if (first) {
      first = false;
      this.logBasic("First Row Analysis.");
      data.indexes = new int[meta.getFields().size() + 1];
      for (int i = 0; i < data.indexes.length - 1; i++) {
        data.indexes[i] = getInputRowMeta().indexOfValue(meta.getFields().get(i).getFieldName());
      }
      data.indexes[data.indexes.length - 1] = getInputRowMeta().indexOfValue(meta.getMeasure().getFieldName());
      this.logBasic("First Row Ok.");

      if (meta.getClearCube()) {
        try {
          data.helper.clearCube(meta.getCube());
        } catch (Exception ex) {
          throw new KettleException("Failed to clear Cube");
        }
      }
    }

    String row = "";
    try {
      Object[] newRow = new Object[meta.getFields().size() + 1];
      for (int i = 0; i < data.indexes.length; i++) {
        if (i == data.indexes.length - 1)
          if (meta.getMeasureType().equals("Numeric"))
            newRow[i] = getInputRowMeta().getNumber(r, data.indexes[i]);
          else
            newRow[i] = getInputRowMeta().getString(r, data.indexes[i]);
        else
          newRow[i] = getInputRowMeta().getString(r, data.indexes[i]);
      }
      data.batchCache.add(newRow);
      if (data.batchCache.size() == meta.getCommitSize()){
    	  try {
    		  data.helper.addCells(data.batchCache, meta.getUpdateMode(), meta.getSplashMode());
    		  for (int i = 0; i < data.batchCache.size(); i++)
    			  incrementLinesOutput();
    	  }
    	  finally{
    		  data.batchCache.clear();
    	  }
      }
    } catch (Exception e) {
      throw new KettleException("Failed to add Cell Row: " + row, e);
    }
    
    return true;
  }

  public final boolean init(StepMetaInterface smi, StepDataInterface sdi) {
    meta = (PaloCellOutputMeta) smi;
    data = (PaloCellOutputData) sdi;

    if (super.init(smi, sdi)) {
      try {
        this.logDebug("Meta Fields: " + meta.getFields().size());
        data.helper = new PaloHelper(meta.getDatabaseMeta(), getLogLevel());
        data.helper.connect();
        data.helper.loadCubeCache(meta.getCube(), meta.getEnableDimensionCache(), meta.getPreloadDimensionCache());
        data.batchCache.clear();
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
	  try {
		  if (getErrors() == 0 && data.batchCache.size() > 0){
			  data.helper.addCells(data.batchCache, meta.getUpdateMode(), meta.getSplashMode());
			  for (int i = 0; i < data.batchCache.size(); i++)
				  incrementLinesOutput();
		  }
	  } catch (Exception ex) {
		  logError("Unexpected error processing batch error", ex);
          setErrors(1);
          stopAll();
	  }
	  finally{
		  data.batchCache.clear();
		  data.helper.clearCubeCache();
		  data.helper.disconnect();
	  }
    
    super.dispose(smi, sdi);
  }
}
