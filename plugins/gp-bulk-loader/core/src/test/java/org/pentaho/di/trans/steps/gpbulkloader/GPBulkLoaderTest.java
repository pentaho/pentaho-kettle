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


package org.pentaho.di.trans.steps.gpbulkloader;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import static org.mockito.Mockito.*;

public class GPBulkLoaderTest {

  private GPBulkLoader loader;
  private StepMockHelper<GPBulkLoaderMeta, GPBulkLoaderData> mockHelper;

  @Before
  public void setUp() throws Exception {
    mockHelper =
      new StepMockHelper<GPBulkLoaderMeta, GPBulkLoaderData>(
        "GPBulkLoad", GPBulkLoaderMeta.class, GPBulkLoaderData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );
  }

  @Test
  public void testInputFileSurroundedBySingleQuotes() throws Exception {

    String datafile = "test-data-file";

    loader =
      new GPBulkLoader( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );

    DatabaseMeta dbMetaMock = mock( DatabaseMeta.class );
    doReturn( "" ).when( dbMetaMock ).getQuotedSchemaTableCombination( anyString(), anyString() );
    doReturn( "" ).when( dbMetaMock ).quoteField( anyString() );

    GPBulkLoaderMeta meta = new GPBulkLoaderMeta();
    meta.setLoadAction( "" );
    meta.setFieldStream( new String[] { "" } );
    meta.setFieldTable( new String[] { "" } );
    meta.setDatabaseMeta( dbMetaMock );
    meta.setDataFile( datafile );

    String actual = loader.getControlFileContents( meta, null, null );

    int first = actual.indexOf( datafile );
    if ( first > 0 ) {
      if ( actual.charAt( first - 1 ) != '\'' || actual.charAt( first + datafile.length() ) != '\'' ) {
        Assert.fail( "Datafile name is not surrounded by single quotes. Actual control file: " + actual );
      }
    } else {
      Assert.fail( "Datafile name not found in control file. Actual control file: " + actual );
    }

  }
}
