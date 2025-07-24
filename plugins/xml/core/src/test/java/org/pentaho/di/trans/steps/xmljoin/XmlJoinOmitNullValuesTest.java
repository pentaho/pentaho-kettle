/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.xmljoin;

import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Test for XmlJoin step
 * 
 * @author Pavel Sakun
 * @see XMLJoin
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class XmlJoinOmitNullValuesTest {
  StepMockHelper<XMLJoinMeta, XMLJoinData> smh;

  @Before
  public void init() {
    smh = new StepMockHelper<XMLJoinMeta, XMLJoinData>( "XmlJoin", XMLJoinMeta.class, XMLJoinData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        smh.logChannelInterface );
    when( smh.trans.isRunning() ).thenReturn( true );
  }

  @Test
  public void testRemoveEmptyNodes() throws KettleException {
    doTest(
        "<child><empty/><subChild a=\"\"><empty/></subChild><subChild><empty/></subChild><subChild><subSubChild a=\"\"/></subChild></child>",
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root xmlns=\"http://www.myns1.com\" xmlns:xsi=\"http://www.myns2.com\" xsi:schemalocation=\"http://www.mysl1.com\"></root>",
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root xmlns=\"http://www.myns1.com\" xmlns:xsi=\"http://www.myns2.com\" xsi:schemalocation=\"http://www.mysl1.com\"><child xmlns=\"\"><subChild a=\"\"/><subChild><subSubChild a=\"\"/></subChild></child></root>" );
  }

  private void doTest( final String sourceXml, final String targetXml, final String expectedXml )
    throws KettleException {
    XMLJoin spy = spy( new XMLJoin( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans ) );

    doReturn( createSourceRowSet( sourceXml ) ).when( spy ).findInputRowSet( "source" );
    doReturn( createTargetRowSet( targetXml ) ).when( spy ).findInputRowSet( "target" );

    XMLJoinMeta stepMeta = smh.initStepMetaInterface;
    lenient().when( stepMeta.getSourceXMLstep() ).thenReturn( "source" );
    lenient().when( stepMeta.getTargetXMLstep() ).thenReturn( "target" );
    lenient().when( stepMeta.getSourceXMLfield() ).thenReturn( "sourceField" );
    lenient().when( stepMeta.getTargetXMLfield() ).thenReturn( "targetField" );
    lenient().when( stepMeta.getValueXMLfield() ).thenReturn( "resultField" );
    lenient().when( stepMeta.getTargetXPath() ).thenReturn( "//root" );
    lenient().when( stepMeta.isOmitNullValues() ).thenReturn( true );

    spy.init( stepMeta, smh.initStepDataInterface );

    spy.addRowListener( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        try {
          XMLUnit.setIgnoreWhitespace(true);
          XMLUnit.setIgnoreAttributeOrder(true);
          Diff diff = new Diff(expectedXml, (String) row[0]);
          XMLAssert.assertXMLEqual("XMLs are not semantically equal", diff, true);
        } catch (Exception e) {
          throw new KettleStepException(e);
        }
      }
    } );

    Assert.assertTrue( spy.processRow( stepMeta, smh.initStepDataInterface ) );
    Assert.assertFalse( spy.processRow( stepMeta, smh.initStepDataInterface ) );
  }

  private RowSet createSourceRowSet( String sourceXml ) {
    RowSet sourceRowSet = smh.getMockInputRowSet( new String[] { sourceXml } );
    RowMetaInterface sourceRowMeta = mock( RowMetaInterface.class );
    lenient().when( sourceRowMeta.getFieldNames() ).thenReturn( new String[] { "sourceField" } );
    when( sourceRowSet.getRowMeta() ).thenReturn( sourceRowMeta );

    return sourceRowSet;
  }

  private RowSet createTargetRowSet( String targetXml ) {
    RowSet targetRowSet = smh.getMockInputRowSet( new String[] { targetXml } );
    RowMetaInterface targetRowMeta = mock( RowMetaInterface.class );
    when( targetRowMeta.getFieldNames() ).thenReturn( new String[] { "targetField" } );
    when( targetRowMeta.clone() ).thenReturn( targetRowMeta );
    when( targetRowMeta.size() ).thenReturn( 1 );
    when( targetRowSet.getRowMeta() ).thenReturn( targetRowMeta );

    return targetRowSet;
  }
}
