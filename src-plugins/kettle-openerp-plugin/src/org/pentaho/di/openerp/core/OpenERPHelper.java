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

package org.pentaho.di.openerp.core;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.xmlrpc.XmlRpcException;
import org.pentaho.di.core.database.DatabaseFactoryInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.debortoliwines.openerp.api.FilterCollection;
import com.debortoliwines.openerp.api.SelectionOption;
import com.debortoliwines.openerp.api.Session;
import com.debortoliwines.openerp.api.Field;
import com.debortoliwines.openerp.api.FieldCollection;
import com.debortoliwines.openerp.api.RowCollection;
import com.debortoliwines.openerp.api.Field.FieldType;
import com.debortoliwines.openerp.api.Session.RowsReadListener;

/**
 * Class to make life easier for transformations and keep transformation code simple
 * @author Pieter van der Merwe
 */
public class OpenERPHelper implements DatabaseFactoryInterface {

	private Session openERPConnection;
	
	// Cache used to store the name_get result of an model to cater for many2many relations
	private HashMap<String, HashMap<String, String>> modelNameCache = new HashMap<String, HashMap<String, String>>();

	@Override
	public String getConnectionTestReport(DatabaseMeta databaseMeta){

		try {
			OpenERPHelper helper = new OpenERPHelper(databaseMeta);
			helper.StartSession();

			return "Successfully connected to [" + databaseMeta.environmentSubstitute(databaseMeta.getName()) + "]";
		} catch (NumberFormatException e) {
			return "Invalid port number: " + e.getMessage();
		} catch (Exception e) {
			return "Connection failed: " + e.getMessage();
		}
	}

	/// Need a default constructor for the "Test" button to work on the connect database dialog
	public OpenERPHelper(){
	}

	public OpenERPHelper(DatabaseMeta databaseMeta){
		openERPConnection = new Session(
				databaseMeta.environmentSubstitute(databaseMeta.getHostname()),
				Integer.parseInt(databaseMeta.environmentSubstitute(databaseMeta.getDatabasePortNumberString())), 
				databaseMeta.environmentSubstitute(databaseMeta.getDatabaseName()),
				databaseMeta.environmentSubstitute(databaseMeta.getUsername()), 
				databaseMeta.environmentSubstitute(databaseMeta.getPassword()));
	}

	public void StartSession() throws Exception{
		openERPConnection.startSession();
		
		// Don't automatically filter out active items in any steps 
		openERPConnection.getContext().setActiveTest(false);
	}

	public String[] getModelList(){

		String [] modelNames = new String[0];

		try {
			RowCollection rows = openERPConnection.searchAndReadObject("ir.model", null, new String[] {"model"});
			modelNames = new String[rows.size()];
			for (int i = 0; i < modelNames.length; i++)
				modelNames[i] = rows.get(i).get("model").toString();
		} catch (Exception e) {}

		return modelNames;
	}

	public void getModelData(String model, FilterCollection filter, int batchSize, ArrayList<FieldMapping> mappings, RowsReadListener listener) throws MalformedURLException, XmlRpcException{

		ArrayList<String> fieldList = new ArrayList<String>();
		for(FieldMapping map : mappings)
			if (!fieldList.contains(map.source_field))
				fieldList.add(map.source_field);

		String [] fieldStringList = new String[fieldList.size()];
		fieldStringList = fieldList.toArray(fieldStringList);

		openERPConnection.searchAndReadObject(model, filter, fieldStringList, batchSize, listener);
	}

	public String [] getOutputFields(String model) throws MalformedURLException, XmlRpcException{
		FieldCollection fields = openERPConnection.getFields(model);

		ArrayList<String> fieldArray = new ArrayList<String>();
		for(Field field : fields){
			if (field.getType() == FieldType.ONE2MANY
					|| field.getReadonly() == true)
				continue;

			fieldArray.add(field.getName());
		}

		return fieldArray.toArray(new String[fieldArray.size()]);
	}

	public void importData(String model, String [] fieldList, ArrayList<Object []> inputRows) throws Exception{
		openERPConnection.importData(model, fieldList, inputRows);
	}

	public Object[] fixImportDataTypes(String model, String [] targetFieldNames, FieldCollection fieldDef, Object [] inputRow) throws Exception {
		Object[] outputRow = new Object[inputRow.length];
		
		for (int i = 0; i < inputRow.length; i++){
			outputRow[i] = inputRow[i];
			String targetField = targetFieldNames[i];
			
			if (targetField.endsWith(".id")){
				if (outputRow[i] == null)
					outputRow[i] = 0;
				else
					outputRow[i] = Integer.parseInt(inputRow[i].toString());
				continue;
			}
			
			// Do any type conversions
			for(Field field : fieldDef)
				if (field.getName().equals(targetField)){
					// Check selection values
					if (field.getType() == FieldType.SELECTION){
						boolean validValue = false;
						for (SelectionOption option : field.getSelectionOptions()){
							// If the database code was specified, replace it with the value.
							// The import procedure uses the value and not the code
							if (option.code.equals(outputRow[i].toString())){
								validValue = true;
								outputRow[i] = option.value;
								break;
							}
							else if (option.value.equals(outputRow[i].toString())){
								validValue = true;
								break;
							}
						}
						if (!validValue)
							throw new Exception("Could not find a valid value for section field " + field.getName() + " with value " + outputRow[i].toString());
					}
					
					
					// Check types
					switch (field.getType()) {
					case MANY2MANY:
						/* The import function uses the Names of the objects for the import.  Replace the ID list passed
						 * in with a Name list for the import_data function that we are about to call
						 */
						HashMap<String, String> idToName = null;
						if (!modelNameCache.containsKey(field.getRelation())){
							idToName = new HashMap<String, String>();
							Object [] ids = openERPConnection.searchObject(field.getRelation(), null);
							Object[] names = openERPConnection.nameGet(field.getRelation(), ids);
							for (int j = 0; j < ids.length; j++){
								Object [] nameValue = (Object [])names[j]; 
								idToName.put(nameValue[0].toString(), nameValue[1].toString());
							}
							modelNameCache.put(field.getRelation(), idToName);
						}
						else idToName = modelNameCache.get(field.getRelation());
						
						String newValue = "";
						for (String singleID : inputRow[i].toString().split(","))
							if (idToName.containsKey(singleID))
								newValue = newValue + "," + idToName.get(singleID);
							else throw new Exception("Could not find " + field.getRelation() + " with ID " + singleID);
						
						outputRow[i] = newValue.substring(1);
						
						break;
					
					// The import procedure expects all types to be strings
					default:
						outputRow[i] = outputRow[i].toString();
						break;
					}
				}
		}
		
		return outputRow;
	}
	
