/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.xmloutput;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class XMLOutputMetaTest {
  @BeforeClass
  public static void setUp() throws Exception {
    if ( !KettleClientEnvironment.isInitialized() ) {
      KettleClientEnvironment.init();
    }

  }

  @Test
  public void testLoadAndGetXml() throws Exception {
    XMLOutputMeta xmlOutputMeta = new XMLOutputMeta();
    Node stepnode = getTestNode();
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    xmlOutputMeta.loadXML( stepnode, Collections.singletonList( dbMeta ), metaStore );
    assertEquals( "xmlOutputFile", xmlOutputMeta.getFileName() );
    assertFalse( xmlOutputMeta.isDoNotOpenNewFileInit() );
    assertFalse( xmlOutputMeta.isServletOutput() );
    assertEquals( "pentaho.xml", xmlOutputMeta.getExtension() );
    assertTrue( xmlOutputMeta.isStepNrInFilename() );
    assertTrue( xmlOutputMeta.isDateInFilename() );
    assertTrue( xmlOutputMeta.isTimeInFilename() );
    assertFalse( xmlOutputMeta.isSpecifyFormat() );
    assertTrue( StringUtil.isEmpty( xmlOutputMeta.getDateTimeFormat() ) );
    assertFalse( xmlOutputMeta.isAddToResultFiles() );
    assertFalse( xmlOutputMeta.isZipped() );
    assertEquals( "UTF-8", xmlOutputMeta.getEncoding() );
    assertTrue( StringUtil.isEmpty( xmlOutputMeta.getNameSpace() ) );
    assertEquals( "Rows", xmlOutputMeta.getMainElement() );
    assertEquals( "Row", xmlOutputMeta.getRepeatElement() );
    assertEquals( 0, xmlOutputMeta.getSplitEvery() );
    assertTrue( xmlOutputMeta.isOmitNullValues() );
    XMLField[] outputFields = xmlOutputMeta.getOutputFields();
    assertEquals( 2, outputFields.length );

    assertEquals( "fieldOne", outputFields[0].getFieldName() );
    assertEquals( XMLField.ContentType.Element, outputFields[0].getContentType() );

    assertEquals( "fieldTwo", outputFields[1].getFieldName() );
    assertEquals( XMLField.ContentType.Attribute, outputFields[1].getContentType() );
    assertEquals( "    <encoding>UTF-8</encoding>\n"
        + "    <name_space/>\n"
        + "    <xml_main_element>Rows</xml_main_element>\n"
        + "    <xml_repeat_element>Row</xml_repeat_element>\n"
        + "    <file>\n"
        + "      <name>xmlOutputFile</name>\n"
        + "      <extention>pentaho.xml</extention>\n"
        + "      <servlet_output>N</servlet_output>\n"
        + "      <do_not_open_newfile_init>N</do_not_open_newfile_init>\n"
        + "      <split>Y</split>\n"
        + "      <add_date>Y</add_date>\n"
        + "      <add_time>Y</add_time>\n"
        + "      <SpecifyFormat>N</SpecifyFormat>\n"
        + "      <omit_null_values>Y</omit_null_values>\n"
        + "      <date_time_format/>\n"
        + "      <add_to_result_filenames>N</add_to_result_filenames>\n"
        + "      <zipped>N</zipped>\n"
        + "      <splitevery>0</splitevery>\n"
        + "    </file>\n"
        + "    <fields>\n"
        + "      <field>\n"
        + "        <content_type>Element</content_type>\n"
        + "        <name>fieldOne</name>\n"
        + "        <element/>\n"
        + "        <type>Number</type>\n"
        + "        <format/>\n"
        + "        <currency/>\n"
        + "        <decimal/>\n"
        + "        <group/>\n"
        + "        <nullif/>\n"
        + "        <length>-1</length>\n"
        + "        <precision>-1</precision>\n"
        + "      </field>\n"
        + "      <field>\n"
        + "        <content_type>Attribute</content_type>\n"
        + "        <name>fieldTwo</name>\n"
        + "        <element/>\n"
        + "        <type>String</type>\n"
        + "        <format/>\n"
        + "        <currency/>\n"
        + "        <decimal/>\n"
        + "        <group/>\n"
        + "        <nullif/>\n"
        + "        <length>-1</length>\n"
        + "        <precision>-1</precision>\n"
        + "      </field>\n"
        + "    </fields>\n", xmlOutputMeta.getXML() );
  }

  private Node getTestNode() throws KettleXMLException {
    String xml = "<step>\n"
        + "<name>My XML Output</name>\n"
        + "<type>XMLOutput</type>\n"
        + "<description/>\n"
        + "<distribute>Y</distribute>\n"
        + "<custom_distribution/>\n"
        + "<copies>1</copies>\n"
        + "<partitioning>\n"
        + "  <method>none</method>\n"
        + "  <schema_name/>\n"
        + "</partitioning>\n"
        + "<encoding>UTF-8</encoding>\n"
        + "<name_space/>\n"
        + "<xml_main_element>Rows</xml_main_element>\n"
        + "<xml_repeat_element>Row</xml_repeat_element>\n"
        + "<file>\n"
        + "  <name>xmlOutputFile</name>\n"
        + "  <extention>pentaho.xml</extention>\n"
        + "  <servlet_output>N</servlet_output>\n"
        + "  <do_not_open_newfile_init>N</do_not_open_newfile_init>\n"
        + "  <split>Y</split>\n"
        + "  <add_date>Y</add_date>\n"
        + "  <add_time>Y</add_time>\n"
        + "  <SpecifyFormat>N</SpecifyFormat>\n"
        + "  <omit_null_values>Y</omit_null_values>\n"
        + "  <date_time_format/>\n"
        + "  <add_to_result_filenames>N</add_to_result_filenames>\n"
        + "  <zipped>N</zipped>\n"
        + "  <splitevery>0</splitevery>\n"
        + "</file>\n"
        + "<fields>\n"
        + "  <field>\n"
        + "    <content_type>Element</content_type>\n"
        + "    <name>fieldOne</name>\n"
        + "    <element/>\n"
        + "    <type>Number</type>\n"
        + "    <format/>\n"
        + "    <currency/>\n"
        + "    <decimal/>\n"
        + "    <group/>\n"
        + "    <nullif/>\n"
        + "    <length>-1</length>\n"
        + "    <precision>-1</precision>\n"
        + "  </field>\n"
        + "  <field>\n"
        + "    <content_type>Attribute</content_type>\n"
        + "    <name>fieldTwo</name>\n"
        + "    <element/>\n"
        + "    <type>String</type>\n"
        + "    <format/>\n"
        + "    <currency/>\n"
        + "    <decimal/>\n"
        + "    <group/>\n"
        + "    <nullif/>\n"
        + "    <length>-1</length>\n"
        + "    <precision>-1</precision>\n"
        + "  </field>\n"
        + "</fields>\n"
        + "<cluster_schema/>\n"
        + "<remotesteps>   <input>   </input>   <output>   </output> </remotesteps>    <GUI>\n"
        + "<xloc>256</xloc>\n"
        + "<yloc>64</yloc>\n"
        + "<draw>Y</draw>\n"
        + "</GUI>\n"
        + "</step>\n";
    return XMLHandler.loadXMLString( xml, "step" );
  }

  @SuppressWarnings( "ConstantConditions" ) @Test
  public void testReadRep() throws Exception {
    XMLOutputMeta xmlOutputMeta = new XMLOutputMeta();
    Repository rep = mock( Repository.class );
    IMetaStore metastore = mock( IMetaStore.class );
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );

    String encoding = "UTF-8";
    String namespace = "";
    String mainElement = "rows";
    String repeatElement = "row";
    String fileName = "repFileName";
    StringObjectId oid = new StringObjectId( "oid" );
    String fileExtension = "repxml";
    boolean servletOutput = true;
    boolean newFile = true;
    long split = 100L;
    boolean addStepNbr = false;
    boolean addDate = false;
    boolean addTime = true;
    boolean specifyFormat = true;
    boolean omitNull = false;
    String dateTimeFormat = "yyyyMMdd";
    boolean addToResult = true;
    boolean zipped = true;
    String contentType = "Element";
    String fieldName = "aField";
    String fieldElement = "field";
    String fieldType = "String";
    long fieldLength = 20L;
    long fieldPrecision = 0L;

    when( rep.getStepAttributeString( oid, "encoding" ) ).thenReturn( encoding );
    when( rep.getStepAttributeString( oid, "name_space" ) ).thenReturn( namespace );
    when( rep.getStepAttributeString( oid, "xml_main_element" ) ).thenReturn( mainElement );
    when( rep.getStepAttributeString( oid, "xml_repeat_element" ) ).thenReturn( repeatElement );
    when( rep.getStepAttributeString( oid, "file_name" ) ).thenReturn( fileName );
    when( rep.getStepAttributeString( oid, "file_extention" ) ).thenReturn( fileExtension );
    when( rep.getStepAttributeBoolean( oid, "file_servlet_output" ) ).thenReturn( servletOutput );
    when( rep.getStepAttributeBoolean( oid, "do_not_open_newfile_init" ) ).thenReturn( newFile );
    when( rep.getStepAttributeInteger( oid, "file_split" ) ).thenReturn( split );
    when( rep.getStepAttributeBoolean( oid, "file_add_stepnr" ) ).thenReturn( addStepNbr );
    when( rep.getStepAttributeBoolean( oid, "file_add_date" ) ).thenReturn( addDate );
    when( rep.getStepAttributeBoolean( oid, "file_add_time" ) ).thenReturn( addTime );
    when( rep.getStepAttributeBoolean( oid, "SpecifyFormat" ) ).thenReturn( specifyFormat );
    when( rep.getStepAttributeBoolean( oid, "omit_null_values" ) ).thenReturn( omitNull );
    when( rep.getStepAttributeString( oid, "date_time_format" ) ).thenReturn( dateTimeFormat );
    when( rep.getStepAttributeBoolean( oid, "add_to_result_filenames" ) ).thenReturn( addToResult );
    when( rep.getStepAttributeBoolean( oid, "file_zipped" ) ).thenReturn( zipped );
    when( rep.countNrStepAttributes( oid, "field_name" ) ).thenReturn( 1 );
    when( rep.getStepAttributeString( oid, 0, "field_content_type" ) ).thenReturn( contentType );
    when( rep.getStepAttributeString( oid, 0, "field_name" ) ).thenReturn( fieldName );
    when( rep.getStepAttributeString( oid, 0, "field_element" ) ).thenReturn( fieldElement );
    when( rep.getStepAttributeString( oid, 0, "field_type" ) ).thenReturn( fieldType );
    when( rep.getStepAttributeString( oid, 0, "field_format" ) ).thenReturn( null );
    when( rep.getStepAttributeString( oid, 0, "field_currency" ) ).thenReturn( null );
    when( rep.getStepAttributeString( oid, 0, "field_decimal" ) ).thenReturn( null );
    when( rep.getStepAttributeString( oid, 0, "field_group" ) ).thenReturn( null );
    when( rep.getStepAttributeString( oid, 0, "field_nullif" ) ).thenReturn( null );
    when( rep.getStepAttributeInteger( oid, 0, "field_length" ) ).thenReturn( fieldLength );
    when( rep.getStepAttributeInteger( oid, 0, "field_precision" ) ).thenReturn( fieldPrecision );

    xmlOutputMeta.readRep( rep, metastore, oid, Collections.singletonList( dbMeta ) );

    assertEquals( fileName, xmlOutputMeta.getFileName() );
    assertTrue( xmlOutputMeta.isDoNotOpenNewFileInit() );
    assertTrue( xmlOutputMeta.isServletOutput() );
    assertEquals( fileExtension, xmlOutputMeta.getExtension() );
    assertFalse( xmlOutputMeta.isStepNrInFilename() );
    assertFalse( xmlOutputMeta.isDateInFilename() );
    assertTrue( xmlOutputMeta.isTimeInFilename() );
    assertTrue( xmlOutputMeta.isSpecifyFormat() );
    assertEquals( dateTimeFormat, xmlOutputMeta.getDateTimeFormat() );
    assertTrue( xmlOutputMeta.isAddToResultFiles() );
    assertTrue( xmlOutputMeta.isZipped() );
    assertEquals( encoding, xmlOutputMeta.getEncoding() );
    assertTrue( StringUtil.isEmpty( xmlOutputMeta.getNameSpace() ) );
    assertEquals( mainElement, xmlOutputMeta.getMainElement() );
    assertEquals( repeatElement, xmlOutputMeta.getRepeatElement() );
    assertEquals( split, xmlOutputMeta.getSplitEvery() );
    assertFalse( xmlOutputMeta.isOmitNullValues() );
    XMLField[] outputFields = xmlOutputMeta.getOutputFields();
    assertEquals( 1, outputFields.length );

    assertEquals( fieldName, outputFields[0].getFieldName() );
    assertEquals( XMLField.ContentType.Element, outputFields[0].getContentType() );
    assertEquals( fieldElement, outputFields[0].getElementName() );
    assertEquals( fieldLength, outputFields[0].getLength() );
    assertEquals( fieldPrecision, outputFields[0].getPrecision() );

    Mockito.reset( rep, metastore );
    StringObjectId transid = new StringObjectId( "transid" );
    xmlOutputMeta.saveRep( rep, metastore, transid, oid );
    verify( rep ).saveStepAttribute( transid, oid, "encoding", encoding );
    verify( rep ).saveStepAttribute( transid, oid, "name_space", namespace );
    verify( rep ).saveStepAttribute( transid, oid, "xml_main_element", mainElement );
    verify( rep ).saveStepAttribute( transid, oid, "xml_repeat_element", repeatElement );
    verify( rep ).saveStepAttribute( transid, oid, "file_name", fileName );
    verify( rep ).saveStepAttribute( transid, oid, "file_extention", fileExtension );
    verify( rep ).saveStepAttribute( transid, oid, "file_servlet_output", servletOutput );
    verify( rep ).saveStepAttribute( transid, oid, "do_not_open_newfile_init", newFile );
    verify( rep ).saveStepAttribute( transid, oid, "file_split", split );
    verify( rep ).saveStepAttribute( transid, oid, "file_add_stepnr", addStepNbr );
    verify( rep ).saveStepAttribute( transid, oid, "file_add_date", addDate );
    verify( rep ).saveStepAttribute( transid, oid, "file_add_time", addTime );
    verify( rep ).saveStepAttribute( transid, oid, "SpecifyFormat", specifyFormat );
    verify( rep ).saveStepAttribute( transid, oid, "omit_null_values", omitNull );
    verify( rep ).saveStepAttribute( transid, oid, "date_time_format", dateTimeFormat );
    verify( rep ).saveStepAttribute( transid, oid, "add_to_result_filenames", addToResult );
    verify( rep ).saveStepAttribute( transid, oid, "file_zipped", zipped );

    verify( rep ).saveStepAttribute( transid, oid, 0, "field_content_type", contentType );
    verify( rep ).saveStepAttribute( transid, oid, 0, "field_name", fieldName );
    verify( rep ).saveStepAttribute( transid, oid, 0, "field_element", fieldElement );
    verify( rep ).saveStepAttribute( transid, oid, 0, "field_type", fieldType );
    verify( rep ).saveStepAttribute( transid, oid, 0, "field_format", null );
    verify( rep ).saveStepAttribute( transid, oid, 0, "field_currency", null );
    verify( rep ).saveStepAttribute( transid, oid, 0, "field_decimal", null );
    verify( rep ).saveStepAttribute( transid, oid, 0, "field_group", null );
    verify( rep ).saveStepAttribute( transid, oid, 0, "field_nullif", null );
    verify( rep ).saveStepAttribute( transid, oid, 0, "field_length", fieldLength );
    verify( rep ).saveStepAttribute( transid, oid, 0, "field_precision", fieldPrecision );


    Mockito.verifyNoMoreInteractions( rep, metastore );
  }
}
