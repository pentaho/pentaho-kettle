/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
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

  @BeforeClass
  public static void setUpBeforeClass() {
    rowWithData = initRowWithData( ILLEGAL_CHARACTERS_IN_XML_ATTRIBUTES );
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
}
