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
