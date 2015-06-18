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

package org.pentaho.di.trans.steps.dimensionlookup;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.i18n.BaseMessages;


public class DimensionLookupMetaTest {

  @Before
  public void setUp() throws Exception {
    LogChannelInterfaceFactory logChannelInterfaceFactory = mock( LogChannelInterfaceFactory.class );
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    KettleLogStore.setLogChannelInterfaceFactory( logChannelInterfaceFactory );
    when( logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      logChannelInterface );
  }

  @Test
  public void testGetFields() throws Exception {

    RowMeta extraFields = new RowMeta();
    extraFields.addValueMeta( new ValueMetaString( "field1" ) );

    DatabaseMeta dbMeta = mock( DatabaseMeta.class );

    DimensionLookupMeta meta = spy( new DimensionLookupMeta() );
    meta.setUpdate( false );
    meta.setKeyField( null );
    meta.setFieldLookup( new String[] { "field1" } );
    meta.setFieldStream( new String[] { "" } );
    meta.setDatabaseMeta( dbMeta );
    doReturn( extraFields ).when( meta ).getDatabaseTableFields( (Database) anyObject(), anyString(), anyString() );
    doReturn( mock( LogChannelInterface.class ) ).when( meta ).getLog();

    RowMeta row = new RowMeta();
    try {
      meta.getFields( row, "DimensionLookupMetaTest", new RowMeta[] { row }, null, null, null, null );
    } catch ( Throwable e ) {
      Assert.assertTrue( e.getMessage().contains(
        BaseMessages.getString( DimensionLookupMeta.class, "DimensionLookupMeta.Error.NoTechnicalKeySpecified" ) ) );
    }
  }
}
