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

package org.pentaho.di.ui.core.database.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class XulStepFieldsController extends AbstractXulEventHandler {

	private Shell shell;
	private DatabaseMeta databaseMeta;
	private String schemaTableCombo;
	private BindingFactory bf;
	private Binding stepFieldsTreeBinding;
	private Binding stepNameBinding;
	private Binding acceptButtonBinding;
	private XulTree stepFieldsTree;
	private XulStepFieldsModel model;
	private Boolean showAcceptButton;
	private RowMetaInterface rowMetaInterface;

	private static Log logger = LogFactory.getLog(XulStepFieldsController.class);

	public XulStepFieldsController(Shell aShell, DatabaseMeta aDatabaseMeta, String schemaTableCombo, RowMetaInterface anInput) {
		this.shell = aShell;
		this.databaseMeta = aDatabaseMeta;
		this.schemaTableCombo = schemaTableCombo;
		this.bf = new DefaultBindingFactory();
		this.model = new XulStepFieldsModel();
		this.rowMetaInterface = anInput;
	}

	public void init() {
		createStepFieldNodes();

		this.bf.setDocument(super.document);
		this.bf.setBindingType(Type.ONE_WAY);

		this.stepFieldsTree = (XulTree) super.document.getElementById("step_fields_data");
		this.stepFieldsTreeBinding = this.bf.createBinding(this.model, "stepFields", this.stepFieldsTree, "elements");
		this.stepNameBinding = this.bf.createBinding(this.model, "stepName", "stepNameLabel", "value");
		this.acceptButtonBinding = this.bf.createBinding(this, "showAcceptButton", "stepFieldsDialog_accept", "visible");

		if (this.getShowAcceptButton()) {
			BindingConvertor<StepFieldNode, Boolean> isDisabledConvertor = new BindingConvertor<StepFieldNode, Boolean>() {
				public Boolean sourceToTarget(StepFieldNode value) {
					return !(value != null);
				}

				public StepFieldNode targetToSource(Boolean value) {
					return null;
				}
			};

			this.acceptButtonBinding = this.bf.createBinding(this.stepFieldsTree, "selectedItem", "stepFieldsDialog_accept", "disabled", isDisabledConvertor);
		}
		fireBindings();

	}

	public void cancelDialog() {
		XulDialog theDialog = (XulDialog) super.document.getElementById("stepFieldsDialog");
		theDialog.setVisible(false);
	}

	public void setShowAcceptButton(boolean isVisible) {
		this.showAcceptButton = isVisible;
	}

	public boolean getShowAcceptButton() {
		return this.showAcceptButton;
	}

	private void createStepFieldNodes() {

		if (this.rowMetaInterface == null) {
			String theSql = this.databaseMeta.getSQLQueryFields(this.schemaTableCombo);
			GetQueryFieldsProgressDialog theProgressDialog = new GetQueryFieldsProgressDialog(this.shell, this.databaseMeta, theSql);
			this.rowMetaInterface = theProgressDialog.open();
		}

		this.model.setStepName("Step name:" + this.schemaTableCombo);

		if (this.rowMetaInterface != null) {
			StepFieldNode theStep = null;
			for (int i = 0; i < this.rowMetaInterface.size(); i++) {
				theStep = new StepFieldNode();
				ValueMetaInterface theMetaInterface = this.rowMetaInterface.getValueMeta(i);
				theStep.setFieldName(theMetaInterface.getName());
				theStep.setType(theMetaInterface.getTypeDesc());
				theStep.setLength(Integer.toString(theMetaInterface.getLength()));
				theStep.setPrecision(Integer.toString(theMetaInterface.getPrecision()));
				theStep.setOrigin(theMetaInterface.getOrigin());
				theStep.setStorageType(Integer.toString(theMetaInterface.getStorageType()));
				theStep.setConversionMask(theMetaInterface.getConversionMask());
				theStep.setDecimalSymbol(theMetaInterface.getDecimalSymbol());
				theStep.setGroupingSymbol(theMetaInterface.getGroupingSymbol());
				theStep.setTrimType(Integer.toString(theMetaInterface.getTrimType()));
				theStep.setComments(theMetaInterface.getComments());
				this.model.addStepField(theStep);
			}
		}
	}

	public void editOriginStep() {
		StepFieldNode theSelectedStep = (StepFieldNode) this.stepFieldsTree.getSelectedItem();
		if (theSelectedStep != null) {
			XulDialog theStepsDialog = (XulDialog) document.getElementById("stepFieldsDialog");
			theStepsDialog.hide();
		}
	}

	public String getSelectedStep() {
		return this.schemaTableCombo;
	}

	public String getName() {
		return "stepFields";
	}

	private void fireBindings() {
		try {
			this.stepFieldsTreeBinding.fireSourceChanged();
			this.stepNameBinding.fireSourceChanged();
			this.acceptButtonBinding.fireSourceChanged();
		} catch (Exception e) {
			logger.info(e);
		}
	}
}
