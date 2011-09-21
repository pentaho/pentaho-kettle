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
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

@Step(id = "OpenERPObjectOutputImport", 
		image = "OpenERPObjectOutput.png", 
		i18nPackageName="org.pentaho.di.trans.steps.openerp.objectoutput", 
		name = "OpenERPObjectOutput.TransName",
		description="OpenERPObjectOutput.TransDescription",
		categoryDescription="i18n:org.pentaho.di.trans.step:BaseStep.Category.OpenERP")
		public class OpenERPObjectOutputMeta extends BaseStepMeta implements StepMetaInterface{

	private DatabaseMeta databaseMeta;
	private String modelName;
	private int commitBatchSize = 100;
	private String[] modelFields = new String[0];
	private String[] streamFields = new String[0];
	private boolean outputIDField = false;
	private String outputIDFieldName = "";
	private ArrayList<String[]> keyLookups = new ArrayList<String[]>();
	
	@Override
	public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
    {
		if (outputIDField){
			if (outputIDFieldName == null || outputIDFieldName.length() == 0)
				throw new KettleStepException("Error while retrieving fields",new Exception("ID field name is null"));
			
			ValueMetaInterface v = new ValueMeta(outputIDFieldName, ValueMetaInterface.TYPE_INTEGER);
	        v.setOrigin(name);
	        
	        row.addValueMeta( v );
		}
    }
	
	@Override
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
			StepMeta stepMeta, RowMetaInterface prev, String[] input,
			String[] output, RowMetaInterface info) {
		// TODO Auto-generated method stub

	}

	@Override
	public StepInterface getStep(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans) {
		return new OpenERPObjectOutput(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	@Override
	public StepDataInterface getStepData() {
		try {
			return new OpenERPObjectOutputData(this.databaseMeta);
		} catch (Exception e) {
			return null;
		}
	}

	public final String getXML() {
		StringBuffer retval = new StringBuffer();

		retval.append("    ").append(XMLHandler.addTagValue("connection", this.databaseMeta == null ? "": this.databaseMeta.getName()));
		retval.append("    ").append(XMLHandler.addTagValue("modelName", this.modelName));
		retval.append("    ").append(XMLHandler.addTagValue("readBatchSize", this.commitBatchSize));
		retval.append("    ").append(XMLHandler.addTagValue("outputIDField", this.outputIDField));
		retval.append("    ").append(XMLHandler.addTagValue("outputIDFieldName", this.outputIDFieldName));
		
		retval.append("    <mappings>").append(Const.CR);
		for (int i = 0; i < modelFields.length; i++) {
			retval.append("      <mapping>").append(Const.CR);
			retval.append("        ").append(XMLHandler.addTagValue("model_field",modelFields[i]));
			retval.append("        ").append(XMLHandler.addTagValue("stream_field",streamFields[i]));
			retval.append("      </mapping>").append(Const.CR);
		}
		retval.append("    </mappings>").append(Const.CR);
		
		retval.append("    <key_mappings>").append(Const.CR);
		for (int i = 0; i < keyLookups.size(); i++) {
			retval.append("      <key_map>").append(Const.CR);
			retval.append("        ").append(XMLHandler.addTagValue("model_key_field",keyLookups.get(i)[0]));
			retval.append("        ").append(XMLHandler.addTagValue("comparitor",keyLookups.get(i)[1]));
			retval.append("        ").append(XMLHandler.addTagValue("stream_key_field",keyLookups.get(i)[2]));
			retval.append("      </key_map>").append(Const.CR);
		}
		retval.append("    </key_mappings>").append(Const.CR);

		return retval.toString();
	}

	@Override
	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode, databases);
	}

	private void allocate(int size){
		modelFields = new String[size];
		streamFields = new String[size];
	}

	@Override
	public void readRep(Repository rep, ObjectId idStep,
			List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException {
		try {
			this.databaseMeta = rep.loadDatabaseMetaFromStepAttribute(idStep, "connection", databases);
			this.modelName = rep.getStepAttributeString(idStep, "modelName");
			this.commitBatchSize = Integer.parseInt(rep.getStepAttributeString(idStep, "readBatchSize"));
			this.outputIDField = rep.getStepAttributeBoolean(idStep, "outputIDField");
			this.outputIDFieldName = rep.getStepAttributeString(idStep, "outputIDFieldName");

			int nrMappings = rep.countNrStepAttributes(idStep, "model_field");
			allocate(nrMappings);

			for (int i=0;i<nrMappings;i++) {
				modelFields[i] = rep.getStepAttributeString (idStep, i, "model_field");
				streamFields[i] = rep.getStepAttributeString (idStep, i, "stream_field");
			}
			
			nrMappings = rep.countNrStepAttributes(idStep, "model_key_field");
			allocate(nrMappings);

			keyLookups.clear();
			for (int i=0;i<nrMappings;i++) {
				String[] key_maps = new String[3];
				
				key_maps[0] = rep.getStepAttributeString (idStep, i, "model_key_field");
				key_maps[1] = rep.getStepAttributeString (idStep, i, "comparitor");
				key_maps[2] = rep.getStepAttributeString (idStep, i, "stream_key_field");
				
				keyLookups.add(key_maps);
			}

		} catch (Exception e) {
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	@Override
	public void saveRep(Repository rep, ObjectId idTransformation,
			ObjectId idStep) throws KettleException {
		try {
			rep.saveDatabaseMetaStepAttribute(idTransformation, idStep, "connection", this.databaseMeta);
			rep.saveStepAttribute(idTransformation, idStep, "modelName", this.modelName);
			rep.saveStepAttribute(idTransformation, idStep, "readBatchSize", this.commitBatchSize);
			rep.saveStepAttribute(idTransformation, idStep, "outputIDField", this.outputIDField);
			rep.saveStepAttribute(idTransformation, idStep, "outputIDFieldName", this.outputIDFieldName);

			for (int i=0;i < modelFields.length;i++) {
				rep.saveStepAttribute(idTransformation, idStep, i, "model_field", modelFields[i]);
				rep.saveStepAttribute(idTransformation, idStep, i, "stream_field", streamFields[i]);
			}
			
			for (int i = 0; i < keyLookups.size(); i++) {
				rep.saveStepAttribute(idTransformation, idStep, i, "model_key_field", keyLookups.get(i)[0]);
				rep.saveStepAttribute(idTransformation, idStep, i, "comparitor", keyLookups.get(i)[1]);
				rep.saveStepAttribute(idTransformation, idStep, i, "stream_key_field", keyLookups.get(i)[2]);
			}
		} catch (Exception e) {
			throw new KettleException("Unable to save step information to the repository for idStep=" + idStep, e);
		}
	}

	@Override
	public void setDefault() {
		// TODO Auto-generated method stub

	}

	private void readData(final Node stepnode, final List < ? extends SharedObjectInterface > databases)
	throws KettleXMLException {
		try {
			this.databaseMeta = DatabaseMeta.findDatabase(databases, XMLHandler.getTagValue(stepnode, "connection"));
			this.modelName = XMLHandler.getTagValue(stepnode, "modelName");
			this.commitBatchSize = Integer.parseInt(XMLHandler.getTagValue(stepnode, "readBatchSize"));
			this.outputIDField = XMLHandler.getTagValue(stepnode, "outputIDField").equals("Y") ? true : false;
			this.outputIDFieldName = XMLHandler.getTagValue(stepnode, "outputIDFieldName");

			Node mappings = XMLHandler.getSubNode(stepnode,"mappings");
			int nrLevels = XMLHandler.countNodes(mappings,"mapping");

			allocate(nrLevels);
			for (int i=0;i<nrLevels;i++) {

				Node fnode = XMLHandler.getSubNodeByNr(mappings, "mapping", i);

				modelFields[i] = XMLHandler.getTagValue(fnode, "model_field");
				streamFields[i] = XMLHandler.getTagValue(fnode, "stream_field");
			}
			
			Node keyMappings = XMLHandler.getSubNode(stepnode,"key_mappings");
			nrLevels = XMLHandler.countNodes(keyMappings,"key_map");

			keyLookups.clear();
			for (int i=0;i<nrLevels;i++) {

				String[] key_maps = new String[3];
				Node fnode = XMLHandler.getSubNodeByNr(keyMappings, "key_map", i);

				key_maps[0] = XMLHandler.getTagValue(fnode, "model_key_field");
				key_maps[1] = XMLHandler.getTagValue(fnode, "comparitor");
				key_maps[2] = XMLHandler.getTagValue(fnode, "stream_key_field");
				
				keyLookups.add(key_maps);
			}

		} catch (Exception e) {
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	public final void setDatabaseMeta(final DatabaseMeta database) {
		this.databaseMeta = database;
	}
	public final DatabaseMeta getDatabaseMeta() {
		return databaseMeta;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getModelName() {
		return modelName;
	}

	public void setCommitBatchSize(int commitBatchSize) {
		this.commitBatchSize = commitBatchSize;
	}

	public int getCommitBatchSize() {
		return commitBatchSize;
	}

	public void setModelFields(String[] modelFields) {
		this.modelFields = modelFields;
	}

	public String[] getModelFields() {
		return modelFields;
	}

	public void setStreamFields(String[] streamFields) {
		this.streamFields = streamFields;
	}

	public String[] getStreamFields() {
		return streamFields;
	}

	public void setKeyLookups(ArrayList<String[]> keyLookups) {
		this.keyLookups = keyLookups;
	}

	public ArrayList<String[]> getKeyLookups() {
		return keyLookups;
	}

	public void setOutputIDField(boolean outputIDField) {
		this.outputIDField = outputIDField;
	}

	public boolean getOutputIDField() {
		return outputIDField;
	}

	public void setOutputIDFieldName(String outputIDFieldName) {
		this.outputIDFieldName = outputIDFieldName;
	}

	public String getOutputIDFieldName() {
		return outputIDFieldName;
	}
}
