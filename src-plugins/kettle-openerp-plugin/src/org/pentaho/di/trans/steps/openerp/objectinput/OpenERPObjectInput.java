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

package org.pentaho.di.trans.steps.openerp.objectinput;

import java.util.ArrayList;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.openerp.core.FieldMapping;
import org.pentaho.di.openerp.core.OpenERPHelper;
import org.pentaho.di.openerp.core.ReadFilter;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.debortoliwines.openerp.api.FilterCollection;
import com.debortoliwines.openerp.api.Row;
import com.debortoliwines.openerp.api.RowCollection;
import com.debortoliwines.openerp.api.Session.RowsReadListener;

public class OpenERPObjectInput extends BaseStep implements StepInterface{
	private OpenERPObjectInputMeta meta;
	private OpenERPObjectInputData data;

	public OpenERPObjectInput(StepMeta stepMeta,StepDataInterface stepDataInterface, int copyNr,TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	private void putRow(Row row, RowMetaInterface rowMeta) throws KettleStepException {
		ArrayList<FieldMapping> mappings = meta.getMappings();
		
		Object [] copyRow = new Object[mappings.size()];
		
		int i = 0;
		for (FieldMapping map : mappings){
			Object value = row.get(map.source_field);
			
			if (map.source_index >= 0 && value != null && value instanceof Object [])
				copyRow[i] = (((Object []) value).length == 0 ? null : ((Object []) value)[map.source_index]);
			else 
				copyRow[i] = value;
			
			copyRow[i] = fixType(map, copyRow[i]);
			
			i++;
		}

		putRow(rowMeta, copyRow);
	}
	
	private Object fixType(FieldMapping map, Object value){
		// Nothing to fix
		if (value == null)
			return null;

		Object fixedValue = value;
		
		if (map.target_field_type == ValueMetaInterface.TYPE_INTEGER)
			fixedValue = Long.parseLong(value.toString());
		else if (map.target_field_type == ValueMetaInterface.TYPE_NUMBER)
			fixedValue = Double.parseDouble(value.toString());
		// ONE2MANY and MANY2MANY fields
		else if (map.target_field_type == ValueMetaInterface.TYPE_STRING && value instanceof Object []){
			String stringValue = "";
			for(Object singleValue : (Object []) value)
				stringValue += "," + singleValue.toString(); 
			
			fixedValue = stringValue.substring(1); 
		}
			
		
		return fixedValue;
	}

	public boolean processRow(final StepMetaInterface smi, final StepDataInterface sdi) throws KettleException {
		this.logBasic("Getting Field Row Meta.");

		this.logBasic("Getting Rows.");
		if (first == true){
			first = false;
			try {
				final RowMetaInterface rowMeta = data.helper.getFieldRowMeta(meta.getMappings());
				
				ArrayList<FieldMapping> allFields = data.helper.getDefaultFieldMappings(meta.getModelName());
				
				// Building search filter
				FilterCollection filter = new FilterCollection();
				for(int i = 0; i < meta.getFilterList().size(); i++){
					ReadFilter filterItem = meta.getFilterList().get(i);
					
					FieldMapping fld = null;
					for (int j = 0; j < allFields.size(); j++)
						if (allFields.get(j).source_field.equals(filterItem.field_name) 
								&& allFields.get(j).source_index <= 0){
							fld = allFields.get(j);
							break;
						}
				
					Object value = filterItem.value;
					
					// Fix the value type if required
					if (fld == null)
						value = filterItem.value;
					else if (fld.target_field_type == ValueMetaInterface.TYPE_BOOLEAN){
						char firstchar = filterItem.value.toLowerCase().charAt(0);
						if (firstchar == '1' || firstchar == 'y' || firstchar == 't')
							value = true;
						else if (firstchar == '0' || firstchar == 'n' || firstchar == 'f') 
							value = false;
						else throw new Exception ("Unknown boolean " + filterItem.value);
					}
					else if (fld.target_field_type == ValueMetaInterface.TYPE_NUMBER)
						value = Double.parseDouble(filterItem.value);
					else if (fld.target_field_type == ValueMetaInterface.TYPE_INTEGER)
						value = Integer.parseInt(filterItem.value);
					
					filter.add(filterItem.field_name, filterItem.operator, value);
					
					this.logBasic("Setting filter: [" + filterItem.field_name + "," + filterItem.operator + "," + value.toString() + "]");
				}
				
				data.helper.getModelData(meta.getModelName(), filter, meta.getReadBatchSize(), meta.getMappings(), new RowsReadListener() {

					@Override
					public void rowsRead(RowCollection rows) {
						for (Row row : rows){
							try {
								putRow(row,rowMeta);
							} catch (KettleStepException e) {
								logError("An error occurred, processing will be stopped: " + e.getMessage());
								setErrors(1);
								stopAll();
							}
							incrementLinesInput();
						}
					}
				});
			} catch (Exception e) {
				throw new KettleException(e.getMessage());
			}
		}
		this.logBasic("Process Ended.");
		setOutputDone();
		return false;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (OpenERPObjectInputMeta) smi;
		data = (OpenERPObjectInputData) sdi;

		if (super.init(smi, sdi)) {
			try {
				this.logDebug("Initializing OpenERP Session");
				data.helper = new OpenERPHelper(meta.getDatabaseMeta());
				data.helper.StartSession();
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
