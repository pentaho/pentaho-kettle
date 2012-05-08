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
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with PaloKettlePlugin.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   Copyright 2008 Stratebi Business Solutions, S.L.
 *   Copyright 2011 De Bortoli Wines Pty Limited (Australia)
 */

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.palo.core.ConsolidationCollection;
import org.pentaho.di.palo.core.ConsolidationElement;
import org.pentaho.di.palo.core.PaloHelper;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class PaloDimOutput extends BaseStep implements StepInterface {
	private PaloDimOutputMeta meta;
	private PaloDimOutputData data;
	private int rowCount = 0;
	private ConsolidationCollection consolidations;

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
			data.helper.manageDimension(meta.getDimension(), meta.getCreateNewDimension(), meta.getClearDimension(),  meta.getClearConsolidations(), meta.getRecreateDimension());
			try{
				data.helper.loadDimensionCache(meta.getDimension(), meta.getEnableElementCache(), meta.getPreloadElementCache());
			} catch (Exception e) {
				throw new KettleException("Failed to load cache", e);
			}
		}

		if (r != null){
			rowCount++;
			
			try {
	
				ConsolidationElement parent = null;
				for (int i = 0; i < data.indexes.length; i++) {
					if (i % 2 == 0)
					{
						if (r[data.indexes[i]] == null)
							continue;
	
						data.elementNamesBatch.add(r[data.indexes[i]].toString());
					}
					else{
						/* Default weight to 1 if it was left to default */
						double consolidation_factor = 1;
						if (data.indexes[i] >= 0)
							consolidation_factor = Double.parseDouble((r[data.indexes[i]] == null ? 0 : r[data.indexes[i]]).toString());
						
						ConsolidationElement child = null;
						
						if (r[data.indexes[i - 1]] != null){
							String elementName = r[data.indexes[i - 1]].toString();
							if (!this.consolidations.hasConsolidationElement(elementName)){
								child = new ConsolidationElement(elementName);
								this.consolidations.add(child);
							}
							else child = this.consolidations.getConsolidationElement(elementName);

							if (parent != null)
								parent.addChild(child, consolidation_factor);
						}
						
						parent = child;
					}
				}
	
				// Should probably make this a parameter on the dialog
				if (data.elementNamesBatch.size() % 100 == 0)
					commitBatch();
	
			} catch (Exception e) {
				throw new KettleException("Failed to add row to the row buffer", e);
			}
		}

		// this also waits for a previous step to be finished.
		if (r == null) { // no more input to be expected...
			try {
				if (data.elementNamesBatch.size() > 0)
					commitBatch();

				// Because we add rows in bulk, the dimension cache isn't complete.  It only keeps a list
				// of added items for validation.  If the cache must be pre-loaded, pre-load it now.
				data.helper.clearDimensionCache();
				data.helper.loadDimensionCache(meta.getDimension(), meta.getEnableElementCache(), meta.getPreloadElementCache());
				
				// if it's the last row create the dimension
				this.logBasic("All rows have been added. Looking for consolidations");
				
				this.logBasic("Updating consolidations for Dimension" + meta.getDimension());
				data.helper.addDimensionConsolidations(meta.getDimension(), this.consolidations);
				this.logBasic("Consolidations updated.");
				
				setOutputDone();
				return false;
			} catch (Exception e) {
				throw new KettleException("Failed to add dimension rows", e);
			}
		}

		return true;
	}

	private void commitBatch() throws Exception{
		data.helper.addDimensionElements(data.elementNamesBatch, meta.getElementType());
		while (rowCount > 0){
			incrementLinesOutput();
			rowCount--;
		}
		data.elementNamesBatch.clear();
	}

	public final boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (PaloDimOutputMeta) smi;
		data = (PaloDimOutputData) sdi;

		this.consolidations = new ConsolidationCollection();
		if (super.init(smi, sdi)) {
			try {
				this.logBasic("Meta Levels:" + meta.getLevels().size());
				data.helper = new PaloHelper(meta.getDatabaseMeta(), getLogLevel());
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
