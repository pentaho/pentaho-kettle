/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.trans.steps.rules;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.steps.rules.RulesMeta;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.util.AbstractModelList;

public class RulesMetaMapper extends XulEventSourceAdapter {
	// Setup properties for holding dialog data
	protected String ruleFile;
	protected String ruleDefinition;
	protected String keepInputFields = "false";
	protected String ruleSource;
	
	protected List<Column> columnList = new AbstractModelList<Column>();
	
	// Create classes for UI mappings
	public class Column extends XulEventSourceAdapter {
		private String name = "";
		private String type = "";
		
		public void setName(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getType() {
			return type;
		}
		
		public Vector<String> getTypeList() {
			return new Vector<String>(Arrays.asList(ValueMeta.getTypes()));
		}
	}
	
	// Expose the properties to XUL
	public String getRuleFile() {
		return ruleFile;
	}
	public void setRuleFile(String ruleFile) {
		this.ruleFile = ruleFile;
	}
	
	public String getRuleDefinition() {
		return ruleDefinition;
	}
	public void setRuleDefinition(String ruleDefinition) {
		this.ruleDefinition = ruleDefinition;
	}
	
	public String getRuleSource() {
		return ruleSource;
	}
	public void setRuleSource(String ruleSource) {
		this.ruleSource = ruleSource;
	}
	
	public void setKeepInputFields(String keepInputFields) {
		this.keepInputFields = keepInputFields;
	}
	public String getKeepInputFields() {
		return keepInputFields;
	}
	
	public List<Column> getColumnList() {
		return columnList;
	}
	
	// Utility methods for UI
	public void addNewRow() {
	    getColumnList().add(new Column());
	}
	
	/**
	 * Load data into the MetaMapper from the RulesMeta
	 * @param meta
	 */
	public void loadMeta(RulesMeta meta) {
		setRuleFile(meta.getRuleFile());
		setRuleDefinition(meta.getRuleDefinition());
		setKeepInputFields(Boolean.toString(meta.isKeepInputFields()));
		
		for(ValueMetaInterface vm : meta.getRuleResultColumns()) {
			Column c = new Column();
			c.setName(vm.getName());
			c.setType(vm.getTypeDesc());
			
			getColumnList().add(c);
		}
	}
	
	/**
	 * Save data from the MetaMapper into the RulesMeta
	 * @param meta
	 */
	public void saveMeta(RulesMeta meta) {
		if(ruleSource != null && ruleSource.equalsIgnoreCase("file")) {
			if(meta.getRuleFile() != null && !meta.getRuleFile().equals(getRuleFile())
					|| (meta.getRuleFile() != getRuleFile())
					|| meta.getRuleDefinition() != null) {
				meta.setRuleFile(getRuleFile());
				meta.setRuleDefinition(null);
				meta.setChanged();
			}
		} else if(ruleSource != null && ruleSource.equalsIgnoreCase("definition")) {
			if(meta.getRuleDefinition() != null && !meta.getRuleDefinition().equals(getRuleDefinition())
					|| (meta.getRuleDefinition() != getRuleDefinition())
					|| meta.getRuleFile() != null) {
				meta.setRuleDefinition(getRuleDefinition());
				meta.setRuleFile(null);
				meta.setChanged();
			}
		}
		
		ValueMetaInterface vm = null;
		Column c = null;
		for(int i = 0; i < getColumnList().size(); i++) {
			vm = i < meta.getRuleResultColumns().size() ? meta.getRuleResultColumns().get(i) : null;
			c = getColumnList().get(i);
			if(c != null) {
				if(c.getName() != null) {
					// The column has a name and is valid for insertion
					if(vm == null) {
						vm = new ValueMeta();
						meta.getRuleResultColumns().add(vm);
						meta.setChanged();
					}
		
					if(!c.getName().equals(vm.getName())) {
						vm.setName(c.getName());
						meta.setChanged();
					}
					if(c.getType() != null && !c.getType().equals(vm.getTypeDesc())
							|| (c.getType() != vm.getTypeDesc())) {
						vm.setType(ValueMeta.getType(c.getType()));
						meta.setChanged();
					}
				} else {
					// The column does not have a name and should be removed or skipped over
					if(vm != null) {
						// This item exists in the meta; remove it
						if(i < meta.getRuleResultColumns().size()) {
							meta.getRuleResultColumns().remove(i);
						}
					}
					// Remove the item from column list table
					getColumnList().remove(i);
					// All items have shifted after the removal; Process this position again
					i--;
				}
			}
		}
	}
}
