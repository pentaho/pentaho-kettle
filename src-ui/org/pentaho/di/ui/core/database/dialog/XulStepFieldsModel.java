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
