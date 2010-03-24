/* Copyright (c) 2010 Aschauer EDV GmbH.  All rights reserved. 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This software was developed by Aschauer EDV GmbH and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 * 
 * Please contact Aschauer EDV GmbH www.aschauer-edv.at if you need additional
 * information or have any questions.
 * 
 * @author  Robert Wintner robert.wintner@aschauer-edv.at
 * @since   PDI 4.0
 */

package org.pentaho.di.trans.steps.sapinput.sap;

import java.util.Collection;
import java.util.Vector;

public class SAPRow {
	
	private Collection<SAPField> fields = new Vector<SAPField>();

	public SAPRow() {
		super();
	}

	public SAPRow(Collection<SAPField> fields) {
		super();
		this.fields = fields;
	}

	@Override
	public String toString() {
		return "SAPRow [field=" + fields + "]";
	}

	public Collection<SAPField> getFields() {
		return fields;
	}

	public void setField(Collection<SAPField> fields) {
		this.fields = fields;
	}

	public void addField(SAPField field) {
		this.fields.add(field);
	}
		
}
