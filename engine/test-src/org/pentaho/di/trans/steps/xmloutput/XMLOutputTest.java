/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
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
  private static Object[] expectedRowWithData;

  @BeforeClass
  public static void setUpBeforeClass() {
    rowWithData = initRowWithData( ILLEGAL_CHARACTERS_IN_XML_ATTRIBUTES );
    expectedRowWithData = initRowWithData( getEscapedCharacters() );
  }

  @Before
  public void setup() throws Exception {

    stepMockHelper =
        new StepMockHelper<XMLOutputMeta, XMLOutputData>( "XML_OUTPUT_TEST", XMLOutputMeta.class, XMLOutputData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    when( stepMockHelper.transMeta.findStep( Matchers.anyString() ) ).thenReturn( mock( StepMeta.class ) );
    when( trans.getLogLevel() ).thenReturn( LogLevel.DEBUG );

    // Create and set Meta with some realistic data
    xmlOutputMeta = new XMLOutputMeta();
    xmlOutputMeta.setOutputFields( initOutputFields( rowWithData.length, ContentType.Attribute ) );
    // Set as true to prevent unnecessary for this test checks at initialization
    xmlOutputMeta.setDoNotOpenNewFileInit( true );

    xmlOutputData = new XMLOutputData();
    xmlOutputData.formatRowMeta = initRowMeta( rowWithData.length );
    xmlOutputData.fieldnrs = initFieldNmrs( rowWithData.length );

    StepMeta stepMeta = new StepMeta( "StepMetaId", "StepMetaName", xmlOutputMeta );
    xmlOutput = new XMLOutput( stepMeta, xmlOutputData, 0, stepMockHelper.transMeta, stepMockHelper.trans );
  }

  @Test
  public void testSpecialSymbolsInAttributeValuesAreEscaped() throws KettleException {
    xmlOutput.init( xmlOutputMeta, xmlOutputData );
    String buildRowAttributes = xmlOutput.buildRowAttributes( rowWithData );
    //System.out.println( "Actual row: " + buildRowAttributes );

    String expectedAttributesRow = getExpectedAttributesRow();
    //System.out.println( "Expected row: " + expectedAttributesRow );

    assertEquals( expectedAttributesRow, buildRowAttributes );

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

  private String getExpectedAttributesRow() {
    StringBuilder sb = new StringBuilder();
    XMLField[] expectedXmlFields = initOutputFields( rowWithData.length, ContentType.Attribute );

    for ( int i = 0; i < rowWithData.length; i++ ) {
      String attributeName = expectedXmlFields[i].getElementName();
      String attributeValue = String.valueOf( expectedRowWithData[i] );
      sb.append( ' ' ).append( attributeName ).append( "=\"" ).append( attributeValue ).append( "\"" );
    }
    return sb.toString();

  }

  private static String[] getEscapedCharacters() {
    Encoder encoder = ESAPI.encoder();
    String[] escCharacters = new String[ILLEGAL_CHARACTERS_IN_XML_ATTRIBUTES.length];
    for ( int i = 0; i < escCharacters.length; i++ ) {
      escCharacters[i] = encoder.encodeForXML( String.valueOf( ILLEGAL_CHARACTERS_IN_XML_ATTRIBUTES[i] ) );
    }
    return escCharacters;

  }

}
