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

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.xmlrpc.XmlRpcException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.openerp.core.FieldMapping;
import org.pentaho.di.openerp.core.OpenERPHelper;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.debortoliwines.openerp.api.FieldCollection;
import com.debortoliwines.openerp.api.FilterCollection;
import com.debortoliwines.openerp.api.Row;
import com.debortoliwines.openerp.api.RowCollection;

public class OpenERPObjectOutput extends BaseStep implements StepInterface {
	private OpenERPObjectOutputMeta meta;
	private OpenERPObjectOutputData data;

	private String[] fieldList;
	private FieldCollection fieldCache;
	private int[] index;
	private FilterCollection readSourceFilter;
	private ArrayList<String> readFieldList = new ArrayList<String>();
	private int[] readRowIndex = new int[0];
	private HashMap<String, Object> filterRowCache = new HashMap<String, Object>();
	private final String SEPARATOR = "A|a";

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
				
				/* If the ID isn't used as the filter, prepare the filter */
				if (index[0] == -1 && meta.getKeyLookups().size() > 0){
					prepareReadParameters();
					prepareCache();
				}
			} catch (Exception e) {
				throw new KettleException("Failed to initialize step ", e);
			}
			first = false;
			data.batchRows.clear();
		}

		String row = "";
		try {
			Object[] newRow = new Object[fieldList.length];

			// If ID field was mapped in the filter, use it.  Otherwise try and find it from cache.
			if (index[0] >= 0)
				newRow[0] = inputRow[this.index[0]];
			else{
				String combinedKey = "";
				for (int i : readRowIndex)
					combinedKey += SEPARATOR + (inputRow[i] == null ? "" : inputRow[i]);
				if (filterRowCache.containsKey(combinedKey))
					newRow[0] = filterRowCache.get(combinedKey);
			}
			
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
	
	private void prepareCache() throws XmlRpcException{
		filterRowCache.clear();
		
		RowCollection rows = data.helper.getModelData(meta.getModelName(), readSourceFilter, readFieldList.toArray(new String[readFieldList.size()]));
		for (Row row : rows){
			String combinedKey = "";
			for (String fieldname : readFieldList){
				Object value = row.get(fieldname);
				
				// For id fields
				if (value instanceof Object[])
					value = ((Object[]) value)[0];
				
				combinedKey += SEPARATOR + (value == null ? "" : value);
			}
			filterRowCache.put(combinedKey, row.get("id"));
		}
	}
	
	private void prepareReadParameters() throws Exception{
		ArrayList<FieldMapping> allFields = data.helper.getDefaultFieldMappings(meta.getModelName());
		
		ArrayList<Integer> readIdx = new ArrayList<Integer>();
		// Building search filter with constant values
		readSourceFilter = new FilterCollection();
		for(int i = 0; i < meta.getKeyLookups().size(); i++){
			
			String modelField = meta.getKeyLookups().get(i)[0];
			String comparison = meta.getKeyLookups().get(i)[1];
			String streamField = meta.getKeyLookups().get(i)[2];

			// Only pass through filters for constant values that aren't linked to stream fileds 
			if (streamField != null 
					&& streamField.length() > 0 
					&& getInputRowMeta().indexOfValue(streamField) >= 0){
				readFieldList.add(modelField);
				readIdx.add(getInputRowMeta().indexOfValue(streamField));
				continue;
			}
			
			// Get the source field to filter on
			FieldMapping fld = null;
			for (int j = 0; j < allFields.size(); j++)
				if (allFields.get(j).source_field.equals(modelField) 
						&& allFields.get(j).source_index <= 0){
					fld = allFields.get(j);
					break;
				}
			
			Object[] result = data.helper.formatFilterValue(modelField, comparison, streamField, fld);
			readSourceFilter.add(modelField, result[0].toString(), result[1]);
			this.logBasic("Setting filter: [" + modelField + "," + result[0].toString() + "," + result[1].toString() + "]");
		}
		readRowIndex = new int[readIdx.size()];
		for (int i = 0; i < readRowIndex.length; i++)
			readRowIndex[i] = readIdx.get(i);
	}
	
	private void prepareFieldList() throws Exception {

		index = new int[meta.getModelFields().length + 1];

		// Place holder for the ID field that must be first 
		index[0] = -1;
		
		// If the ID field is the only filter, include it in the field
		if (meta.getKeyLookups().size() == 1
				&& meta.getKeyLookups().get(0)[0].equals("id")
				&& meta.getKeyLookups().get(0)[1].equals("="))
			index[0] = getInputRowMeta().indexOfValue(meta.getKeyLookups().get(0)[2]);
		
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
