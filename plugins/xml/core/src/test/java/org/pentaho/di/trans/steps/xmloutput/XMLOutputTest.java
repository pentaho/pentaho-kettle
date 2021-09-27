/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.xmloutput.XMLField.ContentType;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tatsiana_Kasiankova
 *
 */
public class XMLOutputTest {

  private StepMockHelper<XMLOutputMeta, XMLOutputData> stepMockHelper;
  private XMLOutput xmlOutput;
  private XMLOutputMeta xmlOutputMeta;
  private XMLOutputData xmlOutputData;
  private Trans trans = mock( Trans.class );
  private static final String[] ILLEGAL_CHARACTERS_IN_XML_ATTRIBUTES = { "<", ">", "&", "\'", "\"" };

  private static Object[] rowWithData;
  private static Object[] rowWithNullData;

  @BeforeClass
  public static void setUpBeforeClass() {

    rowWithData = initRowWithData( ILLEGAL_CHARACTERS_IN_XML_ATTRIBUTES );
    rowWithNullData = initRowWithNullData();
  }

  @Before
  public void setup() throws Exception {

    stepMockHelper =
        new StepMockHelper<XMLOutputMeta, XMLOutputData>( "XML_OUTPUT_TEST", XMLOutputMeta.class, XMLOutputData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    StepMeta mockMeta = mock( StepMeta.class );
    when( stepMockHelper.transMeta.findStep( Matchers.anyString() ) ).thenReturn( mockMeta );
    when( trans.getLogLevel() ).thenReturn( LogLevel.DEBUG );

    // Create and set Meta with some realistic data
    xmlOutputMeta = new XMLOutputMeta();
    xmlOutputMeta.setOutputFields( initOutputFields( rowWithData.length, ContentType.Attribute ) );
    // Set as true to prevent unnecessary for this test checks at initialization
    xmlOutputMeta.setDoNotOpenNewFileInit( false );

    xmlOutputData = new XMLOutputData();
    xmlOutputData.formatRowMeta = initRowMeta( rowWithData.length );
    xmlOutputData.fieldnrs = initFieldNmrs( rowWithData.length );
    xmlOutputData.OpenedNewFile = true;

    StepMeta stepMeta = new StepMeta( "StepMetaId", "StepMetaName", xmlOutputMeta );
    xmlOutput = spy( new XMLOutput( stepMeta, xmlOutputData, 0, stepMockHelper.transMeta, stepMockHelper.trans ) );
  }

  @Test
  public void testSpecialSymbolsInAttributeValuesAreEscaped() throws KettleException, XMLStreamException {
    xmlOutput.init( xmlOutputMeta, xmlOutputData );

    xmlOutputData.writer = mock( XMLStreamWriter.class );
    xmlOutput.writeRowAttributes( rowWithData );
    xmlOutput.dispose( xmlOutputMeta, xmlOutputData );
    verify( xmlOutputData.writer, times( rowWithData.length ) ).writeAttribute( any(), any() );
    verify( xmlOutput, atLeastOnce() ).closeOutputStream( any() );
  }

  @Test
  public void testNullInAttributeValuesAreEscaped() throws KettleException, XMLStreamException {

    testNullValuesInAttribute( 0 );
  }

  @Test
  public void testNullInAttributeValuesAreNotEscaped() throws KettleException, XMLStreamException {

    xmlOutput.setVariable( Const.KETTLE_COMPATIBILITY_XML_OUTPUT_NULL_VALUES, "Y" );

    testNullValuesInAttribute( rowWithNullData.length  );
  }

  /**
   * [PDI-15575] Testing to verify that getIfPresent defaults the XMLField ContentType value
   */
  @Test
  public void testDefaultXmlFieldContentType() {
    XMLField[] xmlFields = initOutputFields( 4, null );
    xmlFields[0].setContentType( ContentType.getIfPresent( "Element" ) );
    xmlFields[1].setContentType( ContentType.getIfPresent( "Attribute" ) );
    xmlFields[2].setContentType( ContentType.getIfPresent( "" ) );
    xmlFields[3].setContentType( ContentType.getIfPresent( "WrongValue" ) );
    assertEquals( xmlFields[0].getContentType(), ContentType.Element );
    assertEquals( xmlFields[1].getContentType(), ContentType.Attribute );
    assertEquals( xmlFields[2].getContentType(), ContentType.Element );
    assertEquals( xmlFields[3].getContentType(), ContentType.Element );
  }

  /**
   * [PDI-19286] Testing to verify that namespace is valid (URI)
   */
  @Test
  public void testIsValidNamespace() {
    String[] validNamespaces = {
      "http://www.foobar.com",
      "https://www.foobar.com%20foobar",
      "namespace",
      "%3Cnamespace%26%3E",
      "ftp://ftp.is.co.za/rfc/rfc1808.txt",
      "www.foobar.com",
      "www.foobar.com%7b%7D"
    };
    String[] invalidNamespaces = {
      "<namespace&>",
      "www.foobar.com{}",
      "www.foobar.com@##"
    };
    for ( String s : validNamespaces ) {
      assertTrue( XMLOutput.isValidNamespace( s ) );
    }
    for ( String s : invalidNamespaces ) {
      assertFalse( XMLOutput.isValidNamespace( s ) );
    }
  }

  private void testNullValuesInAttribute( int writeNullInvocationExpected ) throws KettleException, XMLStreamException {

    xmlOutput.init( xmlOutputMeta, xmlOutputData );

    xmlOutputData.writer = mock( XMLStreamWriter.class );
    xmlOutput.writeRowAttributes( rowWithNullData );
    xmlOutput.dispose( xmlOutputMeta, xmlOutputData );
    verify( xmlOutputData.writer, times( writeNullInvocationExpected ) ).writeAttribute( any(), any() );
    verify( xmlOutput, atLeastOnce() ).closeOutputStream( any() );
  }

  private static Object[] initRowWithData( String[] dt ) {

    Object[] data = new Object[dt.length * 3];
    for ( int i = 0; i < dt.length; i++ ) {
      data[3 * i] = dt[i] + "TEST";
      data[3 * i + 1] = "TEST" + dt[i] + "TEST";
      data[3 * i + 2] = "TEST" + dt[i];
    }
    return data;
  }

  private static Object[] initRowWithNullData() {

    Object[] data = new Object[15];
    for ( int i = 0; i < data.length; i++ ) {

      data[i] = null;
    }

    return data;
  }

  private RowMeta initRowMeta( int count ) {
    RowMeta rm = new RowMeta();
    for ( int i = 0; i < count; i++ ) {
      rm.addValueMeta( new ValueMeta( "string", ValueMetaInterface.TYPE_STRING ) );
    }
    return rm;
  }

  private XMLField[] initOutputFields( int i, ContentType attribute ) {

    XMLField[] fields = new XMLField[i];
    for ( int j = 0; j < fields.length; j++ ) {
      fields[j] =
          new XMLField( attribute, "Fieldname" + ( j + 1 ), "ElementName" + ( j + 1 ), 2, null, -1, -1, null, null,
              null, null );
    }

    return fields;
  }

  private int[] initFieldNmrs( int i ) {
    int[] fNmrs = new int[i];
    for ( int j = 0; j < fNmrs.length; j++ ) {
      fNmrs[j] = j;
    }
    return fNmrs;
  }
}
