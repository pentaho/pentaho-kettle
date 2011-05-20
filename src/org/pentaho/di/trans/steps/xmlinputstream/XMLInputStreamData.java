/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.xmlinputstream;

import java.io.FileInputStream;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;

/**
 * @author Jens Bleuel
 * @since 2011-01-13
 */
public class XMLInputStreamData extends BaseStepData implements StepDataInterface
{

	public RowMetaInterface outputRowMeta;

	public XMLInputFactory staxInstance;

	public FileObject fileObject;
	public FileInputStream fileInputStream;
	public XMLEventReader xmlEventReader;

	// from meta data but replaced by variables
	public String filename;
	public long nrRowsToSkip;
	public long rowLimit;
	public String encoding;

	// runtime data
	public Long rowNumber;
	public int elementLevel;
	public Long elementID;
	public Long elementLevelID[];
	public Long elementParentID[];
	public String elementName[];
	public String elementPath[];

	// positions of fields in the row (-1: field is not included in the stream)
	public int pos_xml_filename;
	public int pos_xml_row_number;
	public int pos_xml_data_type_numeric;
	public int pos_xml_data_type_description;
	public int pos_xml_location_line;
	public int pos_xml_location_column;
	public int pos_xml_element_id;
	public int pos_xml_parent_element_id;
	public int pos_xml_element_level;
	public int pos_xml_path;
	public int pos_xml_parent_path;
	public int pos_xml_data_name;
	public int pos_xml_data_value;

	public XMLInputStreamData()
	{
		super();
	}
}
