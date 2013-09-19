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

public class StepFieldNode extends XulEventSourceAdapter {

	private String fieldName;
	private String type;
	private String length;
	private String precision;
	private String origin;
	private String storageType;
	private String conversionMask;
	private String decimalSymbol;
	private String groupingSymbol;
	private String trimType;
	private String comments;

	public String getFieldName() {
		return this.fieldName;
	}

	public void setFieldName(String aFieldName) {
		this.fieldName = aFieldName;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String aType) {
		this.type = aType;
	}

	public String getLength() {
		return this.length;
	}

	public void setLength(String aLength) {
		this.length = aLength;
	}

	public String getPrecision() {
		return this.precision;
	}

	public void setPrecision(String aPrecision) {
		this.precision = aPrecision;
	}

	public String getOrigin() {
		return this.origin;
	}

	public void setOrigin(String aOrigin) {
		this.origin = aOrigin;
	}

	public String getStorageType() {
		return this.storageType;
	}

	public void setStorageType(String aStorageType) {
		this.storageType = aStorageType;
	}

	public String getConversionMask() {
		return this.conversionMask;
	}

	public void setConversionMask(String aConversionMask) {
		this.conversionMask = aConversionMask;
	}

	public String getDecimalSymbol() {
		return this.decimalSymbol;
	}

	public void setDecimalSymbol(String aDecimalSymbol) {
		this.decimalSymbol = aDecimalSymbol;
	}

	public String getGroupingSymbol() {
		return this.groupingSymbol;
	}

	public void setGroupingSymbol(String aGroupingSymbol) {
		this.groupingSymbol = aGroupingSymbol;
	}

	public String getTrimType() {
		return this.trimType;
	}

	public void setTrimType(String aTrimType) {
		this.trimType = aTrimType;
	}

	public String getComments() {
		return this.comments;
	}

	public void setComments(String aComments) {
		this.comments = aComments;
	}
}