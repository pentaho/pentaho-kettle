/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.xmlinputstream;

import java.io.InputStream;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Jens Bleuel
 * @since 2011-01-13
 */
public class XMLInputStreamData extends BaseStepData implements StepDataInterface {

  public RowMetaInterface outputRowMeta;
  public RowMetaInterface finalOutputRowMeta;

  public XMLInputFactory staxInstance;

  public FileObject fileObject;
  public InputStream inputStream;
  public XMLEventReader xmlEventReader;

  // from meta data but replaced by variables
  public String[] filenames;
  public int filenr;
  public long nrRowsToSkip;
  public long rowLimit;
  public String encoding;
  public int previousFieldsNumber = 0;
  public Map<String, Object[]> inputDataRows;
  public Object[] currentInputRow;

  // runtime data
  public Long rowNumber;
  public int elementLevel;
  public Long elementID;
  public Long[] elementLevelID;
  public Long[] elementParentID;
  public String[] elementName;
  public String[] elementPath;

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

  public XMLInputStreamData() {
    super();
  }
}
