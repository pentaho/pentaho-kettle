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

package org.pentaho.di.trans.steps.openerp.objectoutput;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.openerp.core.OpenERPHelper;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.debortoliwines.openerp.api.FieldCollection;

public class OpenERPObjectOutput extends BaseStep implements StepInterface {
	private OpenERPObjectOutputMeta meta;
	private OpenERPObjectOutputData data;

	private String[] fieldList;
	private FieldCollection fieldCache;
	private int[] index;

	public OpenERPObjectOutput(StepMeta stepMeta,
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
			try{
				prepareFieldList();
			} catch (Exception e) {
				throw new KettleException("Failed to initialize step ", e);
			}
			first = false;
			data.batchRows.clear();
		}

		String row = "";
		try {
			Object[] newRow = new Object[fieldList.length];
			
			// Add by default if no id field was specified
			if (this.index[0] == -1)
				newRow[0] = 0;
			else
				newRow[0] = inputRow[this.index[0]];
			
			for (int i = 1; i < fieldList.length; i++)
				newRow[i] = inputRow[this.index[i]];
				
			newRow = data.helper.fixImportDataTypes(meta.getModelName(),fieldList, fieldCache, newRow);
			
			data.batchRows.add(newRow);

			if (data.batchRows.size() == meta.getCommitBatchSize())
				CommitBatch();

		} catch (Exception e) {
			throw new KettleException("Failed to commit batch: " + row, e);
		}

		return true;
	}

	private void CommitBatch() throws Exception{
		// In the process of stopping, return
		if (isStopped())
			return;
		
		try {
			data.helper.importData(meta.getModelName(), fieldList,	data.batchRows);
			for (int i = 0; i < data.batchRows.size(); i++)
				incrementLinesOutput();
		} finally {
			data.batchRows.clear();
		}
	}

	private void prepareFieldList() throws Exception {

		index = new int[meta.getModelFields().length + 1];

		// Set to import the id field if this step is an update step
		// Must be the first field
		if (meta.getIdFieldName() == null || meta.getIdFieldName() == "")
			index[0] = -1;
		else
			index[0] = getInputRowMeta().indexOfValue(meta.getIdFieldName());

		for (int i = 0; i < meta.getModelFields().length; i++)
			index[i + 1] = getInputRowMeta().indexOfValue(meta.getStreamFields()[i]);

		this.fieldList = data.helper.getFieldListForImport(meta.getModelName(), meta.getModelFields());
		this.fieldCache = data.helper.getModelFields(meta.getModelName());
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (OpenERPObjectOutputMeta) smi;
		data = (OpenERPObjectOutputData) sdi;

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
