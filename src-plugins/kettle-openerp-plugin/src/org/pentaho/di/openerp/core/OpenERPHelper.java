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
import java.util.Collections;

import org.apache.xmlrpc.XmlRpcException;
import org.pentaho.di.core.database.DatabaseFactoryInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.debortoliwines.openerp.api.FilterCollection;
import com.debortoliwines.openerp.api.ObjectAdapter;
import com.debortoliwines.openerp.api.OpenERPCommand;
import com.debortoliwines.openerp.api.OpeneERPApiException;
import com.debortoliwines.openerp.api.Session;
import com.debortoliwines.openerp.api.Field;
import com.debortoliwines.openerp.api.FieldCollection;
import com.debortoliwines.openerp.api.RowCollection;
import com.debortoliwines.openerp.api.Field.FieldType;

/**
 * Helper class to keep common functionality in one class
 * @author Pieter van der Merwe
 */
public class OpenERPHelper implements DatabaseFactoryInterface {

	private Session openERPConnection;
	private OpenERPCommand commands;
	

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
		
		commands = openERPConnection.getOpenERPCommand();
		
		// Don't automatically filter out active items in any steps 
		openERPConnection.getContext().setActiveTest(false);
	}

	public String[] getModelList(){

		String [] modelNames = new String[0];
		try {
			ObjectAdapter modelAdapter = new ObjectAdapter(openERPConnection, "ir.model");
			RowCollection rows = modelAdapter.searchAndReadObject(null, new String[] {"model"});
			modelNames = new String[rows.size()];
			for (int i = 0; i < modelNames.length; i++)
				modelNames[i] = rows.get(i).get("model").toString();
		} catch (Exception e) {}

		return modelNames;
	}
	
	public int getModelDataCount(String model, FilterCollection filter) throws XmlRpcException, OpeneERPApiException{
		ObjectAdapter modelAdapter = new ObjectAdapter(openERPConnection, model);
		return modelAdapter.getObjectCount(filter);
	}
		
	public RowCollection getModelData(String model, FilterCollection filter, ArrayList<FieldMapping> mappings, int offset, int limit) throws XmlRpcException, OpeneERPApiException{
		ArrayList<String> fieldList = new ArrayList<String>();
		for(FieldMapping map : mappings)
			if (!fieldList.contains(map.source_field))
				fieldList.add(map.source_field);

		String [] fieldStringList = new String[fieldList.size()];
		fieldStringList = fieldList.toArray(fieldStringList);

		ObjectAdapter modelAdapter = new ObjectAdapter(openERPConnection, model);
		return modelAdapter.searchAndReadObject(filter, fieldStringList, offset, limit, null);
	}
	
	public RowCollection getModelData(String model, FilterCollection filter, String [] fieldStringList) throws XmlRpcException, OpeneERPApiException{
		ObjectAdapter modelAdapter = new ObjectAdapter(openERPConnection, model);
		return modelAdapter.searchAndReadObject(filter, fieldStringList);
	}

	public String [] getOutputFields(String model) throws MalformedURLException, XmlRpcException, OpeneERPApiException{
		ObjectAdapter modelAdapter = new ObjectAdapter(openERPConnection, model);
		FieldCollection fields = modelAdapter.getFields();

		ArrayList<String> fieldArray = new ArrayList<String>();
		for(Field field : fields){
			if (field.getType() == FieldType.ONE2MANY
					|| field.getReadonly() == true)
				continue;

			fieldArray.add(field.getName());
		}

		// Sort the fields alphabetically
		Collections.sort(fieldArray);
		
		return fieldArray.toArray(new String[fieldArray.size()]);
	}

	public ObjectAdapter getAdapter(String objectName) throws XmlRpcException, OpeneERPApiException{
		return openERPConnection.getObjectAdapter(objectName);
	}

	public void deleteObjects(String model, ArrayList<Object> ids) throws XmlRpcException{
		commands.unlinkObject(model, ids.toArray(new Object[ids.size()]));
	}

	public ArrayList<FieldMapping> getDefaultFieldMappings(String model) throws Exception{

		ArrayList<FieldMapping> mappings = new ArrayList<FieldMapping>();
		ObjectAdapter adapter = new ObjectAdapter(openERPConnection, model);
		FieldCollection fields = adapter.getFields();

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
}
