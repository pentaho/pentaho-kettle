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
      data.indexes = new int[meta.getLevels().size()];
      for (int i = 0; i < data.indexes.length; i++) {
        String fieldName = meta.getLevels().get(i).getFieldName();
        int numRow = getInputRowMeta().indexOfValue(fieldName);
        if (numRow < 0)
          throw new KettleException("DimOutput: failed to find input row meta for ".concat(meta.getLevels().get(i).getLevelName()));
        data.indexes[i] = numRow;
        this.logDebug(meta.getLevels().get(i).getLevelName() + " has index: " + numRow);
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
        data.helper.addDimension(meta.getDimension(), newDimension, meta.getCreateNewDimension(), meta.getClearDimension(), meta.getElementType());
        this.logBasic("Dimension Added.");
        setOutputDone();
        return false;
      } catch (Exception e) {
        throw new KettleException("Failed to add dimension rows", e);
      }
    }

    try {

      String[] newRow = new String[meta.getLevels().size()];
      for (int i = 0; i < data.indexes.length; i++) {
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

}
