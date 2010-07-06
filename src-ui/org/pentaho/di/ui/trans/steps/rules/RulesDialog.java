/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.di.ui.trans.steps.rules;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.rules.Rules;
import org.pentaho.di.trans.steps.rules.RulesMeta;
import org.pentaho.di.ui.trans.step.BaseStepXulDialog;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.components.XulRadio;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulTree;

public class RulesDialog extends BaseStepXulDialog implements StepDialogInterface {
	private RulesMetaMapper metaMapper;
	private String workingStepname;
	
	public RulesDialog( Shell parent, Object in, TransMeta tr, String sname ) {
	    super("org/pentaho/di/ui/trans/steps/rules/dialog.xul", parent, (BaseStepMeta) in, tr, sname);
	    init();
	}

	@Override
	protected final Class<?> getClassForMessages() {
		return Rules.class; // for i18n purposes
	}

	public void init() {
		workingStepname = stepname;
		
		metaMapper = new RulesMetaMapper();
		metaMapper.loadMeta((RulesMeta)baseStepMeta);
		
		// Set dialog values
		((XulTextbox)document.getElementById("step-name")).setValue(getStepName());
		((XulTextbox)document.getElementById("rule-file")).setValue(metaMapper.getRuleFile());
		((XulTextbox)document.getElementById("rule-definition")).setValue(metaMapper.getRuleDefinition());
		((XulTree)document.getElementById("fields-table")).setElements(metaMapper.getColumnList());

		// Set the initial dialog state
		if(metaMapper.getRuleDefinition() != null && !metaMapper.getRuleDefinition().equals("")) {
			setRuleSource("definition");
			((XulRadio)document.getElementById("rule-definition-radio-button")).setSelected(true);
		} else {
			setRuleSource("file");
			((XulRadio)document.getElementById("rule-file-radio-button")).setSelected(true);
		}
		
		// Bind data objects to UI
		bf.setBindingType(Binding.Type.ONE_WAY);
	    try {
	      bf.createBinding("step-name", "value", this, "stepName");
	      bf.createBinding("rule-file", "value", metaMapper, "ruleFile");
	      bf.createBinding("rule-definition", "value", metaMapper, "ruleDefinition");
	      bf.createBinding(metaMapper.getColumnList(), "children", "fields-table", "elements").fireSourceChanged();
	      // TODO: Add converter to clear out opposing text box
	      bf.createBinding("rule-file-radio-button", "selected", "rule-file", "!disabled").fireSourceChanged();
	      bf.createBinding("rule-file-radio-button", "selected", "rule-definition", "disabled").fireSourceChanged();
	      
	      bf.createBinding("rule-definition-radio-button", "selected", "rule-definition", "!disabled").fireSourceChanged();
	      bf.createBinding("rule-definition-radio-button", "selected", "rule-file", "disabled").fireSourceChanged();
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	}
	
	public void setStepName(String stepname) {
		workingStepname = stepname;
	}
	
	public String getStepName() {
		return workingStepname;
	}
	
	public void setRuleSource(String ruleSource) {
		metaMapper.setRuleSource(ruleSource);
	}
	
	public void addNewRow() {
		metaMapper.addNewRow();
	}

	@Override
	public void onAccept() {
		if(!workingStepname.equals(stepname)) {
			stepname = workingStepname;
			baseStepMeta.setChanged();
		}
		
		metaMapper.saveMeta((RulesMeta)baseStepMeta);
		dispose();
	}

	@Override
	public void onCancel() {
		setStepName(null);
		dispose();
	}
}
