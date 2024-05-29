/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.vfs2.FileObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    assertXmlOutputMeta( xmlOutputMeta );
  }

  private void assertXmlOutputMeta( XMLOutputMeta xmlOutputMeta ) {
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
    assertEquals( "    <encoding>UTF-8</encoding>" + Const.CR
        + "    <name_space/>" + Const.CR
        + "    <xml_main_element>Rows</xml_main_element>" + Const.CR
        + "    <xml_repeat_element>Row</xml_repeat_element>" + Const.CR
        + "    <file>" + Const.CR
        + "      <name>xmlOutputFile</name>" + Const.CR
        + "      <extention>pentaho.xml</extention>" + Const.CR
        + "      <servlet_output>N</servlet_output>" + Const.CR
        + "      <do_not_open_newfile_init>N</do_not_open_newfile_init>" + Const.CR
        + "      <split>Y</split>" + Const.CR
        + "      <add_date>Y</add_date>" + Const.CR
        + "      <add_time>Y</add_time>" + Const.CR
        + "      <SpecifyFormat>N</SpecifyFormat>" + Const.CR
        + "      <omit_null_values>Y</omit_null_values>" + Const.CR
        + "      <date_time_format/>" + Const.CR
        + "      <add_to_result_filenames>N</add_to_result_filenames>" + Const.CR
        + "      <zipped>N</zipped>" + Const.CR
        + "      <splitevery>0</splitevery>" + Const.CR
        + "    </file>" + Const.CR
        + "    <fields>" + Const.CR
        + "      <field>" + Const.CR
        + "        <content_type>Element</content_type>" + Const.CR
        + "        <name>fieldOne</name>" + Const.CR
        + "        <element/>" + Const.CR
        + "        <type>Number</type>" + Const.CR
        + "        <format/>" + Const.CR
        + "        <currency/>" + Const.CR
        + "        <decimal/>" + Const.CR
        + "        <group/>" + Const.CR
        + "        <nullif/>" + Const.CR
        + "        <length>-1</length>" + Const.CR
        + "        <precision>-1</precision>" + Const.CR
        + "      </field>" + Const.CR
        + "      <field>" + Const.CR
        + "        <content_type>Attribute</content_type>" + Const.CR
        + "        <name>fieldTwo</name>" + Const.CR
        + "        <element/>" + Const.CR
        + "        <type>String</type>" + Const.CR
        + "        <format/>" + Const.CR
        + "        <currency/>" + Const.CR
        + "        <decimal/>" + Const.CR
        + "        <group/>" + Const.CR
        + "        <nullif/>" + Const.CR
        + "        <length>-1</length>" + Const.CR
        + "        <precision>-1</precision>" + Const.CR
        + "      </field>" + Const.CR
        + "    </fields>" + Const.CR, xmlOutputMeta.getXML() );
  }

  private Node getTestNode() throws KettleXMLException {
    String xml = "<step>" + Const.CR
        + "<name>My XML Output</name>" + Const.CR
        + "<type>XMLOutput</type>" + Const.CR
        + "<description/>" + Const.CR
        + "<distribute>Y</distribute>" + Const.CR
        + "<custom_distribution/>" + Const.CR
        + "<copies>1</copies>" + Const.CR
        + "<partitioning>" + Const.CR
        + "  <method>none</method>" + Const.CR
        + "  <schema_name/>" + Const.CR
        + "</partitioning>" + Const.CR
        + "<encoding>UTF-8</encoding>" + Const.CR
        + "<name_space/>" + Const.CR
        + "<xml_main_element>Rows</xml_main_element>" + Const.CR
        + "<xml_repeat_element>Row</xml_repeat_element>" + Const.CR
        + "<file>" + Const.CR
        + "  <name>xmlOutputFile</name>" + Const.CR
        + "  <extention>pentaho.xml</extention>" + Const.CR
        + "  <servlet_output>N</servlet_output>" + Const.CR
        + "  <do_not_open_newfile_init>N</do_not_open_newfile_init>" + Const.CR
        + "  <split>Y</split>" + Const.CR
        + "  <add_date>Y</add_date>" + Const.CR
        + "  <add_time>Y</add_time>" + Const.CR
        + "  <SpecifyFormat>N</SpecifyFormat>" + Const.CR
        + "  <omit_null_values>Y</omit_null_values>" + Const.CR
        + "  <date_time_format/>" + Const.CR
        + "  <add_to_result_filenames>N</add_to_result_filenames>" + Const.CR
        + "  <zipped>N</zipped>" + Const.CR
        + "  <splitevery>0</splitevery>" + Const.CR
        + "</file>" + Const.CR
        + "<fields>" + Const.CR
        + "  <field>" + Const.CR
        + "    <content_type>Element</content_type>" + Const.CR
        + "    <name>fieldOne</name>" + Const.CR
        + "    <element/>" + Const.CR
        + "    <type>Number</type>" + Const.CR
        + "    <format/>" + Const.CR
        + "    <currency/>" + Const.CR
        + "    <decimal/>" + Const.CR
        + "    <group/>" + Const.CR
        + "    <nullif/>" + Const.CR
        + "    <length>-1</length>" + Const.CR
        + "    <precision>-1</precision>" + Const.CR
        + "  </field>" + Const.CR
        + "  <field>" + Const.CR
        + "    <content_type>Attribute</content_type>" + Const.CR
        + "    <name>fieldTwo</name>" + Const.CR
        + "    <element/>" + Const.CR
        + "    <type>String</type>" + Const.CR
        + "    <format/>" + Const.CR
        + "    <currency/>" + Const.CR
        + "    <decimal/>" + Const.CR
        + "    <group/>" + Const.CR
        + "    <nullif/>" + Const.CR
        + "    <length>-1</length>" + Const.CR
        + "    <precision>-1</precision>" + Const.CR
        + "  </field>" + Const.CR
        + "</fields>" + Const.CR
        + "<cluster_schema/>" + Const.CR
        + "<remotesteps>   <input>   </input>   <output>   </output> </remotesteps>    <GUI>" + Const.CR
        + "<xloc>256</xloc>" + Const.CR
        + "<yloc>64</yloc>" + Const.CR
        + "<draw>Y</draw>" + Const.CR
        + "</GUI>" + Const.CR
        + "</step>" + Const.CR;
    return XMLHandler.loadXMLString( xml, "step" );
  }

  @SuppressWarnings( "ConstantConditions" )
  @Test
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

  @Test
  public void testGetNewline() throws Exception {
    XMLOutputMeta xmlOutputMeta = new XMLOutputMeta();
    assertEquals( "\r\n", xmlOutputMeta.getNewLine( "DOS" ) );
    assertEquals( "\n", xmlOutputMeta.getNewLine( "UNIX" ) );
    assertEquals( System.getProperty( "line.separator" ), xmlOutputMeta.getNewLine( null ) );
  }

  @Test
  public void testClone() throws Exception {
    XMLOutputMeta xmlOutputMeta = new XMLOutputMeta();
    Node stepnode = getTestNode();
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    xmlOutputMeta.loadXML( stepnode, Collections.singletonList( dbMeta ), metaStore );
    XMLOutputMeta cloned = (XMLOutputMeta) xmlOutputMeta.clone();
    assertNotSame( cloned, xmlOutputMeta );
    assertXmlOutputMeta( cloned );
  }

  @Test
  public void testSetDefault() throws Exception {
    XMLOutputMeta xmlOutputMeta = new XMLOutputMeta();
    xmlOutputMeta.setDefault();
    assertEquals( "file", xmlOutputMeta.getFileName() );
    assertEquals( "xml", xmlOutputMeta.getExtension() );
    assertFalse( xmlOutputMeta.isStepNrInFilename() );
    assertFalse( xmlOutputMeta.isDoNotOpenNewFileInit() );
    assertFalse( xmlOutputMeta.isDateInFilename() );
    assertFalse( xmlOutputMeta.isTimeInFilename() );
    assertFalse( xmlOutputMeta.isAddToResultFiles() );
    assertFalse( xmlOutputMeta.isZipped() );
    assertEquals( 0, xmlOutputMeta.getSplitEvery() );
    assertEquals( Const.XML_ENCODING, xmlOutputMeta.getEncoding() );
    assertEquals( "", xmlOutputMeta.getNameSpace() );
    assertNull( xmlOutputMeta.getDateTimeFormat() );
    assertFalse( xmlOutputMeta.isSpecifyFormat() );
    assertFalse( xmlOutputMeta.isOmitNullValues() );
    assertEquals( "Rows", xmlOutputMeta.getMainElement() );
    assertEquals( "Row", xmlOutputMeta.getRepeatElement() );
  }

  @Test
  public void testGetFiles() throws Exception {
    XMLOutputMeta xmlOutputMeta = new XMLOutputMeta();
    xmlOutputMeta.setDefault();
    xmlOutputMeta.setStepNrInFilename( true );
    xmlOutputMeta.setSplitEvery( 100 );
    xmlOutputMeta.setSpecifyFormat( true );
    xmlOutputMeta.setDateTimeFormat( "99" );
    String[] files = xmlOutputMeta.getFiles( new Variables() );
    assertEquals( 10, files.length );
    assertArrayEquals( new String[] { "file99_0_00001.xml", "file99_0_00002.xml", "file99_0_00003.xml",
      "file99_1_00001.xml", "file99_1_00002.xml", "file99_1_00003.xml", "file99_2_00001.xml", "file99_2_00002.xml",
      "file99_2_00003.xml", "..." }, files );
  }

  @Test
  public void testGetFields() throws Exception {
    XMLOutputMeta xmlOutputMeta = new XMLOutputMeta();
    xmlOutputMeta.setDefault();
    XMLField xmlField = new XMLField();
    xmlField.setFieldName( "aField" );
    xmlField.setLength( 10 );
    xmlField.setPrecision( 3 );
    xmlOutputMeta.setOutputFields( new XMLField[] { xmlField } );
    RowMetaInterface row = mock( RowMetaInterface.class );
    RowMetaInterface rmi = mock( RowMetaInterface.class );
    StepMeta nextStep = mock( StepMeta.class );
    Repository repo = mock( Repository.class );
    IMetaStore metastore = mock( IMetaStore.class );
    ValueMetaInterface vmi = mock( ValueMetaInterface.class );
    when( row.searchValueMeta( "aField" ) ).thenReturn( vmi );
    xmlOutputMeta.getFields( row, "", new RowMetaInterface[] { rmi }, nextStep, new Variables(), repo, metastore );
    verify( vmi ).setLength( 10, 3 );
  }

  @Test
  public void testLoadXmlException() throws Exception {
    XMLOutputMeta xmlOutputMeta = new XMLOutputMeta();
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    Node stepNode = mock( Node.class );
    when( stepNode.getChildNodes() ).thenThrow( new RuntimeException( "some words" ) );
    try {
      xmlOutputMeta.loadXML( stepNode, Collections.singletonList( dbMeta ), metaStore );
    } catch ( KettleXMLException e ) {
      assertEquals( "some words", e.getCause().getMessage() );
    }
  }

  @Test
  public void testReadRepException() throws Exception {
    XMLOutputMeta xmlOutputMeta = new XMLOutputMeta();
    Repository rep = mock( Repository.class );
    IMetaStore metastore = mock( IMetaStore.class );
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    ObjectId oid = new StringObjectId( "oid" );
    when( rep.getStepAttributeString( oid, "encoding" ) ).thenThrow( new RuntimeException( "encoding exception" ) );
    try {
      xmlOutputMeta.readRep( rep, metastore, oid, Collections.singletonList( dbMeta ) );
    } catch ( KettleException e ) {
      assertEquals( "encoding exception", e.getCause().getMessage() );
    }
  }

  @Test
  public void testGetRequiredFields() throws Exception {
    XMLOutputMeta xmlOutputMeta = new XMLOutputMeta();
    xmlOutputMeta.setDefault();
    XMLField xmlField = new XMLField();
    xmlField.setFieldName( "aField" );
    xmlField.setType( 1 );
    xmlField.setLength( 10 );
    xmlField.setPrecision( 3 );

    XMLField xmlField2 = new XMLField();
    xmlField2.setFieldName( "bField" );
    xmlField2.setType( 3 );
    xmlField2.setLength( 4 );
    xmlField2.setPrecision( 5 );
    xmlOutputMeta.setOutputFields( new XMLField[] { xmlField, xmlField2 } );
    RowMetaInterface requiredFields = xmlOutputMeta.getRequiredFields( new Variables() );
    List<ValueMetaInterface> valueMetaList = requiredFields.getValueMetaList();
    assertEquals( 2, valueMetaList.size() );
    assertEquals( "aField", valueMetaList.get( 0 ).getName() );
    assertEquals( 1, valueMetaList.get( 0 ).getType() );
    assertEquals( 10, valueMetaList.get( 0 ).getLength() );
    assertEquals( 3, valueMetaList.get( 0 ).getPrecision() );

    assertEquals( "bField", valueMetaList.get( 1 ).getName() );
    assertEquals( 3, valueMetaList.get( 1 ).getType() );
    assertEquals( 4, valueMetaList.get( 1 ).getLength() );
    assertEquals( 5, valueMetaList.get( 1 ).getPrecision() );
  }

  @Test
  public void testExportResources() throws Exception {
    XMLOutputMeta xmlOutputMeta = new XMLOutputMeta();
    xmlOutputMeta.setDefault();
    ResourceNamingInterface resourceNamingInterface = mock( ResourceNamingInterface.class );
    Variables space = new Variables();
    when( resourceNamingInterface.nameResource( any( FileObject.class ), eq( space ), eq( true ) ) ).thenReturn(
        "exportFile" );
    xmlOutputMeta.exportResources( space, null, resourceNamingInterface, null, null );
    assertEquals( "exportFile", xmlOutputMeta.getFileName() );
  }

  @Test
  public void testCheck() throws Exception {
    XMLOutputMeta xmlOutputMeta = new XMLOutputMeta();
    xmlOutputMeta.setDefault();
    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepInfo = mock( StepMeta.class );
    RowMetaInterface prev = mock( RowMetaInterface.class );
    Repository repos = mock( Repository.class );
    IMetaStore metastore = mock( IMetaStore.class );
    RowMetaInterface info = mock( RowMetaInterface.class );
    ArrayList<CheckResultInterface> remarks = new ArrayList<>();
    xmlOutputMeta.check( remarks, transMeta, stepInfo, prev, new String[] { "input" }, new String[] { "output" }, info,
        new Variables(), repos, metastore );
    assertEquals( 2, remarks.size() );
    assertEquals( "Step is receiving info from other steps.", remarks.get( 0 ).getText() );
    assertEquals( "File specifications are not checked.", remarks.get( 1 ).getText() );

    XMLField xmlField = new XMLField();
    xmlField.setFieldName( "aField" );
    xmlField.setType( 1 );
    xmlField.setLength( 10 );
    xmlField.setPrecision( 3 );
    xmlOutputMeta.setOutputFields( new XMLField[] { xmlField } );
    when( prev.size() ).thenReturn( 1 );
    remarks.clear();
    xmlOutputMeta.check( remarks, transMeta, stepInfo, prev, new String[] { "input" }, new String[] { "output" }, info,
        new Variables(), repos, metastore );
    assertEquals( 4, remarks.size() );
    assertEquals( "Step is connected to previous one, receiving 1 fields", remarks.get( 0 ).getText() );
    assertEquals( "All output fields are found in the input stream.", remarks.get( 1 ).getText() );
    assertEquals( "Step is receiving info from other steps.", remarks.get( 2 ).getText() );
    assertEquals( "File specifications are not checked.", remarks.get( 3 ).getText() );

  }
}