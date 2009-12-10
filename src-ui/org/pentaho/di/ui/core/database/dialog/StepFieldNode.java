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