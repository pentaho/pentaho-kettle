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

package org.pentaho.metastore.locator.impl.local;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 4/18/16.
 */
@RunWith( MockitoJUnitRunner.class )
public class LocalFileMetastoreProviderTest {
  @Mock LocalFileMetastoreProvider.MetastoreSupplier supplier;
  private LocalFileMetastoreProvider localFileMetastoreProvider;

  @Before
  public void setup() {
    localFileMetastoreProvider = new LocalFileMetastoreProvider( supplier );
  }

  @Test
  public void testNoArgConstructor() {
    assertNotNull( new LocalFileMetastoreProvider() );
  }

  @Test
  public void testGetMetastoreException() throws MetaStoreException {
    when( supplier.getMetastore() ).thenThrow( new MetaStoreException() );
    assertNull( localFileMetastoreProvider.getMetastore() );
  }

  @Test
  public void testGetMetastoreSuccess() throws MetaStoreException {
    IMetaStore metaStore = mock( IMetaStore.class );
    when( supplier.getMetastore() ).thenReturn( metaStore );
    assertEquals( metaStore, localFileMetastoreProvider.getMetastore() );
  }
}
