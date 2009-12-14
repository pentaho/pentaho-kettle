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
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 * 
 * Author: Ezequiel Cuellar
 */
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
	private String table;
	private BindingFactory bf;
	private Binding stepFieldsTreeBinding;
	private Binding stepNameBinding;
	private Binding acceptButtonBinding;
	private XulTree stepFieldsTree;
	private XulStepFieldsModel model;
	private String selectedStep;
	private Boolean showAcceptButton;

	private static Log logger = LogFactory.getLog(XulStepFieldsController.class);

	public XulStepFieldsController(Shell aShell, DatabaseMeta aDatabaseMeta, String aTable) {
		this.shell = aShell;
		this.databaseMeta = aDatabaseMeta;
		this.table = aTable;
		this.bf = new DefaultBindingFactory();
		this.model = new XulStepFieldsModel();
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

		String theSql = this.databaseMeta.getSQLQueryFields(this.table);
		GetQueryFieldsProgressDialog theProgressDialog = new GetQueryFieldsProgressDialog(this.shell, this.databaseMeta, theSql);
		RowMetaInterface theRowMeta = theProgressDialog.open();

		this.model.setStepName("Step name:" + this.table);

		if (theRowMeta != null) {
			StepFieldNode theStep = null;
			for (int i = 0; i < theRowMeta.size(); i++) {
				theStep = new StepFieldNode();
				ValueMetaInterface theMetaInterface = theRowMeta.getValueMeta(i);
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
			this.selectedStep = theSelectedStep.getFieldName();
			theStepsDialog.hide();
		}
	}

	public String getSelectedStep() {
		return this.selectedStep;
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
