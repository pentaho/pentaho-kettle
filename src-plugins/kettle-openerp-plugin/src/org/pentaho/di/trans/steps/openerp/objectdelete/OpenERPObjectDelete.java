/*
 *   This software is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This software is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this software.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   Copyright 2011 De Bortoli Wines Pty Limited (Australia)
 */

package org.pentaho.di.trans.steps.openerp.objectdelete;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.openerp.core.OpenERPHelper;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class OpenERPObjectDelete extends BaseStep implements StepInterface {
	private OpenERPObjectDeleteMeta meta;
	private OpenERPObjectDeleteData data;

	private int idIndex;

	public OpenERPObjectDelete(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(final StepMetaInterface smi,
			final StepDataInterface sdi) throws KettleException {

		Object[] inputRow = getRow(); // this also waits for a previous step to be finished.
		if (inputRow == null) {
			try{
				// Commit the last batch
				CommitBatch();
			} catch (Exception e) {
				throw new KettleException("Failed to commit batch: ", e);
			}

			// no more input to be expected...
			this.logDebug("No More Rows.");
			setOutputDone();
			return false;
		}

		if (first) {
			// Set to import the id field if this step is an update step
			// Must be the first field
			if (meta.getIdFieldName() == null || meta.getIdFieldName() == "")
				idIndex = -1;
			else
				idIndex = getInputRowMeta().indexOfValue(meta.getIdFieldName());
			
			if (idIndex == -1)
				throw new KettleException("Failed to initialize step ", new Exception("Could not find ID field from input step with name: '" + meta.getIdFieldName() + "'"));
			
			first = false;
			data.batchRows.clear();
		}

		String row = "";
		try {
			data.batchRows.add(Integer.parseInt(inputRow[idIndex].toString()));

			if (data.batchRows.size() == meta.getCommitBatchSize())
				CommitBatch();

		} catch (Exception e) {
			throw new KettleException("Failed to commit batch: " + row, e);
		}

		return true;
	}

	private void CommitBatch() throws Exception{
		try {
			data.helper.deleteObjects(meta.getModelName(), data.batchRows);
			for (int i = 0; i < data.batchRows.size(); i++)
				incrementLinesOutput();
		} finally {
			data.batchRows.clear();
		}
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (OpenERPObjectDeleteMeta) smi;
		data = (OpenERPObjectDeleteData) sdi;

		if (super.init(smi, sdi)) {
			try {
				this.logDebug("Initializing OpenERP Session");
				data.helper = new OpenERPHelper(meta.getDatabaseMeta());
				data.helper.StartSession();
				return true;
			} catch (Exception e) {
				logError("An error occurred, processing will be stopped: "
						+ e.getMessage());
				setErrors(1);
				stopAll();
			}
		}

		return false;
	}
}
