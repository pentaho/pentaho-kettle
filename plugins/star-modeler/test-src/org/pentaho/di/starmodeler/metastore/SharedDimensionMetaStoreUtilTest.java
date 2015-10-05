/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.starmodeler.metastore;

import org.junit.Test;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;

import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class SharedDimensionMetaStoreUtilTest {

  @Test
  public void testSaveSharedDimension() throws Exception {
    final String locale = Locale.US.toString();

    final IMetaStore metaStore = mock( IMetaStore.class );

    final IMetaStoreElementType metaStoreElementType = mock( IMetaStoreElementType.class );
    when( metaStore.newElementType( anyString() ) ).thenReturn( metaStoreElementType );
    final IMetaStoreElement metaStoreElement = mock( IMetaStoreElement.class );
    final String sdId = "sdId";
    when( metaStore.getElement( anyString(), eq( metaStoreElementType ), eq( sdId ) ) ).thenReturn( metaStoreElement );

    final LogicalTable sharedDimension = mock( LogicalTable.class );
    final String sdName = "sdName";
    when( sharedDimension.getName( eq( locale ) ) ).thenReturn( sdName );
    when( sharedDimension.getId() ).thenReturn( sdId );

    SharedDimensionMetaStoreUtil.saveSharedDimension( metaStore, sharedDimension, locale );

    verify( metaStoreElement, times( 1 ) ).setElementType( eq( metaStoreElementType ) );
    verify( metaStoreElement, times( 1 ) ).setName( eq( sdName ) );
    verify( metaStoreElement, times( 2 ) ).addChild( any( IMetaStoreAttribute.class ) );
    verify( metaStore, times( 1 ) )
        .updateElement( anyString(), eq( metaStoreElementType ), eq( sdId ), eq( metaStoreElement ) );
    verify( sharedDimension, times( 1 ) ).setId( anyString() );
  }

  @Test
  public void testGetSharedDimensionElementTypeNew() throws Exception {
    final IMetaStore metaStore = mock( IMetaStore.class );

    final IMetaStoreElementType metaStoreElementType = mock( IMetaStoreElementType.class );
    when( metaStore.newElementType( anyString() ) ).thenReturn( metaStoreElementType );

    final IMetaStoreElementType sharedDimensionElementType = SharedDimensionMetaStoreUtil
        .getSharedDimensionElementType( metaStore );
    verify( metaStore, times( 1 ) ).createNamespace( anyString() );
    assertNotNull( sharedDimensionElementType );
    assertEquals( metaStoreElementType, sharedDimensionElementType );
    verify( sharedDimensionElementType, times( 1 ) )
        .setName( eq( SharedDimensionMetaStoreUtil.METASTORE_SHARED_DIMENSION_TYPE_NAME ) );
    verify( sharedDimensionElementType, times( 1 ) )
        .setDescription( eq( SharedDimensionMetaStoreUtil.METASTORE_SHARED_DIMENSION_TYPE_DESCRIPTION ) );
    verify( metaStore, times( 1 ) ).createElementType( anyString(), eq( sharedDimensionElementType ) );
  }

  @Test
  public void testGetSharedDimensionElementTypeExist() throws Exception {
    final IMetaStore metaStore = mock( IMetaStore.class );

    final IMetaStoreElementType metaStoreElementType = mock( IMetaStoreElementType.class );
    when( metaStore
        .getElementTypeByName( anyString(), eq( SharedDimensionMetaStoreUtil.METASTORE_SHARED_DIMENSION_TYPE_NAME ) ) )
        .thenReturn( metaStoreElementType );

    final IMetaStoreElementType sharedDimensionElementType = SharedDimensionMetaStoreUtil
        .getSharedDimensionElementType( metaStore );
    verify( metaStore, times( 1 ) ).createNamespace( anyString() );
    assertNotNull( sharedDimensionElementType );
    assertEquals( metaStoreElementType, sharedDimensionElementType );
    verify( sharedDimensionElementType, never() )
        .setName( eq( SharedDimensionMetaStoreUtil.METASTORE_SHARED_DIMENSION_TYPE_NAME ) );
    verify( sharedDimensionElementType, never() )
        .setDescription( eq( SharedDimensionMetaStoreUtil.METASTORE_SHARED_DIMENSION_TYPE_DESCRIPTION ) );
    verify( metaStore, never() ).createElementType( anyString(), eq( sharedDimensionElementType ) );
  }
}
