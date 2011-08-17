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
package org.pentaho.di.trans.steps.palo.dimoutput;

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
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with PaloKettlePlugin.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   Copyright 2008 Stratebi Business Solutions, S.L.
 */

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.palo.core.DimensionGroupingCollection;
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
public class PaloDimOutput extends BaseStep implements StepInterface {
  private PaloDimOutputMeta meta;
  private PaloDimOutputData data;
  private List<String[]>    currentTransformationRows = null;

  public PaloDimOutput(final StepMeta stepMeta, final StepDataInterface stepDataInterface, final int copyNr, final TransMeta transMeta, final Trans trans) {
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }

  public final boolean processRow(final StepMetaInterface smi, final StepDataInterface sdi) throws KettleException {

    meta = (PaloDimOutputMeta) smi;
    data = (PaloDimOutputData) sdi;

    Object[] r = getRow();

    if (first) {
      first = false;
      this.logBasic("First Row Analysis:");
      if (meta.getLevels().size() == 0)
        throw new KettleException("Number of levels must be greater that 0 to process the rows");
      this.logBasic("Number of defined levels: " + meta.getLevels().size());
      
      /* Indexes will follow [data index],[consolidation index],[data index],[consolidation index] .... */ 
      data.indexes = new int[meta.getLevels().size() * 2];
      for (int i = 0; i < meta.getLevels().size(); i++) {
    
    	/* Get data field index */
    	String dataFieldName = meta.getLevels().get(i).getFieldName();
        int numRow = getInputRowMeta().indexOfValue(dataFieldName);
        if (numRow < 0)
          throw new KettleException("DimOutput: failed to find input row meta for ".concat(meta.getLevels().get(i).getLevelName()));
        data.indexes[i * 2] = numRow;
        this.logDebug(meta.getLevels().get(i).getLevelName() + " has index: " + numRow);
        
        /* Get consolidation field index */
        String consolidationFieldName = meta.getLevels().get(i).getConsolidationFieldName();
        if (consolidationFieldName == null){
        	numRow = -1;
	        this.logDebug("Consolidation factor was left to the default");
        }
        else{
	        numRow = getInputRowMeta().indexOfValue(consolidationFieldName);
	        if (numRow < 0)
	          throw new KettleException("DimOutput: failed to find input row meta for ".concat(meta.getLevels().get(i).getConsolidationFieldName()));
	        this.logDebug(meta.getLevels().get(i).getConsolidationFieldName() + " has index: " + numRow);
        }
        data.indexes[(i * 2) + 1] = numRow;
      }
    }

    // this also waits for a previous step to be finished.
    if (r == null) { // no more input to be expected...
      try {
        // if it's the last row create the dimension
        this.logBasic("All rows have been read. Looking for consolidations");
        this.logDebug("Read rows:" + this.currentTransformationRows.size());
        DimensionGroupingCollection newDimension = data.helper.getConsolidations(meta.getDimension(), this.currentTransformationRows);
        this.logBasic("Consolidations got.");
        this.logBasic(newDimension == null ? "Null Consolidations" : " Consolidations Ok");
        this.logBasic("Add Dimension " + meta.getDimension());
        data.helper.addDimension(meta.getDimension(), newDimension, meta.getCreateNewDimension(), meta.getClearDimension(), meta.getClearConsolidations(), meta.getRecreateDimension(),meta.getEnableElementCache(), meta.getPreloadElementCache(),  meta.getElementType());
        this.logBasic("Dimension Added.");
        setOutputDone();
        return false;
      } catch (Exception e) {
        throw new KettleException("Failed to add dimension rows", e);
      }
    }

    try {

      String[] newRow = new String[data.indexes.length];
      for (int i = 0; i < data.indexes.length; i++) {
    	/* Default weight to 1 if it was left to default */
        if (i % 2 == 1 && data.indexes[i] < 0)
    		newRow[i] = "1";
    	else
    		newRow[i] = r[data.indexes[i]].toString();
      }
      incrementLinesOutput();
      this.currentTransformationRows.add(newRow);

    } catch (Exception e) {
      throw new KettleException("Failed to add row to the row buffer", e);
    }
    return true;
  }

  public final boolean init(StepMetaInterface smi, StepDataInterface sdi) {
    meta = (PaloDimOutputMeta) smi;
    data = (PaloDimOutputData) sdi;

    this.currentTransformationRows = new ArrayList<String[]>();
    if (super.init(smi, sdi)) {
      try {
        this.logBasic("Meta Levels:" + meta.getLevels().size());
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
