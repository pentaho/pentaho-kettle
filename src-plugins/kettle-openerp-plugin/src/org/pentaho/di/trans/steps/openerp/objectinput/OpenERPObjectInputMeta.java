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
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.openerp.core.FieldMapping;
import org.pentaho.di.openerp.core.OpenERPHelper;
import org.pentaho.di.openerp.core.ReadFilter;
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

@Step(id = "OpenERPObjectInput", 
		image = "OpenERPObjectInput.png", 
		i18nPackageName="org.pentaho.di.trans.steps.openerp.objectinput", 
		name = "OpenERPObjectInput.TransName",
		description="OpenERPObjectInput.TransDescription",
		categoryDescription="i18n:org.pentaho.di.trans.step:BaseStep.Category.OpenERP")
public class OpenERPObjectInputMeta extends BaseStepMeta implements StepMetaInterface{

	private DatabaseMeta databaseMeta;
	private String modelName;
	private int readBatchSize = 1000;
	private ArrayList<ReadFilter> filterList = new ArrayList<ReadFilter>();
	private ArrayList<FieldMapping> mappings = new ArrayList<FieldMapping>();

	public void getFields(final RowMetaInterface row, final String origin, 
			final RowMetaInterface[] info, final StepMeta nextStep, 
			final VariableSpace space) throws KettleStepException {

		if (databaseMeta == null) 
			throw new KettleStepException("There is no OpenERP database server connection defined");

		final OpenERPHelper helper = new OpenERPHelper(databaseMeta);
		try {
			helper.StartSession();
			final RowMetaInterface rowMeta = helper.getFieldRowMeta(mappings);
			row.addRowMeta(rowMeta);
		} catch (Exception e) {
			throw new KettleStepException(e);
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
		return new OpenERPObjectInput(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	@Override
	public StepDataInterface getStepData() {
		try {
			return new OpenERPObjectInputData(this.databaseMeta);
		} catch (Exception e) {
			return null;
		}
	}

	public final String getXML() {
		StringBuffer retval = new StringBuffer();

		retval.append("    ").append(XMLHandler.addTagValue("connection", this.databaseMeta == null ? "": this.databaseMeta.getName()));
		retval.append("    ").append(XMLHandler.addTagValue("modelName", this.modelName));
		retval.append("    ").append(XMLHandler.addTagValue("readBatchSize", this.readBatchSize));
		
		retval.append("    <mappings>").append(Const.CR);
        for (FieldMapping map : this.getMappings()) {
            retval.append("      <mapping>").append(Const.CR);
            retval.append("        ").append(XMLHandler.addTagValue("source_model",map.source_model));
            retval.append("        ").append(XMLHandler.addTagValue("source_field",map.source_field));
            retval.append("        ").append(XMLHandler.addTagValue("source_index",map.source_index));
            retval.append("        ").append(XMLHandler.addTagValue("target_model",map.target_model));
            retval.append("        ").append(XMLHandler.addTagValue("target_field",map.target_field));
            retval.append("        ").append(XMLHandler.addTagValue("target_field_label",map.target_field_label));
            retval.append("        ").append(XMLHandler.addTagValue("target_field_type",map.target_field_type));
            retval.append("      </mapping>").append(Const.CR);
        }
        retval.append("    </mappings>").append(Const.CR);
        
        retval.append("    <filters>").append(Const.CR);
        for (ReadFilter filter : this.getFilterList()) {
            retval.append("      <filter>").append(Const.CR);
            retval.append("        ").append(XMLHandler.addTagValue("field_name",filter.field_name));
            retval.append("        ").append(XMLHandler.addTagValue("operator",filter.operator));
            retval.append("        ").append(XMLHandler.addTagValue("value",filter.value));
            retval.append("      </filter>").append(Const.CR);
        }
        retval.append("    </filters>").append(Const.CR);
		
		return retval.toString();
	}

	@Override
	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode, databases);
	}

	@Override
	public void readRep(Repository rep, ObjectId idStep,
			List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException {
		try {
			this.databaseMeta = rep.loadDatabaseMetaFromStepAttribute(idStep, "connection", databases);
			this.modelName = rep.getStepAttributeString(idStep, "modelName");
			this.readBatchSize = Integer.parseInt(rep.getStepAttributeString(idStep, "readBatchSize"));
			
			int nrMappings = rep.countNrStepAttributes(idStep, "source_model");
            
            for (int i=0;i<nrMappings;i++) {
            	FieldMapping map = new FieldMapping();
            	
            	map.source_model = rep.getStepAttributeString (idStep, i, "source_model");
            	map.source_field = rep.getStepAttributeString (idStep, i, "source_field");
            	map.source_index = Integer.valueOf(rep.getStepAttributeString (idStep, i, "source_index"));
            	map.target_model = rep.getStepAttributeString (idStep, i, "target_model");
            	map.target_field = rep.getStepAttributeString (idStep, i, "target_field");
            	map.target_field_label = rep.getStepAttributeString (idStep, i, "target_field_label");
            	map.target_field_type = Integer.valueOf(rep.getStepAttributeString (idStep, i, "target_field_type")); 
            	
            	this.getMappings().add(map);
            }
            
            int nrFilters = rep.countNrStepAttributes(idStep, "field_name");
            for (int i=0;i<nrFilters;i++) {
            	ReadFilter filter = new ReadFilter();
            	
            	filter.field_name = rep.getStepAttributeString (idStep, i, "field_name");
            	filter.operator = rep.getStepAttributeString (idStep, i, "operator");
            	filter.value = rep.getStepAttributeString (idStep, i, "value");
            	
            	this.getFilterList().add(filter);
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
			rep.saveStepAttribute(idTransformation, idStep, "readBatchSize", this.readBatchSize);
			
			for (int i=0;i<getMappings().size();i++) {
				FieldMapping map = this.getMappings().get(i);
                rep.saveStepAttribute(idTransformation, idStep, i, "source_model", map.source_model);
                rep.saveStepAttribute(idTransformation, idStep, i, "source_field", map.source_field);
                rep.saveStepAttribute(idTransformation, idStep, i, "source_index", map.source_index);
                rep.saveStepAttribute(idTransformation, idStep, i, "target_model", map.target_model);
                rep.saveStepAttribute(idTransformation, idStep, i, "target_field", map.target_field);
                rep.saveStepAttribute(idTransformation, idStep, i, "target_field_label", map.target_field_label);
                rep.saveStepAttribute(idTransformation, idStep, i, "target_field_type", map.target_field_type);
            }
			
			for (int i=0;i<getFilterList().size();i++) {
				ReadFilter filter = this.getFilterList().get(i);
                rep.saveStepAttribute(idTransformation, idStep, i, "field_name", filter.field_name);
                rep.saveStepAttribute(idTransformation, idStep, i, "operator", filter.operator);
                rep.saveStepAttribute(idTransformation, idStep, i, "value", filter.value);
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
			this.readBatchSize = Integer.parseInt(XMLHandler.getTagValue(stepnode, "readBatchSize"));
			
			this.setMappings(new ArrayList <FieldMapping>());
            
            Node mappings = XMLHandler.getSubNode(stepnode,"mappings");
            int nrLevels = XMLHandler.countNodes(mappings,"mapping");

            for (int i=0;i<nrLevels;i++) {
            	FieldMapping map = new FieldMapping();
            	
            	Node fnode = XMLHandler.getSubNodeByNr(mappings, "mapping", i);
            	
            	map.source_model = XMLHandler.getTagValue(fnode, "source_model");
            	map.source_field = XMLHandler.getTagValue(fnode, "source_field");
            	map.source_index = Integer.parseInt(XMLHandler.getTagValue(fnode, "source_index"));
            	map.target_model = XMLHandler.getTagValue(fnode, "target_model");
            	map.target_field = XMLHandler.getTagValue(fnode, "target_field");
            	map.target_field_label = XMLHandler.getTagValue(fnode, "target_field_label");
            	map.target_field_type = Integer.parseInt(XMLHandler.getTagValue(fnode, "target_field_type")); 

            	this.getMappings().add(map);
            }
            
            Node filters = XMLHandler.getSubNode(stepnode,"filters");
            int nrFilters = XMLHandler.countNodes(filters,"filter");

            for (int i=0;i<nrFilters;i++) {
            	ReadFilter filter = new ReadFilter();
            	
            	Node fnode = XMLHandler.getSubNodeByNr(filters, "filter", i);
            	
            	filter.field_name = XMLHandler.getTagValue(fnode, "field_name");
            	filter.operator = XMLHandler.getTagValue(fnode, "operator");
            	filter.value = XMLHandler.getTagValue(fnode, "value");
            	
            	this.getFilterList().add(filter);
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

	public void setReadBatchSize(int readBatchSize) {
		this.readBatchSize = readBatchSize;
	}

	public int getReadBatchSize() {
		return readBatchSize;
	}

	public void setMappings(ArrayList<FieldMapping> mappings) {
		this.mappings = mappings;
	}

	public ArrayList<FieldMapping> getMappings() {
		return mappings;
	}

	public void setFilterList(ArrayList<ReadFilter> filterList) {
		this.filterList = filterList;
	}

	public ArrayList<ReadFilter> getFilterList() {
		return filterList;
	}
}
