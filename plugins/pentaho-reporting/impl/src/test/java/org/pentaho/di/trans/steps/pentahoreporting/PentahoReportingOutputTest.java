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


package org.pentaho.di.trans.steps.pentahoreporting;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class PentahoReportingOutputTest {

  @BeforeClass
  public static void setUpBeforeClass() {
    ClassicEngineBoot.getInstance().start();
  }

  private URL testResourceUrl;

  @Before
  public void setUp() {
    testResourceUrl = this.getClass().getResource( "relative-path.prpt" );
  }

  @Test
  public void testLoadLocalReport() throws Exception {

    MasterReport report = PentahoReportingOutput.loadMasterReport( DefaultBowl.getInstance(),
      testResourceUrl.getPath() );

    assertNotNull( report );
    assertNotNull( report.getDataFactory() );
  }

  @Test
  public void testLocalFile() throws KettleFileException, MalformedURLException {
    Object keyValue = PentahoReportingOutput.getKeyValue(
      PentahoReportingOutput.getFileObject( DefaultBowl.getInstance(), testResourceUrl.getPath(), null ) );

    assertTrue( keyValue instanceof URL );

  }

  @Test( expected = KettleException.class )
  public void testProcessRowWitUsingValuesFromFields() throws KettleException {
    PentahoReportingOutput pentahoReportingOutput = mock( PentahoReportingOutput.class );
    PentahoReportingOutputMeta meta = mock( PentahoReportingOutputMeta.class );
    PentahoReportingOutputData data = mock( PentahoReportingOutputData.class );
    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );
    LogChannelInterface log = mock( LogChannelInterface.class );

    when( pentahoReportingOutput.getRow() ).thenReturn( new Object[] { "Value1", "value2" } );
    when( pentahoReportingOutput.processRow( meta, data ) ).thenCallRealMethod();
    when( meta.getUseValuesFromFields() ).thenReturn( true );
    when( pentahoReportingOutput.getInputRowMeta() ).thenReturn( rowMetaInterface );
    when( meta.getInputFileField() ).thenReturn( "field" );
    when( rowMetaInterface.indexOfValue( "field" ) ).thenReturn( -1 );
    ReflectionTestUtils.setField( pentahoReportingOutput, "first", true );
    ReflectionTestUtils.setField( pentahoReportingOutput, "log", log );

    pentahoReportingOutput.processRow( meta, data );
  }

  @Test
  public void testProcessRowWithoutUsingValuesFromFields() throws KettleException {
    // Static mock to avoid PentahoReportingOutput.performPentahoReportingBoot(), so that
    // we don't need to mock DefaultResourceManagerBackend
    try ( MockedStatic<PentahoReportingOutput> mocked = mockStatic( PentahoReportingOutput.class ) ) {
      String inputFileString = "inputFile";
      String outputFileString = "outputFile";
      PentahoReportingOutput pentahoReportingOutput = mock( PentahoReportingOutput.class );
      PentahoReportingOutputMeta meta = mock( PentahoReportingOutputMeta.class );
      PentahoReportingOutputData data = mock( PentahoReportingOutputData.class );
      LogChannelInterface log = mock( LogChannelInterface.class );

      when( pentahoReportingOutput.getRow() ).thenReturn( new Object[] { "Value1", "value2" } );
      when( pentahoReportingOutput.processRow( meta, data ) ).thenCallRealMethod();
      when( meta.getUseValuesFromFields() ).thenReturn( false );
      when( meta.getInputFile() ).thenReturn( inputFileString);
      when( meta.getOutputFile() ).thenReturn( outputFileString );

      ReflectionTestUtils.setField( pentahoReportingOutput, "first", true );
      ReflectionTestUtils.setField( pentahoReportingOutput, "log", log );

      pentahoReportingOutput.processRow( meta, data );

      verify( pentahoReportingOutput, times( 1 ) )
        .processReport( any(), eq( inputFileString ), eq( outputFileString ), any(), any() );
    }
  }

}
