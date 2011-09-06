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
 * Simple class to hold the field mapping information for the OpenERPObjectInput step
 * @author Pieter van der Merwe
 */
public class FieldMapping {
	
	public String source_model;
	public String source_field;
	public int source_index;
	public String target_model;
	public String target_field;
	public String target_field_label;
	public int target_field_type;  // org.pentaho.di.core.row.ValueMetaInterface
	
	public FieldMapping Clone(){
		FieldMapping copy = new FieldMapping();
		copy.source_model = this.source_model;
		copy.source_field = this.source_field;
		copy.source_index = this.source_index;
		copy.target_model = this.target_model;
		copy.target_field = this.target_field;
		copy.target_field_label = this.target_field_label;
		copy.target_field_type = this.target_field_type ;
		return copy;
	}
}
