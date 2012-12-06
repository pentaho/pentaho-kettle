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

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.util.AbstractModelList;

public class XulStepFieldsModel extends XulEventSourceAdapter {

	private String stepName;
	private FieldsCollection stepFields;

	public XulStepFieldsModel() {
		this.stepFields = new FieldsCollection();
	}

	public FieldsCollection getStepFields() {
		return this.stepFields;
	}

	public void setStepFields(FieldsCollection aStepFields) {
		this.stepFields = aStepFields;
	}

	public String toString() {
		return "Step Fields Node";
	}

	public void setStepName(String aStepName) {
		this.stepName = aStepName;
	}

	public String getStepName() {
		return this.stepName;
	}

	public void addStepField(StepFieldNode aStepField) {
		this.stepFields.add(aStepField);
	}

	public static class FieldsCollection extends AbstractModelList<StepFieldNode> {
    private static final long serialVersionUID = -2489107137334871323L;
	}
}