	public FieldCollection getModelFields(String model) throws Exception{
		return openERPConnection.getFields(model);
	}

	public String[] getFieldListForImport(String model, String [] targetFieldNames) throws Exception {

		ArrayList<String> fieldList = new ArrayList<String>();
		fieldList.add(".id");

		FieldCollection fields = openERPConnection.getFields(model);

		for (String targetField : targetFieldNames){
			boolean found = false;
			for(Field field : fields)
				if (field.getName().equals(targetField)){
					found = true;
					if (field.getType() == FieldType.MANY2ONE)
						fieldList.add(field.getName() + ".id");
					else
						fieldList.add(field.getName());
				}

			if (!found)
				throw new Exception("Could not find field '" + targetField + "' in object '" + model + "'");
		}

		return fieldList.toArray(new String[fieldList.size()]);

	}
	
	public void deleteObjects(String model, ArrayList<Object> ids) throws XmlRpcException{
		openERPConnection.unlinkObject(model, ids.toArray(new Object[ids.size()]));
	}

	public ArrayList<FieldMapping> getDefaultFieldMappings(String model) throws MalformedURLException, XmlRpcException{

		ArrayList<FieldMapping> mappings = new ArrayList<FieldMapping>();
		FieldCollection fields = openERPConnection.getFields(model);

		FieldMapping fieldMap = new FieldMapping();
		fieldMap.source_model = model;
		fieldMap.source_field = "id";
		fieldMap.source_index = -1;
		fieldMap.target_model = model;
		fieldMap.target_field = "id";
		fieldMap.target_field_label = "Database ID";
		fieldMap.target_field_type = ValueMetaInterface.TYPE_INTEGER;
		mappings.add(fieldMap);

		for (Field field : fields){
			fieldMap = new FieldMapping();
			String fieldName = field.getName();

			fieldMap.source_model = model;
			fieldMap.source_field = fieldName;
			fieldMap.source_index = -1;
			fieldMap.target_model = model;
			fieldMap.target_field = fieldName;
			fieldMap.target_field_label = field.getDescription();

			Field.FieldType fieldType = field.getType();

			switch (fieldType) {
			case CHAR:
			case TEXT:
				fieldMap.target_field_type = ValueMetaInterface.TYPE_STRING;
				mappings.add(fieldMap);
				break;
			case BOOLEAN:
				fieldMap.target_field_type = ValueMetaInterface.TYPE_BOOLEAN;
				mappings.add(fieldMap);
				break;
			case FLOAT:
				fieldMap.target_field_type = ValueMetaInterface.TYPE_NUMBER;
				mappings.add(fieldMap);
				break;
			case DATETIME:
			case DATE:
				fieldMap.target_field_type = ValueMetaInterface.TYPE_DATE;
				mappings.add(fieldMap);
				break;
			case MANY2ONE:
				FieldMapping newFieldMap = fieldMap.Clone();

				// Normal id field
				newFieldMap.source_index = 0;
				newFieldMap.target_model = field.getRelation();
				newFieldMap.target_field = fieldName + "_id";
				newFieldMap.target_field_label = field.getDescription() + "/Id";
				newFieldMap.target_field_type = ValueMetaInterface.TYPE_INTEGER;

				mappings.add(newFieldMap);

				// Add name field
				newFieldMap = fieldMap.Clone();

				newFieldMap.source_index = 1;
				newFieldMap.target_model = field.getRelation();;
				newFieldMap.target_field = fieldName + "_name";
				newFieldMap.target_field_label = field.getDescription() +"/Name";
				newFieldMap.target_field_type = ValueMetaInterface.TYPE_STRING;

				mappings.add(newFieldMap);
				break;
			case ONE2MANY:
			case MANY2MANY:
			default:
				fieldMap.target_field_type = ValueMetaInterface.TYPE_STRING;
				mappings.add(fieldMap);
				break;
			}
		}

		return mappings;

	}

	public final RowMetaInterface getFieldRowMeta(ArrayList<FieldMapping> mappings) throws MalformedURLException, XmlRpcException{

		RowMetaInterface rowMeta = new RowMeta();
		for (FieldMapping map : mappings){
			rowMeta.addValueMeta(new ValueMeta(map.target_field, map.target_field_type));
		}

		return rowMeta;

	}
}
