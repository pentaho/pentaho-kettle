/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.pentahoreporting;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.libraries.resourceloader.CompoundResource;
import org.pentaho.reporting.libraries.resourceloader.DefaultResourceManagerBackend;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( DefaultResourceManagerBackend.class )
public class PentahoReportingOutputTest {

  private static final String QUERY_NAME = "LocalFileQueryName";
  private URL testResourceUrl;
  private ResourceKey resourceKey;

  @Before
  public void setUp() throws ResourceException {

    testResourceUrl = this.getClass().getResource( "relative-path.prpt" );
    resourceKey =
      new ResourceKey( "org.pentaho.reporting.libraries.resourceloader.loader.URLResourceLoader",
        testResourceUrl, null );

    DataFactory mockedDataFactory = mock( DataFactory.class );
    when( mockedDataFactory.getQueryNames() ).thenReturn( new String[] { QUERY_NAME } );

    ResourceManager manager = new ResourceManager();
    manager.registerDefaults();

    MasterReport mockedMasterReport = mock( MasterReport.class );
    when( mockedMasterReport.getDataFactory() ).thenReturn( mockedDataFactory );
    when( mockedMasterReport.getResourceManager() ).thenReturn( manager );


    Resource resource = mock( CompoundResource.class );
    when( resource.getResource() ).thenReturn( mockedMasterReport );
    when( resource.getDependencies() ).thenReturn( new ResourceKey[] {} );
    when( resource.getSource() ).thenReturn( resourceKey );
    when( resource.getTargetType() ).thenReturn( MasterReport.class );
    manager.getFactoryCache().put( resource );

    PowerMockito.stub( PowerMockito.method( DefaultResourceManagerBackend.class, "create" ) ).toReturn( resource );
  }

  @Test
  public void testLoadLocalReport() throws Exception {

    MasterReport report = PentahoReportingOutput.loadMasterReport( testResourceUrl.getPath() );

    URL returnedUrl = report.getResourceManager().toURL( resourceKey );

    assertTrue( returnedUrl.equals( testResourceUrl ) );
    assertTrue( QUERY_NAME.equals( report.getDataFactory().getQueryNames()[ 0 ] ) );

  }

  @Test
  public void testLocalFile() throws KettleFileException, MalformedURLException {
    Object keyValue = PentahoReportingOutput.getKeyValue(
      PentahoReportingOutput.getFileObject( testResourceUrl.getPath(), null ) );

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
    setInternalState( pentahoReportingOutput, "first", true );
    setInternalState( pentahoReportingOutput, "log", log );

    pentahoReportingOutput.processRow( meta, data );
  }

  @Test
  public void testProcessRowWithoutUsingValuesFromFields() throws KettleException {
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

    setInternalState( pentahoReportingOutput, "first", true );
    setInternalState( pentahoReportingOutput, "log", log );

    pentahoReportingOutput.processRow( meta, data );

    verify( pentahoReportingOutput, times( 1 ) )
      .processReport( any(), eq( inputFileString ), eq( outputFileString ), any(), any() );
  }

}
