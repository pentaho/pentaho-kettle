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

package org.pentaho.di.openerp.core;

/**
 * Simple class to hold the filter information for the OpenERPObjectInput step
 * @author Pieter van der Merwe
 *
 */
public class ReadFilter {
	private String operator = "";
	private String fieldName = "";
	private String comparator = "";
	private String value = "";
	
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		if (operator == null)
			operator = "";
		
		this.operator = operator;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		if (fieldName == null)
			fieldName = "";
		
		this.fieldName = fieldName;
	}
	public String getComparator() {
		return comparator;
	}
	public void setComparator(String comparator) {
		if (comparator == null)
			comparator = "";
		
		this.comparator = comparator;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		if (value == null)
			value = "";
		
		this.value = value;
	}
}
